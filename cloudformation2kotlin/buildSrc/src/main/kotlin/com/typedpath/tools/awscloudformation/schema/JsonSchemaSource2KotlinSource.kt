package com.typedpath.tools.awscloudformation.schema

import jdk.nashorn.api.scripting.ScriptObjectMirror
import java.util.*
import java.util.Arrays.asList

val IamPolicyQualifiedClassName = "com.typedpath.awscloudformation.IamPolicy"
val UntypedJsonQualifiedClassName = "Any"
val PipelineStageActionConfigurationQualifiedClassName="com.typedpath.awscloudformation.schema.PipelineStageActionConfiguration"
val ResourceQualifiedClassName = "com.typedpath.awscloudformation.Resource"

fun jsonSchemaString2Kotlin(strJsonSchema: String, strPackage: String): Pair<String, String> {

  val jsonSchema: ScriptObjectMirror = stringToJson(strJsonSchema)
  val resourceTypeJson = jsonSchema["ResourceType"]
  if (resourceTypeJson == null || resourceTypeJson !is ScriptObjectMirror)
    throw throw RuntimeException("no ResourceType map found in $jsonSchema")
  if (resourceTypeJson.size == 0) throw RuntimeException("expected ResourceType size 1 but was ${resourceTypeJson.size}")
  val roots: List<Map.Entry<String, ScriptObjectMirror>> = resourceTypeJson.entries.map { entry ->
    entry as Map.Entry<String, ScriptObjectMirror>
  }.filter { tentry -> !tentry.key.contains(".") }

  if (roots.size == 0) {
    throw RuntimeException("no root classes in $strJsonSchema")
  }
  if (roots.size > 1) {
    throw RuntimeException("found ${roots.size} not 1 as expected in $strJsonSchema")

  }
  val root = roots.first().value

  val innerClassSpecs =
    if (jsonSchema.containsKey("PropertyTypes"))
      (jsonSchema.get("PropertyTypes") as ScriptObjectMirror).map { entry -> entry as Map.Entry<String, ScriptObjectMirror> }
    else asList()

  return jsonSchema2Kotlin(strPackage, roots.first().key, root, innerClassSpecs)
}

private fun propertySpec(
  parentType: String,
  name: String,
  propertySpec: ScriptObjectMirror,
  isExternal: Boolean = false
): String {
  var type = propertySpec.get("Type") as String?

  val isList = "List".equals(type)
  val isMap = "Map".equals(type)
  var primitiveItemType = propertySpec.get("PrimitiveItemType")
  var itemType = propertySpec.get("ItemType") as String?

  var primitiveType = propertySpec.get("PrimitiveType")

  //TODO isolate these crazy rules
  if (/*("AWS::IAM::Policy".equals(parentType)
                || "AWS::IAM::ManagedPolicy".equals(parentType) )
        &&*/ "policyDocument".equals(name) || "assumeRolePolicyDocument".equals(name)
  ) {
    primitiveType = IamPolicyQualifiedClassName
  }
  if ("Timestamp".equals(primitiveType)) {
    primitiveType = Date::class.java.name
  }
  if ("Integer".equals(primitiveType)) {
    primitiveType = "Int"
  }
  if ("Integer".equals(primitiveItemType)) {
    primitiveItemType = "Int"
  }
  //TODO eliminate this by resolving all json schemas
  if ("Json".equals(primitiveType)) {
    primitiveType = UntypedJsonQualifiedClassName
  }
  if ("Map".equals(primitiveType)) {
    primitiveType = "Map<Any,Any>"
  }
  if ("Json".equals(primitiveItemType)) {
    primitiveItemType = UntypedJsonQualifiedClassName
  }
  //TODO remove this
  if (parentType.endsWith("ActionDeclaration") && name.equals("configuration")) {
    type = PipelineStageActionConfigurationQualifiedClassName
    primitiveType = null
  }

  fun scopedItemType(type: String?) = if (isExternal) "$parentType.$type" else type

  val typeSpec =
    if (isMap) "Map<String, ${if (primitiveItemType != null) primitiveItemType else scopedItemType(itemType)}>"
    else if (isList) "List<${if (primitiveItemType != null) primitiveItemType else scopedItemType(itemType)}>"
    else ("${if (primitiveType != null) primitiveType else scopedItemType(type)}")
  //println("***name: typespec=> $name: $typeSpec primitiveType=>$primitiveType  type=>$type  isList=>$isList primitiveItemType=?$primitiveItemType itemType:$itemType")

  return "$name: $typeSpec"
}

private fun camelCase(str: String) = "${str.substring(0, 1).toLowerCase()}${str.replace(".", "").substring(1)}"

private class PropertyEntry(val key: String, val value: ScriptObjectMirror) {
  fun name(): String {
    return camelCase(key)
  }
}

private fun propertiesToMutableProperties(properties: ScriptObjectMirror): List<PropertyEntry> {
  return if (properties == null) listOf() else
    properties.entries.map { it as Map.Entry<String, ScriptObjectMirror> }.filter {
      !true.equals(it.value.get("Required"))
    }.map { tentry ->
      PropertyEntry(tentry.key, tentry.value)
    }
}

private fun propertiesToImmutableProperties(properties: ScriptObjectMirror): List<PropertyEntry> {
  return properties.entries.map { it as Map.Entry<String, ScriptObjectMirror> }.filter {
    true.equals(it.value.get("Required"))
  }.map { tentry ->
    PropertyEntry(tentry.key, tentry.value)
  }
}

private fun jsonSchema2Kotlin(
  strPackage: String, resourceName: String, resourceSpec: ScriptObjectMirror,
  innerClassSpecs: List<Map.Entry<String, ScriptObjectMirror>>, isRoot: Boolean = true
): Pair<String, String> {
  println("jsonSchema2Kotlin processing $resourceName")
  var className = resourceName.replace("::", "_")
  val lastDotIndex = className.lastIndexOf('.')
  if (lastDotIndex > 0) {
    className = className.substring(lastDotIndex + 1)
  }

  val immutableProperties: List<PropertyEntry>
  val mutableProperties: List<PropertyEntry>
  if (!resourceSpec.containsKey("Properties")) {
    //TODO fix this e.g. AWS::CodeBuild::Project.FilterGroup is a straight list
    println("jsonSchema2Kotlin no properties for $resourceName")
    immutableProperties = listOf()
    mutableProperties = listOf()
  } else {
    val propertiesJs = resourceSpec["Properties"] as ScriptObjectMirror
    immutableProperties = if (propertiesJs == null) listOf() else propertiesToImmutableProperties(propertiesJs)
    mutableProperties = propertiesToMutableProperties(propertiesJs)
  }

  val attributeNames = if (!resourceSpec.containsKey("Attributes")) listOf<String>() else
    (resourceSpec.get("Attributes") as ScriptObjectMirror).keys

  val hasImmutableProperties = immutableProperties.size > 0

  val strClass = """
${if (isRoot)
    """package $strPackage
// created on ${Date()} by ${::jsonSchema2Kotlin.toString()}
            """ else ""}
class $className(${
  immutableProperties.joinToString(",") {
    """
  val ${propertySpec(resourceName, it.name(), it.value)}"""
  }
  })${if (isRoot) """: ${ResourceQualifiedClassName} () """ else ""} {
  ${if (isRoot) "override" else ""} fun getResourceType_() = "$resourceName"
  // Properties:${
  mutableProperties.joinToString("") {
    """
  var ${propertySpec(resourceName, it.name(), it.value)}? = null"""
  }
  }
${attributeNames.joinToString("") {
    """  fun ${camelCase(it)}Attribute() = Attribute(this, "${it}");
"""
  }}

${if (isRoot) {
    innerClassSpecs.map {
      jsonSchema2Kotlin(
        strPackage,
        it.key,
        it.value,
        emptyList<Map.Entry<String, ScriptObjectMirror>>(),
        false
      ).second
    }.joinToString("")
  } else ""
  }
}
fun ${camelCase(className)}(${
  immutableProperties.joinToString(",") {
    """${propertySpec(className, it.name(), it.value, isRoot)}"""
  }
  }${if (hasImmutableProperties) "," else ""} init: $className.()
    -> Unit
): $className = $className(${immutableProperties.joinToString(",")
  { """ ${it.name()}""" }}).apply(init)

"""
  return Pair(className, strClass)
}