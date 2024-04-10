package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Activity
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeFunction
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeRepository
import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeFunctionType
import kotlinx.coroutines.flow.Flow

@RapidPrototypeRepository
interface IAppRepository {

    @RapidPrototypeFunction(type = RapidPrototypeFunctionType.SELECT)
    fun getSavedActivites(): Flow<List<Activity>>

    @RapidPrototypeFunction(type = RapidPrototypeFunctionType.INSERT)
    suspend fun insertActivity(activity: Activity): Long

    @RapidPrototypeFunction(type = RapidPrototypeFunctionType.DELETE)
    suspend fun deleteActivity(activity: Activity)
}