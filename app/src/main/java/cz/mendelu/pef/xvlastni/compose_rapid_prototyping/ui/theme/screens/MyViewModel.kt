package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.ui.theme.screens

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.BaseViewModel
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.Error
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database.IAppRepository
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.UiState
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.User
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeFunction
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeViewModel
import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeFunctionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@RapidPrototypeViewModel
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: IAppRepository
) : BaseViewModel() {
    val uiState: MutableState<UiState<List<User>, Error>>
        = mutableStateOf(UiState())

    @RapidPrototypeFunction(RapidPrototypeFunctionType.SELECT)
    fun getUsers() {
        launch {
            repository.getUsers()
                .onStart {
                    uiState.value = UiState(loading = true, data = null, errors = null)
                }
                .catch {
                    uiState.value = UiState(loading = false, data = null, errors = Error(0, it.localizedMessage))
                }
                .collect {
                    uiState.value = UiState(loading = false, data = it, errors = null)
                }
        }
    }

    @RapidPrototypeFunction(RapidPrototypeFunctionType.INSERT)
    fun insertUser() {
        launch {
            val user = User("Divad", "divadek@email.cz", age = 23)

            val id = repository.insertUser(user)

            if (id > 0) {
                Log.d("Rapid Prototype", "Saved")
            }
            else {
                Log.d("Rapid Prototype", "Not saved")
            }
        }
    }
}