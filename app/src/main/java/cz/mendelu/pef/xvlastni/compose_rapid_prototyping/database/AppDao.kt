package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM users")
    fun getUsers(): Flow<List<User>>

    @Insert
    suspend fun insertUser(user: User): Long

    @Delete
    suspend fun deleteUser(user: User)
}