package com.onboardstats;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by Programar on 04/07/2017.
 */
public class BluetoothAdmin extends AppCompatActivity {
    private BluetoothAdapter ba;
    private ArrayAdapter pairedDevicesArrayAdapter;

    private static String address;
    DBAdapter db;

    //FECHA Y HORA
    private java.util.Date utilDate;
    private long lnMilisegundos;
    private java.sql.Date sqlDate;
    private java.sql.Time sqlTime;

    Button bt;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
    }
    public void onResume(){
        super.onResume();

        //Valor nulo para que cada vez que entre tenga que seleccionar el dispositivo
        address=null;

        //Inicializar array adapter
        pairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_name);

        //Preparar la ListView
        ListView pairedListView = (ListView) findViewById(R.id.listBluetooth);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        //Obtener el adaptador Bluetooth del dispositivo
        ba = BluetoothAdapter.getDefaultAdapter();

        if(ba!=null){
            //Si tiene adaptador pero no está activado, se dirige a la pantalla de activación de Bluetooth
            if(!ba.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
            //Obtener el conjunto de dispositivos a los que está emparejado
            Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();

            //Inlcuirlos en el array
            if(pairedDevices.size() > 0){
                for(BluetoothDevice device : pairedDevices){
                    pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }else{
                Toast.makeText(getApplicationContext(), R.string.bt_admin1, Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), R.string.bt_admin2, Toast.LENGTH_LONG).show();

        }

    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
        // Obtener la dirección MAC del dispositivo, la cual está en los últimos 17 caracteres de la View
        String info = ((TextView) v).getText().toString();
        address = info.substring(info.length() - 17);
        Toast.makeText(getApplicationContext(), getString(R.string.bt_admin3)+address+getString(R.string.bt_admin4), Toast.LENGTH_SHORT).show();
        }
    };

    public void onClickSend(View v) {
        switch (v.getId()) {
            case R.id.buttonSend:
                db = new DBAdapter(this);
                db.openRead();
                int cont = db.getRueda().getCount();
                db.close();
                if(address==null) {
                    Toast.makeText(getApplicationContext(), R.string.bt_admin5, Toast.LENGTH_SHORT).show();
                }else if(cont==0){
                    Toast.makeText(getApplicationContext(), R.string.bt_admin7, Toast.LENGTH_SHORT).show();
                }else{
                    utilDate = new java.util.Date(); //fecha actual
                    lnMilisegundos = utilDate.getTime();
                    sqlDate = new java.sql.Date(lnMilisegundos);
                    sqlTime = new java.sql.Time(lnMilisegundos);
                    db.openWrite();
                    long res = db.insertAuxiliar(sqlDate.toString(), sqlTime.toString(), address);
                    //System.out.println("RES: "+res);
                    db.close();
                    finish();
                }
                break;
        }
    }
}
