package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Tag(
    var id: Long?,
    var name: String?
) {
    override fun toString(): String {
        if (name != null) {
            return name!!
        }
        else {
            return "None"
        }
    }
}
