package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.CommunitationResult
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.IBaseRemoteRepository
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Activity

interface IRemoteRepository : IBaseRemoteRepository {
    suspend fun getRandomActivity(): CommunitationResult<Activity>
}