package lbs.map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper{

	  private static final int DB_VERSION = 1;
	  private static final String DB_NAME = "LBS_Coverage";

	  public static final String TABLE_NAME = "BS";
	  
	  public static final String CELL_ID = "cellid";
	  public static final String LAC = "lac";
	  public static final String MCC = "mcc";
	  public static final String MNC = "mnc";
	  public static final String DATA_TIME = "datetime";

	  
	  
	  private static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( _id integer primary key autoincrement, "
	  + CELL_ID + " TEXT, " + LAC + " TEXT, " + MCC + " TEXT, " + MNC + " TEXT, " + DATA_TIME + ")";

	  public DbOpenHelper(Context context) {
	    super(context, DB_NAME, null,DB_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase sqLiteDatabase) {
	    sqLiteDatabase.execSQL(CREATE_TABLE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
	  }
	  
	  
	}