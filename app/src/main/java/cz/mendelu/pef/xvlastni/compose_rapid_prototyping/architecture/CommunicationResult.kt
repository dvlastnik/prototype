package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture

sealed class CommunitationResult<out T: Any> {
    class Success<T: Any>(val data: T): CommunitationResult<T>()
    class Error(val error: cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.Error): CommunitationResult<Nothing>()
    class CommunicationError(): CommunitationResult<Nothing>()
    class Exception(val exception: Throwable): CommunitationResult<Nothing>()
}