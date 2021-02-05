package com.arteriatech.geotrack.rspl.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.arteriatech.geotrack.rspl.Constants;

/** Helper to the database, manages versions and creation */
public class EventUserDetail extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = Constants.DATABASE_NAME;
	private static final int DATABASE_VERSION = 1;
	Context context;

	public EventUserDetail(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		int upgradeTo = oldVersion + 1;
		String sql = null;
		switch (upgradeTo) {
		case 2:

			break;

		case 3:
			
			break;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Constants.createDB(db);
	}
}
