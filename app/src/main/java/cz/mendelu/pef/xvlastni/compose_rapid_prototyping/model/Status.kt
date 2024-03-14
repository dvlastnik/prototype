package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Status(
    val code: Int?,
    val message: String?,
    val type: String?
)