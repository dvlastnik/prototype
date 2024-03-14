package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Pet
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Status
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PetsAPI {

    @GET("pet/findByStatus")
    suspend fun findByStatus(@Query("status") status: String): Response<List<Pet>>

    @GET("pet/{id}")
    suspend fun findById(@Path("id") id: Long): Response<Pet>

    @POST("pet")
    suspend fun postPet(@Body pet: Pet): Response<Pet>

    @DELETE("pet/{id}")
    suspend fun deletePet(@Path("id") id: Long): Response<Status>
}