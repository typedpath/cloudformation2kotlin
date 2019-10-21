package com.typedpath.testdomain

import jdk.nashorn.api.scripting.ScriptObjectMirror
import java.util.*
import javax.script.ScriptEngineManager
import kotlin.reflect.*
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

val ID_FIELD = "_id"

private fun KClass<*>.properties(): List<KProperty<*>> = this.members.filter { it is KProperty && !it.name.equals("serialVersionUID") }.map { it -> it as KProperty }

private fun KClass<*>. propertiesMap(): Map<String, KProperty<*>> {
    val result: HashMap<String, KProperty<*>> = HashMap()
    properties().forEach {
        result.put(it.name, it)
    }
    return result
}

private fun isComplex(theClass: KClass<*>) = ! theClass.qualifiedName!!.startsWith("kotlin")
private fun isComplex(o: Any) = ! o::class.qualifiedName!!.startsWith("kotlin")

// returns json and object to id map
fun  toJsonInsertUuidsTypeInfo(o: Any, object2Id:  MutableMap<Any, UUID>) : String{
    fun fObject2Id (o: Any) : UUID {
        if (object2Id.containsKey(o)) {
            return object2Id.get(o)!!
        } else {
            val id = UUID.randomUUID()
            object2Id.put(o, id)
            return id
        }
    }
    return toJsonInsertUuidsTypeInfo(o, null, false, "\r\n", ::fObject2Id)
}

//TODO emit new idsS
fun toJsonInsertUuidsTypeInfo(o: Any, parent: Any?, isListItem: Boolean, lineDelimiter: String = "\r\n",
                              object2Id: (Any)->UUID  = {
                                  o -> UUID.randomUUID()
                              }

) : String {
    //TODO deal with non strings !
    if (o is List<*>) {
        val sb = StringBuilder()
        sb.append("[$lineDelimiter")
        //assume no nulls in array
        sb.append(o.map{toJsonInsertUuidsTypeInfo(it!!, parent, true, lineDelimiter, object2Id)}
                .joinToString (","))
        sb.append("]$lineDelimiter")
        return sb.toString()
    }
    if (!isComplex(o)) return """"$o""""
    val sb = StringBuilder()
    sb.append("{")
    sb.append(lineDelimiter)
    o.javaClass.kotlin.propertiesMap()

            .forEach {
                val pValue =  it.value.getter.call(o)
                if (pValue!=null) {
                        sb.append(""""${it.key}":${toJsonInsertUuidsTypeInfo(pValue, o, false, lineDelimiter, object2Id)}, """)
                    }
                }

    sb.append(""""_id":"${object2Id(o)}"""")
    sb.append(""", """)
    sb.append(""""type":"${o::class.simpleName}"""")
    //TODO if this is in a container insert up reference
    if (isListItem) {
        sb.append(""", "${parent!!.javaClass.simpleName.decapitalize()}":"${object2Id(parent)}" """)
    }
    sb.append(lineDelimiter)
    sb.append("}")
    return sb.toString()
}

fun crawl(theClass: KClass<*>, onProperty:  (KClass<*>, KProperty1<*, *>, Deque<KClass<*>>, isList: Boolean) -> Boolean) {
    crawl(theClass, ArrayDeque<KClass<*>>(), onProperty)
}


fun crawl(theClass: KClass<*>,  context: Deque<KClass<*>>, onProperty:  (KClass<*>,
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

@Throws(Exception::class)
fun stringToJson(strJson: String): ScriptObjectMirror {
    val json = "var result = $strJson; result;"
    val factory = ScriptEngineManager()
    val engine = factory.getEngineByName("JavaScript")
    return engine.eval(json) as ScriptObjectMirror
}

fun <T : Any> toObject(strJson: String, rootClass: KClass<T>,
                       object2Id: MutableMap<Any, UUID>) : Any {
    val json = stringToJson(strJson)
    return toObject(json, rootClass, object2Id)
}

private fun isList(theClass: KClass<*>) : Boolean = theClass.isSubclassOf(List::class)

fun <T : Any> toObject(json: ScriptObjectMirror, rootClass: KClass<T>,
                       object2Id: MutableMap<Any, UUID>?
                       ) : T {
    val constructor: KFunction<T> = rootClass.constructors.first()
    val arguments = constructor.parameters.map {
        Pair(it,
        if (isList(it.type.classifier as KClass<*>)) {
            val listValue = mutableListOf<Any>()
            val jsonArrayValue = json.get(it.name) as ScriptObjectMirror
            val listType = it.type.arguments.get(0).type!!.classifier as KClass<*>
            for (i in 0..jsonArrayValue!!.size-1) {
                val itemJsonValue = jsonArrayValue.get(i.toString()) as ScriptObjectMirror
                listValue.add(toObject(itemJsonValue, listType, object2Id))
            }
             listValue
//            throw RuntimeException("cant process list class ${it.type.classifier} \r\n    argument: ${it.type.arguments.get(0).type!!.classifier} \r\n ${json.get(it.name)!!.javaClass.name}")
        }
        else if (isComplex(it.type.classifier as KClass<*>))
            toObject(json.get(it.name) as ScriptObjectMirror, it.type.classifier as KClass<*>,
                    object2Id
                    )
         else json.get(it.name))
    }.toMap()
    val result = constructor.callBy(arguments)
    if (object2Id!=null && json.containsKey(ID_FIELD)) {
        object2Id.put(result, UUID.fromString(json.get(ID_FIELD).toString()))
    }
    return result
}

fun main(args: Array<String>) {
    println("here ")

val person = Person("Andrew", "Eldritch", Address("addr1", "addr2", "AL5 8HJ"))
val object2Id = mutableMapOf<Any, UUID>()
val jsonMapping = toJsonInsertUuidsTypeInfo(person, object2Id)
val o = toObject(jsonMapping, Person::class, object2Id)
    println("$o $object2Id")

}

