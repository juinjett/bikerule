package com.cycling.assistant;

import java.lang.reflect.Method; 
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View.OnClickListener; 

public class ConnectionActivity extends Activity {
        
	private LinearLayout leftHandleBar, rightHandleBar;
	private ImageButton connectBtn;
	
	private MyApp appStatus;
	
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.connection);  
          
        leftHandleBar = (LinearLayout)findViewById(R.id.leftbutton);
        leftHandleBar.setClickable(true);
        leftHandleBar.setOnClickListener(leftListener); 
          
        rightHandleBar=(LinearLayout)findViewById(R.id.rightbutton);
        rightHandleBar.setClickable(true);
        rightHandleBar.setOnClickListener(rightListener); 
        
        connectBtn = (ImageButton) findViewById(R.id.connectButton);
        connectBtn.setOnClickListener(connectListener);
    }
    
    public OnClickListener connectListener=new OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
        	Intent serviceControl = new Intent(".BluetoothService");
        	appStatus = ((MyApp)getApplicationContext());  
            boolean connect = appStatus.getConnectStatus(); 
            if (connect == false) {
 				// connect
 				if (startService(serviceControl) != null) {
 					connectBtn.setImageDrawable(getResources().getDrawable(R.drawable.disconnectbutton));
 					appStatus.setConnectStatus(true);
 				} else {
 				    Toast.makeText(getBaseContext(), "Please Check your Bikerules Indicator", Toast.LENGTH_SHORT).show();
 				}
            } else{
 				// disconnect
            	stopService(serviceControl);
 			    connectBtn.setImageDrawable(getResources().getDrawable(R.drawable.connectbutton)); 	
 			    appStatus.setConnectStatus(false);
            }
        }
 	};         
    
    public OnClickListener leftListener=new OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
 	    	Intent command = new Intent("turning command");
			int cmdInfo = -1; //left
			command.putExtra("cmd", cmdInfo);
		    sendBroadcast(command);
            //Toast.makeText(getApplicationContext(), "yes", Toast.LENGTH_SHORT).show();
        }
 	};         
    
    public OnClickListener rightListener=new OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
 	    	Intent command = new Intent("turning command");
			int cmdInfo = 1; //right
			command.putExtra("cmd", cmdInfo);
		    sendBroadcast(command);
            //Toast.makeText(getApplicationContext(), "yes", Toast.LENGTH_SHORT).show();
        }
 	};  	 	
 	
	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		setIconEnable(menu, true);  
		
        MenuItem item1 = menu.add(0, Menu.FIRST + 1, 0, "Map");  
        item1.setIcon(R.drawable.mapmenu);  
        MenuItem item2 = menu.add(0, Menu.FIRST + 2, 0, "Health Data");  
        item2.setIcon(R.drawable.healthdatamenu);     
        MenuItem item3 = menu.add(0, Menu.FIRST + 3, 0, "About");  
        item3.setIcon(R.drawable.aboutmenu);  
		return true;
	}
	
    @Override  
    public boolean onPrepareOptionsMenu(Menu menu)   
    {  
        // TODO Auto-generated method stub  
        return super.onPrepareOptionsMenu(menu);  
    }  
      
    //enable为true时，菜单添加图标有效，enable为false时无效。4.0系统默认无效  
    private void setIconEnable(Menu menu, boolean enable)  
    {  
        try   
        {  
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");  
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);  
            m.setAccessible(true);  
              
            m.invoke(menu, enable);  
              
        } catch (Exception e)   
        {  
            e.printStackTrace();  
        }  
    }  
	
	// Menu Event
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case Menu.FIRST + 1:
            // show map
			Intent intent_map = new Intent(ConnectionActivity.this ,MapDisplayActivity.class);
			startActivity(intent_map);
			break;
		case Menu.FIRST + 2:
            // show health data
			Intent intent_healthdata = new Intent(ConnectionActivity.this ,HealthDataActivity.class);
			startActivity(intent_healthdata);
			break;
		case Menu.FIRST + 3:
            // show version
			Intent intent_version = new Intent(ConnectionActivity.this ,VersionShowActivity.class);
			startActivity(intent_version);
			break;
		}
		return true;
	}
	
	// On key down
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
	        // quit this app
		    Quit();
	        return true;
	    }
	    return false;
	 }
	  
	 private void Quit() {
         new AlertDialog.Builder(this)          
         .setTitle("Quit")  
         .setMessage("Are you sure")  
         .setNegativeButton("No",  
           new DialogInterface.OnClickListener() {  		
           @Override
		  public void onClick(DialogInterface dialog, int which) {  
                       // TODO Auto-generated method stub  		                              
               }  
         })  		  
         .setPositiveButton("Yes",  
             new DialogInterface.OnClickListener() {  		  
                 @Override
		    	 public void onClick(DialogInterface dialog, int whichButton) {  		  
                   finish();  		  
               }  		  
         }).show();  
	 }
}
    
