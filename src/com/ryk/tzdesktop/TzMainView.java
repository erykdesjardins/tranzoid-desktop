package com.ryk.tzdesktop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

public class TzMainView extends View {
	ParticuleEngine particuleEngine;
	TzMainMenu mainView;
	
	public TzMainView(Context context) {
		super(context);
		
		setWillNotDraw (false);
		particuleEngine = new ParticuleEngine(context);
		mainView = new TzMainMenu(context);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// Update items
		particuleEngine.Update();
		mainView.Update();
		
		// Draw everything
		particuleEngine.Draw(canvas);
		mainView.Draw(canvas);
		invalidate();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// Detect touch position
		Point p = new Point((int)e.getRawX(), (int)e.getRawY());
		
		// Trigger events
		particuleEngine.detectCollision(p);
		mainView.detectCollision(p);
		
		return true;
	}
	
	public void castMenu() {
		mainView.show();
	}
}
