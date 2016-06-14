package com.qinglu.livewall;

import java.util.Timer;
import java.util.TimerTask;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.spot.SpotDialogListener;
import net.youmi.android.spot.SpotManager;

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
private static int width;
private static int height;
private static int w_y = 0;
public static boolean isShow = false;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}		
		return super.onKeyDown(keyCode, event);
		
	}

	
	
	
	@Override
	protected void onPause() {
		isShow = false;
		this.finish();
		LiveWallpaperService.openAdActivity();
		super.onPause();
	}

	@Override
	protected void onResume() {
		isShow = true;
		super.onResume();
	}



	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		isShow = true;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		
		final LayoutParams p = getWindow().getAttributes();  //获取对话框当前的参数值    
		p.width = (int) getX(100);  
		p.height = (int) getY(80);    
		//Log.e("-----------", "w="+p.width + "  h="+p.height);
        p.x = 0;
        p.y = (int) getPY(568);
        getWindow().setAttributes(p);   
        

		LinearLayout.LayoutParams layoutGrayParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		final LinearLayout layoutGray = new LinearLayout(this);
		layoutGray.setAlpha(0.0f);
		layoutGray.setLayoutParams(layoutGrayParams);
		
		this.setContentView(layoutGray);
	       
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);  	
    	adView = new AdView(context, AdSize.SIZE_320x50);
    	layoutGray.addView(adView, layoutParams);
    	    	
    	handler = new Handler() {  
            public void handleMessage(Message msg) {  
            	super.handleMessage(msg);  
                if (msg.what == 103) {  
                	FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);  	
                	adView = new AdView(context, AdSize.SIZE_320x50);
                	layoutGray.addView(adView, layoutParams);
                } 
                else if (msg.what == 104)
                {
                	layoutGray.removeView(adView);
                }
                else if (msg.what == 105)
                {
                	p.y = w_y;
                	getWindow().setAttributes(p);  
                }
                else if (msg.what == 108)
                {
                	((Activity) context).finish();
                }
            };  
        };  
       
//        SpotManager.getInstance(this).showSpotAds(this,new SpotDialogListener() {
//			
//			@Override
//			public void onSpotClosed() {
//				((Activity) context).finish();
//			}
//			
//			@Override
//			public void onSpotClick(boolean arg0) {
//				((Activity) context).finish();
//			}
//			
//			@Override
//			public void onShowSuccess() {
//				
//			}
//			
//			@Override
//			public void onShowFailed() {
//				((Activity) context).finish();
//			}
//		});
//        show();
	}
	public static void checkPos(float y)
	{
		w_y = (int) getPY(y);
		Message message = new Message(); 
        message.what = 105;              
        handler.sendMessage(message);  	
		
		
//		LayoutParams p = ((Activity) context).getWindow().getAttributes();		
//		int dy = (int) getPY(y);
//		int jd = (int) getY(30);
//		if(dy > p.y)
//			jd = (int) getY(80);
//		if(Math.abs(dy - p.y) < jd)
//		{
//			int max =  (int) getPY(0);
//			int min = (int) getPY(1136);
//			int dis =  (int) getY(80);
//			
//			if(dy > p.y)
//			{
//				w_y = p.y - dis;
//				if(w_y-dis*2 < min)
//					w_y = 0;
//			}
//			else
//			{
//				w_y = p.y + dis;
//				if(w_y+dis*2 > max)
//					w_y = 0;
//			}
//			 Message message = new Message(); 
//	         message.what = 105;              
//	         handler.sendMessage(message);  			 
//		}
	}
	public static void show()
	{				
		 Timer timer = new Timer();  
		 TimerTask task = new TimerTask() {  		  
		        @Override  
		        public void run() {  
		            // 需要做的事:发送消息  		        	
		            Message message = new Message(); 
		           
		            message.what = 108;
		              
		            handler.sendMessage(message);  
		        }  
		    }; 
		 timer.schedule(task, 10000); // 1s后执行task,经过1s再次执行  
	}
	public static float getScaleX()
	{
		return width / 640.f;
	}
	
	public static float getScaleY()
	{
		return height / 1136.f;
	}
	
	public static float getX(float x)
	{
		return getScaleX() * x;
	}
			
	public static float getY(float y)
	{
		return getScaleY() * y;
	}
	
	public static float getPY(float y)
	{
		return  height/2 - getY(y);
	}
}
