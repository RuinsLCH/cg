package com.wordpress.laaptu;

import static android.provider.BaseColumns._ID;
import static com.wordpress.laaptu.DbConstants.Time;
import static com.wordpress.laaptu.DbConstants.Word;
import static com.wordpress.laaptu.DbConstants.DWord;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	private final static String DATABASE_NAME = "CutGold.db";
	private final static int DATABASE_VERSION = 1;
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		final String INIT_TABLE = "CREATE TABLE favorite (" +
								  _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
								  Time + " CHAR, " +
								  Word + " CHAR);"; 
		db.execSQL(INIT_TABLE);
		
		final String INIT_TABLE2 = "CREATE TABLE dictionary (" +
				  _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				  DWord + " CHAR UNIQUE ) ;"; 
		db.execSQL(INIT_TABLE2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		final String DROP_TABLE = "DROP TABLE IF EXISTS favorite";
		db.execSQL(DROP_TABLE);
		final String DROP_TABLE2 = "DROP TABLE IF EXISTS dictionary";
		db.execSQL(DROP_TABLE2);
		onCreate(db);
	}

}
