package com.corne.rainfall.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.corne.rainfall.db.dao.LocationDAO
import com.corne.rainfall.db.dao.NotificationDAO
import com.corne.rainfall.db.dao.RainfallDAO
import com.corne.rainfall.db.entity.LocationEntity
import com.corne.rainfall.db.entity.NotificationEntity
import com.corne.rainfall.db.entity.RainfallEntity
import com.corne.rainfall.utils.Constants

@Database(
    entities = [RainfallEntity::class, LocationEntity::class, NotificationEntity::class],
    version = 4,
)
abstract class RainfallDatabase : RoomDatabase() {
    abstract fun rainEntityDao(): RainfallDAO
    abstract fun locationDao(): LocationDAO
    abstract fun notificationDAO(): NotificationDAO

    // Singleton prevents multiple instances of database opening at the same time.
    // In order to keep ensure this thread synchronization is needed.
    // This is a slightly modified version of the code from this example:
    // https://github.com/JeremyLeyvraz/WorkersKotlinExample/blob/43fb582a3740e4bfa474cf72881ecde07cc51dfb/libraryExample/src/main/java/com/lj/libraryExample/AppDatabase.kt#L13C23-L29
    companion object {
        private var INSTANCE: RainfallDatabase? = null
        fun getInstance(context: Context): RainfallDatabase {
            return INSTANCE ?: synchronized(this) {
                val rainfallDatabase = Room.databaseBuilder(
                    context.applicationContext,
                    RainfallDatabase::class.java,
                    Constants.DATABASE_NAME

                ).fallbackToDestructiveMigration().build()
                INSTANCE = rainfallDatabase


                rainfallDatabase
            }
        }
    }
}