package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.CommunitationResult
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture.IBaseRemoteRepository
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Pet
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Status

interface IPetsRemoteRepository : IBaseRemoteRepository {
    suspend fun findByStatus(status: String): CommunitationResult<List<Pet>>
    suspend fun findById(id: Long): CommunitationResult<Pet>
    suspend fun postPet(pet: Pet): CommunitationResult<Pet>
    suspend fun deletePet(id: Long): CommunitationResult<Status>
}