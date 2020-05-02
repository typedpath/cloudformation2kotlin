package com.typedpath.awscloudformation

import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.text.*

class MultiLineValue(val lines: List<String>) {
}

fun toYamlString(any: Any): String {
  var myStringBuilder = MyStringBuilder("\r\n")
  toYaml(myStringBuilder, newIndent(), any)
  return myStringBuilder.toString()
}

fun toYaml(template: CloudFormationTemplate, newLine: String = "\r\n"): String {
  //for subclasses map root level properties into the containers: resources, parameters, Outputs
  if (template.javaClass != CloudFormationTemplate::class.java) {
    template.javaClass.kotlin.memberProperties.map { it -> Pair(it, it.get(template)) }
      .filter { it.second != null }.forEach {
        if (it.second is Resource) template.resource(it.first.name, it.second as Resource)
        else if (it.second is CloudFormationTemplate.Parameter) template.parameter(
          it.first.name,
          it.second as CloudFormationTemplate.Parameter
        )
        else if (it.second is CloudFormationTemplate.Output) template.output(
          it.first.name,
          it.second as CloudFormationTemplate.Output
        )

      }
  }
  val jsValue = toJsCompatible(template)
  val sb = MyStringBuilder(newLine)
  toYaml(sb, newIndent(), jsValue)
  return sb.toString()
}

// private below

private fun propertyName2YamlName(name: String) = "${name.substring(0, 1).toUpperCase()}${name.substring(1)}"

private fun isEmpty(propertyValue: Any?): Boolean {
  return propertyValue == null || propertyValue is List<*> && propertyValue.size == 0 || propertyValue is Map<*, *> && propertyValue.size==0
}

private fun toJsCompatibleComplex(value: Any, dereferencer: CloudFormationTemplate): Map<String, Any> {
  // ignore all properties in subclasses
  val propertiesClass = if (value is CloudFormationTemplate)
    CloudFormationTemplate::class.java else value.javaClass

  val ktmp = propertiesClass.kotlin.memberProperties
  val jtmp = propertiesClass.javaClass.declaredFields

  val properties = propertiesClass.kotlin.memberProperties
    .map { p -> p as KProperty1<*,*> }
    .filter { p ->
    p.visibility == KVisibility.PUBLIC
            && !p.name.equals("deletionPolicy") && !isEmpty(p.getter.call(value))
  }

  val result = mutableMapOf<String, Any>()
  var propertyContainer: MutableMap<String, Any> = result

  if (value is Resource) {
    result.put("Type", toJsSimpleValue(value.getResourceType_()))
    if (value.deletionPolicy != null) {
      result.put("DeletionPolicy", toJsSimpleValue(value.deletionPolicy!!))
    }
    val resourceProperties = mutableMapOf<String, Any>()
    propertyContainer = resourceProperties
  }

  properties.forEach {
    val pValue = it.getter.call(value)
    if (pValue != null && !(pValue is Map<*, *> && pValue.size == 0)
      && !(pValue is List<*> && pValue.size == 0)
    ) {
      propertyContainer.put(
        propertyName2YamlName(it.name),
        toJsCompatible(pValue, dereferencer)
      )
    }
  }

  if (value is Resource && propertyContainer.size > 0) {
    result.put("Properties", propertyContainer)
  }

  return result
}

class MyStringBuilder(private val newLine: String) {
  private val sb = StringBuilder()
  private var unNewLined = false;

  fun isNewLined() = !unNewLined

  fun append(indent: () -> String, lines: MultiLineValue ) {
       lines.lines.forEach {
         sb.append(indent())
         sb.append(it)
         newLine()
       }
  }

  fun append(str: String) {
    sb.append(str)
    if (str.length > 0) {
      unNewLined = true
    }
  }

  fun newLine() {
    sb.append(newLine)
    unNewLined = false
  }

  override fun toString(): String = sb.toString()
}

private fun isComplex(value: Any?): Boolean = value != null &&
        value.javaClass.name.startsWith("com.typedpath") &&
        !value.javaClass.kotlin.equals(ParameterType::class) &&
        !value.javaClass.kotlin.equals(MultiLineValue::class)


private fun toJsSimpleValue(
  value: Any
): Any {
  var printValue =
    if (value is ParameterType) {
      value.awsTypeName
    } else if (value is Number) {
      var strValue = "$value"
      if (strValue.endsWith(".0")) {
        strValue = strValue.substring(0, strValue.length-2)
      }
      strValue
    }

    else "'${value}'"
  return printValue
}

private fun toJsCompatible(value: Any, dereferencer: CloudFormationTemplate): Any {

  return if (value is CloudFormationTemplate.Materializeable) {
    value.materialise()
  } /*else if (value is CloudFormationTemplate.AttributeReference) {
    value.materialise()
  }*/
  else if (value is MultiLineValue) {
    value
  }
  else if (value.javaClass.isEnum) {
    value.toString()
  } else if (value is List<*>) {
    val resultList = mutableListOf<Any>()
    if (value.size > 0) {
      value.forEach {
        if (it != null) {
          val lValue = toJsCompatible(it, dereferencer)
          resultList.add(lValue)
        }
      }
    }
    return resultList
  } else if (value is Map<*, *>) {
    val mapResult = mutableMapOf<String, Any>()
    if (value.size > 0) {
      value.forEach { key, mapValue ->
        if (mapValue != null) {
          mapResult.put(key.toString(), toJsCompatible(mapValue, dereferencer))
        }
      }
    }
    return mapResult
  } else if (isComplex(value)) {
    return toJsCompatibleComplex(value, dereferencer)
  } else {
    if (dereferencer.isRef(value)) {
      toJsCompatible(dereferencer.deref(value), dereferencer)
    } else
      toJsSimpleValue(value)
  }
}

fun toJsCompatible(template: CloudFormationTemplate) =
  toJsCompatible(template, template)

private val indentStep = "  "

private fun indent(indent: Stack<String>): String {
  if (indent.size == 0) {
    indent.push("")
  } else {
    indent.push(indentPeek(indent) + indentStep)
  }
  return indentPeek(indent)
}

private fun indentPeek(indent: Stack<String>): String {
  return if (indent.size == 0) "" else indent.peek()
}

private fun toYaml(
  sb: MyStringBuilder,
  indent: Stack<String>,
  value: Any
) {

  fun effectivePeekIndent() = "${if (!sb.isNewLined()) " " else indent.peek()}"
  if (value is List<*>) {
    var index = 0
    value.forEach {
      sb.append(effectivePeekIndent())
      sb.append("-")
      indent(indent)
      toYaml(sb, indent, it!!)
      indent.pop()
      sb.newLine()
      index++
    }
  } else if (value is Map<*, *>) {
    value.forEach() {
      if (it.value != null) {
        sb.append(effectivePeekIndent())
        sb.append(it.key.toString())
        //TODO review this
        if (!it.key.toString().startsWith("!")) sb.append(":")
        if (it.value is List<*> || it.value is Map<*, *>) {
          sb.newLine()
          indent(indent)
          toYaml(sb, indent, it.value!!)
          indent.pop()
        } else {
          sb.append(effectivePeekIndent())
          if (it.value is MultiLineValue) {
            indent(indent)
            sb.append(::effectivePeekIndent, it.value as MultiLineValue)
            indent.pop()
          }
          else sb.append(it.value.toString())
          sb.newLine()
        }
      }
    }
  } else {
    sb.append(effectivePeekIndent())
    if (value is MultiLineValue) sb.append(::effectivePeekIndent, value)
    else sb.append(value.toString())
  }
}

private fun newIndent(): Stack<String> {
  val indent = Stack<String>()
  indent.push("")
  return indent
}

