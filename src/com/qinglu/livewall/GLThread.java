package com.qinglu.livewall;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxRenderer;

import android.util.Log;
import android.view.SurfaceHolder;

public class GLThread extends Thread {
	private final static boolean LOG_THREADS = false;
	public final static int DEBUG_CHECK_GL_ERROR = 1;
	public final static int DEBUG_LOG_GL_CALLS = 2;

	private final GLThreadManager sGLThreadManager = new GLThreadManager();
	private GLThread mEglOwner;

	private Cocos2dxEGLConfigChooser mEGLConfigChooser;
	private EGLContextFactory mEGLContextFactory;
	private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;

	public SurfaceHolder mHolder;
	private boolean mSizeChanged = true;

	// Once the thread is started, all accesses to the following member
	// variables are protected by the sGLThreadManager monitor
	public boolean mDone;
	private boolean mPaused;
	private boolean mHasSurface;
	private boolean mWaitingForSurface;
	private boolean mHaveEgl;
	private int mWidth;
	private int mHeight;
	private int mRenderMode;
	private boolean mRequestRender;
	private boolean mEventsWaiting;
	// End of member variables protected by the sGLThreadManager monitor.

	private Cocos2dxRenderer mRenderer;
	private ArrayList<Runnable> mEventQueue = new ArrayList<Runnable>();
	private EglHelper mEglHelper;

	GLThread(Cocos2dxRenderer renderer, Cocos2dxEGLConfigChooser chooser,
			EGLContextFactory contextFactory,
			EGLWindowSurfaceFactory surfaceFactory) {
		super();
		mDone = false;
		mWidth = 0;
		mHeight = 0;
		mRequestRender = true;
		mRenderMode =GLWallEngine.RENDERMODE_CONTINUOUSLY;
		mRenderer = renderer;
		this.mEGLConfigChooser = chooser;
		this.mEGLContextFactory = contextFactory;
		this.mEGLWindowSurfaceFactory = surfaceFactory;
	}

	@Override
	public void run() {
		setName("GLThread " + getId());
		if (LOG_THREADS) {
			Log.i("GLThread", "starting tid=" + getId());
		}

		try {
			guardedRun();
		} catch (InterruptedException e) {
			// fall thru and exit normally
		} finally {
			sGLThreadManager.threadExiting(this);
		}
	}

	/*
	 * This private method should only be called inside a
	 * synchronized(sGLThreadManager) block.
	 */
	private void stopEglLocked() {
		if (mHaveEgl) {
			mHaveEgl = false;
			mEglHelper.destroySurface();
			sGLThreadManager.releaseEglSurface(this);
		}
	}
	

	private void guardedRun() throws InterruptedException {
		mEglHelper = new EglHelper(mEGLConfigChooser, mEGLContextFactory,
				mEGLWindowSurfaceFactory);
		try {
			GL10 gl = null;
			boolean tellRendererSurfaceCreated = true;
			boolean tellRendererSurfaceChanged = true;

			/*
			 * This is our main activity thread's loop, we go until asked to
			 * quit.
			 */
			while (!isDone()) {
				/*
				 * Update the asynchronous state (window size)
				 */
				int w = 0;
				int h = 0;
				boolean changed = false;
				boolean needStart = false;
				boolean eventsWaiting = false;

				synchronized (sGLThreadManager) {
					while (true) {
						// Manage acquiring and releasing the SurfaceView
						// surface and the EGL surface.
						if (mPaused) {
							stopEglLocked();
						}
						if (!mHasSurface) {
							if (!mWaitingForSurface) {
								stopEglLocked();
								mWaitingForSurface = true;
								sGLThreadManager.notifyAll();
							}
						} else {
							if (!mHaveEgl) {
								if (sGLThreadManager.tryAcquireEglSurface(this)) {									
									mHaveEgl = true;
									mEglHelper.start();
									mRequestRender = true;
									needStart = true;
								}
							}
						}

						// Check if we need to wait. If not, update any state
						// that needs to be updated, copy any state that
						// needs to be copied, and use "break" to exit the
						// wait loop.

						if (mDone) {
							return;
						}

						if (mEventsWaiting) {
							eventsWaiting = true;
							mEventsWaiting = false;
							break;
						}

						if ((!mPaused)
								&& mHasSurface
								&& mHaveEgl
								&& (mWidth > 0)
								&& (mHeight > 0)
								&& (mRequestRender || (mRenderMode == GLWallEngine.RENDERMODE_CONTINUOUSLY))) {
							changed = mSizeChanged;
							w = mWidth;
							h = mHeight;
							mSizeChanged = false;
							mRequestRender = false;
							if (mHasSurface && mWaitingForSurface) {
								changed = true;
								mWaitingForSurface = false;
								sGLThreadManager.notifyAll();
							}
							break;
						}

						// By design, this is the only place where we wait().

						if (LOG_THREADS) {
							Log.i("GLThread", "waiting tid=" + getId());
						}
						sGLThreadManager.wait();
					}
				} // end of synchronized(sGLThreadManager)

				/*
				 * Handle queued events
				 */
				if (eventsWaiting) {
					Runnable r;
					while ((r = getEvent()) != null) {
						r.run();
						if (isDone()) {
							return;
						}
					}
					// Go back and see if we need to wait to render.
					continue;
				}
				if (needStart) {
					tellRendererSurfaceCreated = true;
					changed = true;
				}
				if (changed) {
					gl = (GL10) mEglHelper.createSurface(mHolder);
					tellRendererSurfaceChanged = true;
					
				}

				if (tellRendererSurfaceCreated) {
					mRenderer.onSurfaceCreated(gl, mEglHelper.mEglConfig);
					tellRendererSurfaceCreated = false;
				}
				if (tellRendererSurfaceChanged) {
					mRenderer.onSurfaceChanged(gl, w, h);
					tellRendererSurfaceChanged = false;
				}
				if ((w > 0) && (h > 0)) {
					/* draw a frame here */
					mRenderer.onDrawFrame(gl);

					/*
					 * Once we're done with GL, we need to call swapBuffers() to
					 * instruct the system to display the rendered frame
					 */
					mEglHelper.swap();
					Thread.sleep(10);
				}
			}
		} finally {
			/*
			 * clean-up everything...
			 */
			synchronized (sGLThreadManager) {
				stopEglLocked();
				mEglHelper.finish();
			}
		}
	}

	private boolean isDone() {
		synchronized (sGLThreadManager) {
			return mDone;
		}
	}

	public void setRenderMode(int renderMode) {
		if (!((GLWallEngine.RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= GLWallEngine.RENDERMODE_CONTINUOUSLY))) {
			throw new IllegalArgumentException("renderMode");
		}
		synchronized (sGLThreadManager) {
			mRenderMode = renderMode;
			if (renderMode == GLWallEngine.RENDERMODE_CONTINUOUSLY) {
				sGLThreadManager.notifyAll();
			}
		}
	}

	public int getRenderMode() {
		synchronized (sGLThreadManager) {
			return mRenderMode;
		}
	}

	public void requestRender() {
		synchronized (sGLThreadManager) {
			mRequestRender = true;
			sGLThreadManager.notifyAll();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mHolder = holder;
		synchronized (sGLThreadManager) {
			if (LOG_THREADS) {
				Log.i("GLThread", "surfaceCreated tid=" + getId());
			}
			mHasSurface = true;
			sGLThreadManager.notifyAll();
		}
	}

	public void surfaceDestroyed() {
		synchronized (sGLThreadManager) {
			if (LOG_THREADS) {
				Log.i("GLThread", "surfaceDestroyed tid=" + getId());
			}
			mHasSurface = false;
			sGLThreadManager.notifyAll();
			while (!mWaitingForSurface && isAlive() && !mDone) {
				try {
					sGLThreadManager.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public void onPause() {
		synchronized (sGLThreadManager) {
			mPaused = true;
			sGLThreadManager.notifyAll();
			Cocos2dxHelper.onPause();
			mRenderer.handleOnPause();
		}
	}

	public void onResume() {
		synchronized (sGLThreadManager) {
			mPaused = false;
			mRequestRender = true;
			sGLThreadManager.notifyAll();
			Cocos2dxHelper.onResume();
			mRenderer.handleOnResume();
		}
	}

	public void onWindowResize(int w, int h) {
		synchronized (sGLThreadManager) {
			mWidth = w;
			mHeight = h;
			mSizeChanged = true;
			sGLThreadManager.notifyAll();
		}
	}

	public void requestExitAndWait() {
		// don't call this from GLThread thread or it is a guaranteed
		// deadlock!
		synchronized (sGLThreadManager) {
			mDone = true;
			sGLThreadManager.notifyAll();
		}
		try {
			join();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Queue an "event" to be run on the GL rendering thread.
	 * 
	 * @param r
	 *            the runnable to be run on the GL rendering thread.
	 */
	public void queueEvent(Runnable r) {
		synchronized (this) {
			mEventQueue.add(r);
			synchronized (sGLThreadManager) {
				mEventsWaiting = true;
				sGLThreadManager.notifyAll();
			}
		}
	}

	private Runnable getEvent() {
		synchronized (this) {
			if (mEventQueue.size() > 0) {
				return mEventQueue.remove(0);
			}

		}
		return null;
	}

	private class GLThreadManager {

		public synchronized void threadExiting(GLThread thread) {
			if (LOG_THREADS) {
				Log.i("GLThread", "exiting tid=" + thread.getId());
			}
			thread.mDone = true;
			if (mEglOwner == thread) {
				mEglOwner = null;
			}
			notifyAll();
		}

		/*
		 * Tries once to acquire the right to use an EGL surface. Does not
		 * block.
		 * 
		 * @return true if the right to use an EGL surface was acquired.
		 */
		public synchronized boolean tryAcquireEglSurface(GLThread thread) {
			if (mEglOwner == thread || mEglOwner == null) {
				mEglOwner = thread;
				notifyAll();
				return true;
			}
			return false;
		}

		public synchronized void releaseEglSurface(GLThread thread) {
			if (mEglOwner == thread) {
				mEglOwner = null;
			}
			notifyAll();
		}
	}
}
