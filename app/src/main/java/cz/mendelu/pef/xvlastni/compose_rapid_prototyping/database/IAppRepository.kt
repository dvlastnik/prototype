package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.User
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeFunction
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototypeRepository
import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeFunctionType
import kotlinx.coroutines.flow.Flow

@RapidPrototypeRepository
interface IAppRepository {
    @RapidPrototypeFunction(RapidPrototypeFunctionType.SELECT)
    suspend fun getUsers(): Flow<List<User>>

    @RapidPrototypeFunction(RapidPrototypeFunctionType.INSERT)
    suspend fun insertUser(user: User): Long

    @RapidPrototypeFunction(RapidPrototypeFunctionType.DELETE)
    suspend fun deleteUser(user: User)
}