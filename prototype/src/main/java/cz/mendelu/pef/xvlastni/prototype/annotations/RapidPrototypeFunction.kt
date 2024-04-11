package cz.mendelu.pef.xvlastni.prototype.annotations

import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeFunctionType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class RapidPrototypeFunction(val type: RapidPrototypeFunctionType)
