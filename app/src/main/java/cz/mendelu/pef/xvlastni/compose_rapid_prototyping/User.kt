package cz.mendelu.pef.xvlastni.compose_rapid_prototyping

import cz.mendelu.pef.xvlastni.prototype.RapidPrototype

@RapidPrototype
data class User(
    val id: Long,
    val username: String
)