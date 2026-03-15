package com.example.hearingaidcontroller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SpeakerController.db";
    private static final int DATABASE_VERSION = 2; // Incremented to force schema update

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_DEVICE_SETTINGS = "device_settings";

    // User columns
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_HEARING_LOSS = "hearing_loss";
    private static final String COLUMN_PASSWORD = "password";

    // Device settings columns
    private static final String COLUMN_SETTINGS_ID = "id";
    private static final String COLUMN_USER_ID_FK = "user_id";        // Foreign key to users.id
    private static final String COLUMN_DEVICE_ADDRESS = "device_address";
    private static final String COLUMN_DEVICE_NAME = "device_name";
    private static final String COLUMN_VOLUME = "volume";
    private static final String COLUMN_BASS = "bass";
    private static final String COLUMN_TREBLE = "treble";

    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FULL_NAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT UNIQUE,"
            + COLUMN_HEARING_LOSS + " TEXT,"
            + COLUMN_PASSWORD + " TEXT" + ")";

    private static final String CREATE_DEVICE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_DEVICE_SETTINGS + "("
            + COLUMN_SETTINGS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER,"
            + COLUMN_DEVICE_ADDRESS + " TEXT,"
            + COLUMN_DEVICE_NAME + " TEXT,"
            + COLUMN_VOLUME + " INTEGER,"
            + COLUMN_BASS + " INTEGER,"
            + COLUMN_TREBLE + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
            + "UNIQUE(" + COLUMN_USER_ID_FK + "," + COLUMN_DEVICE_ADDRESS + ")" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_DEVICE_SETTINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // User operations
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, user.getFullName());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_HEARING_LOSS, user.getHearingLossType());
        values.put(COLUMN_PASSWORD, user.getPassword());
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public User getUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID, COLUMN_FULL_NAME, COLUMN_EMAIL, COLUMN_HEARING_LOSS, COLUMN_PASSWORD},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setId(cursor.getInt(0));
            user.setFullName(cursor.getString(1));
            user.setEmail(cursor.getString(2));
            user.setHearingLossType(cursor.getString(3));
            user.setPassword(cursor.getString(4));
            cursor.close();
            db.close();
            return user;
        }
        return null;
    }

    public User getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID, COLUMN_FULL_NAME, COLUMN_EMAIL, COLUMN_HEARING_LOSS, COLUMN_PASSWORD},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setId(cursor.getInt(0));
            user.setFullName(cursor.getString(1));
            user.setEmail(cursor.getString(2));
            user.setHearingLossType(cursor.getString(3));
            user.setPassword(cursor.getString(4));
            cursor.close();
            db.close();
            return user;
        }
        return null;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, user.getFullName());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_HEARING_LOSS, user.getHearingLossType());
        values.put(COLUMN_PASSWORD, user.getPassword());
        int rows = db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?", new String[]{String.valueOf(user.getId())});
        db.close();
        return rows > 0;
    }

    // Device Settings operations
    public long addOrUpdateDeviceSettings(DeviceSettings settings) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID_FK, settings.getUserId());
        values.put(COLUMN_DEVICE_ADDRESS, settings.getDeviceAddress());
        values.put(COLUMN_DEVICE_NAME, settings.getDeviceName());
        values.put(COLUMN_VOLUME, settings.getVolumeLevel());
        values.put(COLUMN_BASS, settings.getBassLevel());
        values.put(COLUMN_TREBLE, settings.getTrebleLevel());

        Cursor cursor = db.query(TABLE_DEVICE_SETTINGS,
                new String[]{COLUMN_SETTINGS_ID},
                COLUMN_USER_ID_FK + "=? AND " + COLUMN_DEVICE_ADDRESS + "=?",
                new String[]{String.valueOf(settings.getUserId()), settings.getDeviceAddress()},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            db.update(TABLE_DEVICE_SETTINGS, values, COLUMN_SETTINGS_ID + "=?", new String[]{String.valueOf(id)});
            db.close();
            return id;
        } else {
            if (cursor != null) cursor.close();
            long id = db.insert(TABLE_DEVICE_SETTINGS, null, values);
            db.close();
            return id;
        }
    }

    public DeviceSettings getDeviceSettings(int userId, String deviceAddress) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DEVICE_SETTINGS,
                new String[]{COLUMN_SETTINGS_ID, COLUMN_DEVICE_NAME, COLUMN_VOLUME, COLUMN_BASS, COLUMN_TREBLE},
                COLUMN_USER_ID_FK + "=? AND " + COLUMN_DEVICE_ADDRESS + "=?",
                new String[]{String.valueOf(userId), deviceAddress},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            DeviceSettings settings = new DeviceSettings();
            settings.setId(cursor.getInt(0));
            settings.setUserId(userId);
            settings.setDeviceAddress(deviceAddress);
            settings.setDeviceName(cursor.getString(1));
            settings.setVolumeLevel(cursor.getInt(2));
            settings.setBassLevel(cursor.getInt(3));
            settings.setTrebleLevel(cursor.getInt(4));
            cursor.close();
            db.close();
            return settings;
        }
        return null;
    }

    public List<DeviceSettings> getAllDeviceSettings(int userId) {
        List<DeviceSettings> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DEVICE_SETTINGS,
                new String[]{COLUMN_DEVICE_ADDRESS, COLUMN_DEVICE_NAME, COLUMN_VOLUME, COLUMN_BASS, COLUMN_TREBLE},
                COLUMN_USER_ID_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DeviceSettings settings = new DeviceSettings();
                settings.setUserId(userId);
                settings.setDeviceAddress(cursor.getString(0));
                settings.setDeviceName(cursor.getString(1));
                settings.setVolumeLevel(cursor.getInt(2));
                settings.setBassLevel(cursor.getInt(3));
                settings.setTrebleLevel(cursor.getInt(4));
                list.add(settings);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }
}