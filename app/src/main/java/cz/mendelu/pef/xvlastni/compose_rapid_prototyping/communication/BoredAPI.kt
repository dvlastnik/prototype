package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Activity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BoredAPI {
    //random activity
    @GET("activity")
    suspend fun getRandomActivity(): Response<Activity>

    //specified type
    @GET("activity")
    suspend fun getSpecifiedActivity(@Query("type") type: String): Response<Activity>
}