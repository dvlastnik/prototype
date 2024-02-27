package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.User
import kotlinx.coroutines.flow.Flow

interface IAppRepository {
    suspend fun getUsers(): Flow<List<User>>

    suspend fun insertUser(user: User): Long

    suspend fun deleteUser(user: User)
}