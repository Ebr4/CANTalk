package ye.com.ebra.cantalk.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.widget.Toast;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ye.com.ebra.cantalk.adapter.Item;

public class DBConnection {
    // create an object from DBInfo (custom database ) and DataBaseHelper (Default database)
    // to control the operations in the databases
    DBInfo dbInfo;
    DataBaseHelper dataBaseHelper;
    // constructor
    public DBConnection(Context context) {
        dbInfo =new DBInfo(context);
        dataBaseHelper=new DataBaseHelper(context);
    }
    // insert category to categories table (in custom database only !)
    public long InsertItemCategory(Item item){
        SQLiteDatabase sqLiteDatabase=dbInfo.getWritableDatabase();
        ContentValues categoryValues=new ContentValues();
        // put values (label,image,ParentCategoryID,type= category) in content values object
        categoryValues.put(DBInfo.Category_label,item.getName());
        categoryValues.put(DBInfo.Category_Image_File,item.getImage());
        categoryValues.put(DBInfo.Category_Parent_ID,item.getCat_id());
        categoryValues.put(DBInfo.type,item.getType());
        // store the result in long variable to check later for any errors occur while the running
        long inserted= sqLiteDatabase.insert(DBInfo.Categories_Table,null,categoryValues);
        sqLiteDatabase.close();
        return inserted;
    }
    // insert image to images table (in custom database only!)
    public long InsertItemImage(Item item){
        SQLiteDatabase sqLiteDatabase=dbInfo.getWritableDatabase();
        ContentValues imageValues=new ContentValues();
        // put values (CategoryID,label,image,type= Image) in content values object
        imageValues.put(DBInfo.Image_Category_FK,item.getCat_id());
        imageValues.put(DBInfo.Image_label,item.getName());
        imageValues.put(DBInfo.Image_File,item.getImage());
        imageValues.put(DBInfo.type,item.getType());
        // store the result in long variable to check later for any errors occur while the running
        long insertImage    =   sqLiteDatabase.insert(DBInfo.Images_Table,null,imageValues);
        sqLiteDatabase.close();
        return insertImage;
    }
    // get  contents from all databases and all tables stored in Arraylist of items ;)
    public List<Item> getAll(int Cat_id){
        List<Item> items=new ArrayList<>();
            //get default database contents
            getDefaultDBItems(items,Cat_id);
            // get custom database contents
            getCustomDBItems(items,Cat_id);
        return items;
    }
    // search in custom and default databases by Labels
    public List<Item> SearchImages(String label){
        // return items in list
        List<Item> result = new ArrayList<>();

        SQLiteDatabase dbCustom = dbInfo.getReadableDatabase();
        SQLiteDatabase dbDefault= dataBaseHelper.getReadableDatabase();
        // search queries in array of strings
        String[] sqlSelect          =   {DBInfo.Image_ID_PK, DBInfo.Image_Category_FK, DBInfo.Image_File, DBInfo.Image_label};
        String[] sqlDefaultSelect   =   {DataBaseHelper.Image_ID_PK, DataBaseHelper.Image_Category_FK, DataBaseHelper.Image_File, DataBaseHelper.Image_label};

        //  This will like query : select * from Images where label LIKE %pattern%
        // > results must contain the label characters
        // Store the results from queries in Cursors

        @SuppressLint("Recycle") Cursor cursorCustomDB   = dbCustom.query(DBInfo.Images_Table, sqlSelect, DBInfo.Image_label +" LIKE ? ",new String[]{"%"+label+"%"}, null, null, null);
        @SuppressLint("Recycle") Cursor cursorDefaultDB  = dbDefault.query(DataBaseHelper.Images_Table,sqlDefaultSelect,DataBaseHelper.Image_label+" LIKE ? ",new String[]{"%"+label+"%"}, null, null, null);
        // search in custom database then store the results in the arraylist
        if (cursorCustomDB.moveToFirst()) {
            do {
                Item item = new Item();

                item.setID(cursorCustomDB.getInt        (cursorCustomDB.getColumnIndex(DBInfo.Image_ID_PK)));
                item.setCat_id(cursorCustomDB.getInt    (cursorCustomDB.getColumnIndex(DBInfo.Image_Category_FK)));
                item.setImage(cursorCustomDB.getBlob    (cursorCustomDB.getColumnIndex(DBInfo.Image_File)));
                item.setName(cursorCustomDB.getString   (cursorCustomDB.getColumnIndex(DBInfo.Image_label)));
                item.setType("Image");

                result.add(item);
            }while (cursorCustomDB.moveToNext());
        }
        // search in default database then store the results in the arraylist
        if (cursorDefaultDB.moveToFirst()) {
            do {
                Item item = new Item();
                item.setID(cursorDefaultDB.getInt        (cursorDefaultDB.getColumnIndex(DataBaseHelper.Image_ID_PK)));
                item.setCat_id(cursorDefaultDB.getInt    (cursorDefaultDB.getColumnIndex(DataBaseHelper.Image_Category_FK)));
                item.setImage(cursorDefaultDB.getBlob    (cursorDefaultDB.getColumnIndex(DataBaseHelper.Image_File)));
                item.setName(cursorDefaultDB.getString   (cursorDefaultDB.getColumnIndex(DataBaseHelper.Image_label)));
                item.setType("Image");
                result.add(item);
            }while (cursorDefaultDB.moveToNext());
        }
        return result;
    }
    // get items according to category ID store the results in the given arraylist...
    private void getCustomDBItems(List<Item> items, int Cat_id){
        // select queries get items from categories  and images tables
        String getAllCategoriesQuery="SELECT * FROM "+ DBInfo.Categories_Table
                +" WHERE "   + DBInfo.Category_Parent_ID +" = "+Cat_id+";";
        String getAllImagesQuery="SELECT * FROM "+ DBInfo.Images_Table
                +" WHERE "   + DBInfo.Image_Category_FK +" = "+Cat_id+";";

        SQLiteDatabase db        = dbInfo.getWritableDatabase();
        // implement the select queries then store the results in cursors for each table..
        Cursor cursorCategories  = db.rawQuery(getAllCategoriesQuery,null);
        Cursor cursorImages      = db.rawQuery(getAllImagesQuery,null);
        // fill categories cursor
        if(cursorCategories.moveToFirst()){
            do{
                Item category=new Item();
                category.setID(Integer.parseInt(cursorCategories.getString(0)));
                //category.setImage(cursor.getBlob(1));
                category.setImage(cursorCategories.getBlob(2));
                category.setName(cursorCategories.getString(3));
                category.setType(cursorCategories.getString(4));

                items.add(category);
            }while (cursorCategories.moveToNext());
        }
        // fill images cursor
        if (cursorImages.moveToFirst()){
            do {
                Item Image=new Item();
                Image.setID(Integer.parseInt(cursorImages.getString(0)));
                Image.setCat_id(Integer.parseInt(cursorImages.getString(1)));
                Image.setImage(cursorImages.getBlob(2));
                Image.setName(cursorImages.getString(3));
                Image.setType(cursorImages.getString(4));
                items.add(Image);
            }while (cursorImages.moveToNext());
        }
        cursorCategories.close();
        cursorImages.close();
        db.close();
    }
    // get items according to category ID store the results in the given arraylist...
    // the same as previous method but in default database ..
    private void getDefaultDBItems(List<Item> items, int Cat_id){
        String getAllCategoriesQuery="SELECT * FROM "+ DBInfo.Categories_Table
                +" WHERE "   + DBInfo.Category_Parent_ID +" = "+Cat_id+";";
        String getAllImagesQuery="SELECT * FROM "+ DBInfo.Images_Table
                +" WHERE "   + DBInfo.Image_Category_FK +" = "+Cat_id+";";
        /// get default data
        if(!dataBaseHelper.checkDataBase()){
            dataBaseHelper.createDataBase();
        }
        try {
            dataBaseHelper.openDataBase();
        }catch(SQLException sqle){
            throw sqle;
        }

        SQLiteDatabase defaultdb=dataBaseHelper.getReadableDatabase();
        Cursor cursordefaultCategories  = defaultdb.rawQuery(getAllCategoriesQuery,null);
        Cursor cursordefaultImages      = defaultdb.rawQuery(getAllImagesQuery,null);
        if(cursordefaultCategories.moveToFirst()){
            do{
                Item category=new Item();
                category.setID(Integer.parseInt(cursordefaultCategories.getString(0)));
                category.setImage(cursordefaultCategories.getBlob(2));
                category.setName(cursordefaultCategories.getString(3));
                category.setType(cursordefaultCategories.getString(4));

                items.add(category);
            }while (cursordefaultCategories.moveToNext());
        }
        if (cursordefaultImages.moveToFirst()){
            do {
                Item Image=new Item();
                Image.setID(Integer.parseInt(cursordefaultImages.getString(0)));
                Image.setCat_id(Integer.parseInt(cursordefaultImages.getString(1)));
                Image.setImage(cursordefaultImages.getBlob(2));
                Image.setName(cursordefaultImages.getString(3));
                Image.setType(cursordefaultImages.getString(4));
                items.add(Image);
            }while (cursordefaultImages.moveToNext());
        }

        cursordefaultCategories.close();
        cursordefaultImages.close();
        dataBaseHelper.close();
    }
    // rename category or image according to its id ..
    // if the id > 2000 *the first of default database*
    public int rename(int id, String type, String newLabel){
        int update=0;
        // check id
        if(id>2000){
            // work with default database ..
            SQLiteDatabase sqLiteDatabase=dataBaseHelper.getWritableDatabase();
            ContentValues values=new ContentValues();
            // rename according to id
            String Id=String.valueOf(id);
            String[] ID={Id};
            // if type is category work with categories table otherwise work with images table
            if(type.equals("Category")){
                values.put(DataBaseHelper.Category_label,newLabel);
                update=sqLiteDatabase.update(DataBaseHelper.Categories_Table,values, DataBaseHelper.Category_ID_PK +" =? ",ID);
            }else {
                values.put(DataBaseHelper.Category_label,newLabel);
                update=sqLiteDatabase.update(DataBaseHelper.Images_Table,values, DataBaseHelper.Category_ID_PK +" =? ",ID);
            }
            return update;
        }else{
            // work with default database ..
            SQLiteDatabase sqLiteDatabase=dbInfo.getWritableDatabase();
            ContentValues values=new ContentValues();
            // rename according to id
            String Id=String.valueOf(id);
            String[] ID={Id};
            // if type is category work with categories table otherwise work with images table
            if(type.equals("Category")){
                values.put(DBInfo.Category_label,newLabel);
                update=sqLiteDatabase.update(DBInfo.Categories_Table,values, DBInfo.Category_ID_PK +" =? ",ID);
            }else {
                values.put(DBInfo.Category_label,newLabel);
                update=sqLiteDatabase.update(DBInfo.Images_Table,values, DBInfo.Category_ID_PK +" =? ",ID);
            }
            return update;
        }
    }
    // update photo of item according to id if id > 2000
    // update it from default database otherwise remove it from custom db
    public int updatePhoto(int id, String type, byte[] newImage) {
        // to get back the result if the update done successfully or not ...
        int update=0;
        // check id to determine where to update
        if(id > 2000){
            // work with default database
            SQLiteDatabase sqLiteDatabase=dataBaseHelper.getWritableDatabase();
            ContentValues values=new ContentValues();
            // update according to ID of the item
            String Id=String.valueOf(id);
            String[] ID={Id};
            if(type.equals("Category")){
                values.put(DataBaseHelper.Category_Image_File,newImage);
                // update query
                update=sqLiteDatabase.update(DataBaseHelper.Categories_Table,values, DataBaseHelper.Category_ID_PK +" =? ",ID);
            }else {
                values.put(DataBaseHelper.Image_File,newImage);
                // update query
                update=sqLiteDatabase.update(DataBaseHelper.Images_Table,values, DataBaseHelper.Category_ID_PK +" =? ",ID);
            }
            return update;
        }else {
            // work with custom database
            SQLiteDatabase sqLiteDatabase=dbInfo.getWritableDatabase();
            ContentValues values=new ContentValues();

            String Id=String.valueOf(id);
            String[] ID={Id};

            if(type.equals("Category")){
                values.put(DBInfo.Category_Image_File,newImage);
                // update query
                update=sqLiteDatabase.update(DBInfo.Categories_Table,values, DBInfo.Category_ID_PK +" =? ",ID);
            }else {
                values.put(DBInfo.Image_File,newImage);
                // update query
                update=sqLiteDatabase.update(DBInfo.Images_Table,values, DBInfo.Category_ID_PK +" =? ",ID);
            }
            return update;
        }
    }
    // remove items according to id if id > 2000 remove it from default database otherwise remove it from custom db
    public int remove(int id,String type){
        // return value of remove integer to determine the remove done successfully or not..
        int remove=0;
        // check id
        if(id > 2000){
        // work with default database
            SQLiteDatabase sqLiteDatabase=dataBaseHelper.getWritableDatabase();
            SQLiteDatabase DBCustom=dbInfo.getWritableDatabase();
            // remove according to id
            String Id=String.valueOf(id);
            String[] ID={Id};
            // if the type is category then remove the category and all of its contents...
            if(type.equals("Category")){
                remove  = sqLiteDatabase.delete(DataBaseHelper.Categories_Table, DataBaseHelper.Category_ID_PK +" =? ",ID);
                remove *= sqLiteDatabase.delete(DataBaseHelper.Images_Table, DataBaseHelper.Image_Category_FK +" =? ",ID);
                remove *= DBCustom.delete(DBInfo.Categories_Table, DBInfo.Category_ID_PK +" =? ",ID);
                remove *= DBCustom.delete(DBInfo.Images_Table, DBInfo.Image_Category_FK +" =? ",ID);
            }else {
                // remove only the image
                remove = sqLiteDatabase.delete(DataBaseHelper.Images_Table, DataBaseHelper.Image_ID_PK +" =? ",ID);
            }
            return remove;
        }else {
            // work with custom database
            SQLiteDatabase sqLiteDatabase=dbInfo.getWritableDatabase();
            // remove according to id
            String Id=String.valueOf(id);
            String[] ID={Id};
            // if the type is category then remove the category and all of its contents...
            if(type.equals("Category")){
                remove  = sqLiteDatabase.delete(DBInfo.Categories_Table, DBInfo.Category_ID_PK +" =? ",ID);
                remove *= sqLiteDatabase.delete(DBInfo.Images_Table, DBInfo.Image_Category_FK +" =? ",ID);
            }else {
                // remove only the image
                remove = sqLiteDatabase.delete(DBInfo.Images_Table, DBInfo.Image_ID_PK +" =? ",ID);
            }
            return remove;
        }
    }
    // Create the custom database (create the database as empty database )
    static class DBInfo extends SQLiteOpenHelper {
        //Database name
        private final static String DB_name="CANTalkDB.db";
        //Database version
        private final static int version=7;
        //Tables names
        private final static String Images_Table="Images";
        private final static String Categories_Table="Categories";

        // Table Images columns
        private final static String Image_ID_PK="ID";
        private final static String Image_Category_FK="Cat_ID";
        private final static String Image_File="ImageFile";
        private final static String Image_label="label";

        // Table Categories Columns
        private final static String Category_ID_PK="ID";
        private final static String Category_Parent_ID="Parent_ID";
        private final static String Category_label="label";
        private final static String Category_Image_File="image_ID";

        // type Image or Category ..
        private final static String type="type";
        //Create Table Categories query
        private final static String Create_Table_Categories =
                "CREATE TABLE "+ Categories_Table + " ("
                        + Category_ID_PK +" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, "
                        + Category_Parent_ID +" INTEGER NOT NULL, "
                        + Category_Image_File +" BLOB, "
                        + Category_label +" TEXT, "
                        + type+" TEXT );";

        //Create Table Images query
        private final static String Create_Table_Images =
                "CREATE TABLE "+ Images_Table + " ("
                        + Image_ID_PK +" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, "
                        + Image_Category_FK +" INTEGER NOT NULL, "
                        + Image_File +" BLOB, "
                        + Image_label +" TEXT, "
                        + type+" TEXT );";


        // Drop Tables
        private final static String Drop_Table_Images       ="DROP TABLE IF EXISTS "+Images_Table;
        private final static String Drop_Table_Categories   ="DROP TABLE IF EXISTS "+Categories_Table;
        private Context context;

        public DBInfo(Context context) {
            super(context, DB_name, null, version);
            this.context=context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                Toast.makeText(context,"onCreate DONE ,,, ",Toast.LENGTH_LONG).show();
                db.execSQL(Create_Table_Categories);
                db.execSQL(Create_Table_Images);

            } catch (SQLException e) {
                Toast.makeText(context,"OnCreate Error "+e,Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                Toast.makeText(context,"onUpgrade",Toast.LENGTH_LONG).show();
                db.execSQL(Drop_Table_Images);
                db.execSQL(Drop_Table_Categories);

                onCreate(db);
            } catch (SQLException e) {
                Toast.makeText(context,"onUpgrade Error "+e,Toast.LENGTH_LONG).show();
            }
        }
    }
    // create default database (copy database from assets folder to the empty one in the system folder)
    static class DataBaseHelper extends SQLiteOpenHelper {

        //The Android's default system path of your application database.
        private static String DB_PATH = "/data/data/ye.com.ebra.cantalk/databases/";
        // DB name
        private static String DB_NAME = "DefaultDatabase_CANTalk.db";
        //
        private final static int version=5;
        //Tables names
        private final static String Images_Table="Images";
        private final static String Categories_Table="Categories";

        // Table Images columns
        private final static String Image_ID_PK="ID";
        private final static String Image_Category_FK="Cat_ID";
        private final static String Image_File="ImageFile";
        private final static String Image_label="label";

        // Table Categories Columns
        private final static String Category_ID_PK="ID";
        private final static String Category_Parent_ID="Parent_ID";
        private final static String Category_label="label";
        private final static String Category_Image_File="image_ID";

        private SQLiteDatabase myDataBase;
        private final Context myContext;

         // Constructor
        public DataBaseHelper(Context context) {
            super(context, DB_NAME, null, version);
            this.myContext = context;
        }

        /*
         * Creates a empty database on the system and rewrites it with your own database.
         *
         */
        public void createDataBase() {
            boolean dbExist = checkDataBase();
            if(dbExist){
                //do nothing - database already exist
            }else{
                //By calling this method and empty database will be created into the default system path
                //of your application so we are gonna be able to overwrite that database with our database.
                this.getReadableDatabase();
                try {
                    copyDataBase();
                } catch (IOException e) {

                    throw new Error("Error copying database");
                }
            }

        }
         /*
         * Check if the database already exist to avoid re-copying the file each time you open the application.
         * @return true if it exists, false if it doesn't
         */
        private boolean checkDataBase(){
            SQLiteDatabase checkDB = null;
            try{
                String myPath = DB_PATH + DB_NAME;
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

            }catch(SQLiteException e){
                //database does't exist yet.
            }if(checkDB != null){
                checkDB.close();
            }
            return checkDB != null;
        }

         /*
         * Copies database from your local assets-folder to the just created empty database in the
         * system folder, from where it can be accessed and handled.
         * This is done by transferring bytestream.
         */
        private void copyDataBase() throws IOException {

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

        void openDataBase() throws SQLException {
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

        @Override
        public void onCreate(SQLiteDatabase db) {
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}