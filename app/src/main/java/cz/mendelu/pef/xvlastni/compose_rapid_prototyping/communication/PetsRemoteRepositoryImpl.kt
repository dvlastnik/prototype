package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Pet
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Status
import cz.mendelu.pef.xvlastni.prototype.classes.CommunicationResult
import javax.inject.Inject

class PetsRemoteRepositoryImpl @Inject constructor(private val petsAPI: PetsAPI)
    : IPetsRemoteRepository {
    override suspend fun findByStatus(): CommunicationResult<List<Pet>> {
        return processResponse {
            petsAPI.findByStatus("available")
        }
    }

    override suspend fun findById(id: Long): CommunicationResult<Pet> {
        return processResponse {
            petsAPI.findById(id)
        }
    }

    override suspend fun postPet(pet: Pet): CommunicationResult<Pet> {
        return processResponse {
            petsAPI.postPet(pet)
        }
    }

    override suspend fun deletePet(id: Long): CommunicationResult<Status> {
        return processResponse {
            petsAPI.deletePet(id)
        }
    }
}