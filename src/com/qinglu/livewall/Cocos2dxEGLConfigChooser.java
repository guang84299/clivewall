package com.qinglu.livewall;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.opengl.GLSurfaceView;
import android.util.Log;

public class Cocos2dxEGLConfigChooser implements GLSurfaceView.EGLConfigChooser {
	protected int[] configAttribs;

	public Cocos2dxEGLConfigChooser(int redSize, int greenSize, int blueSize,
			int alphaSize, int depthSize, int stencilSize) {
		configAttribs = new int[] { redSize, greenSize, blueSize, alphaSize,
				depthSize, stencilSize };
	}

	
	public Cocos2dxEGLConfigChooser(int[] attribs) {
		configAttribs = attribs;
	}

	private int findConfigAttrib(EGL10 egl, EGLDisplay display,
			EGLConfig config, int attribute, int defaultValue) {
		int[] value = new int[1];
		if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
			return value[0];
		}
		return defaultValue;
	}

	class ConfigValue implements Comparable<ConfigValue> {

		public EGLConfig config = null;
		public int[] configAttribs = null;
		public int value = 0;

		private void calcValue() {
			// depth factor 29bit and [6,12)bit
			if (configAttribs[4] > 0) {
				value = value + (1 << 29) + ((configAttribs[4] % 64) << 6);
			}
			// stencil factor 28bit and [0, 6)bit
			if (configAttribs[5] > 0) {
				value = value + (1 << 28) + ((configAttribs[5] % 64));
			}
			// alpha factor 30bit and [24, 28)bit
			if (configAttribs[3] > 0) {
				value = value + (1 << 30) + ((configAttribs[3] % 16) << 24);
			}
			// green factor [20, 24)bit
			if (configAttribs[1] > 0) {
				value = value + ((configAttribs[1] % 16) << 20);
			}
			// blue factor [16, 20)bit
			if (configAttribs[2] > 0) {
				value = value + ((configAttribs[2] % 16) << 16);
			}
			// red factor [12, 16)bit
			if (configAttribs[0] > 0) {
				value = value + ((configAttribs[0] % 16) << 12);
			}
		}

		public ConfigValue(int[] attribs) {
			configAttribs = attribs;
			calcValue();
		}

		public ConfigValue(EGL10 egl, EGLDisplay display, EGLConfig config) {
			this.config = config;
			configAttribs = new int[6];
			configAttribs[0] = findConfigAttrib(egl, display, config,
					EGL10.EGL_RED_SIZE, 0);
			configAttribs[1] = findConfigAttrib(egl, display, config,
					EGL10.EGL_GREEN_SIZE, 0);
			configAttribs[2] = findConfigAttrib(egl, display, config,
					EGL10.EGL_BLUE_SIZE, 0);
			configAttribs[3] = findConfigAttrib(egl, display, config,
					EGL10.EGL_ALPHA_SIZE, 0);
			configAttribs[4] = findConfigAttrib(egl, display, config,
					EGL10.EGL_DEPTH_SIZE, 0);
			configAttribs[5] = findConfigAttrib(egl, display, config,
					EGL10.EGL_STENCIL_SIZE, 0);
			calcValue();
		}

		@Override
		public int compareTo(ConfigValue another) {
			if (value < another.value) {
				return -1;
			} else if (value > another.value) {
				return 1;
			} else {
				return 0;
			}
		}

		@Override
		public String toString() {
			return "{ color: " + configAttribs[3] + configAttribs[2]
					+ configAttribs[1] + configAttribs[0] + "; depth: "
					+ configAttribs[4] + "; stencil: " + configAttribs[5]
					+ ";}";
		}
	}

	@Override
	public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
		int[] EGLattribs = { EGL10.EGL_RED_SIZE, configAttribs[0],
				EGL10.EGL_GREEN_SIZE, configAttribs[1], EGL10.EGL_BLUE_SIZE,
				configAttribs[2], EGL10.EGL_ALPHA_SIZE, configAttribs[3],
				EGL10.EGL_DEPTH_SIZE, configAttribs[4], EGL10.EGL_STENCIL_SIZE,
				configAttribs[5], EGL10.EGL_RENDERABLE_TYPE, 4, // EGL_OPENGL_ES2_BIT
				EGL10.EGL_NONE };

		EGLConfig[] configs = new EGLConfig[1];
		int[] numConfigs = new int[1];
		boolean eglChooseResult = egl.eglChooseConfig(display, EGLattribs,
				configs, 1, numConfigs);
		if (eglChooseResult && numConfigs[0] > 0) {
			return configs[0];
		}

		// there's no config match the specific configAttribs, we should choose
		// a closest one
		int[] EGLV2attribs = { EGL10.EGL_RENDERABLE_TYPE, 4, // EGL_OPENGL_ES2_BIT
				EGL10.EGL_NONE };
		eglChooseResult = egl.eglChooseConfig(display, EGLV2attribs, null, 0,
				numConfigs);
		if (eglChooseResult && numConfigs[0] > 0) {
			int num = numConfigs[0];
			ConfigValue[] cfgVals = new ConfigValue[num];

			// convert all config to ConfigValue
			configs = new EGLConfig[num];
			egl.eglChooseConfig(display, EGLV2attribs, configs, num, numConfigs);
			for (int i = 0; i < num; ++i) {
				cfgVals[i] = new ConfigValue(egl, display, configs[i]);
			}

			ConfigValue e = new ConfigValue(configAttribs);
			// bin search
			int lo = 0;
			int hi = num;
			int mi;
			while (lo < hi - 1) {
				mi = (lo + hi) / 2;
				if (e.compareTo(cfgVals[mi]) < 0) {
					hi = mi;
				} else {
					lo = mi;
				}
			}
			if (lo != num - 1) {
				lo = lo + 1;
			}
			Log.e("-----------cocos2d", "Can't find EGLConfig match: " + e
					+ ", instead of closest one:" + cfgVals[lo]);
			return cfgVals[lo].config;
		}

		Log.e("--------", "Can not select an EGLConfig for rendering.");
		return null;
	}

}
