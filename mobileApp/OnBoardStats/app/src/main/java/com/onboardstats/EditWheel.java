package com.onboardstats;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Programar on 21/05/2017.
 */
public class EditWheel extends AppCompatActivity {
    DBAdapter db;
    EditText et, anc, dia;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_wheel);

        et = (EditText) findViewById(R.id.editText);
        anc = (EditText) findViewById(R.id.editAnchura);
        dia = (EditText) findViewById(R.id.editDiametro);
        //Comprobamos el contenido de la tabla Rueda
        db = new DBAdapter(this);
        db.openRead();
        int cont = db.getRueda().getCount();
        if(cont!=0){ //si no está vacia, ponemos en el EditText la longitud que tenemos guardada
            Cursor cursor = db.getRueda();
            cursor.moveToFirst();
            et.setText(cursor.getString(1));
        }
        db.close();
    }

    public void onClickSave(View v){
        switch(v.getId()) {
            case R.id.buttonSave:
                saveLong(et.getText().toString());
                break;
            case R.id.buttonConvertir:
                //Ponemos el resultado de la conversión en el EditText de la longitud
                int conversion = ISO_to_long(anc.getText().toString(), dia.getText().toString());
                if(conversion!=-1) {
                    et.setText(new String(""+conversion));
                }
                break;
        }
    }
    public void saveLong(String longitud){ //Guarda la longitud de la rueda
        try{
            Integer.parseInt(longitud); //Para comprobar formato correcto
            db = new DBAdapter(this);
            db.openRead();
            int cont = db.getRueda().getCount();
            if(cont==0){//Si todavia no he guardado el radio, hago un insert
                if(db.insertRueda(longitud)==-1){
                    Toast.makeText(getApplicationContext(), R.string.edit_wheel1, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.edit_wheel2, Toast.LENGTH_SHORT).show();
                }
            }else{//Si ya tengo un radio, hago un update
                if(db.updateRueda(longitud)==1){
                    Toast.makeText(getApplicationContext(), R.string.edit_wheel3, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.edit_wheel4, Toast.LENGTH_LONG).show();
                }
            }
        }catch(NumberFormatException e){
            Toast.makeText(getApplicationContext(), R.string.error_save, Toast.LENGTH_LONG).show();
        }
    }


    public int ISO_to_long(String anchura, String diametro){  //Convierte el marcado ISO a longitud de la rueda
        int resultado;
        try{
            int anchu = Integer.parseInt(anchura);  //Para comprobar formato correcto
            int diame = Integer.parseInt(diametro); //Para comprobar formato correcto

            //long = (2*anchura neumatico + diametro llanta) * 3.1416
            double longitud = (2*anchu + diame) * 3.1416;
            resultado = (int) longitud;
        }catch(NumberFormatException e){
            Toast.makeText(getApplicationContext(), R.string.edit_wheel5, Toast.LENGTH_LONG).show();
            resultado = -1;
        }
        return resultado;
    }
}
