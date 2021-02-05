package com.arteriatech.geotrack.rspl.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;


import com.arteriatech.geotrack.rspl.Constants;

import java.util.Enumeration;
import java.util.Hashtable;

/** Helper to the database, manages versions and creation */
public class EventDataSqlHelper {
	private static EventDataSqlHelper acc_List_ = null;
	SQLiteDatabase db;
	EventDataSqlHelper database = null;
	private String delSql;

	public EventDataSqlHelper(Context context) {
		db = Constants.EventUserHandler;

	}


	public void crateTableConfig(String tableName, Hashtable hashtable) {

		String[] clms = hashtoString(hashtable);

		String clumsname = "";
		for (int i = 0; i < clms.length; i++) {
			if (clms[i] != null) {
				clumsname = clumsname + clms[i] + " text ";
			}
			if (i < clms.length - 1) {
				clumsname = clumsname + " ,";
			}

		}

		Constants.deleteTable(db,tableName);

		Constants.createTable(db,tableName,clumsname);

	}

	public void inserthistortTable(String tblName, String inspectionLot,
								   String clmname, String value) {

		Constants.insertHistoryDB(db,tblName,clmname,value);
	}

	public void updateStatus(String tblName, String inspectionLot,
							 String clmname, String value) {

		Constants.updateStatus(db,tblName,clmname,value,inspectionLot);


	}

	private String[] hashtoString(Hashtable hashtable) {

		String[] hashtostring = new String[hashtable.size()];
		if (null != hashtable) {
			int size = hashtable.size();
			if (size > 0) {
				Enumeration enumeration = hashtable.keys();
				if (enumeration.hasMoreElements()) {
					int i = 0;

					while (enumeration.hasMoreElements()) {

						hashtostring[i] = (String) enumeration.nextElement();
						i++;
					}
				}
			}
		}

		return hashtostring;

	}

	public void insert(String tableName, Hashtable<String, String> hashtable) {
		try {
			ContentValues values = hashtocontentView(hashtable);
			db.insert(tableName, "", values);
		} catch (Exception e) {
			System.out.println("Error Post method" + e);
		}

	}

	public Cursor getEvents(String Tbl) {


		Cursor cursor = db.query(true, Tbl, null, null, null, null, null, null,
				null);

		return cursor;
	}


	public String getRetailerName(String selectQuery) {

		String counterName = "";

		Cursor cursor  = db.rawQuery(selectQuery, null);
//		Cursor cursor = db.query(true, Tbl, null, null, null, null, null, null,
//				null);

		if (cursor != null) {
			while (cursor.moveToNext()) {
				counterName = cursor
						.getString(cursor
								.getColumnIndex("CounterName"));

			}
			cursor.deactivate();
			cursor.close();
		}

		return counterName;
	}

	private ContentValues hashtocontentView(Hashtable<String, String> hashtable) {
		ContentValues values = new ContentValues();
		String[] hashtostring = new String[hashtable.size()];
		if (null != hashtable) {
			int size = hashtable.size();
			if (size > 0) {
				Enumeration enumeration = hashtable.keys();
				if (enumeration.hasMoreElements()) {
					int i = 0;

					while (enumeration.hasMoreElements()) {
						String key = (String) enumeration.nextElement();
						values.put(key, hashtable.get(key).toString());
						i++;
					}
				}
			}
		}

		return values;

	}

	public String getVisitActDone(String tableName,String CustNoColumnName,String custNo,String mStrDateColumn,String dateVal){
		String configValDesc = "";
		try {
			String configQry="";
			configQry = "select * from "+tableName+" Where "+CustNoColumnName+"='"+custNo+"' and "+mStrDateColumn+"='"+dateVal+"' ";
			Cursor configCursor = db.rawQuery(configQry, new String[] {});

			if (configCursor != null
					&& configCursor.getCount() > 0) {
				while (configCursor.moveToNext()) {
					configValDesc = configCursor
							.getString(configCursor
									.getColumnIndex(CustNoColumnName)) != null ? configCursor
							.getString(configCursor
									.getColumnIndex(CustNoColumnName))
							: "";
					if(!configValDesc.equalsIgnoreCase("")){
						return configValDesc;
					}
				}
			}
			configCursor.close();
			configCursor.deactivate();
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return configValDesc;
	}
	/*insert history table*/
	public void insertLogHistoryTable(String tblName, String collectionName, String time) {
		if (CheckIsDataAlreadyInDBorNot(db, tblName, Constants.Collections, collectionName)) {
			if (!TextUtils.isEmpty(time))
				updateStatus(tblName, collectionName, Constants.TimeStamp, time);
		} else {
			Constants.insertHistoryDB(db, tblName, Constants.Collections, collectionName);
			if (!TextUtils.isEmpty(time))
				updateStatus(tblName, collectionName, Constants.TimeStamp, time);
		}
	}
	public static boolean CheckIsDataAlreadyInDBorNot(SQLiteDatabase sqldb, String TableName,
													  String dbfield, String fieldValue) {
		String Query = "Select * from " + TableName + " where " + dbfield + " = '" + fieldValue + "'";
		Cursor cursor = sqldb.rawQuery(Query, null);
		if (cursor.getCount() <= 0) {
			cursor.close();
			return false;
		}
		cursor.close();
		return true;
	}
	public boolean syncHistoryTableExist(String tableName) {
		try {
			Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
			if (cursor != null) {
				if (cursor.getCount() > 0) {
					cursor.close();
					return true;
				}
				cursor.close();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}
}
