package com.carrecorder.utils.animation;

import android.widget.ImageView;

public class SpeedPoindRoatation extends CompassRoatation{

	public SpeedPoindRoatation(ImageView image) {
		super(image);
		this.rotate(10);
	}
	/**
	 * speed unit km/s
	 * @param speed
	 */
	public void rotatePonit(double speed)
	{
		float degree = 10;//修正 方便P图的时候只需要把指针P的水平
		if(speed>240)
		{
			rotatePonit(240);
			return;
		}
		if(speed<0)
			return;
		degree += (float) (-speed/240*200);
		this.rotate(degree);
	}
	
}
