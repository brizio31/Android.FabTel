package it.cloudhome.android.fabtel;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBContatto {
	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { "id","interno","nominativo","chat" };
	
	public DBContatto(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public List<ClsContatto> GetCollection(String filter,String[] filterPar,String order)
	{
		List<ClsContatto> contatti = new ArrayList<ClsContatto>();
		Cursor cursor=database.query("utenti", allColumns, filter, filterPar, null, null, order);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ClsContatto contatto = new ClsContatto(cursor.getString(2), cursor.getString(1), cursor.getString(3));
			contatti.add(contatto);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return contatti;
	}
	
	public ClsContatto GetByFilter(String filter,String[] filterPar,String order)
	{
		Cursor cursor=database.query("utenti", allColumns, filter, filterPar, null, null, order);
		cursor.moveToFirst();
		//while (!cursor.isAfterLast()) {
			ClsContatto contatto = new ClsContatto(cursor.getString(2), cursor.getString(1), cursor.getString(3));
			contatto.setId(cursor.getInt(0));
			//cursor.moveToNext();
		//}
		// Make sure to close the cursor
		cursor.close();
		return contatto;
	}

	public void Insert(ClsContatto contatto)
	{
		ContentValues values = new ContentValues();
		values.put("interno", contatto.getInterno());
		values.put("nominativo", contatto.getNome());
		values.put("chat", contatto.getChatAddress());
		database.insert("utenti", null,values);
	}
	public void Delete(String id)
	{
		database.delete("utenti", "id="+id,null);
	}

	public void executeSQL(String sql) {
		// TODO Auto-generated method stub
		database.execSQL(sql);
	}

}
