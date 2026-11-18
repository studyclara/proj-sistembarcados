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

/**
 * Registro histórico de eventos da sala (acessos, acionamentos de ar condicionado, alertas de segurança).
 * Utilizado para gerar telas e relatórios no aplicativo mobile.
 */
@Entity(tableName = "room_event_logs")
data class RoomEventLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // "ENTRADA", "SAIDA", "ALERTA", "AR_CONDICIONADO", "SISTEMA"
    val userName: String = "Sistema",
    val description: String,
    val temperature: Float = 22.0f,
    val acState: Boolean = false
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

@Dao
interface EventLogDao {
    @Query("SELECT * FROM room_event_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<RoomEventLog>>

    @Query("SELECT * FROM room_event_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<RoomEventLog>

    @Insert
    suspend fun insert(log: RoomEventLog)

    @Query("DELETE FROM room_event_logs")
    suspend fun clearAll()
}

@Database(entities = [User::class, RoomConfig::class, RoomEventLog::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun configDao(): ConfigDao
    abstract fun eventLogDao(): EventLogDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sala_automatizada.db"
                )
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
            }
    }
}
