package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model

import java.io.Serializable

open class UiState<T, E>
    (
    var loading: Boolean = true,
    var data: T? = null,
    var errors: E? = null) : Serializable {
}