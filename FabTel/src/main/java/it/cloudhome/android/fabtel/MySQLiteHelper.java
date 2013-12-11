package it.cloudhome.android.fabtel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {


	private static final String DATABASE_NAME = "comunicacon.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE1 = "create table utenti "
			+ "(id integer primary key autoincrement, "
			+ " interno text not null, "
			+ " nominativo text not null, "
			+ " chat text not null "
			+ "); ";
			
	private static final String DATABASE_CREATE2 = "create table chat_messages "
			+ "(id integer primary key autoincrement,"
			+ "chat_from text not null,"
			+ "chat_to text not null,"
			+ "message text not null,"
			+ "timestamp text not null)"
			+ ";";
			

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE1);
		database.execSQL(DATABASE_CREATE2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS utenti DROP TABLE IF EXISTS chat_messages;");
		onCreate(db);
	}
}
