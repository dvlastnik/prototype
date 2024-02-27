package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private var dao: AppDao
) : IAppRepository {
    override suspend fun getUsers(): Flow<List<User>> {
        return dao.getUsers()
    }

    override suspend fun insertUser(user: User): Long {
        return dao.insertUser(user)
    }

    override suspend fun deleteUser(user: User) {
        return dao.deleteUser(user)
    }

}