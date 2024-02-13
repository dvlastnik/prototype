package cz.mendelu.pef.xvlastni.annotationsprocessor

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PrintMessage(val message: String)
