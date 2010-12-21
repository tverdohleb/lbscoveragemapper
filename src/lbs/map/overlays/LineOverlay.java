package lbs.map.overlays;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class LineOverlay extends Overlay {
	private GeoPoint mSource = null;
	private GeoPoint mDest = null;
	private Paint mPaint = null;

	public LineOverlay(GeoPoint source, GeoPoint dest, Paint paint) {
		mSource = source;
		mDest = dest;
		mPaint = paint;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		if (mSource != null && mDest != null) {
			Projection proj = mapView.getProjection();
			Point sourcePoint = proj.toPixels(mSource, null);
			Point destPoint = proj.toPixels(mDest, null);

			canvas.drawLine(sourcePoint.x, sourcePoint.y, destPoint.x,
					destPoint.y, mPaint);
		}
	}

	public void setPositions(GeoPoint source, GeoPoint dest) {
		mSource = source;
		mDest = dest;
	}
}
