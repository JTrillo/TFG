package com.onboardstats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Created by Programar on 21/05/2017.
 */
public class Routes extends AppCompatActivity {
    ListView listView;
    Button ver, eliminar;
    DBAdapter db;

    private static String pos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routes);
    }
    public void onResume(){
        super.onResume();
        pos = "ninguna";
        actualizaLista(this);
    }

    public void onClickRoutes(View v){
        if(pos.equals("ninguna")){
            Toast.makeText(this, R.string.routes_str1, Toast.LENGTH_SHORT).show(); //Primero hay que seleccionar una ruta
        }else{
            Intent intent;
            switch(v.getId()){
                case R.id.buttonVer:
                    intent = new Intent(this, RouteDetail.class);
                    intent.putExtra("id", pos);
                    startActivity(intent);
                    break;
                case R.id.buttonEliminar:
                    final Context ctx = this;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage(getString(R.string.delete_dialog));
                    dialog.setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            db.openWrite();
                            db.deleteRoute(pos);
                            db.close();
                            Toast.makeText(ctx, R.string.ToastConfirmDelete, Toast.LENGTH_SHORT).show();
                            actualizaLista(ctx);
                        }
                    });
                    dialog.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            Toast.makeText(ctx, R.string.ToastCancelDelete, Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.show();
                    break;
            }
        }
    }

    public void actualizaLista(Context ctx){
        listView = (ListView) findViewById(R.id.listView);
        db = new DBAdapter(ctx);
        db.openRead();
        int cont = db.getAllRoutes().getCount();
        //System.out.println("RUTAS --> "+cont);
        if(cont==0){//Si no tenemos almacenada ninguna ruta
            ver = (Button) findViewById(R.id.buttonVer);
            eliminar = (Button) findViewById(R.id.buttonEliminar);
            ver.setEnabled(false);
            eliminar.setEnabled(false);
            Toast.makeText(this, R.string.ToastRouteDetail, Toast.LENGTH_SHORT).show();
            listView.setAdapter(null);
        }else{
            Cursor cursor = db.getAllRoutes();
            SimpleCursorAdapter adapter =
                    new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,cursor,
                            new String[] {"fecha", "hora"}, new int[] {android.R.id.text1, android.R.id.text2}, 0);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    db.openRead();
                    Cursor c = (Cursor)listView.getItemAtPosition(i);
                    //c.moveToFirst();
                    pos = c.getString(0);
                    db.close();
                    Toast.makeText(getApplicationContext(), R.string.route_slctd,Toast.LENGTH_SHORT).show();
                }
            });
        }
        db.close();
    }
}
