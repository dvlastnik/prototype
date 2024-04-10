package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM activities")
    fun getSavedActivities(): Flow<List<Activity>>

    @Insert
    suspend fun insertActivity(activity: Activity): Long

    @Delete
    suspend fun deleteActivity(activity: Activity)
}