package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Pet(
    var id: Long?,
    var category: Category?,
    var name: String?,
    var photoUrls: List<String>?,
    var tags : List<Tag>?,
    var status: String?
)
