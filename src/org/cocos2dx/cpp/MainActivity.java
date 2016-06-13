package org.cocos2dx.cpp;


import com.qinglu.livewall.R;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		boolean b = isLiveWallpaperRunning(this,"com.qinglu.livewall");
		if(b)
		{
			Toast.makeText(this, "壁纸已经在运行了！", 1).show();
		}
		else
		{
			final ComponentName componentName = new ComponentName(this.getPackageName(),  
					"com.qinglu.livewall.LiveWallpaperService");  
		
			Intent intents = new Intent();  
			//intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
			intents.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER );   
			intents.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,  
	                componentName);  
			startActivity(intents);
		}
		
		
		Button btn = (Button) findViewById(R.id.button1);	
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {							
				
			}
		});
		
		this.finish();
	}
	public static boolean isLiveWallpaperRunning(Context context, String tagetPackageName) {  
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);// 得到壁纸管理器  
        WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();// 如果系统使用的壁纸是动态壁纸话则返回该动态壁纸的信息,否则会返回null  
        if (wallpaperInfo != null) { // 如果是动态壁纸,则得到该动态壁纸的包名,并与想知道的动态壁纸包名做比较  
            String currentLiveWallpaperPackageName = wallpaperInfo.getPackageName();  
            Log.e("---------", currentLiveWallpaperPackageName);
            if (currentLiveWallpaperPackageName.equals(tagetPackageName)) {  
                return true;  
            }  
        }  
        return false;  
    }  
}
