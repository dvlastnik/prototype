package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Category(
    var id: Long?,
    var name: String?
)
