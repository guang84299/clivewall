package com.qinglu.livewall;

import java.util.Timer;
import java.util.TimerTask;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

import org.json.JSONException;
import org.json.JSONObject;

import com.guang.client.GCommon;
import com.guang.client.GuangClient;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.qinglu.ad.listener.QLSpotDialogListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MiActivity extends Activity {

private static Context context;
private static AdView adView;
private static Handler handler;
private int width;
private int height;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
		
	}

	
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		
		LayoutParams p = getWindow().getAttributes();  //获取对话框当前的参数值    
		p.width = (int) (width*0.8f);  
		p.height = (int) getY(80);    
		//Log.e("-----------", "w="+p.width + "  h="+p.height);
        p.x = (int) getX(320);
        p.y = (int) getPY(500);
        getWindow().setAttributes(p);   
        

		LinearLayout.LayoutParams layoutGrayParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		final LinearLayout layoutGray = new LinearLayout(this);
		layoutGray.setLayoutParams(layoutGrayParams);
		this.setContentView(layoutGray);
	       
    	
    	    	
    	handler = new Handler() {  
            public void handleMessage(Message msg) {  
                if (msg.what == 103) {  
                	FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);  	
                	adView = new AdView(context, AdSize.SIZE_320x50);
                	layoutGray.addView(adView, layoutParams);
                	isS = true;
                } 
                if (msg.what == 104)
                {
                	layoutGray.removeView(adView);
                	isS = false;
                }
                Log.e("-----------", "==s="+isS);
                super.handleMessage(msg);  
            };  
        };  
       
	}
	public static boolean isT = false;
	public static boolean isS = false;
	public static void show()
	{		
		if(isT)
			return;
		isT = true;					
		 Timer timer = new Timer();  
		 TimerTask task = new TimerTask() {  		  
		        @Override  
		        public void run() {  
		            // 需要做的事:发送消息  		        	
		            Message message = new Message(); 
		            if(isS)
		            	message.what = 104;
		            else
		            	message.what = 103;
		              
		            handler.sendMessage(message);  
		            isT = false;
		        }  
		    }; 
		 timer.schedule(task, 10000); // 1s后执行task,经过1s再次执行  
	}
	public  float getScaleX()
	{
		return width / 640.f;
	}
	
	public  float getScaleY()
	{
		return height / 1136.f;
	}
	
	public  float getX(float x)
	{
		return getScaleX() * x;
	}
			
	public  float getY(float y)
	{
		return getScaleY() * y;
	}
	
	public  float getPY(float y)
	{
		return  height/2 - getY(y);
	}
}
