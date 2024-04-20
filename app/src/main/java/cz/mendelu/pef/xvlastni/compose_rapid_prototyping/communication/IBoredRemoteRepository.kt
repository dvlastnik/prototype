package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Activity
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeFunction
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeRepository
import cz.mendelu.pef.xvlastni.prototype.classes.CommunicationResult
import cz.mendelu.pef.xvlastni.prototype.classes.IBaseRemoteRepository
import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeFunctionType

@RapidPrototypeRepository
interface IBoredRemoteRepository : IBaseRemoteRepository {
    @RapidPrototypeFunction(type = RapidPrototypeFunctionType.SELECT)
    suspend fun getRandomActivity(): CommunicationResult<Activity>
    suspend fun getSpecifiedActivity(type: String): CommunicationResult<Activity>
}