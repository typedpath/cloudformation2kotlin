package com.typedpath.serialization

import java.lang.StringBuilder
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty

/**
 * implements type safe access
 */

private fun byName(themap: Map<*,*>, name: String ) : Any? {
    return if (themap.containsKey(name)) themap[name] else themap.entries
            .filter { it.key.toString().toLowerCase().replace("_", "").equals(name.toLowerCase().replace("_", "")) }
            .map {it.value}
            .firstOrNull()
}

abstract class TypedVertex<F : Any?, T : Any?>(val name: String, val toNode: TypedNode<T>) {
    abstract fun traverse(p: Any?): Any?
}

class IndexVertex<T : Any?>(val index: Int, toNode: TypedNode<T>) : TypedVertex<List<T>, T>(index.toString(), toNode) {
    override fun traverse(p: Any?): Any? {
        if (p == null) return null
        else {
            val l = p as List<*>
            return if (l.size <= index) null else l.get(index)
        }
    }
}

private fun <F, T> toName(getter: (from: F) -> T): String {
    var name = if (getter is KProperty<*>) getter.name
    else if (getter is KFunction1) getter.name
    else getter.toString()
    if (name.startsWith("get") && name.length > 3 && name.get(3).isUpperCase()) {
        name = name.substring(3, 4).toLowerCase() + name.substring(4)
    }
    return name
}

class One2OneVertex<F : Any?, T : Any?>(val getter: (from: F) -> T, toNode: TypedNode<T>) :
        TypedVertex<F, T>(toName(getter), toNode) {

    override fun traverse(p: Any?): Any? {
        return if (p == null) return null
        else if (p is Map<*, *>) byName(p, name)
        else getter.invoke(p as F)

    }
}

class One2OneVertexOpt<F : Any?, T : Any?>(val getter: (from: F) -> T?, toNode: TypedNode<T>) :
        TypedVertex<F, T>(toName(getter), toNode) {

    override fun traverse(p: Any?): Any? {
        return if (p == null) return null
        else if (p is Map<*, *>) byName(p, name)
        else getter.invoke(p as F)

    }
}


class ToListVertex<F : Any?, T : Any?>(val getter: (from: F) -> java.util.List<T>?, toNode: ListNode<T>) :
        TypedVertex<F, List<T>>(toName(getter), toNode as TypedNode<List<T>>) {

    override fun traverse(p: Any?): Any? {
        return if (p == null) return null
        else if (p is Map<*, *>) byName(p, name)
        else getter.invoke(p as F)

    }
}

class ToKListVertex<F : Any?, T : Any?>(val getter: (from: F) -> kotlin.collections.List<T>?, toNode: ListNode<T>) :
        TypedVertex<F, kotlin.collections.List<T>>(toName(getter), toNode as TypedNode<kotlin.collections.List<T>>) {

    override fun traverse(p: Any?): Any? {
        return if (p == null) return null
        else if (p is Map<*, *>) byName(p, name)
        else getter.invoke(p as F)

    }
}

open class TypedNode<T : Any?>() {
    val tos = mutableListOf<TypedVertex<*, *>>()
    val froms = mutableListOf<TypedNode<*>>()

    //assume tree
    fun origin(): TypedNode<*> {
        var origin: TypedNode<*> = this
        while (origin.froms.size > 0) {
            origin = origin.froms.get(0)
        }
        return origin
    }

    fun singlePathString(): String {
        var node = origin()
        val stringBuilder = StringBuilder()
        while (node.tos.size > 0) {
            if (stringBuilder.length != 0) stringBuilder.append(".")
            stringBuilder.append(node.tos.get(0).name)
            node = node.tos.get(0).toNode
        }
        return stringBuilder.toString()
    }

    operator fun <R : Any?> invoke(getter: (from: T) -> R): TypedNode<R> = p(getter)

    infix fun <R : Any?> p(getter: (from: T) -> R): TypedNode<R> {
        val toNode = TypedNode<R>()
        val vertex = One2OneVertex<T, R>(getter, toNode)
        tos.add(vertex)
        toNode.froms.add(this)
        return toNode
    }

    infix fun <R : Any?> po(getter: (from: T) -> R?): TypedNode<R> {
        val toNode = TypedNode<R>()
        val vertex = One2OneVertexOpt<T, R>(getter, toNode)
        tos.add(vertex)
        toNode.froms.add(this)
        return toNode
    }


    fun <L : Any?> toList(getter: (from: T) -> java.util.List<L>?): ListNode<L> {
        val toNode = ListNode<L>()
        val vertex = ToListVertex<T, L>(getter, toNode)
        tos.add(vertex)
        toNode.froms.add(this)
        return toNode
    }

    operator fun <L : Any?> invoke(getter: (from: T) -> kotlin.collections.List<L>?): ListNode<L> = toKList(getter)

    fun <L : Any?> toKList(getter: (from: T) -> kotlin.collections.List<L>?): ListNode<L> {
        val toNode = ListNode<L>()
        val vertex = ToKListVertex<T, L>(getter, toNode)
        tos.add(vertex)
        toNode.froms.add(this)
        return toNode
    }

}

class ListNode<X : Any?> : TypedNode<java.util.List<X>>() {
    operator fun get(index: Int): TypedNode<X> = index(index)
    fun index(index: Int): TypedNode<X> {
        val toNode = TypedNode<X>()
        val vertex = IndexVertex<X>(index, toNode)
        tos.add(vertex)
        toNode.froms.add(this)
        return toNode
    }
}

// autocreated getters of even java.util.List are implemented as kotlin.collections.List
class KListNode<X : Any?> : TypedNode<kotlin.collections.List<X>>() {
    operator fun get(index: Int): TypedNode<X> = index(index)
    fun index(index: Int): TypedNode<X> {
        val toNode = TypedNode<X>()
        val vertex = IndexVertex<X>(index, toNode)
        tos.add(vertex)
        toNode.froms.add(this)
        return toNode
    }
}

fun <R : Any?> getFromOriginSingleValue(node: TypedNode<R>, root: Any?): R? {
    var cursor = node.origin()
    var result = root
    while (cursor != node && result != null) {
        val vertex = cursor.tos.get(0)
        result = vertex.traverse(result)
        cursor = vertex.toNode
    }
    return result as R?
}


fun <T : Any, R: Any?> getFromOriginSingleValue(root: Any?, path: (TypedNode<T>)->TypedNode<R> ) : R? {
    return getFromOriginSingleValue( path(TypedNode<T>()), root )
}




