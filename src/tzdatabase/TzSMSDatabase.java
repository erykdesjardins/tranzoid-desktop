package tzdatabase;

import java.util.Hashtable;

import android.content.Context;
import android.database.Cursor;

import com.ryk.customproviders.CpMessenger;
import com.ryk.tzandroidutil.TzFileBrowser;

public class TzSMSDatabase {
	TzDatabaseLite db;
	String filename = "tzmsgdb.sqlite";
	String dbName = "tzTexts";
	
	public TzSMSDatabase() {
		db = new TzDatabaseLite();
	
		TzFileBrowser browser = new TzFileBrowser();
		db.open(browser.getNewFileAtDefaultDirectory(filename), tables);
	}
	
	public void syncDatabase(Context ctx) {
        Cursor cursor = ctx.getContentResolver().query(
            	CpMessenger.CONTENT_URI, 
            	new String[] {}, "", new String[] {}, 
            	CpMessenger.DATE + " DESC" 
            );		
		
        db.execute("DELETE FROM SMS");
        if (cursor.moveToFirst()){
        	do {
        		// Insert SMS Into DB
        	} while (cursor.moveToNext());
        }
        
        cursor.close();        
	}
	
	public Cursor getLatestMessages() {
		return db.query(String.format("SELECT MAX(x.%s), x.%s, x.%s, x.%s FROM %s ", 
			""));
		
		/*
		  SELECT MIN(x.id),  -- change to MAX if you want the highest
	         x.customer, 
	         x.total
	    FROM PURCHASES x
	    JOIN (SELECT p.customer,
	                 MAX(total) AS max_total
	            FROM PURCHASES p
	        GROUP BY p.customer) y ON y.customer = x.customer
	                              AND y.max_total = x.total
		GROUP BY x.customer, x.total
		*/
	}
	
	public static final String _ID = CpMessenger._ID;
	public static final String ADDRESS = CpMessenger.ADDRESS;
	public static final String BODY = CpMessenger.BODY;
	public static final String DATE = CpMessenger.DATE;
	public static final String PERSON = CpMessenger.PERSON;
	public static final String READ = CpMessenger.READ;
	public static final String SEEN = CpMessenger.SEEN;
	public static final String SUBJECT = CpMessenger.SUBJECT;
	public static final String TYPE = CpMessenger.TYPE;
	
	static Hashtable <String, TzDatabaseTable> tables; 
	static {
		tables = new Hashtable <String, TzDatabaseTable>();
		tables.put("SMS", new TzDatabaseTable("SMS", new TzDatabaseTable.TzDatabaseColumn[] {
			new TzDatabaseTable.TzDatabaseColumn(TzDatabaseTable.TzTableDataType.Int, _ID),
			new TzDatabaseTable.TzDatabaseColumn(TzDatabaseTable.TzTableDataType.String, ADDRESS),
			new TzDatabaseTable.TzDatabaseColumn(TzDatabaseTable.TzTableDataType.String, BODY),
			new TzDatabaseTable.TzDatabaseColumn(TzDatabaseTable.TzTableDataType.Int, DATE),
			new TzDatabaseTable.TzDatabaseColumn(TzDatabaseTable.TzTableDataType.String, PERSON),
			new TzDatabaseTable.TzDatabaseColumn(TzDatabaseTable.TzTableDataType.Bool, READ),
			new TzDatabaseTable.TzDatabaseColumn(TzDatabaseTable.TzTableDataType.Bool, SEEN),
			new TzDatabaseTable.TzDatabaseColumn(TzDatabaseTable.TzTableDataType.String, SUBJECT),
			new TzDatabaseTable.TzDatabaseColumn(TzDatabaseTable.TzTableDataType.Int, TYPE)
		}));
	};	
}
