package tzopengles;

import javax.microedition.khronos.opengles.GL10;

import com.ryk.tzdesktop.ParticuleEngine;

import android.content.Context;
import android.opengl.GLES20;

public class GLES20Renderer extends GLRenderer {
	ParticuleEngine particuleEngine;
	
	public GLES20Renderer(Context context) {
		particuleEngine = new ParticuleEngine(context);
	}
	
    @Override
    public void onCreate(int width, int height, boolean contextLost) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
    }
   
    @Override
    public void onDrawFrame(boolean firstDraw) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    } 

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
   
 
}