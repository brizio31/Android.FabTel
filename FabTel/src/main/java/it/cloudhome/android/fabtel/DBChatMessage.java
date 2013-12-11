package it.cloudhome.android.fabtel;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBChatMessage {
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { "id","chat_from","chat_to","message","timestamp" };
	
	public DBChatMessage(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public List<ClsChatMessage> GetCollection(String filter,String[] filterPar,String order)
	{
		return GetCollection(filter, filterPar, order,null);
	}
	public List<ClsChatMessage> GetCollection(String filter,String[] filterPar,String order,String limit)
	{
		List<ClsChatMessage> msgs = new ArrayList<ClsChatMessage>();
		Cursor cursor=database.query("chat_messages", allColumns, filter, filterPar, null, null, order,limit);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ClsChatMessage msg = new ClsChatMessage(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
			msgs.add(msg);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return msgs;
	}
	
	public ClsChatMessage GetByFilter(String filter,String[] filterPar,String order)
	{
		Cursor cursor=database.query("chat_messages", allColumns, filter, filterPar, null, null, order);
		cursor.moveToFirst();
		ClsChatMessage msg = new ClsChatMessage(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
		msg.setId(cursor.getInt(0));
		// Make sure to close the cursor
		cursor.close();
		return msg;
	}

	public void Insert(ClsChatMessage msg)
	{
		ContentValues values = new ContentValues();
		values.put("chat_from", msg.getFrom());
		values.put("chat_to", msg.getTo());
		values.put("message", msg.getMessage());
		values.put("timestamp", msg.getTimeStamp());
		database.insert("chat_messages", null,values);
	}
	public void Delete(String id)
	{
		database.delete("chat_messages", "id="+id,null);
	}

	public void upgradeDB()
	{
		this.open();
		database.execSQL("DROP TABLE IF EXISTS chat_messages");
		String Sql = "create table chat_messages "
		+ "(id integer primary key autoincrement,"
		+ "chat_from text not null,"
		+ "chat_to text not null,"
		+ "message text not null,"
		+ "timestamp text not null)"
		+ ";";

		database.execSQL(Sql);
		this.close();
	}

	public void executeSQL(String sql) {
		// TODO Auto-generated method stub
		database.execSQL(sql);
	}
}
