package cx.ath.laghaim.waxprivacy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dante on 20.05.17.
 */


public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "wa.db";
    public static final String CONTACTS_TABLE_NAME    = "contacts";
    public static final String CONTACTS_COLUMN_STATUS = "status";

    public final Context myContext;
    public static DBHelper mInstance;
    public static SQLiteDatabase myWritableDb;

    public DBHelper(Context context) {

        super(context, DATABASE_NAME, null, 3);
        this.myContext = context;
    }

    public static DBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBHelper(context);
        }
        return mInstance;
    }

    public SQLiteDatabase getMyWritableDatabase() {
        if ((myWritableDb == null) || (!myWritableDb.isOpen())) {
            myWritableDb = this.getWritableDatabase();
        }

        return myWritableDb;
    }

    @Override
    public void close() {
        super.close();
        if (myWritableDb != null) {
            myWritableDb.close();
            myWritableDb = null;
        }
    }

    public void dropall()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tables = new ArrayList<>();

        while (c.moveToNext()) {
            tables.add(c.getString(0));
        }

        for (String table : tables) {
            String dropQuery = "DROP TABLE IF EXISTS " + table;
            db.execSQL(dropQuery);
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        //dropall();

        String TABEL="CREATE TABLE IF not exists "+CONTACTS_TABLE_NAME+" " +
                "("+
                "H_ROWS  integer," +
                "H_DATA1 text," +
                "H_PHONE   text," +
                "STATUS  integer" +
                ");";
        db.execSQL(TABEL);
    }


    public void todb(int H_ROWS, String H_DATA1,String H_PHONE)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Integer> re=new ArrayList<>();

        String whereClause = "H_ROWS = ? and H_DATA1 = ? and H_PHONE = ? ";
        String[] whereArgs = new String[] {
                String.valueOf(H_ROWS),
                H_DATA1,
                H_PHONE
        };
        Cursor res = db.query(CONTACTS_TABLE_NAME, new String[]{"rowid","*"}, whereClause, whereArgs,null,null,null);
        res.moveToFirst();

        if (res.getCount()==0) {
            //Log.w("Xposed insert",H_ROWS+" "+ H_DATA1 + " " + H_PHONE);
            ContentValues contentValues = new ContentValues();
            contentValues.put("H_ROWS"   , H_ROWS);
            contentValues.put("H_DATA1"   , H_DATA1);
            contentValues.put("H_PHONE"  , H_PHONE);
            contentValues.put("STATUS"   , 0 );
            db.insert(CONTACTS_TABLE_NAME, null, contentValues);
        } else {
            //Log.w("Xposed update",res.getString(res.getColumnIndex("rowid"))+" "+H_ROWS+" "+ H_DATA1 + " " + H_PHONE);
            ContentValues contentValues = new ContentValues();
            contentValues.put("H_ROWS", H_ROWS);
            contentValues.put("H_DATA1", H_DATA1);
            contentValues.put("H_PHONE", H_PHONE);
            db.update(CONTACTS_TABLE_NAME, contentValues, "rowid = ? ", new String[]{res.getString(res.getColumnIndex("rowid"))});
        }
    }

    public boolean updateContact (int rowid, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACTS_COLUMN_STATUS, status);
        db.update(CONTACTS_TABLE_NAME, contentValues, "rowid = ? ", new String[] { Integer.toString(rowid) } );
        return true;
    }

    public ArrayList<ArrayList <String> > getAllCotacts() {

        ArrayList<ArrayList <String> > re =new ArrayList<>();
        ArrayList<String> array_list;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select rowid,* from "+CONTACTS_TABLE_NAME + " ORDER BY H_DATA1 ASC;", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            array_list =new ArrayList<>();
            array_list.add(res.getString(res.getColumnIndex("rowid"  )));
            array_list.add(res.getString(res.getColumnIndex("H_ROWS" )));
            array_list.add(res.getString(res.getColumnIndex("H_DATA1")));
            array_list.add(res.getString(res.getColumnIndex("H_PHONE")));
            array_list.add(res.getString(res.getColumnIndex("STATUS" )));
            re.add(array_list);
            res.moveToNext();
        }
        return re;
    }

    public ArrayList<ArrayList <String> > getAllCotacts_s() {

        ArrayList<ArrayList <String> > re =new ArrayList<>();
        ArrayList<String> array_list =new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select rowid,* from "+CONTACTS_TABLE_NAME + " where STATUS=1 ORDER BY H_DATA1 ASC;", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            array_list =new ArrayList<>();
            array_list.add(res.getString(res.getColumnIndex("rowid"    )));
            array_list.add(res.getString(res.getColumnIndex("H_ROWS"   )));
            array_list.add(res.getString(res.getColumnIndex("H_DATA1")));
            array_list.add(res.getString(res.getColumnIndex("H_PHONE"  )));
            array_list.add(res.getString(res.getColumnIndex("STATUS" )));
            res.moveToNext();
            re.add(array_list);
        }
        return re;
    }

    public ArrayList<Integer> get_H_ROWS()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Integer> re=new ArrayList<>();
        Cursor res =  db.rawQuery("SELECT * FROM H_ROWS", null );
        while(res.isAfterLast() == false)
        {
            re.add(res.getInt(res.getColumnIndex("H_ROWS"    )));
        }
        return re;
    }
    public ArrayList<String> get_H_DATA1()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> re=new ArrayList<>();
        Cursor res =  db.rawQuery("SELECT * FROM H_DATA1", null );
        while(res.isAfterLast() == false)
        {
            re.add(res.getString(res.getColumnIndex("H_DATA1"    )));
        }
        return re;
    }
    public ArrayList<String> get_H_PHONE()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> re=new ArrayList<>();
        Cursor res =  db.rawQuery("SELECT * FROM H_PHONE", null );
        while(res.isAfterLast() == false)
        {
            re.add(res.getString(res.getColumnIndex("H_PHONE"    )));
        }
        return re;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        //db.execSQL("DROP TABLE IF EXISTS "+CONTACTS_TABLE_NAME);
        //onCreate(db);
        //db.execSQL("DROP TABLE IF EXISTS "+CONTACTS_TABLE_NAME_ID);
        //onCreate(db);
    }

    public void update_to() {
        ArrayList<ArrayList <String> > re =new ArrayList<>();
        ArrayList<String> array_list =new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select rowid,* from " + CONTACTS_TABLE_NAME + " ORDER BY H_DATA1 ASC;", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            array_list =new ArrayList<>();
            array_list.add(res.getString(res.getColumnIndex("rowid"    )));
            array_list.add(res.getString(res.getColumnIndex("H_ROWS"   )));
            array_list.add(res.getString(res.getColumnIndex("H_DATA1")));
            array_list.add(res.getString(res.getColumnIndex("H_PHONE"  )));
            array_list.add(res.getString(res.getColumnIndex("STATUS" )));
            res.moveToNext();
            re.add(array_list);
        }

        for (int i=0; i<re.size(); i++) {
            if (re.get(i).get(4).toString().equals("1")) {
                //Log.w("Xposed DB",re.get(i).get(1).toString());
                //Log.w("Xposed DB",re.get(i).get(2).toString());
                //Log.w("Xposed DB",re.get(i).get(3).toString());

                String H_ROWS=re.get(i).get(1).toString();
                ContentValues contentValues = new ContentValues();
                contentValues.put("H_ROWS"   , H_ROWS);

                try {
                db.insertOrThrow("H_ROWS", null, contentValues);

                contentValues = new ContentValues();
                contentValues.put("H_DATA1"   , re.get(i).get(2).toString());
                db.insertOrThrow("H_DATA1", null, contentValues);

                contentValues = new ContentValues();
                contentValues.put("H_PHONE"   , re.get(i).get(3).toString());
                db.insertOrThrow("H_PHONE", null, contentValues);
                } catch (SQLiteConstraintException e) {}

            } else {

                String table = "H_ROWS";
                String whereClause = "H_ROWS=?";
                String[] whereArgs = new String[] { re.get(i).get(1).toString() };
                db.delete(table, whereClause, whereArgs);

                table = "H_DATA1";
                whereClause = "H_DATA1=?";
                whereArgs = new String[] { re.get(i).get(2).toString() };
                db.delete(table, whereClause, whereArgs);

                table = "H_PHONE";
                whereClause = "H_PHONE=?";
                whereArgs = new String[] { re.get(i).get(3).toString() };
                db.delete(table, whereClause, whereArgs);
            }
        }
    }
}
