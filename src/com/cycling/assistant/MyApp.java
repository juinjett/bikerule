package com.cycling.assistant;

import android.app.Application;

public class MyApp extends Application {  
	  
    private boolean connect;  
  
    @Override 
    public void onCreate() { 
    	connect = false;  
        super.onCreate(); 
    } 
    
    public boolean getConnectStatus(){  
        return connect;  
    }  
    public void setConnectStatus(boolean newConnect){  
	    connect = newConnect;  
    }  
}  