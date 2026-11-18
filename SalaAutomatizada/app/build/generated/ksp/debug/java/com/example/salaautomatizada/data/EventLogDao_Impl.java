package com.example.salaautomatizada.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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
public final class EventLogDao_Impl implements EventLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RoomEventLog> __insertionAdapterOfRoomEventLog;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public EventLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRoomEventLog = new EntityInsertionAdapter<RoomEventLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `room_event_logs` (`id`,`timestamp`,`eventType`,`userName`,`description`,`temperature`,`acState`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RoomEventLog entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindString(3, entity.getEventType());
        statement.bindString(4, entity.getUserName());
        statement.bindString(5, entity.getDescription());
        statement.bindDouble(6, entity.getTemperature());
        final int _tmp = entity.getAcState() ? 1 : 0;
        statement.bindLong(7, _tmp);
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM room_event_logs";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final RoomEventLog log, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRoomEventLog.insert(log);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RoomEventLog>> observeAll() {
    final String _sql = "SELECT * FROM room_event_logs ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"room_event_logs"}, new Callable<List<RoomEventLog>>() {
      @Override
      @NonNull
      public List<RoomEventLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "eventType");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "userName");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfAcState = CursorUtil.getColumnIndexOrThrow(_cursor, "acState");
          final List<RoomEventLog> _result = new ArrayList<RoomEventLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RoomEventLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpEventType;
            _tmpEventType = _cursor.getString(_cursorIndexOfEventType);
            final String _tmpUserName;
            _tmpUserName = _cursor.getString(_cursorIndexOfUserName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final float _tmpTemperature;
            _tmpTemperature = _cursor.getFloat(_cursorIndexOfTemperature);
            final boolean _tmpAcState;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAcState);
            _tmpAcState = _tmp != 0;
            _item = new RoomEventLog(_tmpId,_tmpTimestamp,_tmpEventType,_tmpUserName,_tmpDescription,_tmpTemperature,_tmpAcState);
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
  public Object getRecent(final int limit,
      final Continuation<? super List<RoomEventLog>> $completion) {
    final String _sql = "SELECT * FROM room_event_logs ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RoomEventLog>>() {
      @Override
      @NonNull
      public List<RoomEventLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfEventType = CursorUtil.getColumnIndexOrThrow(_cursor, "eventType");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "userName");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfTemperature = CursorUtil.getColumnIndexOrThrow(_cursor, "temperature");
          final int _cursorIndexOfAcState = CursorUtil.getColumnIndexOrThrow(_cursor, "acState");
          final List<RoomEventLog> _result = new ArrayList<RoomEventLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RoomEventLog _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpEventType;
            _tmpEventType = _cursor.getString(_cursorIndexOfEventType);
            final String _tmpUserName;
            _tmpUserName = _cursor.getString(_cursorIndexOfUserName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final float _tmpTemperature;
            _tmpTemperature = _cursor.getFloat(_cursorIndexOfTemperature);
            final boolean _tmpAcState;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAcState);
            _tmpAcState = _tmp != 0;
            _item = new RoomEventLog(_tmpId,_tmpTimestamp,_tmpEventType,_tmpUserName,_tmpDescription,_tmpTemperature,_tmpAcState);
            _result.add(_item);
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
