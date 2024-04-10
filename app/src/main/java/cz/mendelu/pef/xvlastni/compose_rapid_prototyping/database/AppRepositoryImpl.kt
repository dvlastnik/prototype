package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database

import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Activity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(private val appDao: AppDao) : IAppRepository {
    override fun getSavedActivites(): Flow<List<Activity>> {
        return appDao.getSavedActivities()
    }

    override suspend fun insertActivity(activity: Activity): Long {
        return appDao.insertActivity(activity)
    }

    override suspend fun deleteActivity(activity: Activity) {
        appDao.deleteActivity(activity)
    }
}