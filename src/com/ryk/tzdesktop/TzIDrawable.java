package com.ryk.tzdesktop;

public interface TzIDrawable {
	public void Draw(android.graphics.Canvas canvas);
	public void Update();
	public void detectCollision(android.graphics.Point p);
}
