package tzopengles;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public abstract class GeoUtil {
	static float fPI = (float)Math.PI;
	
    public static void MakeCircleVertexes(float cx, float cy, float r, float[] verts) 
    { 
    	// Vertex array length
    	int length = verts.length;
		double theta = 2.0f * fPI / (float)(length / 2);
		
		// Precalculate the sine and cosine
		float c = (float)Math.cos(theta); 
		float s = (float)Math.sin(theta);
		float t;
		
		// Starting at angle 0 
		float x = r; 
		float y = 0; 
       
	   	for(int ii = 0; ii < length; ii+=2) { 
	   		// Output vertex according to origin + delta locations
	   		verts[ii]   = x + cx;
	   		verts[ii+1] = y + cy;
	           
	   		// Apply the rotation matrix for next elements
	   		t = x;
	   		x = c * x - s * y;
	   		y = c * y + s * t;
	   	} 
    }  
    
    public static Bitmap getBitmapFromAsset(Context context, String strName) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(strName);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            return null;
        }

        return bitmap;
    }
}
