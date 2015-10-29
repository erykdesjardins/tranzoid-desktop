package tzdatabase;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class TzDatabaseLite {
	SQLiteDatabase dbObj;
	CursorFactory cursorFacto;
	Cursor currentCursor;
	File dbFile;
	
	public TzDatabaseLite() {

	}
	
	public void open(File path, Hashtable <String, TzDatabaseTable> defaultTables) {
		dbFile = path;
		
		if (path.exists()) {
			dbObj = SQLiteDatabase.openDatabase(path.getAbsolutePath(), cursorFacto, SQLiteDatabase.OPEN_READWRITE);
		} else {
			dbObj = SQLiteDatabase.openOrCreateDatabase(path, cursorFacto);
			
			Iterator<Map.Entry<String, TzDatabaseTable>> it = defaultTables.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry<String, TzDatabaseTable> entry = it.next();
				
				entry.getValue().createIntoDB(this);
			}
		}
	}
	
	public void execute(String sql) {
		Log.i("ryk", "Executing SQL query : " + sql);
		dbObj.execSQL(sql);
	}
	
	public Cursor query(String sql) {
		currentCursor = dbObj.rawQuery(sql, null);
		
		return currentCursor;
	}
}
