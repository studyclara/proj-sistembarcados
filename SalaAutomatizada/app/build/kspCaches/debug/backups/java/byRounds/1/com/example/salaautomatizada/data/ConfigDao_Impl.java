package com.example.salaautomatizada.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ConfigDao_Impl implements ConfigDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RoomConfig> __insertionAdapterOfRoomConfig;

  private final EntityDeletionOrUpdateAdapter<RoomConfig> __deletionAdapterOfRoomConfig;

  public ConfigDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRoomConfig = new EntityInsertionAdapter<RoomConfig>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `room_configs` (`id`,`userEmail`,`name`,`roomType`,`lightsOn`,`brightness`,`lightColor`,`curtainsOpen`,`acOn`,`temperature`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RoomConfig entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUserEmail());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getRoomType());
        final int _tmp = entity.getLightsOn() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getBrightness());
        statement.bindLong(7, entity.getLightColor());
        final int _tmp_1 = entity.getCurtainsOpen() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.getAcOn() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindLong(10, entity.getTemperature());
      }
    };
    this.__deletionAdapterOfRoomConfig = new EntityDeletionOrUpdateAdapter<RoomConfig>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `room_configs` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RoomConfig entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object upsert(final RoomConfig config, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRoomConfig.insertAndReturnId(config);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final RoomConfig config, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRoomConfig.handle(config);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RoomConfig>> observe(final String email) {
    final String _sql = "SELECT * FROM room_configs WHERE userEmail = ? ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, email);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"room_configs"}, new Callable<List<RoomConfig>>() {
      @Override
      @NonNull
      public List<RoomConfig> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "userEmail");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRoomType = CursorUtil.getColumnIndexOrThrow(_cursor, "roomType");
          final int _cursorIndexOfLightsOn = CursorUtil.getColumnIndexOrThrow(_cursor, "lightsOn");
          final int _cursorIndexOfBrightness = CursorUtil.getColumnIndexOrThrow(_cursor, "brightness");
          final int _cursorIndexOfLightColor = CursorUtil.getColumnIndexOrThrow(_cursor, "lightColor");
          final int _cursorIndexOfCurtainsOpen = CursorUtil.getColumnIndexOrThrow(_cursor, "curtainsOpen");
          final int _cursorIndexOfAcOn = CursorUtil.getColumnIndexOrThrow(_cursor, "acOn");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final List<RoomConfig> _result = new ArrayList<RoomConfig>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RoomConfig _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUserEmail;
            _tmpUserEmail = _cursor.getString(_cursorIndexOfUserEmail);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRoomType;
            _tmpRoomType = _cursor.getString(_cursorIndexOfRoomType);
            final boolean _tmpLightsOn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLightsOn);
            _tmpLightsOn = _tmp != 0;
            final int _tmpBrightness;
            _tmpBrightness = _cursor.getInt(_cursorIndexOfBrightness);
            final int _tmpLightColor;
            _tmpLightColor = _cursor.getInt(_cursorIndexOfLightColor);
            final boolean _tmpCurtainsOpen;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfCurtainsOpen);
            _tmpCurtainsOpen = _tmp_1 != 0;
            final boolean _tmpAcOn;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAcOn);
            _tmpAcOn = _tmp_2 != 0;
            final int _tmpTemperature;
            _tmpTemperature = _cursor.getInt(_cursorIndexOfTemperature);
            _item = new RoomConfig(_tmpId,_tmpUserEmail,_tmpName,_tmpRoomType,_tmpLightsOn,_tmpBrightness,_tmpLightColor,_tmpCurtainsOpen,_tmpAcOn,_tmpTemperature);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object get(final long id, final Continuation<? super RoomConfig> $completion) {
    final String _sql = "SELECT * FROM room_configs WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RoomConfig>() {
      @Override
      @Nullable
      public RoomConfig call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "userEmail");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRoomType = CursorUtil.getColumnIndexOrThrow(_cursor, "roomType");
          final int _cursorIndexOfLightsOn = CursorUtil.getColumnIndexOrThrow(_cursor, "lightsOn");
          final int _cursorIndexOfBrightness = CursorUtil.getColumnIndexOrThrow(_cursor, "brightness");
          final int _cursorIndexOfLightColor = CursorUtil.getColumnIndexOrThrow(_cursor, "lightColor");
          final int _cursorIndexOfCurtainsOpen = CursorUtil.getColumnIndexOrThrow(_cursor, "curtainsOpen");
          final int _cursorIndexOfAcOn = CursorUtil.getColumnIndexOrThrow(_cursor, "acOn");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final RoomConfig _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUserEmail;
            _tmpUserEmail = _cursor.getString(_cursorIndexOfUserEmail);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpRoomType;
            _tmpRoomType = _cursor.getString(_cursorIndexOfRoomType);
            final boolean _tmpLightsOn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLightsOn);
            _tmpLightsOn = _tmp != 0;
            final int _tmpBrightness;
            _tmpBrightness = _cursor.getInt(_cursorIndexOfBrightness);
            final int _tmpLightColor;
            _tmpLightColor = _cursor.getInt(_cursorIndexOfLightColor);
            final boolean _tmpCurtainsOpen;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfCurtainsOpen);
            _tmpCurtainsOpen = _tmp_1 != 0;
            final boolean _tmpAcOn;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfAcOn);
            _tmpAcOn = _tmp_2 != 0;
            final int _tmpTemperature;
            _tmpTemperature = _cursor.getInt(_cursorIndexOfTemperature);
            _result = new RoomConfig(_tmpId,_tmpUserEmail,_tmpName,_tmpRoomType,_tmpLightsOn,_tmpBrightness,_tmpLightColor,_tmpCurtainsOpen,_tmpAcOn,_tmpTemperature);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
