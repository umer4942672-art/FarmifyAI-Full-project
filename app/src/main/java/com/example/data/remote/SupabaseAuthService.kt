package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SupabaseAuthResult(val isSuccess:Boolean,val userId:String?=null,val email:String?=null,val accessToken:String?=null,val userMetadata:Map<String,Any?>=emptyMap(),val errorMessage:String?=null)

/** Legacy class name retained so existing repositories need minimal changes. Calls FarmifyAI backend, not Supabase directly. */
class SupabaseAuthService {
    private val client=OkHttpClient.Builder().connectTimeout(20,TimeUnit.SECONDS).readTimeout(20,TimeUnit.SECONDS).build()
    private val media="application/json; charset=utf-8".toMediaType()
    private suspend fun post(path:String, body:JSONObject):SupabaseAuthResult=withContext(Dispatchers.IO){
        try{
            val r=client.newCall(Request.Builder().url(ApiConfig.BASE_URL.trimEnd('/')+path).post(body.toString().toRequestBody(media)).build()).execute()
            val text=r.body?.string().orEmpty(); val j=JSONObject(text)
            if(!r.isSuccessful) return@withContext SupabaseAuthResult(false,errorMessage=j.optString("detail", "Request failed"))
            val u=j.optJSONObject("user"); val meta=mutableMapOf<String,Any?>(); u?.optJSONObject("user_metadata")?.keys()?.forEach{meta[it]=u.getJSONObject("user_metadata").get(it)}
            SupabaseAuthResult(true,u?.optString("id"),u?.optString("email"),j.optString("access_token").ifBlank{null},meta)
        }catch(e:Exception){ SupabaseAuthResult(false,errorMessage="Backend connection failed: ${e.localizedMessage}") }
    }
    suspend fun signUp(email:String,password:String,metadata:Map<String,Any> = emptyMap()):SupabaseAuthResult {
        val meta=JSONObject(); metadata.forEach{(k,v)->meta.put(k,v)}
        return post("/api/auth/signup",JSONObject().put("email",email.trim()).put("password",password).put("metadata",meta))
    }
    suspend fun signInWithPassword(email:String,password:String)=post("/api/auth/login",JSONObject().put("email",email.trim()).put("password",password))
    suspend fun recoverPassword(email:String)=post("/api/auth/forgot-password",JSONObject().put("email",email.trim()))
}
