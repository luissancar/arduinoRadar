package com.example.arduinosensors;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
  
public class MainActivity extends Activity {
    
 Handler bluetoothIn;

  final int handlerState = 0;        				 //used to identify handler message
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private StringBuilder recDataString = new StringBuilder();

  private ConnectedThread mConnectedThread;
    
  // SPP UUID service - this should work for most devices
  private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  
  // String for MAC address
  private static String address;

@Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  
    setContentView(R.layout.activity_main);
    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    RelativeLayout layout1 = (RelativeLayout) findViewById(R.id.Layout1);
    Lienzo fondo = new Lienzo(this);
    layout1.addView(fondo);

    //Link the buttons and textViews to respective views



    bluetoothIn = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == handlerState) {										//if message is what we want
            	String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                recDataString.append(readMessage);      								//keep appending to string until ~
                int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                if (endOfLineIndex > 0) {                                           // make sure there data before ~
                    String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                    //txtString.setText("Data Received = " + dataInPrint);
                    int dataLength = dataInPrint.length();							//get length of data received
                  //  txtStringLength.setText("String Length = " + String.valueOf(dataLength));
                    
                    if (recDataString.charAt(0) == '#')								//if it starts with # we know it is what we are looking for
                    {
                    	//String sensor0 = recDataString.substring(1, 5);             //get sensor value from string between indices 1-5
                    	//String sensor1 = recDataString.substring(6, 10);            //same again...
                        String sensor0 = recDataString.substring(1, recDataString.indexOf("+"));
                        String sensor1 = recDataString.substring(recDataString.indexOf("+")+1, recDataString.indexOf("-"));
                        String sensor2 = "g"; //recDataString.substring(11, 15);
                    	String sensor3 = "h";//recDataString.substring(16, 20);

                  //  	sensorView0.setText(" Sensor 0 Voltage = " + sensor0 + "cms");	//update the textviews with sensor values
                    //	sensorView1.setText(" Sensor 1 Voltage = " + sensor1 + "grados");
                    //	sensorView2.setText(" Sensor 2 Voltage = " + sensor2 + "V");
                    //	sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");
                    }
                    recDataString.delete(0, recDataString.length()); 					//clear all string data 
                   // strIncom =" ";
                    dataInPrint = " ";
                }            
            }
        }
    };
      
    btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
    checkBTState();	
    

  // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED


  }

   
  private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
      
      return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
      //creates secure outgoing connecetion with BT device using UUID
  }
    
  @Override
  public void onResume() {
    super.onResume();
    
    //Get MAC address from DeviceListActivity via intent
    Intent intent = getIntent();
    
    //Get the MAC address from the DeviceListActivty via EXTRA
    address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

    //create device and set the MAC address
    BluetoothDevice device = btAdapter.getRemoteDevice(address);
     
    try {
        btSocket = createBluetoothSocket(device);
    } catch (IOException e) {
    	Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
    }  
    // Establish the Bluetooth socket connection.
    try 
    {
      btSocket.connect();
    } catch (IOException e) {
      try 
      {
        btSocket.close();
      } catch (IOException e2) 
      {
    	//insert code to deal with this 
      }
    } 
    mConnectedThread = new ConnectedThread(btSocket);
    mConnectedThread.start();
    
    //I send a character when resuming.beginning transmission to check device is connected
    //If it is not an exception will be thrown in the write method and finish() will be called
    mConnectedThread.write("x");
  }
  
  @Override
  public void onPause() 
  {
    super.onPause();
    try
    {
    //Don't leave Bluetooth sockets open when leaving activity
      btSocket.close();
    } catch (IOException e2) {
    	//insert code to deal with this 
    }
  }

 //Checks that the Android device Bluetooth is available and prompts to be turned on if off 
  private void checkBTState() {
 
    if(btAdapter==null) { 
    	Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
    } else {
      if (btAdapter.isEnabled()) {
      } else {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
      }
    }
  }
  
  //create new class for connect thread
  private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
      
        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

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
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget(); 
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {  
            	//if you cannot write, close the application
            	Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
            	finish();
            	
              }
        	}
    	}



    	///////Lienzo
        class Lienzo extends View {
            float f2=0;
            public Lienzo(Context context) {
                super(context);

            }

            protected void onDraw(Canvas canvas) {
                canvas.drawRGB(0, 0, 0);
                int ancho = canvas.getWidth();
                int alto = canvas.getHeight();
                int mitadAncho=(int)ancho/2;
                int mitadAlto=(int)alto/2;
                Paint pincel1 = new Paint();
                //pincel1.setARGB(0, 255, 0, 0);
                pincel1.setColor(Color.GREEN);
                pincel1.setStyle(Paint.Style.STROKE);

                int radio=0;
                // circulos
                for (int i=50; i< mitadAlto; i=i+100)
                {
                    canvas.drawCircle(mitadAncho,mitadAlto,  i ,pincel1);
                    radio=i;
                }
/*
            for (float f=0.0f; f<Math.PI*2; f=f+0.2f) {
                int x2=new Double(mitadAncho+radio*Math.cos(f)).intValue();
                int y2=new Double(mitadAlto+radio*Math.sin(f)).intValue();
                canvas.drawLine(mitadAncho,mitadAlto,x2,y2,pincel1);
            }*/
                //canvas.drawLine(mitadAncho,mitadAlto,mitadAncho,10,pincel1);



                pincel1.setColor(Color.RED);
                pincel1.setStyle(Paint.Style.FILL_AND_STROKE);
                int x2=new Double(mitadAncho+radio*Math.cos(f2)).intValue();
                int y2=new Double(mitadAlto+radio*Math.sin(f2)).intValue();
                canvas.drawLine(mitadAncho,mitadAlto,x2,y2,pincel1);

                f2=f2+0.1f;
                if (f2>Math.PI*2)
                    f2=0;
                invalidate();
            }
        }




    //////





}
    
