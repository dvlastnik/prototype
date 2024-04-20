package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Activity
import cz.mendelu.pef.xvlastni.prototype.classes.CommunicationResult
import javax.inject.Inject

class BoredRemoteRepositoryImpl @Inject constructor(private val boredAPI: BoredAPI) : IBoredRemoteRepository {
    override suspend fun getRandomActivity(): CommunicationResult<Activity> {
        return processResponse {
            boredAPI.getRandomActivity()
        }
    }

    override suspend fun getSpecifiedActivity(type: String): CommunicationResult<Activity> {
        return processResponse {
            boredAPI.getSpecifiedActivity(type)
        }
    }

}