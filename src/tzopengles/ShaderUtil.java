package tzopengles;

import android.opengl.GLES20;

public abstract class ShaderUtil {
	private static String vertexShaderCode =
		    "attribute vec4 vPosition;" +
		    "void main() {" +
		    "  gl_Position = vPosition;" +
		    "}";

	private static String fragmentShaderCode =
	    "precision mediump float;" +
	    "uniform vec4 vColor;" +
	    "void main() {" +
	    "  gl_FragColor = vColor;" +
	    "}";	

	private static int loadShader(int type, String shaderCode){
	    // Create a vertex shader type (GLES20.GL_VERTEX_SHADER)
	    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
	    int shader = GLES20.glCreateShader(type);

	    // Add the source code to the shader and compile it
	    GLES20.glShaderSource(shader, shaderCode);
	    GLES20.glCompileShader(shader);

	    return shader;
	}
	
	private static int loadVertexShader() {
		return loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
	}

	private static int loadFragmentShader() {
		return loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
	}
	
	public static int getProgram() {
		int mProg = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProg, loadVertexShader());   // Adds the vertex shader to program
	    GLES20.glAttachShader(mProg, loadFragmentShader()); // Adds the fragment shader to program
	    GLES20.glLinkProgram(mProg);                  		// Creates OpenGL ES program executables
	    
	    return mProg;
	}
}
