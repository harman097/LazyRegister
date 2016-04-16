package com.brent.harman.lazyregister;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;

import com.brent.harman.lazyregister.DrawerData.CashDrawer;

public class CashDrawerDBHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "cashdrawers.db"; // .db ext recognized by android
    public static final String TABLE_DRAWERS = "drawers";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LASTACCESS = "last_access";

    public CashDrawerDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_DRAWERS + "(" +
                COLUMN_ID + "INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LASTACCESS + " TEXT " +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRAWERS);
        onCreate(db);
    }

    // Add a new row to the database
    public void addDrawer(CashDrawer drawer){
        ContentValues values = new ContentValues();
        values.put(COLUMN_LASTACCESS, drawer.getTotalAsString(false));
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_DRAWERS, null, values);
        db.close();
    }

    public void deleteDrawer(int _id){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_DRAWERS + " WHERE " + COLUMN_ID +
                "=\"" + String.valueOf(_id) + "\";"
        );
        db.close();
    }

    public String databaseToString(){
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_DRAWERS + " WHERE 1";

        // Cursor points to location in results
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            if(cursor.getString(cursor.getColumnIndex(COLUMN_ID)) != null){
                dbString += cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                dbString += "\n";
            }
            cursor.moveToNext();
        }
        db.close();
        return "LEWLZ";
    }
}
