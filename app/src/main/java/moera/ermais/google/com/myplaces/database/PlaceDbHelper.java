package moera.ermais.google.com.myplaces.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class PlaceDbHelper extends SQLiteOpenHelper {

    // The database name
    private static final String DATABASE_NAME = "myplaces.db";

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 2;

    // Constructor
    public PlaceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create a table to hold the places data
        final String SQL_CREATE_PLACES_TABLE = "CREATE TABLE " + PlaceContract.PlaceEntry.TABLE_NAME + " (" +
                PlaceContract.PlaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PlaceContract.PlaceEntry.COLUMN_PLACE_ID + " TEXT NOT NULL, " +
                PlaceContract.PlaceEntry.COLUMN_NOTIFICATION + " TEXT NOT NULL, " +
                PlaceContract.PlaceEntry.COLUMN_REPLY + " TEXT NOT NULL, " +
                PlaceContract.PlaceEntry.COLUMN_PLACE_NAME + " TEXT, " +
                PlaceContract.PlaceEntry.COLUMN_PLACE_DESCR + " TEXT, " +
                PlaceContract.PlaceEntry.COLUMN_PLACE_XY_LAT + " NUMBER, " +
                PlaceContract.PlaceEntry.COLUMN_PLACE_XY_LNG + " NUMBER, " +
                "UNIQUE (" + PlaceContract.PlaceEntry.COLUMN_PLACE_ID + ") ON CONFLICT REPLACE" +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_PLACES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // For now simply drop the table and create a new one.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PlaceContract.PlaceEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
