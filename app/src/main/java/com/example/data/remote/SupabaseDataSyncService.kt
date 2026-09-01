package com.example.data.remote

import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

/** Sends cloud data through FarmifyAI backend. No Supabase service key exists in the APK. */
class SupabaseDataSyncService {
    private val client=OkHttpClient(); private val media="application/json; charset=utf-8".toMediaType()
    private suspend fun send(path:String,data:JSONObject):Boolean=withContext(Dispatchers.IO){try{
        val body=JSONObject().put("data",data).toString().toRequestBody(media)
        client.newCall(Request.Builder().url(ApiConfig.BASE_URL.trimEnd('/')+path).post(body).build()).execute().use{it.isSuccessful}
    }catch(_:Exception){false}}
    suspend fun syncProfile(u:UserEntity):Boolean {
        val id=UUID.nameUUIDFromBytes(u.phoneOrEmail.toByteArray()).toString()
        return send("/api/sync/profile",JSONObject().apply{put("id",id);put("email",u.email);put("full_name",u.fullName);put("phone",u.phone);put("farm_name",u.farmName);put("district",u.district);put("province",u.province);put("farm_location",u.farmLocation);put("total_acres",u.totalAcres);put("primary_crops",u.primaryCropsString)})
    }
    private fun khata(e:KhataEntryEntity,id:Long)=JSONObject().apply{put("id",id);put("type",e.entryType.uppercase());put("crop_name",e.cropName.ifBlank{"General"});put("activity_type",e.activityType.ifBlank{e.entryType});put("amount",e.totalAmount);put("quantity",e.quantity);put("unit",e.unit.ifBlank{"Mann"});put("field_name",e.fieldName.ifBlank{"Main Field"});put("field_size_acres",e.fieldSizeAcres);put("buyer_or_mandi",e.buyerOrMandi.ifBlank{"Local Mandi"});put("description",e.description.ifBlank{"Agri transaction"});put("date_timestamp",if(e.timestamp>0)e.timestamp else System.currentTimeMillis())}
    suspend fun syncKhataTransaction(e:KhataEntryEntity)=send("/api/sync/khata",khata(e,if(e.id>0)e.id else System.currentTimeMillis()))
    suspend fun syncMultipleKhataEntries(entries:List<KhataEntryEntity>):Boolean { var ok=true; entries.forEachIndexed{i,e->ok=syncKhataTransaction(e.copy(id=if(e.id>0)e.id else System.currentTimeMillis()+i)) && ok}; return ok }
    suspend fun syncDiseaseDetection(s:DiseaseScanEntity):Boolean=send("/api/sync/disease",JSONObject().apply{put("id",if(s.id>0)s.id else System.currentTimeMillis());put("crop_name",s.cropName);put("disease_name",s.diseaseNameEn);put("disease_name_ur",s.diseaseNameUr);put("confidence",s.confidencePercent);put("severity",s.severityLevel);put("symptoms",s.symptoms);put("treatment_chemical",s.chemicalTreatment);put("treatment_organic",s.organicPrevention);put("recommendation",s.advisoryNote)})
}
