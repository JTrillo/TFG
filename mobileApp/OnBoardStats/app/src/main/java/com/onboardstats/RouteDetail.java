package com.onboardstats;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Programar on 20/05/2017.
 */
public class RouteDetail extends AppCompatActivity {
    TextView fec, hora, dur, vel, dis;
    DBAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_detail);
        db = new DBAdapter(this);
        db.openRead();
        int cont = db.getAllRoutes().getCount();
        if(cont==0){//Si no tenemos almacenada ninguna ruta
            Toast.makeText(this, R.string.ToastRouteDetail, Toast.LENGTH_SHORT).show();
        }else{
            //Los TextView a rellenar
            fec = (TextView) findViewById(R.id.textDate);
            hora = (TextView) findViewById(R.id.textHour);
            dur = (TextView) findViewById(R.id.textLength);
            vel = (TextView) findViewById(R.id.textSpeed);
            dis = (TextView) findViewById(R.id.textDistance);

            String id = getIntent().getExtras().getString("id");
            Cursor c;
            if(id.equals("last")){
                c = db.getLastRoute();

            }else{
                c = db.getRoute(id);
            }
            c.moveToFirst();
            fec.setText(c.getString(1));
            hora.setText(c.getString(2));
            dur.setText(formatoHoras(c.getString(3))+" h");
            vel.setText(c.getString(4)+" Km/h");
            dis.setText(c.getString(5)+" Km");
        }
        db.close();
    }
    public String formatoHoras(String segs){
        StringBuilder sb = new StringBuilder();
        long horas,minutos,segundos;
        long aux = Long.parseLong(segs);
        horas = aux/3600;
        if(horas<10){
            sb.append("0");
        }
        sb.append(horas);
        sb.append(":");
        minutos = (aux%3600)/60;
        if(minutos<10){
            sb.append("0");
        }
        sb.append(minutos);
        sb.append(":");
        segundos = aux%60;
        if(segundos<10){
            sb.append("0");
        }
        sb.append(segundos);
        //System.out.println("RETORNO --> "+sb.toString());
        return sb.toString();
    }
}
