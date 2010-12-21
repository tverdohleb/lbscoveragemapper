package lbs.map.overlays;

import java.util.ArrayList;

import lbs.map.Coverage;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class RouteOverlay extends Overlay {
	private Paint mPaint;
	private Path path;
	private ArrayList<Coverage> coverage;
	private Projection projection;
	private ArrayList<Integer> mColors;
	private int cellIDiterator = -1;
	private Integer oldCid = 0;

	public RouteOverlay(ArrayList<Coverage> coverage, Projection projection) {
		this.coverage = coverage;
		this.projection = projection;
	}

	public void draw(Canvas canvas, MapView mapv, boolean shadow) {
		super.draw(canvas, mapv, shadow);

		mColors = new ArrayList<Integer>();

		mColors.add(Color.BLUE);
		mColors.add(Color.CYAN);
		mColors.add(Color.DKGRAY);
		mColors.add(Color.GRAY);
		mColors.add(Color.GREEN);
		mColors.add(Color.LTGRAY);
		mColors.add(Color.MAGENTA);
		mColors.add(Color.RED);
		mColors.add(Color.YELLOW);

		mPaint = new Paint();
		mPaint.setDither(true);
		// mPaint.setColor(mColors.get(posit));
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(5);

		GeoPoint gP1 = null;
		path = new Path();

		Point p1 = new Point();
		Point p2 = new Point();
		for (Coverage point : coverage) {
			GeoPoint gP2 = new GeoPoint(
					(int) (Double.valueOf(point.sLatitude) * 1E6),
					(int) (Double.valueOf(point.sLongitude) * 1E6));
			if (null != gP1) {
/*
				Integer cid = (int) Integer.valueOf(point.cid);
				if (!cid.equals(oldCid)) {
					cellIDiterator++;
					if (cellIDiterator >= mColors.size()) {
						cellIDiterator = 0;
					}
//					oldCid = Integer.valueOf(point.cid);
					// canvas.drawPath(path, mPaint);
					// path = new Path();
				}

*/
				// mPaint.setColor(mColors. get(posit));

				projection.toPixels(gP1, p1);
				projection.toPixels(gP2, p2);
				
				path.moveTo(p2.x, p2.y);
				path.lineTo(p1.x, p1.y);
				// canvas.drawPath(path, mPaint);
			}
			gP1 = gP2;
		}

		canvas.drawPath(path, mPaint);
	}
}