package com.qinglu.wall.miaonvpu;



import javax.microedition.khronos.opengles.GL10;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxRenderer;
import org.cocos2dx.lib.Cocos2dxHelper.Cocos2dxHelperListener;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LiveWallpaperPainting extends Thread {

		/** State */
	private boolean wait;
	private boolean run;

	/** Dimensions */
	private int width;
	private int height;

	/** Time tracking */
	private long previousTime;
	private long currentTime;
	private long dt;

	private Context context;
	private Cocos2dxEGLConfigChooser mEGLConfigChooser;
	private EGLContextFactory mEGLContextFactory;
	private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
	private Cocos2dxRenderer mRenderer;
	private EglHelper mEglHelper;
	private SurfaceHolder holder;
	private GL10 gl;
	private int[] mGLContextAttrs = null;
	
	private boolean tellRendererSurfaceCreated = false;
	private boolean tellRendererSurfaceChanged = false;
	
	private long tt = 0;
	 

	public LiveWallpaperPainting(Context context,SurfaceHolder holder) {
		this.context = context;
		this.holder = holder;
		
		this.mGLContextAttrs = Cocos2dxActivity.getGLContextAttrs();
		//if(this.mGLContextAttrs[3] > 0) holder.setFormat(PixelFormat.TRANSLUCENT);
		this.mEGLConfigChooser = new Cocos2dxEGLConfigChooser(
				this.mGLContextAttrs);		
		
		this.mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
		this.mEGLContextFactory = new DefaultContextFactory();
		
		mEglHelper = new EglHelper(mEGLConfigChooser, mEGLContextFactory,
				mEGLWindowSurfaceFactory);
		mEglHelper.start();
		this.mRenderer = new Cocos2dxRenderer();
		this.wait = true;	
	}
	
	public Context getContext()
	{
		return this.context;
	}

	/**
	 * Pauses the live wallpaper animation
	 */
	public void pausePainting() {
		this.wait = true;
		Cocos2dxHelper.onPause();
		mRenderer.handleOnPause();
		synchronized (this) {			
			this.notify();
			
		}
	}

	/**
	 * Resume the live wallpaper animation
	 */
	public void resumePainting() {
		this.wait = false;		
		Cocos2dxHelper.onResume();
		mRenderer.handleOnResume();
		synchronized (this) {
			
			this.notify();
			
		}
	}

	/**
	 * Stop the live wallpaper animation
	 */
	public void stopPainting() {
		this.run = false;
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public void run() {
		this.run = true;
		
		while (run) {
			try {
				synchronized (this.holder) {
					currentTime = System.currentTimeMillis();
					dt = currentTime - previousTime;
					if(gl == null)
						gl = (GL10) mEglHelper.createSurface(holder);
					updatePhysics();
					doDraw();
					previousTime = currentTime;		
					//Thread.sleep(10);
				}
			}
			catch(Exception e){
				
			}
			finally {
				
			}
			// pause if no need to animate
			synchronized (this) {
				if (wait) {
					try {
						wait();
					} catch (Exception e) {
					}
				}
			}
		}
	}

	public void onSurfaceCreate() {
		this.tellRendererSurfaceCreated = true;
		synchronized (this) {
			
			this.notify();
		}
	}
	
	/**
	 * Invoke when the surface dimension change
	 */
	public void setSurfaceSize(int width, int height) {
		mRenderer.setScreenWidthAndHeight(width, height);
		
		this.tellRendererSurfaceChanged = true;
		this.width = width;
		this.height = height;
		
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Invoke while the screen is touched
	 */
	public void doTouchEvent(MotionEvent event) {
 
		this.wait = false;
		synchronized (this) {
			notify();
		}
	}

	/**
	 * Do the actual drawing stuff
	 */
	private void doDraw( ) {	
		
		if ((width > 0) && (height > 0)) {
			/* draw a frame here */
			mRenderer.onDrawFrame(gl);
			mEglHelper.swap();
			
			
		}
		
		
	}

	/**
	 * Update the animation, sprites or whatever. If there is nothing to animate
	 * set the wait attribute of the thread to true
	 */
	private void updatePhysics() {
		if (tellRendererSurfaceCreated) {			
			mRenderer.onSurfaceCreated(gl, mEglHelper.mEglConfig);
			tellRendererSurfaceCreated = false;
			
		}
		if (tellRendererSurfaceChanged) {
			mRenderer.onSurfaceChanged(gl, width, height);
			tellRendererSurfaceChanged = false;
		}
		
		
	}

	

}
