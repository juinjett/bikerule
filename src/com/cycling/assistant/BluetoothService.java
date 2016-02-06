package com.cycling.assistant;

import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BluetoothService extends Service {
	
    private static final String TAG = "MyService";
    
    //*************************************************

	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_CONNECTING = 1;
	public static final int STATE_CONNECTED = 2;
	private boolean isBLEConnected = false;             //¿∂—¿ «∑Ò¡¨Ω”
    //private static String BLE_Address_L = "7C:66:9D:9A:6C:71"; 
	//private static String BLE_Address_L = "D0:39:72:D9:F4:F7"; 
	//private static String BLE_Address_L = "D0:39:72:D9:AD:04"; 
	//private static String BLE_Address_L = "D0:39:72:D9:BA:34";
	private static String BLE_Address_L = "D0:39:72:D9:B0:9E"; 
    private static String BLE_L_UUID_SER_String = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static String BLE_L_UUID_CHR_String = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static UUID BLE_L_UUID_SER = UUID.fromString(BLE_L_UUID_SER_String);
    public final static UUID BLE_L_UUID_CHR = UUID.fromString(BLE_L_UUID_CHR_String);
   
    //private static String BLE_Address_R = "7C:66:9D:9A:71:F5"; 
    //private static String BLE_Address_R = "D0:39:72:D9:CA:99"; 
	//private static String BLE_Address_R = "D0:39:72:D9:B8:E5"; 
    //private static String BLE_Address_R = "D0:39:72:D4:0B:3E"; 
    private static String BLE_Address_R = "D0:39:72:D4:0B:3E"; 
    private static String BLE_R_UUID_SER_String = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static String BLE_R_UUID_CHR_String = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public final static UUID BLE_R_UUID_SER = UUID.fromString(BLE_R_UUID_SER_String);
    public final static UUID BLE_R_UUID_CHR = UUID.fromString(BLE_R_UUID_CHR_String);
    
    private BluetoothGattCharacteristic mGattCharacteristics_L;
    private BluetoothGattCharacteristic mGattCharacteristics_R;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt_L;
    private BluetoothGatt mBluetoothGatt_R;
    BluetoothDevice device_L = null;
    BluetoothDevice device_R = null;
    
    private short asyncTAG = 0;
    private boolean serviceDiscoverd = false;
    
    //*************************************************
	
    @Override
    public void onCreate() {
    	//
    	initBLE();
    	
        IntentFilter filter = new IntentFilter();
        filter.addAction("turning command");
        this.registerReceiver(commandReceiver, filter);       
    }
	
	@Override
	public void onDestroy() {
		// 
        if (mBluetoothAdapter == null || mBluetoothGatt_L == null || mBluetoothGatt_R == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        
        extinguishLeftLight();
        extinguishRightLight();
        
        mBluetoothGatt_L.disconnect();
        mBluetoothGatt_R.disconnect();
        mBluetoothGatt_L.close();
        mBluetoothGatt_R.close();
        mBluetoothGatt_L = null;
        mBluetoothGatt_R = null;
		Log.e("command", "disconnect bluetooth");
		this.unregisterReceiver(commandReceiver);

		super.onDestroy();

	}
    

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private BluetoothGattCallback mGattCallback_L = new BluetoothGattCallback() {
        @Override  
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {  
            super.onConnectionStateChange(gatt, status, newState);  
            Log.v(TAG, "Connection State Changed: " +   
                (newState == BluetoothProfile.STATE_CONNECTED ?   
                    "Connected" : "Disconnected"));  
            if (newState == BluetoothProfile.STATE_CONNECTED) {    
                gatt.discoverServices();  
            } else {  
                //
            }  
        }  
      
        @Override  
        public void onServicesDiscovered(BluetoothGatt gatt,   
            int status) {  
            if(status == BluetoothGatt.GATT_SUCCESS) {  
                Log.v(TAG, "onServicesDiscovered: " + status);  
            }  
        }  	
        
    };
    
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private BluetoothGattCallback mGattCallback_R = new BluetoothGattCallback() {
        @Override  
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {  
            super.onConnectionStateChange(gatt, status, newState);  
            Log.v(TAG, "Connection State Changed: " +   
                (newState == BluetoothProfile.STATE_CONNECTED ?   
                    "Connected" : "Disconnected"));  
            if (newState == BluetoothProfile.STATE_CONNECTED) {    
                gatt.discoverServices();  
            } else {  
                //
            }  
        }  
      
        @Override  
        public void onServicesDiscovered(BluetoothGatt gatt,   
            int status) {  
            if(status == BluetoothGatt.GATT_SUCCESS) {  
                Log.v(TAG, "onServicesDiscovered: " + status);  
            }  
        }  	
        
    };
    
	public boolean sendTurnLeft() {
		
        BluetoothGattService gattService_L = mBluetoothGatt_L.getService(BLE_L_UUID_SER);
        
        if (gattService_L == null) {
        	//gattService_L = mBluetoothGatt_L.getService(BLE_L_UUID_SER);
        	Log.e("fuck", "you");
        	return false;
        }
        
        final BluetoothGattCharacteristic gattCharacteristic_L = gattService_L.getCharacteristic(BLE_L_UUID_CHR);
        
        mGattCharacteristics_L = gattCharacteristic_L;
		
        mBluetoothGatt_L.setCharacteristicNotification(mGattCharacteristics_L, false);
        //mGattCharacteristics_L.setValue("AT+PIO21");
        mGattCharacteristics_L.setValue("SET");
    	mBluetoothGatt_L.writeCharacteristic(mGattCharacteristics_L);
    	
    	return true;
	}
	
	public boolean sendTurnRight() {
    	
        BluetoothGattService gattService_R = mBluetoothGatt_R.getService(BLE_R_UUID_SER);
        
        if (gattService_R == null) {
        	//gattService_R = mBluetoothGatt_R.getService(BLE_R_UUID_SER);
        	Log.e("fuck", "youtubes");
        	return false;
        }
        
        final BluetoothGattCharacteristic gattCharacteristic_R = gattService_R.getCharacteristic(BLE_R_UUID_CHR);
        
        mGattCharacteristics_R = gattCharacteristic_R;
		
        mBluetoothGatt_R.setCharacteristicNotification(mGattCharacteristics_R, false);
        //mGattCharacteristics_R.setValue("AT+PIO21");
        mGattCharacteristics_R.setValue("SET");
    	mBluetoothGatt_R.writeCharacteristic(mGattCharacteristics_R);
    	
    	return true;
	}
	
	public void sendTurnOff() {
		extinguishLeftLight(); 
		extinguishRightLight();
	}
	
	public boolean extinguishLeftLight() {
		
        BluetoothGattService gattService_L = mBluetoothGatt_L.getService(BLE_L_UUID_SER);
        
        if (gattService_L == null) {
        	//gattService_L = mBluetoothGatt_L.getService(BLE_L_UUID_SER);
        	Log.e("fuck", "you");
        	return false;
        }
        
        final BluetoothGattCharacteristic gattCharacteristic_L = gattService_L.getCharacteristic(BLE_L_UUID_CHR);
        
        mGattCharacteristics_L = gattCharacteristic_L;
		
		mBluetoothGatt_L.setCharacteristicNotification(mGattCharacteristics_L, false);
		//mGattCharacteristics_L.setValue("AT+PIO20");
		mGattCharacteristics_L.setValue("CLR");
    	mBluetoothGatt_L.writeCharacteristic(mGattCharacteristics_L);
    	Log.e("extinguish", "L");
    	
    	return true;
	}
	
	public boolean extinguishRightLight() {
		
        BluetoothGattService gattService_R = mBluetoothGatt_R.getService(BLE_R_UUID_SER);
        
        if (gattService_R == null) {
        	//gattService_R = mBluetoothGatt_R.getService(BLE_R_UUID_SER);
        	Log.e("fuck", "youtubes");
        	return false;
        }
        
        final BluetoothGattCharacteristic gattCharacteristic_R = gattService_R.getCharacteristic(BLE_R_UUID_CHR);
        
        mGattCharacteristics_R = gattCharacteristic_R;
		
		mBluetoothGatt_R.setCharacteristicNotification(mGattCharacteristics_R, false);
		//mGattCharacteristics_R.setValue("AT+PIO20");
		mGattCharacteristics_R.setValue("CLR");
    	mBluetoothGatt_R.writeCharacteristic(mGattCharacteristics_R);
    	Log.e("extinguish", "R");
    	
    	return true;
	}
	
    private BroadcastReceiver commandReceiver = new BroadcastReceiver(){  
    	private int cmdInfo = 0;
        @Override  
        public void onReceive(Context context, Intent intent) {  
            // TODO Auto-generated method stub 
        	if (intent.getAction().equals("turning command")) {
        		cmdInfo = intent.getIntExtra("cmd", 0);
        		if (cmdInfo == -1) {
        			sendTurnLeft();
        			extinguishRightLight();
        			Log.e("command", "turn left");
        		} else if (cmdInfo == 1){
        			sendTurnRight();
        			extinguishLeftLight();
        			Log.e("command", "turn right");
        		} else {
        			sendTurnOff();
        			Log.e("command", "turn off led");
        		}
        	}
        }  
    };  
    
	
	private void initBLE() {
		//
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                Toast.makeText(this, "No Manager", Toast.LENGTH_LONG).show();
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            Toast.makeText(this, "No Adapter", Toast.LENGTH_LONG).show();
        }

        device_L = mBluetoothAdapter.getRemoteDevice(BLE_Address_L);
        device_R = mBluetoothAdapter.getRemoteDevice(BLE_Address_R);
        
        if (device_L == null && device_R == null) {
        	Toast.makeText(this, "No Device", Toast.LENGTH_LONG).show();
        }
        
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt_L = device_L.connectGatt(this, false, mGattCallback_L);
        mBluetoothGatt_R = device_R.connectGatt(this, false, mGattCallback_R);
        
//		new ConnectBLE_L_Task().execute();
//		
//		while(asyncTAG == 0){
//			//do nothing
//		}
//		
//    	new ConnectBLE_R_Task().execute();
        
	}   
	
//	private class ConnectBLE_L_Task extends AsyncTask<String, Void, String>{
//		//
//		@Override
//		protected void onPreExecute() {
//			// TODO Auto-generated method stub
//		}
//
//		@Override
//		protected String doInBackground(String... url) {
//			// TODO Auto-generated method stub
//			String s = "";
//			mBluetoothGatt_L = device_L.connectGatt(BluetoothService.this, false, mGattCallback_L);
//			//asyncTAG = 1;
//			return s;
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			// TODO Auto-generated method stub
//		}
//	}
//	
//	private class ConnectBLE_R_Task extends AsyncTask<String, Void, String>{
//		//
//		@Override
//		protected void onPreExecute() {
//			// TODO Auto-generated method stub
//		}
//
//		@Override
//		protected String doInBackground(String... url) {
//			// TODO Auto-generated method stub
//			String s = "";
//			mBluetoothGatt_L = device_R.connectGatt(BluetoothService.this, false, mGattCallback_R);
//			asyncTAG = 1;
//			return s;
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			// TODO Auto-generated method stub
//		}
//	}
		
}