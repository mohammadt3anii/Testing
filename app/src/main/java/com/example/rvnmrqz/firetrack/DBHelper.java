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

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arvin on 6/21/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    Context con;

    long result;
    private static  final  int DATABASE_VERSION=5;
    private static  final String DATABASE_NAME= "db_firetrack.db";

    //LOGGED USER
    public static  final String TABLE_USER = "tbl_user";
    public static  final String COL_USER_LOC_ID="_id";//LOCAL ID

    //COMMON TO ALL USERS
    public static  final String COL_ACC_ID = "acc_id";
    public static  final String COL_USERNAME = "acc_username";
    public static  final String COL_PASSWORD = "acc_password";
    public static  final String COL_ACC_TYPE = "acc_type";

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
    public static  final String BARANGAY_COORDINATES = "barangay_coordinates";


    //UPDATES (NOTIFICATION)
    public static final String TABLE_UPDATES= "tbl_user_updates";
    public static final String COL_UPDATE_LOC_ID   = "update_loc_id";
    public static final String COL_UPDATE_ID   = "update_id";
    public static final String COL_CATEGORY = "category";
    public static final String COL_NOTIF_RECEIVER = "receiver";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_SENDER_ID ="sender_id";
    public static final String COL_DATETIME = "datetime";
    public static final String COL_OPENED = "opened";

    //reports
    public static final String TABLE_REPORTS ="tbl_reports";
    public static final String COL_REPORT_ID = "report_id";
    public static final String COL_REPORT_FIRE_STATUS = "fire_status"; //on going | stopped
    public static final String COL_ALARM_LEVEL = "alarm_level";
    public static final String COL_REPORTER_id ="reporter_id"; //Account_id of the user
    public static final String COL_REPORT_STATUS ="report_status"; //APPROVED|DECLINED
    public static final String COL_REPORT_DATETIME = "report_datetime";
    public static final String COL_REPORT_ADDITIONAL_INFO = "additional_info";
    public static final String COL_REPORT_COORDINATES  = "coordinates";
    public static final String COL_REPORT_PICUTRE = "picture";

    //firenotifs
    //not used in sqlite, just used to reference online table
    public static final String TABLE_FIRENOTIFS="tbl_firenotifs";
    public static final String COL_FIRENOTIF_ID="firenotif_id";
    //public static final String COL_REPORT_ID = "report_id";
    public static final String COL_FIRENOTIF_RECEIVER = "firenotif_receiver";
    public static final String COL_BARANGAY_ID_SENDER = "barangay_id_sender";
    public static final String COL_DELIVERED = "delivered";

    //firenotif_response
    public static final String TABLE_FIRENOTIF_RESPONSE = "firenotif_response";
    public static final String COL_RESPONSE_ID = "response_id";
    public static final String COL_RESPONDENT_ID= "respondent_id";
    //public static final String COL_FIRENOTIF_ID = "firenotif_id";
    public static final String COL_RESPONSES_DATETIME = "response_datetime";
    public static final String COL_RESPONSE = "response";


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
                        COL_ACC_TYPE+" TEXT, "+
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
                    BARANGAY_ID+" INTEGER, "+
                    BARANGAY_NAME + " TEXT," +
                    BARANGAY_CEL+" TEXT,"+
                    BARANGAY_TEL+" TEXT," +
                    BARANGAY_COORDINATES+" TEXT)");

            db.execSQL("CREATE TABLE "+TABLE_UPDATES+"(" +
                    COL_UPDATE_LOC_ID+ " INTEGER PRIMARY KEY, " +
                    COL_UPDATE_ID+ " INTEGER, " +
                    COL_CATEGORY+" TEXT, " +
                    COL_NOTIF_RECEIVER +" TEXT, " +
                    COL_TITLE+" TEXT, " +
                    COL_CONTENT+" TEXT, " +
                    COL_SENDER_ID+ " INTEGER, " +
                    COL_DATETIME+" TEXT," +
                    COL_OPENED+" TEXT)");

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
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_UPDATES+";");
        Log.wtf("DBHELPER","Old database is dropped");
        onCreate(db);
    }

    public long insertLoggedUser(String acc_id, String username,String pass,String acc_type,String fname,String mname,String lname,String gender, String birthday,String barangay_id, String contact_no, String coordinates,String picture)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_LOC_ID,1);
        contentValues.put(COL_ACC_ID,acc_id);
        contentValues.put(COL_ACC_TYPE, acc_type);
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

    public long insertLoggedTruck(String acc_id, String username,String pass,String acc_type,String plateno){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USER_LOC_ID,1);
        contentValues.put(COL_ACC_ID,acc_id);
        contentValues.put(COL_ACC_TYPE, acc_type);
        contentValues.put(COL_USERNAME,username);
        contentValues.put(COL_PASSWORD,pass);
        contentValues.put(COL_PLATE_NO,plateno);
      //  contentValues.put(COL_CONTACT_NO,contact_no);

        result =  db.insertOrThrow(TABLE_USER,null,contentValues);
        return result;
    }


    public long insertBarangay(String b_id,String b_name,String cellno, String tel,String coordinates) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BARANGAY_ID,b_id);
        contentValues.put(BARANGAY_NAME,b_name);
        contentValues.put(BARANGAY_CEL,cellno);
        contentValues.put(BARANGAY_TEL,tel);
        contentValues.put(BARANGAY_COORDINATES,coordinates);
        long res = db.insertOrThrow(TABLE_BARANGAY,null,contentValues);
    return res;
    }

    public void insertUpdate(String id,String category, String title, String content, String sender_id, String datetime, String opened){
        SQLiteDatabase db = getWritableDatabase();
        recheckAndFixTable();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_UPDATE_ID,id);
        contentValues.put(COL_CATEGORY,category);
        contentValues.put(COL_TITLE,title);
        contentValues.put(COL_CONTENT,content);
        contentValues.put(COL_SENDER_ID,sender_id);
        contentValues.put(COL_DATETIME,datetime);
        contentValues.put(COL_OPENED,opened);
        db.insertOrThrow(TABLE_UPDATES,null,contentValues);
    }

    public void recheckAndFixTable(){
        try{
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+TABLE_UPDATES+"'", null);
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    cursor.close();
                    //table exists
                }else{
                    //table does not exist, create one
                    db.execSQL("CREATE TABLE "+TABLE_UPDATES+"(" +
                            COL_UPDATE_LOC_ID+ " INTEGER PRIMARY KEY, " +
                            COL_UPDATE_ID+ " INTEGER, " +
                            COL_CATEGORY+" TEXT, " +
                            COL_NOTIF_RECEIVER +" TEXT, " +
                            COL_TITLE+" TEXT, " +
                            COL_CONTENT+" TEXT, " +
                            COL_SENDER_ID+ " INTEGER, " +
                            COL_DATETIME+" TEXT," +
                            COL_OPENED+" TEXT)");
                    Log.wtf("insertUpdate","table updates does not exist, now created");
                }
            }

            cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+TABLE_BARANGAY+"'", null);
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    cursor.close();
                    //table exists
                }else{
                    //table does not exist, create one
                    db.execSQL("CREATE TABLE "+TABLE_BARANGAY+"("+
                            BARANGAY_LOC_ID+" INTEGER PRIMARY KEY, "+
                            BARANGAY_ID+" INTEGER, "+
                            BARANGAY_NAME + " TEXT," +
                            BARANGAY_CEL+" TEXT,"+
                            BARANGAY_TEL+" TEXT)");
                    Log.wtf("insertUpdate","table barangay does not exist, now created");
                }
            }
        }catch (Exception e){
            Log.wtf("recheckAndFixTable","A problem encountered "+e.getMessage());
            Toast.makeText(con, "Problem in checking database", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeTableData(String TABLE_NAME){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_NAME+";");
    }

    public Cursor getSqliteData(String qry){
        try {
            recheckAndFixTable();

            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery(qry,null);

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


    public void executeThisQuery(String query){
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL(query);
            Log.wtf("ExecuteThisQuery","A query has been executed");
        }catch (Exception ee){
            Log.wtf("executeThisQuery",ee.getMessage());
        }
    }
    public void removeAllData(){
        Log.wtf("removeAllData","FUNCTION IS CALLED\n\n\n\n\n");

        // query to obtain the names of all tables in your database
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tables = new ArrayList<>();

// iterate over the result set, adding every table name to a list
        while (c.moveToNext()) {
            tables.add(c.getString(0));
            Log.wtf("while","table name: "+c.getString(0));

        }

        Log.wtf("deleting","DELETING\n\n\n\n");
        for (String table : tables) {
            Log.wtf("inside for loop","table name: "+table);
            String DELETE_DATA = "DELETE FROM " + table+";";
            db.execSQL(DELETE_DATA);
        }
    }
}
