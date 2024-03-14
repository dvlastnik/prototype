package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.CommunitationResult
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Pet
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Status
import javax.inject.Inject

class PetsRemoteRepositoryImpl @Inject constructor(private val petsAPI: PetsAPI)
    : IPetsRemoteRepository {
    override suspend fun findByStatus(status: String): CommunitationResult<List<Pet>> {
        return processResponse {
            petsAPI.findByStatus(status)
        }
    }

    override suspend fun findById(id: Long): CommunitationResult<Pet> {
        return processResponse {
            petsAPI.findById(id)
        }
    }

    override suspend fun postPet(pet: Pet): CommunitationResult<Pet> {
        return processResponse {
            petsAPI.postPet(pet)
        }
    }

    override suspend fun deletePet(id: Long): CommunitationResult<Status> {
        return processResponse {
            petsAPI.deletePet(id)
        }
    }
}