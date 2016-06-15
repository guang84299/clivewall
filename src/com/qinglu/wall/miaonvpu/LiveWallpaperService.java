package com.qinglu.wall.miaonvpu;

import java.util.List;


import org.cocos2dx.cpp.LiveWallReceiver;







import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxHelper.Cocos2dxHelperListener;

import com.qinglu.ad.QLAdController;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgent.EScenarioType;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;



public class LiveWallpaperService extends WallpaperService implements Cocos2dxHelperListener{
	private  GLWallEngine engine = null;
	private LiveWallReceiver receiver;
	public static int t_pid = 0;
	private static LiveWallpaperService service;
	@Override
	public Engine onCreateEngine() {
		//clearBitmaps();
		engine = new GLWallEngine(this);		
		return engine;
	}

	@Override
	public void onCreate() {
		service = this;
		init();
		
		QLAdController.getInstance().init(this, R.drawable.icon, true);
		
		registerListener();
		super.onCreate();
		MobclickAgent.setScenarioType(this, EScenarioType.E_UM_NORMAL);
		MobclickAgent.onResume(this);
			
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		unregisterListener();
		super.onDestroy();
		MobclickAgent.onPause(this);
		
	}

	private void registerListener() {
		receiver = new LiveWallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        this.registerReceiver(receiver, filter);
    }
 
    private void unregisterListener() {
           this.unregisterReceiver(receiver);
    }
    
    public void init()
    {
    	ApplicationInfo ai;
		try {
			ai = this.getPackageManager().getApplicationInfo(this.getPackageName(),
					PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			String libName = bundle.getString("android.app.lib_name");
			System.loadLibrary(libName);
			Cocos2dxHelper.init(null, this);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public native void clearBitmaps();
    
    public static void showAd(float x,float y)
    {
//    	if(service != null && isActivityRunning(service,"com.qinglu.wall.miaonvpu.MiActivity"))
//    	{
//    		MiActivity.show();
//    	}   	
    }

	public static boolean isActivityRunning(Context mContext,
			String activityClassName) {
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> info = activityManager.getRunningTasks(1);
		if (info != null && info.size() > 0) {
			ComponentName component = info.get(0).topActivity;
			if (activityClassName.equals(component.getClassName())) {
				return true;
			}
		}
		return false;
	}
    
    public class SampleEngine extends Engine {

		public LiveWallpaperPainting painting;

		SampleEngine() {
			SurfaceHolder holder = getSurfaceHolder();
			painting = new LiveWallpaperPainting(
					getApplicationContext(),holder);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			
			setTouchEventsEnabled(true);
			Log.e("-------------", "onCreate");
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			// remove listeners and callbacks here
			painting.stopPainting();
			Log.e("-------------", "onDestroy");
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			if (visible) {
				// register listeners and callbacks here
				painting.resumePainting();
			} else {
				// remove listeners and callbacks here
				painting.pausePainting();
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			painting.setSurfaceSize(width, height);
			Log.e("-------------", "onSurfaceChanged");		
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
			// start painting			
			painting.onSurfaceCreate();
			
			painting.start();
			Log.e("-------------", "onSurfaceCreated");
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			boolean retry = true;
			painting.stopPainting();
			while (retry) {
				try {
					painting.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
			Log.e("-------------", "onSurfaceDestroyed");
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xStep,
				float yStep, int xPixels, int yPixels) {
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			super.onTouchEvent(event);
			painting.doTouchEvent(event);
		}

	}

	@Override
	public void showDialog(String pTitle, String pMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runOnGLThread(Runnable pRunnable) {
		// TODO Auto-generated method stub
		
	}

	
  
}
