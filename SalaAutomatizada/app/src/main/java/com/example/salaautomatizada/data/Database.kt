package com.example.salaautomatizada.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val passwordHash: String
)

@Entity(tableName = "room_configs")
data class RoomConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val name: String,
    val roomType: String,
    val lightsOn: Boolean,
    val brightness: Int,        // 0..100
    val lightColor: Int,        // ARGB
    val curtainsOpen: Boolean,
    val acOn: Boolean,
    val temperature: Int        // °C
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun find(email: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)
}

@Dao
interface ConfigDao {
    @Query("SELECT * FROM room_configs WHERE userEmail = :email ORDER BY id DESC")
    fun observe(email: String): Flow<List<RoomConfig>>

    @Query("SELECT * FROM room_configs WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RoomConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: RoomConfig): Long

    @Delete
    suspend fun delete(config: RoomConfig)
}

@Database(entities = [User::class, RoomConfig::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sala_automatizada.db"
                ).build().also { instance = it }
            }
    }
}
