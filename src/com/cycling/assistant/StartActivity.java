package com.cycling.assistant;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class StartActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        ImageView iv = (ImageView)findViewById(R.id.iv);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim);
     
        iv.startAnimation(animation);
        animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub 开始动画

			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub 重复执行
				
			}
			
			
			@Override
			public void onAnimationEnd(Animation animation) {
				finish();
				Intent intent = new Intent(StartActivity.this ,ConnectionActivity.class);
				startActivity(intent);
				//finish();
				// TODO Auto-generated method stub 结束动画
				
			}
		});
    	
    }
    
}

