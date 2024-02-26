package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.CommunitationResult
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Activity

class RemoteRepositoryImpl(private val boredAPI: BoredAPI): IRemoteRepository {
    override suspend fun getRandomActivity(): CommunitationResult<Activity> {
        return processResponse {
            boredAPI.getRandomActivity()
        }
    }
}