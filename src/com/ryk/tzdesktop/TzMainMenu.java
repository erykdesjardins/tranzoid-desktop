package com.ryk.tzdesktop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class TzMainMenu implements TzIDrawable {
	private Boolean shown;
	private Point bounds;
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public TzMainMenu(Context ctx) {
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();	
		
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			bounds = new Point();
			display.getSize(bounds);
		} else {
			bounds = new Point(display.getWidth(), display.getHeight());
		}
		
		shown = false;
	}
	
	public void show() {
		shown = true;
	}
	
	@Override
	public void Draw(Canvas canvas) {
		if (shown) {
			
		}
	}

	@Override
	public void Update() {
		
	}

	@Override
	public void detectCollision(Point p) {
		
	}
	
}
