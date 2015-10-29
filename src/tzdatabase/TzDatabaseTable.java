package tzdatabase;

import android.database.Cursor;

public class TzDatabaseTable {
	public String name;
	public TzDatabaseColumn[] columns;
	
	public TzDatabaseTable(String tableName, TzDatabaseColumn[] cols) {
		name = tableName;
		columns = cols;
	}
	
	public static enum TzTableDataType {
		Int, String, Float, Bool
	}
	
	public static class TzDatabaseColumn {
		public TzTableDataType type;
		public String name;
		
		public TzDatabaseColumn(TzTableDataType pType, String pName) {
			type = pType;
			name = pName;
		}
	}
	
	public void createIntoDB(TzDatabaseLite db) {
		String sql = "CREATE TABLE " + name + "(";
		
		for (int i = 0; i < columns.length; ++i){
			TzDatabaseColumn col = columns[i];
			sql += (i==0?"":",") + col.name + " " + col.type;
		}
		
		sql += ")";
				
		db.execute(sql);
	}
	
	public Cursor totalSelect(TzDatabaseLite db) {
		return db.query("SELECT * FROM " + name);
	}
}
