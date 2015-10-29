package com.ryk.tzdesktop;

import java.util.Random;

import tzopengles.GeoUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class ParticuleEngine implements TzIDrawable {
	private int particuleCount = 200;
	
	private Point boundaries = new Point();
	private Bubble[] particules = new Bubble[particuleCount];
	
	private Bitmap backgroundImage;
	private Rect backgroundRect;
	private Rect backgroundDest;
	private Paint backgroundPaint;
	
	public enum TrackType {
		sin, cos, tan, line
	}
	
	public ParticuleEngine(Context ctx) {
		this(ctx, 20);
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public ParticuleEngine(Context ctx, int count) {
		particuleCount = count;
		backgroundImage = GeoUtil.getBitmapFromAsset(ctx, "textures/tzbg.jpg");
		backgroundRect = new Rect(0, 0, backgroundImage.getWidth(), backgroundImage.getHeight());
		backgroundDest = new Rect(0, 0, backgroundImage.getWidth(), backgroundImage.getHeight());
		
		backgroundPaint = new Paint();
		backgroundPaint.setStyle(Paint.Style.FILL);
		
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();	
		
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			display.getSize(boundaries);
		} else {
			boundaries = new Point(display.getWidth(), display.getHeight());
		}

		Random rand = new Random();
		
		for (int i = 0; i < particuleCount; i++) {
			particules[i] = new Bubble(boundaries, rand);
		}
	}
	
	public void Update() {
		for (int i = 0; i < particuleCount; i++) {
			particules[i].Update();
		}
	}
	
	public void Draw(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		canvas.drawBitmap(backgroundImage, backgroundRect, backgroundDest, backgroundPaint);
		
		for (int i = 0; i < particuleCount; i++) {
			particules[i].Draw(canvas);
		}		
	}
	
    public void detectCollision(Point p)
    {
        for (int i = 0; i < particuleCount; i++)
        {
        	Bubble b = particules[i];
        	
            // If cursor is in corner of the bubble bounderies
            if (p.x <= b.Position.x + b.Radius && p.x > b.Position.x - b.Radius &&
                p.y <= b.Position.y + b.Radius && p.y > b.Position.y - b.Radius)
            {
                // Check if touch location is at "Half Diameter" distance from center of bubble
                if (Math.sqrt(Math.pow(Math.abs(p.x - b.Position.x), 2) + Math.pow(Math.abs(p.y - b.Position.y), 2)) <= b.Radius)
                {
                    // Then pop that bubble
                	b.pop();

                    break;
                }
            }
        }
    }	
	
    public class Bubble
    {
        // Constants
        private final int MinXVelocity = 20;
        private final int MaxXVelocity = 60;
        private final int MinRadius = 40;
        private final int MaxRadius = 300;
        private final int MinXCoef = 35;
        private final int MaxXCoef = 85;
        private final int MinOpacity = 80;
        private final int MaxOpacity = 150;
        private Paint paint;

        private class Rpoint {
        	public int x;
        	public int y;
        	
        	public Rpoint(int x, int y) {
        		this.x = x;
        		this.y = y;
        	}
        }
        
        // Variables
        Rpoint InitPos;
        int color;
        int LifeSpan;
        int LifeTime;
        int initOpacity;
        double XVelocity;
        double YVelocity;
        int XCoef;

        // Properties
        public float Opacity;
        public Rpoint Position;
        public int Radius;

        // Change everything to nextFloat
        public Bubble(Point size, Random rand)
        {
            // Velocity with X = Random Number between 0 and 1 * Random Number between 40 and 90
            XVelocity = rand.nextDouble() * rand.nextDouble() * (MaxXVelocity - MinXVelocity) + MinXVelocity;

            // Velocity with Y = Random Number between 0 and 1 + 1
            YVelocity = rand.nextDouble() + 1d;

            // Diameter = Random Number between 40 and 100
            Radius = (int)(rand.nextDouble() * (MaxRadius - MinRadius) + MinRadius);

            // Coefficient or variation = Random Number between 15 and 25
            XCoef = (int)(rand.nextDouble() * (MaxXCoef - MinXCoef) + MinXCoef);

            // Opacity = Random Number between 80 and 150
            initOpacity = (int)(rand.nextDouble() * (MaxOpacity - MinOpacity) + MinOpacity);
            Opacity = (float)initOpacity;

            // Color = Orange with specified Opacity
            color = Color.argb(initOpacity, 0, 165, 255);

            // Initial position -
            //  X = Random Number between 0 and the Width of the form - Half diameter
            //  Y = Height of the form + Random Number between 5 and the Height of the form
            InitPos = new Rpoint((int)(rand.nextDouble() * size.x - Radius / 2), size.y + (int)(rand.nextDouble() * (5 - size.y) + size.y)); 

            // Current position = Initial position
            Position = new Rpoint(InitPos.x, InitPos.y);

            // Did not live yet
            LifeTime = 0;
            
            // Set color
            paint = new Paint();
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
        }
        
        public void pop() {
        	LifeTime = 0;
        	Opacity = (int)initOpacity;
        }

        private void Update()
        {
        	// Add time to span and fade a bit
    		LifeTime++;
        	Opacity-=0.2f;
        	
            // If bubble is out of window or opacity zero, reinit position and opacity
        	if (Position.y < -Radius || Opacity <= 0) {
        		LifeTime = 0;
        		Opacity = (int)initOpacity;
        	} 
            
            // Position X = Sin( Frame Count / Coefficient or variation ) * Velocity X + Initial Position
            Position.x = (int) (Math.sin((double)LifeTime / (double)XCoef) * XVelocity + InitPos.x);

            // Position Y = Initial Position - Frame Count * Velocity Y
            Position.y = (int) (InitPos.y - ((double)LifeTime * YVelocity));
            
            // Set new alpha value
            paint.setAlpha((int)Opacity);
        }

        public void Draw(Canvas g)
        {
            g.drawCircle(Position.x, Position.y, Radius, paint);
        }
    }	
}
