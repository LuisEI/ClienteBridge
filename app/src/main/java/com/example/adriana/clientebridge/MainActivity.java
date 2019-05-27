package com.example.adriana.clientebridge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    /**
     * Controles
     * */
    private Context context = this;
    private NumberPicker npLimitLow;
    private NumberPicker npLimitHigh;
    private Button limits;
    private Button meas;
    private TextView txtMeas;


    /**
     * Puerto
     * */
    private static final int SERVERPORT = 5000;
    /**
     * HOST
     * */
    private static final String ADDRESS = "192.168.1.16";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        npLimitLow = findViewById(R.id.LimitLow);
        npLimitHigh = findViewById(R.id.LimitHigh);
        limits = findViewById(R.id.btnLimit);
        meas = findViewById(R.id.btnMeas);
        txtMeas = findViewById(R.id.txtMeas);

        npLimitLow.setMinValue(0);
        npLimitLow.setMaxValue(200);

        npLimitHigh.setMinValue(0);
        npLimitHigh.setMaxValue(200);

        npLimitLow.setValue(15);
        npLimitHigh.setValue(25);

        limits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyATaskCliente myATaskYW = new MyATaskCliente();
                myATaskYW.execute(0,npLimitLow.getValue(),npLimitHigh.getValue());
            }
        });

        meas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyATaskCliente myATaskYW = new MyATaskCliente();
                myATaskYW.execute(1,0);
            }
        });

    }//end:onCreate


    /**
     * Clase para interactuar con el servidor
     * */
    class MyATaskCliente extends AsyncTask<Integer,Void,String>{

        /**
         * Ventana que bloqueara la pantalla del movil hasta recibir respuesta del servidor
         * */
        ProgressDialog progressDialog;

        /**
         * muestra una ventana emergente
         * */
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setTitle("Connecting to server");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }

        /**
         * Se conecta al servidor y trata resultado
         * */
        @Override
        protected String doInBackground(Integer... values){

            try {
                //Se conecta al servidor
                InetAddress serverAddr = InetAddress.getByName(ADDRESS);
                Log.i("I/TCP Client", "Connecting...");
                Socket socket = new Socket(serverAddr, SERVERPORT);
                Log.i("I/TCP Client", "Connected to server");

                //envia peticion de cliente
                Log.i("I/TCP Client", "Send data to server");
                PrintStream output = new PrintStream(socket.getOutputStream());

                if(values[0] == 1){
                    String request = String.valueOf(values[0]) +","+ String.valueOf(values[1]);
                    output.println(request);
                }else{
                    String request = String.valueOf(values[0]) +","+ String.valueOf(values[1]) +","+ String.valueOf(values[2]);
                    output.println(request);
                }

                //recibe respuesta del servidor y formatea a String
                Log.i("I/TCP Client", "Received data to server");
                InputStream stream = socket.getInputStream();
                byte[] lenBytes = new byte[256];
                stream.read(lenBytes,0,256);
                String received = new String(lenBytes,"UTF-8").trim();
                Log.i("I/TCP Client", "Received " + received);
                Log.i("I/TCP Client", "");
                //cierra conexion
                socket.close();
                return received;

            }catch (UnknownHostException ex) {
                Log.e("E/TCP Client", "" + ex.getMessage());
                return ex.getMessage();
            } catch (IOException ex) {
                Log.e("E/TCP Client", "" + ex.getMessage());
                return ex.getMessage();
            }
        }

        /**
         * Oculta ventana emergente y muestra resultado en pantalla
         * */
        @Override
        protected void onPostExecute(String value){
            progressDialog.dismiss();
            txtMeas.setText(value);
        }
    }
}
