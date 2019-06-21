package com.typedpath.tools.awscloudformation.schema

import jdk.nashorn.api.scripting.ScriptObjectMirror
import javax.script.ScriptEngineManager

@Throws(Exception::class)
fun stringToJson(strJson: String): ScriptObjectMirror {
    val json = "var result = $strJson; result;"
    val factory = ScriptEngineManager()
    val engine = factory.getEngineByName("JavaScript")
    return engine.eval(json) as ScriptObjectMirror
}

@Throws(Exception::class)
fun printJsonObject(jsonObject: String) {
    val result = stringToJson(jsonObject)
    println(result.javaClass.name + "::" + result)
    print("", result)
}

fun print(indent: String, to: ScriptObjectMirror) {
    for ((key, value) in to) {
        if (value != null && value is ScriptObjectMirror) {
            println(indent + value.className + "::" + key + "==")
            print("$indent  ", value)
        } else {
            println("$indent$key==$value")
        }
    }
}