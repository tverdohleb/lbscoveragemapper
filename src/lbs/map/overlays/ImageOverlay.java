package lbs.map.overlays;


import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class ImageOverlay extends Overlay {
	private Drawable mDrawable = null;
	private GeoPoint mLocation = null;

	public ImageOverlay(Drawable drawable, GeoPoint location) {
		mDrawable = drawable;
		mLocation = location;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		if (mLocation != null) {
			Projection proj = mapView.getProjection();
			Point p = proj.toPixels(mLocation, null);

			int width = mDrawable.getMinimumWidth();
			int height = mDrawable.getMinimumHeight();

			int x = p.x - width / 2;
			int y = p.y - height / 2;

			mDrawable.setBounds(x, y, x + width, y + height);
			mDrawable.draw(canvas);
		}
	}

	public void setLocation(GeoPoint location) {
		mLocation = location;
	}
}
