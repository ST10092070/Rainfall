package com.corne.rainfall.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val userId: Int,
    @ColumnInfo(name = "name") val name: String,
)