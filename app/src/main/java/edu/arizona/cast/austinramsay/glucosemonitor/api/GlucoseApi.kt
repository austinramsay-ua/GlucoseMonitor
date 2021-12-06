package edu.arizona.cast.austinramsay.glucosemonitor.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GlucoseApi {
    @FormUrlEncoded
    @POST("~lxu/cscv381/local_glucose_log.php")
    fun uploadGlucose(@Field("username") user:String, @Field("password") pw: String, @Field("data") data: String): Call<String>
}