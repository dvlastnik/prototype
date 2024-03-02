package cz.mendelu.pef.xvlastni.prototype.annotations

import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeType

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RapidPrototype(val isList: Boolean, val type: RapidPrototypeType)
