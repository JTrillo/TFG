package com.onboardstats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Programar on 20/05/2017.
 */
public class DBAdapter {

    //Columnas de la tabla Rutas
    public static final String KEY_ROWID = "_id";
    public static final String KEY_FECHA = "fecha";
    public static final String KEY_HORA = "hora";
    public static final String KEY_DURACION = "duracion";
    public static final String KEY_VMEDIA = "vmedia";
    public static final String KEY_DISTANCIA = "distancia";

    //Columna de la tabla Rueda
    public static final String KEY_LONG = "longitud";

    //Columnas de la tabla Auxiliar
    public static final String KEY_AUX1 = "aux1";
    public static final String KEY_AUX2 = "aux2";
    public static final String KEY_DIR = "direccion";

    private static final String TAG = "BDAdapter";

    private static final String DATABASE_NAME = "OnBoardDB";
    private static final String DATABASE_TABLE = "Rutas";
    private static final String DATABASE_TABLE2 = "Rueda";
    private static final String DATABASE_TABLE3 = "Auxiliar";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_TABLE_CREATE =
        "create table "+DATABASE_TABLE+
            "("+KEY_ROWID+" integer primary key autoincrement, "
            +KEY_FECHA+" text not null, "
            +KEY_HORA+" text not null, "
            +KEY_DURACION+" text not null, "
            +KEY_VMEDIA+" real not null, "
            +KEY_DISTANCIA+" real not null);";
    private static final String DATABASE_TABLE2_CREATE =
        "create table "+DATABASE_TABLE2+
            "("+KEY_ROWID+" integer primary key autoincrement, "
            +KEY_LONG+" integer not null);";
    private static final String DATABASE_TABLE3_CREATE =
        "create table "+DATABASE_TABLE3+
            "("+KEY_ROWID+" integer primary key, "
            +KEY_AUX1+" text not null, "
            +KEY_AUX2+" text not null, "
            +KEY_DIR+" text not null);";

    private final Context context;


    private DataBaseHelper BDHelper;
    private SQLiteDatabase bsSql;
    private String[] allRows = new String[] {KEY_ROWID,KEY_FECHA,KEY_HORA,KEY_DURACION,KEY_VMEDIA,KEY_DISTANCIA};
    private String[] allRows2 = new String[] {KEY_ROWID,KEY_LONG};
    private String[] allRows3 = new String[] {KEY_ROWID,KEY_AUX1,KEY_AUX2,KEY_DIR};

    public DBAdapter(Context ctx){
        this.context = ctx;
        BDHelper = new DataBaseHelper(context);
    }

    public DBAdapter openWrite() throws SQLException {
        bsSql = BDHelper.getWritableDatabase();
        return this;
    }

    public DBAdapter openRead() throws SQLException{
        bsSql = BDHelper.getReadableDatabase();
        return this;
    }

    public void close(){
        BDHelper.close();
    }

    //Métodos para la gestión de la tabla Rutas
    public long insertRoute(String fec, String hora, String dur, String vel, String dis){
        ContentValues cv = new ContentValues();
        cv.put(KEY_FECHA, fec);
        cv.put(KEY_HORA, hora);
        cv.put(KEY_DURACION, dur);
        cv.put(KEY_VMEDIA, vel);
        cv.put(KEY_DISTANCIA, dis);
        return bsSql.insert(DATABASE_TABLE, null, cv);
    }

    public boolean deleteRoute(String id){
        return bsSql.delete(DATABASE_TABLE, KEY_ROWID+" = "+id, null)>0;
    }
    public Cursor getAllRoutes(){
        return bsSql.query(DATABASE_TABLE, allRows,null,null,null,null,null);
    }
    public Cursor getLastRoute(){
        return bsSql.rawQuery("SELECT * FROM "+DATABASE_TABLE+" ORDER BY "+KEY_ROWID+" DESC LIMIT 1", null);
    }

    public Cursor getRoute(String id){
        return bsSql.query(true, DATABASE_TABLE, allRows,KEY_ROWID+" = "+ id,null,null,null,null,null);
    }

    //Métodos para la gestión de la tabla Radio
    public long insertRueda(String longitud){
        ContentValues cv = new ContentValues();
        cv.put(KEY_LONG, longitud);
        return bsSql.insert(DATABASE_TABLE2, null, cv);
    }

    public int updateRueda(String longitud){
        ContentValues cv = new ContentValues();
        cv.put(KEY_LONG, longitud);
        return bsSql.update(DATABASE_TABLE2, cv, null, null);
    }

    public Cursor getRueda(){
        String aux = "1";
        return bsSql.query(true, DATABASE_TABLE2, allRows2,KEY_ROWID+" = "+ aux,null,null,null,null,null);
    }

    //Métodos para la gestión de la tabla Auxiliar
    public long insertAuxiliar(String aux1, String aux2, String dir){
        ContentValues cv = new ContentValues();
        cv.put(KEY_ROWID, 1);
        cv.put(KEY_AUX1, aux1);
        cv.put(KEY_AUX2, aux2);
        cv.put(KEY_DIR, dir);
        return bsSql.insert(DATABASE_TABLE3, null, cv);
    }

    public boolean deleteAuxiliar(){
        String id = "1";
        return bsSql.delete(DATABASE_TABLE3, KEY_ROWID+" = "+id, null)>0;
    }

    public Cursor getAuxiliar(){
        String aux = "1";
        return bsSql.query(true, DATABASE_TABLE3, allRows3,KEY_ROWID+" = "+ aux,null,null,null,null,null);
    }

    //**** CLASE PRIVADA ***/
    private static class DataBaseHelper extends SQLiteOpenHelper {
        DataBaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db)	{
            db.execSQL(DATABASE_TABLE_CREATE);
            db.execSQL(DATABASE_TABLE2_CREATE);
            db.execSQL(DATABASE_TABLE3_CREATE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,int newVersion){
            Log.w(TAG, "Actualizando base de datos de la version " + oldVersion
                    + " a "
                    + newVersion + ", borraremos todos los datos");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE2);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE3);
            onCreate(db);
        }
    }
}
