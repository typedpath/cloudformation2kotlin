package com.typedpath.testdomain

import jdk.nashorn.api.scripting.ScriptObjectMirror
import java.util.*
import javax.script.ScriptEngineManager
import kotlin.collections.HashSet
import kotlin.reflect.*
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

/*
serialization functions for use with db storage - adds in meta fields to
indicate db id , db action + map containing relationships
 */

val ID_FIELD = "_id"
val INSERT_ACTION = "i"
val UPDATE_ACTION = "u"
val NO_ACTION = "n"
val ACTION_FIELD = "_a"

private fun KClass<*>.properties(): List<KProperty<*>> = this.members.filter { it is KProperty && !it.name.equals("serialVersionUID") }.map { it -> it as KProperty }

private fun KClass<*>.propertiesMap(): Map<String, KProperty<*>> {
    val result: HashMap<String, KProperty<*>> = HashMap()
    properties().forEach {
        result.put(it.name, it)
    }
    return result
}

private fun isComplex(theClass: KClass<*>) = !theClass.qualifiedName!!.startsWith("kotlin")
private fun isComplex(o: Any) = !o::class.qualifiedName!!.startsWith("kotlin")

// returns json and object to id map
fun toJsonInsertUuidsTypeInfo(o: Any, parent: Any?, isListItem: Boolean,
                              object2Id: MutableMap<Any, UUID>, dirty: Set<Any> = HashSet(),
                              lineDelimiter: String = "\r\n"
): String {
    //TODO deal with non strings !
    if (o is List<*>) {
        val sb = StringBuilder()
        sb.append("[$lineDelimiter")
        //assume no nulls in array
        sb.append(o.map {
            toJsonInsertUuidsTypeInfo(it!!, parent, true, object2Id,
                    dirty, lineDelimiter)
        }
                .joinToString(","))
        sb.append("]$lineDelimiter")
        return sb.toString()
    }
    if (!isComplex(o)) return """"$o""""
    val sb = StringBuilder()
    sb.append("{")
    sb.append(lineDelimiter)
    var action: String
    var id: UUID
    if (object2Id.containsKey(o)) {
        action = if (dirty.contains(o)) UPDATE_ACTION else NO_ACTION
        id = object2Id.get(o)!!
    } else {
        id = UUID.randomUUID()
        object2Id.put(o, id)
        action = INSERT_ACTION
    }
    sb.append(""""$ID_FIELD":"${id}"""")
    sb.append(""", "$ACTION_FIELD":"${action}"""")
    sb.append(""", "type":"${o::class.simpleName}"""")

    val actionRequired = action == INSERT_ACTION || action == UPDATE_ACTION
    // properties, if no
    o.javaClass.kotlin.propertiesMap()
            .forEach {
                val pValue = it.value.getter.call(o)
                if (pValue != null && (actionRequired || isComplex(pValue))) {
                    sb.append(""", "${it.key}":${toJsonInsertUuidsTypeInfo(pValue, o, false, object2Id,
                            dirty,
                            lineDelimiter)} """)
                }
            }


    //TODO if this is in a container insert up reference
    if (isListItem) {
        sb.append(""", "${parent!!.javaClass.simpleName.decapitalize()}":"${object2Id.get(parent)}" """)
    }
    sb.append(lineDelimiter)
    sb.append("}")
    return sb.toString()
}

fun crawl(theClass: KClass<*>, onProperty: (KClass<*>, KProperty1<*, *>, Deque<KClass<*>>, isList: Boolean) -> Boolean) {
    crawl(theClass, ArrayDeque<KClass<*>>(), onProperty)
}


/*
crawl through a class model, onProperty hook allows caller to collect information / take actions
and stop or continue the call
 */
fun crawl(theClass: KClass<*>, context: Deque<KClass<*>>, onProperty: (KClass<*>,
                                                                       KProperty1<*, *>, Deque<KClass<*>>, isList: Boolean) -> Boolean
) {
    context.push(theClass)
    theClass.memberProperties.forEach {
        var pClass = it.returnType.classifier as KClass<*>
        //
        var isList = false
        if (pClass.isSubclassOf(List::class)) {
            pClass = it.returnType.arguments.get(0).type!!.classifier as KClass<*>
            isList = true
        }

        if (onProperty(pClass, it, context, isList)) {
            crawl(pClass, context, onProperty)
        }
    }
    context.pop()
}

/**
 * convert string to json
 */
@Throws(Exception::class)
fun stringToJson(strJson: String): ScriptObjectMirror {
    val json = "var result = $strJson; result;"
    val factory = ScriptEngineManager()
    val engine = factory.getEngineByName("JavaScript")
    return engine.eval(json) as ScriptObjectMirror
}

/**
 * convert json string to object
 */
fun <T : Any> toObject(strJson: String, rootClass: KClass<T>,
                       object2Id: MutableMap<Any, UUID>): Any {
    val json = stringToJson(strJson)
    return toObject(json, rootClass, object2Id)
}

/**
 * convert json string to list
 */
fun <T : Any> toList(strJson: String, rootClass: KClass<T>,
                     object2Id: MutableMap<Any, UUID>): List<T> {
    val json = stringToJson(strJson)
    return json.values.map { toObject(it as ScriptObjectMirror, rootClass, object2Id) }
            .toList()
}

private fun isList(theClass: KClass<*>): Boolean = theClass.isSubclassOf(List::class)

/**
 * convert json object to object
 */
fun <T : Any> toObject(json: ScriptObjectMirror, rootClass: KClass<T>,
                       object2Id: MutableMap<Any, UUID>?
): T {
    val constructor: KFunction<T> = rootClass.constructors.first()
    val arguments = constructor.parameters.map {
        Pair(it,
                if (isList(it.type.classifier as KClass<*>)) {
                    val listValue = mutableListOf<Any>()
                    val jsonArrayValue = json.get(it.name) as ScriptObjectMirror
                    val listType = it.type.arguments.get(0).type!!.classifier as KClass<*>
                    for (i in 0..jsonArrayValue!!.size - 1) {
                        val itemJsonValue = jsonArrayValue.get(i.toString()) as ScriptObjectMirror
                        listValue.add(toObject(itemJsonValue, listType, object2Id))
                    }
                    listValue
                } else if (isComplex(it.type.classifier as KClass<*>))
                    toObject(json.get(it.name) as ScriptObjectMirror, it.type.classifier as KClass<*>,
                            object2Id
                    )
                else json.get(it.name))
    }.toMap()
    val result = constructor.callBy(arguments)
    if (object2Id != null && json.containsKey(ID_FIELD)) {
        object2Id.put(result, UUID.fromString(json.get(ID_FIELD).toString()))
    }
    return result
}

