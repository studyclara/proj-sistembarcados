package com.example.salaautomatizada.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile UserDao _userDao;

  private volatile ConfigDao _configDao;

  private volatile EventLogDao _eventLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`email` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, PRIMARY KEY(`email`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `room_configs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userEmail` TEXT NOT NULL, `name` TEXT NOT NULL, `roomType` TEXT NOT NULL, `lightsOn` INTEGER NOT NULL, `brightness` INTEGER NOT NULL, `lightColor` INTEGER NOT NULL, `curtainsOpen` INTEGER NOT NULL, `acOn` INTEGER NOT NULL, `temperature` INTEGER NOT NULL, `roomPassword` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `room_event_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `eventType` TEXT NOT NULL, `userName` TEXT NOT NULL, `description` TEXT NOT NULL, `temperature` REAL NOT NULL, `acState` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '192883496b05a26eee4118894dc86d7c')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `users`");
        db.execSQL("DROP TABLE IF EXISTS `room_configs`");
        db.execSQL("DROP TABLE IF EXISTS `room_event_logs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(2);
        _columnsUsers.put("email", new TableInfo.Column("email", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("passwordHash", new TableInfo.Column("passwordHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(db, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenHelper.ValidationResult(false, "users(com.example.salaautomatizada.data.User).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final HashMap<String, TableInfo.Column> _columnsRoomConfigs = new HashMap<String, TableInfo.Column>(11);
        _columnsRoomConfigs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomConfigs.put("userEmail", new TableInfo.Column("userEmail", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomConfigs.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomConfigs.put("roomType", new TableInfo.Column("roomType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomConfigs.put("lightsOn", new TableInfo.Column("lightsOn", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomConfigs.put("brightness", new TableInfo.Column("brightness", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomConfigs.put("lightColor", new TableInfo.Column("lightColor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomConfigs.put("curtainsOpen", new TableInfo.Column("curtainsOpen", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomConfigs.put("acOn", new TableInfo.Column("acOn", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomConfigs.put("temperature", new TableInfo.Column("temperature", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomConfigs.put("roomPassword", new TableInfo.Column("roomPassword", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRoomConfigs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRoomConfigs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRoomConfigs = new TableInfo("room_configs", _columnsRoomConfigs, _foreignKeysRoomConfigs, _indicesRoomConfigs);
        final TableInfo _existingRoomConfigs = TableInfo.read(db, "room_configs");
        if (!_infoRoomConfigs.equals(_existingRoomConfigs)) {
          return new RoomOpenHelper.ValidationResult(false, "room_configs(com.example.salaautomatizada.data.RoomConfig).\n"
                  + " Expected:\n" + _infoRoomConfigs + "\n"
                  + " Found:\n" + _existingRoomConfigs);
        }
        final HashMap<String, TableInfo.Column> _columnsRoomEventLogs = new HashMap<String, TableInfo.Column>(7);
        _columnsRoomEventLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomEventLogs.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomEventLogs.put("eventType", new TableInfo.Column("eventType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomEventLogs.put("userName", new TableInfo.Column("userName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomEventLogs.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomEventLogs.put("temperature", new TableInfo.Column("temperature", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRoomEventLogs.put("acState", new TableInfo.Column("acState", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRoomEventLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRoomEventLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRoomEventLogs = new TableInfo("room_event_logs", _columnsRoomEventLogs, _foreignKeysRoomEventLogs, _indicesRoomEventLogs);
        final TableInfo _existingRoomEventLogs = TableInfo.read(db, "room_event_logs");
        if (!_infoRoomEventLogs.equals(_existingRoomEventLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "room_event_logs(com.example.salaautomatizada.data.RoomEventLog).\n"
                  + " Expected:\n" + _infoRoomEventLogs + "\n"
                  + " Found:\n" + _existingRoomEventLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "192883496b05a26eee4118894dc86d7c", "52a27304fe6e624a3036ff1125850d9c");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users","room_configs","room_event_logs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `users`");
      _db.execSQL("DELETE FROM `room_configs`");
      _db.execSQL("DELETE FROM `room_event_logs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ConfigDao.class, ConfigDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EventLogDao.class, EventLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public ConfigDao configDao() {
    if (_configDao != null) {
      return _configDao;
    } else {
      synchronized(this) {
        if(_configDao == null) {
          _configDao = new ConfigDao_Impl(this);
        }
        return _configDao;
      }
    }
  }

  @Override
  public EventLogDao eventLogDao() {
    if (_eventLogDao != null) {
      return _eventLogDao;
    } else {
      synchronized(this) {
        if(_eventLogDao == null) {
          _eventLogDao = new EventLogDao_Impl(this);
        }
        return _eventLogDao;
      }
    }
  }
}
