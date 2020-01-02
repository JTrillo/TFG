package com.onboardstats;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Handler bluetoothIn;
    final int handlerState = 0;             //used to identify handler message

    private BluetoothAdapter ba;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    DBAdapter db;
    Button b1,b2,b3,b4,b5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        db = new DBAdapter(this);
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);  //Vamos aumentando el buffer
                    System.out.println(recDataString);
                    int endOfLineIndex = recDataString.indexOf("*"); // Determina el final de mensaje
                    if (endOfLineIndex > 0) {   //Si encuentra el caracter, empezamos con la inserción el DB
                        System.out.println(recDataString);
                        if (recDataString.charAt(0) == '#')        //if it starts with # we know it is what we are looking for
                        {
                            StringTokenizer st = new StringTokenizer(recDataString.substring(1, endOfLineIndex),"+");
                            String duracion = st.nextToken();
                            String distancia = st.nextToken();
                            String velocidad = st.nextToken();
                            db.openRead();
                            Cursor c = db.getAuxiliar();
                            c.moveToFirst();
                            String fecha = c.getString(1);
                            String hora = c.getString(2);
                            db.close();
                            db.openWrite();
                            if(db.insertRoute(fecha,hora,duracion,velocidad,distancia)==-1){
                                Toast.makeText(getApplicationContext(), R.string.save_route_error, Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getApplicationContext(), R.string.save_route, Toast.LENGTH_SHORT).show();
                            }
                            db.deleteAuxiliar();
                            db.close();
                            b1 = (Button) findViewById(R.id.button_start);
                            b2 = (Button) findViewById(R.id.button_last_route);
                            b3 = (Button) findViewById(R.id.button_routes);
                            b4 = (Button) findViewById(R.id.button_change_radio);
                            b5 = (Button) findViewById(R.id.button_help);
                            b2.setEnabled(true);
                            b3.setEnabled(true);
                            b4.setEnabled(true);
                            b5.setEnabled(true);
                            b1.setText(R.string.start);
                        }
                        recDataString.delete(0, recDataString.length()); //Limpiamos el buffer
                        try {
                            btSocket.close(); //Y cerramos el socket
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    @Override
    public void onResume(){
        super.onResume();

        db.openRead();
        int cont = db.getRueda().getCount();
        int cont2 = db.getAuxiliar().getCount();
        db.close();

        ba = BluetoothAdapter.getDefaultAdapter();
        if(cont==0){
            final Context ctx = this;
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.welcome));
            dialog.setMessage(getString(R.string.main_dialog));
            dialog.setPositiveButton(R.string.continuar, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    Intent intent = new Intent(ctx, EditWheel.class);
                    startActivity(intent);
                }
            });
            dialog.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    Toast.makeText(ctx, R.string.ToastMain, Toast.LENGTH_LONG).show();
                }
            });
            dialog.show();
        }else if(cont2!=0){
            boolean conectar = true;
            try{
                db.openRead();
                Cursor c = db.getAuxiliar();
                c.moveToFirst();
                System.out.println(c.getString(1)+" "+c.getString(2)+" "+c.getString(3));
                BluetoothDevice device = ba.getRemoteDevice(c.getString(3));
                db.close();
                btSocket = createBluetoothSocket(device);
            }catch(IOException e){
                conectar=false;
                Toast.makeText(getApplicationContext(), R.string.socket_fail, Toast.LENGTH_LONG).show();
            }
            //Estableciendo conexión.
            if(conectar) {
                try {
                    btSocket.connect();
                    Toast.makeText(getApplicationContext(), R.string.connection_ok, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    try {
                        btSocket.close();
                    } catch (IOException e2) {
                        //insert code to deal with this
                    }
                }
                //Envío de la longitud
                db = new DBAdapter(this);
                db.openRead();
                if (cont != 0) { //si la tabla no está vacia, enviamos la longitud
                    Cursor cursor = db.getRueda();
                    cursor.moveToFirst();
                    //System.out.println("HOLA");
                    mConnectedThread = new ConnectedThread(btSocket);
                    mConnectedThread.start();
                    int res = mConnectedThread.write(cursor.getString(1));
                    if (res==0) {
                        //Actualización de la vista si el operación del envío de la longitud se completa correctamente
                        b1 = (Button) findViewById(R.id.button_start);
                        b2 = (Button) findViewById(R.id.button_last_route);
                        b3 = (Button) findViewById(R.id.button_routes);
                        b4 = (Button) findViewById(R.id.button_change_radio);
                        b5 = (Button) findViewById(R.id.button_help);
                        b2.setEnabled(false);
                        b3.setEnabled(false);
                        b4.setEnabled(false);
                        b5.setEnabled(false);
                        b1.setText(R.string.finish);
                    }else{
                        try {
                            btSocket.close();
                        } catch (IOException e) {
                            //poner algo aquí
                        }
                    }
                }
                db.close();
            }
        }
    }

    public void onClick(View v){
        Intent intent;
        b1 = (Button) findViewById(R.id.button_start);
        b2 = (Button) findViewById(R.id.button_last_route);
        b3 = (Button) findViewById(R.id.button_routes);
        b4 = (Button) findViewById(R.id.button_change_radio);
        b5 = (Button) findViewById(R.id.button_help);
        switch(v.getId()){
            case R.id.button_start:
                if(b1.getText().equals("COMENZAR") || b1.getText().equals("START")) {
                    intent = new Intent(this, BluetoothAdmin.class);
                    startActivity(intent);
                }else{
                    //Finalizar conexión
                    db.openWrite();
                    System.out.println(db.deleteAuxiliar());
                    db.close();
                    b1.setText(R.string.start);
                    b2.setEnabled(true);
                    b3.setEnabled(true);
                    b4.setEnabled(true);
                    b5.setEnabled(true);
                }
                break;
            case R.id.button_last_route:
                intent = new Intent(this, RouteDetail.class);
                intent.putExtra("id", "last");
                startActivity(intent);
                break;
            case R.id.button_routes:
                intent = new Intent(this, Routes.class);
                startActivity(intent);
                break;
            case R.id.button_change_radio:
                intent = new Intent(this, EditWheel.class);
                startActivity(intent);
                break;
            case R.id.button_help:
                intent = new Intent(this, Help.class);
                startActivity(intent);
                break;
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    public void onPause()
    {
        super.onPause();
        try{
            //No dejar el socket abierto cuando se pausa la actividad
            btSocket.close();
        }catch (IOException e) {

        }catch (NullPointerException e2){

        }
    }

    //Clase interna para gestionar la conexión
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private BluetoothSocket bs;
        private DBAdapter dba;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            bs = socket;
            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);         //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //método de escritura, si retorna -1, hubo un fallo en la operación de escritura
        public int write(String input) {
            int res = -1;
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                res = 0;
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), R.string.fallo_conexion, Toast.LENGTH_LONG).show();
                dba = new DBAdapter(getBaseContext());
                dba.openWrite();
                dba.deleteAuxiliar();
                dba.close();
                try {
                    bs.close();
                } catch (IOException e1) {

                }
            }
            return res;
        }
    }
}
