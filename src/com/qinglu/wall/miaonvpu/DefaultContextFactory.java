package com.qinglu.wall.miaonvpu;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class DefaultContextFactory implements EGLContextFactory {
	private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

	public EGLContext createContext(EGL10 egl, EGLDisplay display,
			EGLConfig config) {
		int[] attrib_list = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };

		return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT,
				attrib_list);
	}

	public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
		egl.eglDestroyContext(display, context);
	}

}