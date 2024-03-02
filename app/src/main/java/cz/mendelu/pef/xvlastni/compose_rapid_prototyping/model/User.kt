package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototype
import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeType

@RapidPrototype(isList = true, type = RapidPrototypeType.DATABASE)
@Entity(tableName = "users")
data class User(
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "age")
    val age: Int
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null
}
