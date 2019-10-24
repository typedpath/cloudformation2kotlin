package com.typedpath.awscloudformation.test.serverless.typesafebackendapi

import com.typedpath.testdomain.toJsonInsertUuidsTypeInfo
import com.typedpath.testdomain.toList
import com.typedpath.testdomain.toObject
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.net.URLEncoder
import java.util.*
import kotlin.reflect.KClass

// this class is stateful !
class Saver(val baseUrl: String) {

    private val putUrl = "${baseUrl}"
    // this is the state! - map from object to db id
    private val object2Id = mutableMapOf<Any, UUID>()

    fun id(o: Any) = object2Id.get(o)
    //Set<Any> updates,
    fun save(item: Any, dirty: Set<Any> = setOf()): UUID {
        var httpclient = HttpClients.createDefault()
        val httpPut = HttpPut(putUrl)
        val json = toJsonInsertUuidsTypeInfo(item, null, false, object2Id, dirty)
        httpPut.setEntity(StringEntity(json, ContentType.APPLICATION_JSON))
        val putResponse = httpclient.execute(httpPut)
        if (putResponse.statusLine.statusCode != 200) {
            throw Exception("failed to save statusLine: ${putResponse.statusLine}")
        }
        return object2Id.get(item)!!
    }

    fun <T : Any> retrieve(rootClass: KClass<T>, id: UUID): T {
        val httpclient = HttpClients.createDefault()
        val getUrl = "${baseUrl}${URLEncoder.encode(rootClass.qualifiedName)}/${URLEncoder.encode(id.toString())}"
        val httpGet = HttpGet(getUrl)
        val getResponse = httpclient.execute(httpGet)
        if (getResponse.statusLine.statusCode != 200) {
            throw Exception("failed to retrieve a ${rootClass.qualifiedName}::$id statusLine: ${getResponse.statusLine}")
        }
        val responseString = EntityUtils.toString(getResponse.entity, "UTF-8")
        return toObject(responseString, rootClass, object2Id) as T
    }

    fun delete(item: Any) {
        val httpclient = HttpClients.createDefault()
        if (!object2Id.contains(item)) {
            throw Exception("cant delete unknown item ${item}")
        }
        val id = object2Id.get(item)
        val deleteUrl = "${baseUrl}${item.javaClass.kotlin.qualifiedName}/${id}"
        val httpDelete = HttpDelete(deleteUrl)
        println("testing delete $deleteUrl")
        val deleteResponse = httpclient.execute(httpDelete)
        if (deleteResponse.statusLine.statusCode != 200) {
            throw Exception("failed to delete ${item}::$id statusLine: ${deleteResponse.statusLine}")
        }
        object2Id.remove(item)
    }

    fun <T : Any> retrieveMulti(rootClass: KClass<T>, ids: List<UUID>? = null): List<T> {
        val httpclient = HttpClients.createDefault()
        val url = "${baseUrl}multi/${URLEncoder.encode(rootClass.qualifiedName)}/${if (ids==null) "*" else ids.map{it.toString()}.joinToString (",") }"
        println("calling $url")
        val httpGetMulti = HttpGet(url)
        val getMultiResponse = httpclient.execute(httpGetMulti)
        if (getMultiResponse.statusLine.statusCode != 200) {
            throw Exception("failed to retrieve multi ${rootClass.qualifiedName} statusLine: ${getMultiResponse.statusLine}")
        }
        //   also could val contentOut2 = String(getResponse2.entity.content.readBytes())
        val responseString = EntityUtils.toString(getMultiResponse.entity, "UTF-8")
        return toList(responseString, rootClass, object2Id)
    }

}