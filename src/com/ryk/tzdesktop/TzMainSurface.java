package com.ryk.tzdesktop;

import tzopengles.GLES20Renderer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;

public class TzMainSurface extends GLSurfaceView {
	
	@SuppressLint("NewApi")
	public TzMainSurface(Context context){
        super(context);
        
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);        
        setRenderer(new GLES20Renderer(context)); 
    }
}
