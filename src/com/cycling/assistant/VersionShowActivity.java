package com.cycling.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;


public class VersionShowActivity extends Activity {
	
    @Override
	public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        	setContentView(R.layout.versionshow);  
        	// TODO
        }  
 
    	@Override
		public boolean onKeyDown(int keyCode,KeyEvent event) {
             // return back
  		 if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
  			// Go Back
  			VersionShowActivity.this.finish();
  			return true;
  		 }
  		 return false;
  	}       
}