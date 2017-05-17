package cx.ath.laghaim.waxprivacy;

/**
 * Created by dante on 16.05.17.
 */

import java.util.ArrayList;
import java.util.HashMap;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "WA.db";

    public static final String CONTACTS_TABLE_NAME    = "contacts";
    public static final String CONTACTS_COLUMN_ID     = "id";
    public static final String CONTACTS_COLUMN_RAW_ID = "rid";
    public static final String CONTACTS_COLUMN_NAME   = "name";
    public static final String CONTACTS_COLUMN_PHONE  = "phone";
    public static final String CONTACTS_COLUMN_STATUS = "status";

    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table contacts " +
                        "(id integer,rid integer, name text,phone text, status int)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        //db.execSQL("DROP TABLE IF EXISTS contacts");
        //onCreate(db);
    }

    public boolean insertContact (int id, int rid, String name, String phone,int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACTS_COLUMN_ID    , id);
        contentValues.put(CONTACTS_COLUMN_RAW_ID, rid);
        contentValues.put(CONTACTS_COLUMN_NAME  , name);
        contentValues.put(CONTACTS_COLUMN_PHONE , phone);
        contentValues.put(CONTACTS_COLUMN_STATUS, status);
        db.insert(CONTACTS_TABLE_NAME, null, contentValues);
        return true;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (int rowid, int id, int rid, String name, String phone,int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACTS_COLUMN_ID    , id);
        contentValues.put(CONTACTS_COLUMN_RAW_ID, rid);
        contentValues.put(CONTACTS_COLUMN_NAME  , name);
        contentValues.put(CONTACTS_COLUMN_PHONE , phone);
        contentValues.put(CONTACTS_COLUMN_STATUS, status);
        db.update(CONTACTS_TABLE_NAME, contentValues, "rowid = ? ", new String[] { Integer.toString(rowid) } );
        return true;
    }

    public Integer deleteContact (int rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts","rowid = ? ", new String[] { String.valueOf(rowid) });
    }

    public ArrayList <String> getrow(Integer id)
    {
        ArrayList<String> array_list =new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select rowid,* from "+CONTACTS_TABLE_NAME+" where rowid= "+id , null );

        res.moveToFirst();
        array_list.add(res.getString(res.getColumnIndex("rowid"    )));
        array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID    )));
        array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_RAW_ID)));
        array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME  )));
        array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE )));
        array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_STATUS)));

        return  array_list;
    }


    public ArrayList<ArrayList <String> > contact(Integer id,String name,String phone)
    {
        ArrayList<ArrayList <String> > re =new ArrayList<ArrayList<String>>();


        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACTS_COLUMN_ID    , id);
        contentValues.put(CONTACTS_COLUMN_PHONE  , phone);
        contentValues.put(CONTACTS_COLUMN_NAME  , name);
        String whereClause = "id = ? and name = ? and phone = ? ";
        String[] whereArgs = new String[] {
                String.valueOf(id),
                name,
                phone
        };
        Cursor res = db.query(CONTACTS_TABLE_NAME, new String[]{"rowid","*"}, whereClause, whereArgs, null, null,null);
        res.moveToFirst();
        while(res.isAfterLast() == false){
            ArrayList<String> array_list =new ArrayList<String>();
            array_list.add(res.getString(res.getColumnIndex("rowid"    )));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID    )));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_RAW_ID)));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME  )));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE )));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_STATUS)));
            re.add(array_list);
            res.moveToNext();

        }
        return re;
    }

    public ArrayList<ArrayList <String> > getAllCotacts() {

        ArrayList<ArrayList <String> > re =new ArrayList<ArrayList<String>>();
        ArrayList<String> array_list =new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select rowid,* from "+CONTACTS_TABLE_NAME, null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex("rowid"    )));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID    )));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_RAW_ID)));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME  )));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE )));
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_STATUS)));
            res.moveToNext();
            re.add(array_list);
            array_list.clear();
        }
        return re;
    }
}
