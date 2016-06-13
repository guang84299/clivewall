package com.qinglu.livewall;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxRenderer;
import org.cocos2dx.lib.Cocos2dxHelper.Cocos2dxHelperListener;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperService.Engine;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class GLWallEngine extends Engine implements Cocos2dxHelperListener {
	private static final String TAG = GLWallEngine.class.getSimpleName();
	public final static int RENDERMODE_WHEN_DIRTY = 0;
	public final static int RENDERMODE_CONTINUOUSLY = 1;
	private Context context;
	private GLThread mGLThread;
	private Cocos2dxEGLConfigChooser mEGLConfigChooser;
	private EGLContextFactory mEGLContextFactory;
	private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
	private int mDebugFlags;
	private int[] mGLContextAttrs = null;
	private Cocos2dxRenderer cocos2dxRenderer;

	public GLWallEngine(WallpaperService service) {
		service.super();
		this.context = service;
		
		Log.e(TAG, "GLWallEngine()");
	}
		
	public Context getContext() {
		return context;
	}

	@Override
	public void onVisibilityChanged(boolean visible) {
		if (visible) {
			onResume();
		} else {
			onPause();
		}
		super.onVisibilityChanged(visible);
	}

	@Override
	public void onDesiredSizeChanged(int desiredWidth, int desiredHeight) {
		Log.e(TAG, "onDesiredSizeChanged()");
		super.onDesiredSizeChanged(desiredWidth, desiredHeight);
	}

	@Override
	public void onCreate(SurfaceHolder surfaceHolder) {
		super.onCreate(surfaceHolder);	
		this.mGLContextAttrs = Cocos2dxActivity.getGLContextAttrs();
		if(this.mGLContextAttrs[3] > 0) surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		Cocos2dxEGLConfigChooser chooser = new Cocos2dxEGLConfigChooser(
				this.mGLContextAttrs);
		this.setEGLConfigChooser(chooser);
		this.setEGLContextFactory(new DefaultContextFactory());
		this.setEGLWindowSurfaceFactory(new DefaultWindowSurfaceFactory());
		
		cocos2dxRenderer = new Cocos2dxRenderer();
		this.setRenderer(cocos2dxRenderer);	
		Log.e(TAG, "onCreate()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Log.d(TAG, "GLEngine.onDestroy()");
		mGLThread.requestExitAndWait();
		Log.e(TAG, "onDestroy()");
	}

	@Override
	public void onSurfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Log.d(TAG, "onSurfaceChanged()");
		mGLThread.onWindowResize(width, height);
		cocos2dxRenderer.setScreenWidthAndHeight(width, height);
		super.onSurfaceChanged(holder, format, width, height);
		Log.e(TAG, "onSurfaceChanged()");
	}

	@Override
	public void onSurfaceCreated(SurfaceHolder holder) {
		Log.e(TAG, "onSurfaceCreated()");
		mGLThread.surfaceCreated(holder);
		super.onSurfaceCreated(holder);
	}

	@Override
	public void onSurfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "onSurfaceDestroyed()");
		mGLThread.surfaceDestroyed();
		super.onSurfaceDestroyed(holder);
	}

	/**
	 * An EGL helper class.
	 */

	public void setDebugFlags(int debugFlags) {
		mDebugFlags = debugFlags;
	}

	public int getDebugFlags() {
		return mDebugFlags;
	}

	public void setRenderer(Cocos2dxRenderer renderer) {
		checkRenderThreadState();
		if (mEGLConfigChooser == null) {
			mEGLConfigChooser = new Cocos2dxEGLConfigChooser(
					this.mGLContextAttrs);
		}
		if (mEGLContextFactory == null) {
			mEGLContextFactory = new DefaultContextFactory();
		}
		if (mEGLWindowSurfaceFactory == null) {
			mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
		}
		mGLThread = new GLThread(renderer, mEGLConfigChooser,
				mEGLContextFactory, mEGLWindowSurfaceFactory);
		mGLThread.start();
	}

	public void setEGLContextFactory(EGLContextFactory factory) {
		checkRenderThreadState();
		mEGLContextFactory = factory;
	}

	public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
		checkRenderThreadState();
		mEGLWindowSurfaceFactory = factory;
	}

	public void setEGLConfigChooser(Cocos2dxEGLConfigChooser configChooser) {
		checkRenderThreadState();
		mEGLConfigChooser = configChooser;
	}

	public void setEGLConfigChooser(boolean needDepth) {
		setEGLConfigChooser(new Cocos2dxEGLConfigChooser(this.mGLContextAttrs));
	}

	public void setEGLConfigChooser(int redSize, int greenSize, int blueSize,
			int alphaSize, int depthSize, int stencilSize) {
		setEGLConfigChooser(new Cocos2dxEGLConfigChooser(redSize, greenSize,
				blueSize, alphaSize, depthSize, stencilSize));
	}

	public void setRenderMode(int renderMode) {
		mGLThread.setRenderMode(renderMode);
	}

	public int getRenderMode() {
		return mGLThread.getRenderMode();
	}

	public void requestRender() {
		mGLThread.requestRender();
	}

	public void onPause() {
		
		mGLThread.onPause();
	}

	public void onResume() {
		
		mGLThread.onResume();
	}

	public void queueEvent(Runnable r) {
		mGLThread.queueEvent(r);
	}

	private void checkRenderThreadState() {
		if (mGLThread != null) {
//			throw new IllegalStateException(
//					"setRenderer has already been called for this instance.");
		}
	}

	@Override
	public void showDialog(String pTitle, String pMessage) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runOnGLThread(Runnable pRunnable) {
		mGLThread.queueEvent(pRunnable);
	}

	public boolean isSoftKeyboardShown() {
		return mSoftKeyboardShown;
	}

	public void setSoftKeyboardShown(boolean softKeyboardShown) {
		this.mSoftKeyboardShown = softKeyboardShown;
	}

	private boolean mSoftKeyboardShown = false;
	@Override
	public void onTouchEvent(final MotionEvent pMotionEvent) {
		super.onTouchEvent(pMotionEvent);
		final int pointerNumber = pMotionEvent.getPointerCount();
		final int[] ids = new int[pointerNumber];
		final float[] xs = new float[pointerNumber];
		final float[] ys = new float[pointerNumber];

		if (mSoftKeyboardShown) {
			InputMethodManager imm = (InputMethodManager) this.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			View view = ((Activity) this.getContext()).getCurrentFocus();
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			//this.requestFocus();  todo
			mSoftKeyboardShown = false;
		}

		for (int i = 0; i < pointerNumber; i++) {
			ids[i] = pMotionEvent.getPointerId(i);
			xs[i] = pMotionEvent.getX(i);
			ys[i] = pMotionEvent.getY(i);
		}

		switch (pMotionEvent.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_POINTER_DOWN:
			final int indexPointerDown = pMotionEvent.getAction() >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int idPointerDown = pMotionEvent
					.getPointerId(indexPointerDown);
			final float xPointerDown = pMotionEvent.getX(indexPointerDown);
			final float yPointerDown = pMotionEvent.getY(indexPointerDown);

			this.queueEvent(new Runnable() {
				@Override
				public void run() {
					cocos2dxRenderer.handleActionDown(idPointerDown, xPointerDown,
									yPointerDown);
				}
			});
			break;

		case MotionEvent.ACTION_DOWN:
			// there are only one finger on the screen
			final int idDown = pMotionEvent.getPointerId(0);
			final float xDown = xs[0];
			final float yDown = ys[0];

			this.queueEvent(new Runnable() {
				@Override
				public void run() {
					cocos2dxRenderer.handleActionDown(idDown, xDown, yDown);
				}
			});
			break;

		case MotionEvent.ACTION_MOVE:
			this.queueEvent(new Runnable() {
				@Override
				public void run() {
					cocos2dxRenderer.handleActionMove(ids, xs, ys);
				}
			});
			break;

		case MotionEvent.ACTION_POINTER_UP:
			final int indexPointUp = pMotionEvent.getAction() >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int idPointerUp = pMotionEvent.getPointerId(indexPointUp);
			final float xPointerUp = pMotionEvent.getX(indexPointUp);
			final float yPointerUp = pMotionEvent.getY(indexPointUp);

			this.queueEvent(new Runnable() {
				@Override
				public void run() {
					cocos2dxRenderer.handleActionUp(idPointerUp, xPointerUp, yPointerUp);
				}
			});
			break;

		case MotionEvent.ACTION_UP:
			// there are only one finger on the screen
			final int idUp = pMotionEvent.getPointerId(0);
			final float xUp = xs[0];
			final float yUp = ys[0];

			this.queueEvent(new Runnable() {
				@Override
				public void run() {
					cocos2dxRenderer.handleActionUp(idUp, xUp, yUp);
				}
			});
			break;

		case MotionEvent.ACTION_CANCEL:
			this.queueEvent(new Runnable() {
				@Override
				public void run() {
					cocos2dxRenderer.handleActionCancel(ids, xs, ys);
				}
			});
			break;
		}

		/*
		 * if (BuildConfig.DEBUG) {
		 * Cocos2dxGLSurfaceView.dumpMotionEvent(pMotionEvent); }
		 */
	}

	

	@SuppressWarnings("unused")
	private static void dumpMotionEvent(final MotionEvent event) {
		final String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
				"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		final StringBuilder sb = new StringBuilder();
		final int action = event.getAction();
		final int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_").append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid ").append(
					action >> MotionEvent.ACTION_POINTER_INDEX_SHIFT);
			sb.append(")");
		}
		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#").append(i);
			sb.append("(pid ").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount()) {
				sb.append(";");
			}
		}
		sb.append("]");
	}
}