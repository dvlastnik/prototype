package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.mendelu.pef.xvlastni.prototype.annotations.RapidPrototype
import cz.mendelu.pef.xvlastni.prototype.type.RapidPrototypeType

@RapidPrototype(isList = true, type = RapidPrototypeType.DATABASE)
@Entity(tableName = "activities")
data class Activity(
    @ColumnInfo(name = "key")
    val key: String,
    @ColumnInfo(name = "accessibility")
    val accessibility: Double,
    @ColumnInfo(name = "activity")
    val activity: String,
    @ColumnInfo(name = "link")
    val link: String,
    @ColumnInfo(name = "participants")
    val participants: Int,
    @ColumnInfo(name = "price")
    val price: Double,
    @ColumnInfo(name = "type")
    val type: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null
}