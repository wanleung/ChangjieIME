package com.linkomnia.android.Changjie;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	@SuppressLint("SdCardPath")
	private static String DB_PATH;
	 
    private static String DB_NAME = "changjie.db";
 
    private SQLiteDatabase myDataBase; 
 
    private final Context myContext;
    
    private static final int DATABASE_VERSION = 1;
    
    public DatabaseHelper(Context context) {
    	 
    	super(context, DB_NAME, null, DATABASE_VERSION);
        this.myContext = context;
        DB_PATH = this.myContext.getFilesDir().getPath() + "/";
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
    	try {
    		createDatabase();
    	} catch (IOException ioe) {
    		throw new Error("Unable to create database");
    	}
    	try {
    		openDatabase();
    	}catch(SQLException sqle){
    		throw sqle;
    	}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public void createDatabase() throws IOException{
		 
    	boolean dbExist = checkDatabase();
 
    	if(dbExist){
    		//do nothing - database already exist
    	}else{
 
    		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
 
        	try {
 
    			copyDatabase();
 
    		} catch (IOException e) {
 
        		throw new Error("Error copying database: " + e);
 
        	}
    	}
 
    }
	
	private boolean checkDatabase(){
		 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
 
    		//database does't exist yet.
 
    	}
 
    	if(checkDB != null){
 
    		checkDB.close();
 
    	}
 
    	return checkDB != null ? true : false;
    }
	
	private void copyDatabase() throws IOException{
		 
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }
 
    public void openDatabase() throws SQLException{
 
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    }
 
    @Override
	public synchronized void close() {
 
    	    if(myDataBase != null)
    		    myDataBase.close();
 
    	    super.close();
 
	}

}
