package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.communication

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Pet
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Status
import cz.mendelu.pef.xvlastni.prototype.classes.CommunicationResult
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeFunction
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeRepository
import cz.mendelu.pef.xvlastni.prototype.classes.IBaseRemoteRepository
import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeFunctionType

@RapidPrototypeRepository
interface IPetsRemoteRepository : IBaseRemoteRepository {
    @RapidPrototypeFunction(RapidPrototypeFunctionType.SELECT)
    suspend fun findByStatus(): CommunicationResult<List<Pet>>
    suspend fun findById(id: Long): CommunicationResult<Pet>

    @RapidPrototypeFunction(RapidPrototypeFunctionType.INSERT)
    suspend fun postPet(pet: Pet): CommunicationResult<Pet>

    @RapidPrototypeFunction(RapidPrototypeFunctionType.DELETE)
    suspend fun deletePet(id: Long): CommunicationResult<Status>
}