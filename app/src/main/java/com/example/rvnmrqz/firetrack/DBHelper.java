package com.example.rvnmrqz.firetrack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by arvin on 6/21/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    Context con;

    long result;
    private static  final  int DATABASE_VERSION=11;
    private static  final String DATABASE_NAME= "firetrack.db";

    //LOGGED USER
    public static  final String TABLE_USER = "tbl_user";
    public static  final String COL_USER_LOC_ID="_id";//LOCAL ID

    //COMMON TO ALL USERS
    public static  final String COL_ACC_ID = "acc_id";
    public static  final String COL_USERNAME = "acc_username";
    public static  final String COL_PASSWORD = "acc_password";
    public static  final String COL_ACC_TYPE_ID = "acc_type_id";

    //NORMAL USER
    public static  final String COL_FNAME = "fname";
    public static  final String COL_MNAME = "mname";
    public static  final String COL_LNAME = "lname";
    public static  final String COL_GENDER = "gender";
    public static  final String COL_BIRTHDAY = "birthday";
    public static  final String COL_CONTACT_NO = "contactno";
    public static  final String COL_BARANGAY_ID = "barangay_id";
    public static  final String COL_COORDINATES = "coordinates";
    public static  final String COL_PICTURE = "picture";

    //TRUCK
    public static  final String COL_PLATE_NO = "plateno";
    //public static  final String COL_BARANGAY_ID = "barangay_id";
    //public static  final String COL_CONTACT_NO = "contactno";

    //BARANGAY
    public static  final String TABLE_BARANGAY = "tbl_barangay";
    public static  final String BARANGAY_LOC_ID  = "loc_barangay_id";
    public static  final String BARANGAY_ID  = "barangay_id";
    public static  final String BARANGAY_NAME = "barangay_name";
    public static  final String BARANGAY_CEL = "barangay_cel";
    public static  final String BARANGAY_TEL = "barangay_tel";


    //NOTIFICATION
    public static final String TABLE_NOTIFICATION = "tbl_notifications";
    public static final String COL_NOTIF_LOC_ID = "notif_loc_id";
    public static final String COL_NOTIF_ID = "notif_id";
    public static final String COL_NOTIF_SENDER = "notif_sender";
    public static final String COL_NOTIF_TITLE = "notif_title";
    public static final String COL_NOTIF_MESSAGE = "notif_message";
    public static final String COL_NOTIF_DATETIME = "notif_date_time";
    public static final String COL_NOTIF_PERSONAL = "personal";
    public static final String COL_NOTIF_OPENED = "opened";
    //NOT CREATED, JUST TO REFERENCE THE ONLINE COLUMNS
    public static final String COL_NOTIF_USER_RECEIVER= "notif_user_receiver";
    public static final String COL_NOTIF_BARANGAY_RECEIVER = "notif_barangay_receiver";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        con = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL("CREATE TABLE "+TABLE_USER+"("+
                        COL_USER_LOC_ID+" INTEGER PRIMARY KEY, "+
                        COL_ACC_ID+" TEXT, "+
                        COL_USERNAME + " TEXT, "+
                        COL_PASSWORD + " TEXT, "+
                        COL_ACC_TYPE_ID+" TEXT, "+
                        COL_FNAME+ " TEXT, "+
                        COL_MNAME+" TEXT, "+
                        COL_LNAME+" TEXT, "+
                        COL_GENDER+" TEXT, "+
                        COL_BIRTHDAY+" TEXT, "+
                        COL_CONTACT_NO+" TEXT, "+
                        COL_BARANGAY_ID+" TEXT, "+
                        COL_COORDINATES+" TEXT,"+
                        COL_PICTURE+" TEXT, "+
                        COL_PLATE_NO+" TEXT)");

            db.execSQL("CREATE TABLE "+TABLE_BARANGAY+"("+
                    BARANGAY_LOC_ID+" INTEGER PRIMARY KEY, "+
                    BARANGAY_ID+" TEXT, "+
                    BARANGAY_NAME + " TEXT," +
                    BARANGAY_CEL+" TEXT,"+
                    BARANGAY_TEL+" TEXT)");

            db.execSQL("CREATE TABLE "+TABLE_NOTIFICATION+"(" +
                    COL_NOTIF_LOC_ID+ "INTEGER PRIMARY KEY, " +
                    COL_NOTIF_ID+ " TEXT, " +
                    COL_NOTIF_SENDER+" TEXT, " +
                    COL_NOTIF_TITLE+" TEXT, " +
                    COL_NOTIF_MESSAGE+" TEXT, " +
                    COL_NOTIF_DATETIME+" TEXT, " +
                    COL_NOTIF_PERSONAL+ " TEXT, " +
                    COL_NOTIF_OPENED+" TEXT)");

              Toast.makeText(con, "Tables successfully created", Toast.LENGTH_SHORT).show();
            Log.wtf("DBHELPER","database is created");
        }catch (Exception ee){
            Toast.makeText(con, "Error encountered in creating tables \n"+ee.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_USER+";");
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_BARANGAY+";");
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NOTIFICATION+";");
        Log.wtf("DBHELPER","Old database is dropped");
        onCreate(db);
    }

    public long insertLoggedUser(String acc_id, String username,String pass,String acc_type,String fname,String mname,String lname,String gender, String birthday,String barangay_id, String contact_no, String coordinates,String picture)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_LOC_ID,1);
        contentValues.put(COL_ACC_ID,acc_id);
        contentValues.put(COL_ACC_TYPE_ID, acc_type);
        contentValues.put(COL_USERNAME,username);
        contentValues.put(COL_PASSWORD,pass);
        contentValues.put(COL_FNAME,fname);
        contentValues.put(COL_MNAME,mname);
        contentValues.put(COL_LNAME,lname);
        contentValues.put(COL_GENDER,gender);
        contentValues.put(COL_BIRTHDAY,birthday);
        contentValues.put(COL_CONTACT_NO,contact_no);
        contentValues.put(COL_BARANGAY_ID,barangay_id);
        contentValues.put(COL_COORDINATES,coordinates);
        contentValues.put(COL_PICTURE,picture);

        result =  db.insertOrThrow(TABLE_USER,null,contentValues);
        return result;
    }

    public long insertLoggedTruck(String acc_id, String username,String pass,String acc_type,String contact_no,String plateno){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_LOC_ID,1);
        contentValues.put(COL_ACC_ID,acc_id);
        contentValues.put(COL_ACC_TYPE_ID, acc_type);
        contentValues.put(COL_USERNAME,username);
        contentValues.put(COL_PASSWORD,pass);
        contentValues.put(COL_PLATE_NO,plateno);
        contentValues.put(COL_CONTACT_NO,contact_no);

        result =  db.insertOrThrow(TABLE_USER,null,contentValues);
        return result;
    }

    public void removeLoggedUser(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_USER+" WHERE "+COL_USER_LOC_ID+"=1");
    }

    public long insertBarangay(String b_id,String b_name,String cellno, String tel) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BARANGAY_ID,b_id);
        contentValues.put(BARANGAY_NAME,b_name);
        contentValues.put(BARANGAY_CEL,cellno);
        contentValues.put(BARANGAY_TEL,tel);
        long res = db.insertOrThrow(TABLE_BARANGAY,null,contentValues);
    return res;
    }
    public long insertNotification(String id,String sender, String title, String msg, String datetime, String personal, String opened){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NOTIF_ID,id);
        cv.put(COL_NOTIF_SENDER,sender);
        cv.put(COL_NOTIF_TITLE,title);
        cv.put(COL_NOTIF_MESSAGE,msg);
        cv.put(COL_NOTIF_DATETIME,datetime);
        cv.put(COL_NOTIF_PERSONAL,personal);
        cv.put(COL_NOTIF_OPENED,opened);
        long res = db.insertOrThrow(TABLE_NOTIFICATION,null,cv);
        return res;
    }

    public void removeTableData(String TABLE_NAME){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_NAME+";");
    }

    public Cursor getSqliteData(String qry){
        try {
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery(qry,null);
            Log.wtf("getSliteData- Content: ",""+cursor);
            return cursor;
        }catch (Exception e){
            Log.wtf("getSQLiteData",e.getMessage());
            Toast.makeText(con, "An error encountered", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    //DB MANAGER
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
}
