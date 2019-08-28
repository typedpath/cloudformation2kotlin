package com.typedpath.awscloudformation

import java.util.*

enum class ParameterType(val awsTypeName: String) {
  STRING("String"),
  AWS_EC2_KeyPair("AWS::EC2::KeyPair::KeyName");

  override fun toString(): String {
    return awsTypeName
  }
}

open class CloudFormationTemplate {
  val AWSTemplateFormatVersion = "2010-09-09"
  open val transform: String? = null
  open val description : String? = null

  class Parameter(val type: ParameterType, val description: String) {
    var default: String? = null
    var minLength: Int? = null
    var maxLength: Int? = null
    var allowedPattern: String? = null
  }

  //TO make this lower case
  var parameters = mutableMapOf<String, Parameter>()

  fun parameter(name: String, parameter: Parameter) {
    this.parameters.put(name, parameter)
  }

  // https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/outputs-section-structure.html
  class Output(val value: String) {
    var description: String? = null

    class Export(val name: String)

    var export: Export? = null
  }

  var outputs = mutableMapOf<String, Output>()

  fun output(logicalId: String, output: Output) {
    this.outputs.put(logicalId, output)
  }

  private val currentStackName = "AWS::StackName"
  private val currentRegionName = "AWS::Region"
  private val currentAccountId = "AWS::AccountId"

  var resources = mutableMapOf<String, Resource>()

  fun resource(name: String, resource: Resource) {
    resources.put(name, resource)
  }


  private val refs = mutableMapOf<String, Any>()

  fun ref(resource: Any): String {
    val id = UUID.randomUUID().toString()
    refs.put(id, resource)
    return id
  }

  class StaticResource(val name: String) : Resource() {
    override fun getResourceType(): String = name
  }

  fun refStatic(name: String): String {
    val id = UUID.randomUUID().toString()
    refs.put(id, StaticResource(name))
    return id
  }

  fun refCurrentStack(): String = refStatic(currentStackName)

  fun refCurrentRegion(): String = refStatic(currentRegionName)

  fun refCurrentAccountId(): String = refStatic(currentAccountId)

  fun multilineValue(str: String): String {
    val id = UUID.randomUUID().toString()
    val lines = listOf<String>("!Sub |").plus(str.lines())
    refs.put(id,  MultiLineValue(lines))
    return id
  }

  fun inlineCode(js: String, commentStart: String = "//") : String {
// make sure there first line has no initial space
     return multilineValue("""
$commentStart inline
$js""".trimIndent())
  }

  fun inlinePythonCode(str: String) : String = inlineCode(str, "#")

  class JoinStatement(val delimiter: String, val items: List<String>) {

    val parameters: List<Any>

    init {
      parameters = listOf(delimiter, items)
    }

    fun valuePair() = mapOf(Pair("!Join", parameters))

  }

  private val intrinsicFunctionParameters = mutableSetOf<Any>()

  fun join(delimiter: String, items: List<String>): String {
    val id = UUID.randomUUID().toString()
    // could be Value: !GetAtt [S3Bucket, WebsiteURL]
    val functionCall = JoinStatement(delimiter, items)
    refs.put(id, functionCall.valuePair())
    intrinsicFunctionParameters.add(functionCall.parameters)
    return id
  }

  fun sub(value: String): String {
    val id = UUID.randomUUID().toString()
    val f = SingleLineFunctionCall("""!Sub "$value"""")
    refs.put(id, f)
    return id
  }

  fun Parameter(
      type: ParameterType,
      description: String,
      init: Parameter.() -> Unit
  ): Parameter =
    Parameter(type, description).apply(init)

  abstract class Materializeable {
     abstract fun materialise() : Any
  }

  open class TemplateReference(val name: String) : Materializeable(){
    override fun materialise(): String {
      val isArn = name.toLowerCase().endsWith(".arn")
      return """${if (isArn)"!GetAtt" else "!Ref"} $name"""
    }
  }

  class AttributeReference(val resourceName: String, val attributeName: String) : Materializeable() {
    override  fun materialise(): Map<String, List<String>> {
      return mapOf(Pair("Fn::GetAtt", listOf(resourceName, attributeName)))
    }
  }

  open class SingleLineFunctionCall(val value: String) : Materializeable() {
    override fun materialise(): String {
      return value
    }
  }

  fun deref(value: Any): Any {
    //TODO inline function call


    val obj = refs.get(value)
    if (obj == null) throw RuntimeException("cant deref $obj")
    return if (obj is StaticResource) {
        TemplateReference(obj.name)
    } else if (obj is Resource) {
      val entries = resources.filter { entry -> entry.value == obj }.map { entry2 -> entry2.key }
      val resourceName =
        if (entries.size == 0) obj.getResourceType()
        else entries.get(0)
        TemplateReference(resourceName)
    } else if (obj is Parameter) {
      val names = parameters.filter { entry -> entry.value == obj }.map { entry2 -> entry2.key }
      if (names.size == 0) throw java.lang.RuntimeException("cant reference unkown paramete ${obj}")
        TemplateReference(names.get(0))
    } else if (obj is Resource.Attribute) {
      val entries = resources.filter { entry -> entry.value == obj.parent }.map { entry2 -> entry2.key }

      if (entries.size == 0) {
        throw java.lang.RuntimeException("failed to locate resource ${obj.parent} for attribute ${obj.name}")
      }
        AttributeReference(entries.get(0), obj.name)
    } else
      obj
  }

  fun isRef(value: Any) = refs.containsKey(value)

}

fun cloudFormationTemplate(init: CloudFormationTemplate.() -> Unit): CloudFormationTemplate =
  CloudFormationTemplate().apply(init)


