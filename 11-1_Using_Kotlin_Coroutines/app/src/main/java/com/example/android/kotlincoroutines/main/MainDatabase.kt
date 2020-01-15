package com.example.android.kotlincoroutines.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity
data class Title constructor(val title: String, @PrimaryKey val id: Int = 0)

@Dao
interface TitleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitle(title: Title)

    @get:Query("select * from Title where id = 0")
    val titleLiveData: LiveData<Title?>
}

@Database(entities = [Title::class], version = 1, exportSchema = false)
abstract class TItleDatabase: RoomDatabase() {
    abstract val titleDao: TitleDao
}

private lateinit var INSTANCE: TItleDatabase


/**
 * Instantiate a database from a context
 */
fun getDatabase(context: Context): TItleDatabase {
    synchronized(TItleDatabase::class){
        if(!::INSTANCE.isInitialized) {
            INSTANCE = Room
                .databaseBuilder(
                    context.applicationContext,
                    TItleDatabase::class.java,
                    "titles_db"
                )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}