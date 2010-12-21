package lbs.map.overlays;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class PointOverlay extends Overlay {
        private GeoPoint mPoint;
        private Drawable mMarker;
        private int mMarkerXOffset;
        private int mMarkerYOffset;

        public PointOverlay(Location location, Drawable marker, int XOffset,
                        int YOffset) {
                int lat = (int) (location.getLatitude() * 1E6);
                int lon = (int) (location.getLongitude() * 1E6);
                mPoint = new GeoPoint(lat, lon);
                mMarker = marker;
                mMarkerXOffset = XOffset;
                mMarkerYOffset = YOffset;
        }

        public PointOverlay(GeoPoint point, Drawable marker,  int XOffset,
                        int YOffset) {
                mPoint = point;
                mMarker = marker;
                mMarkerXOffset = XOffset;
                mMarkerYOffset = YOffset;
        }

        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
                Point point = new Point();
                Projection projection = mapView.getProjection();
                projection.toPixels(mPoint, point);
                drawAt(canvas, mMarker, point.x + mMarkerXOffset, point.y
                                + mMarkerYOffset, shadow);
        }
}
