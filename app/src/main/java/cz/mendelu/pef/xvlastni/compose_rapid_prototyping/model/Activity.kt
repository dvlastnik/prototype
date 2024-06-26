package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model

import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototype
import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeType

@RapidPrototype(isList = false, type = RapidPrototypeType.API)
data class Activity(
    val accessibility: Double,
    val activity: String,
    val key: String,
    val link: String,
    val participants: Int,
    val price: Double,
    val type: String
) {
}