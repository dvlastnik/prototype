package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.ui.theme.screens

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.R
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.BaseViewModel
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.CommunitationResult
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.DefaultErrors
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication.IRemoteRepository
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Activity
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.UiState
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeFunction
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@RapidPrototypeViewModel
@HiltViewModel
class ViewModel @Inject constructor(
    private val repository: IRemoteRepository
) : BaseViewModel() {
    val uiState: MutableState<UiState<Activity, DefaultErrors>>
        = mutableStateOf(UiState())

    @RapidPrototypeFunction
    fun generateActivity() {
        launch {
            val result = withContext(Dispatchers.IO) {
                repository.getRandomActivity()
            }

            when(result) {
                is CommunitationResult.CommunicationError ->
                    uiState.value = UiState(loading = false, data = null, errors = DefaultErrors(
                        R.string.no_internet_connection)
                    )
                is CommunitationResult.Error ->
                    uiState.value = UiState(loading = false, data = null, errors = DefaultErrors(
                        R.string.failed_to_load)
                    )
                is CommunitationResult.Exception ->
                    uiState.value = UiState(loading = false, data = null, errors = DefaultErrors(
                        R.string.unknown_error)
                    )
                is CommunitationResult.Success ->
                    uiState.value = UiState(loading = false, data = result.data, errors = null)
            }
        }
    }
}