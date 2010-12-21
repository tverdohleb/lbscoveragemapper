package lbs.map.listeners;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lbs.map.Coverage;
import lbs.map.MainActivity;
import lbs.map.R;
import lbs.map.RouteDistanceCount;
import lbs.map.overlays.ImageOverlay;
import lbs.map.overlays.LineOverlay;
import lbs.map.overlays.RouteOverlay;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.ibm.util.CoordinateConversion;
import com.ibm.util.CoordinateConversion.LatLon2MGRUTM;

public class MyLocationListener implements LocationListener {
	public static final int AUTO_CENTER_GPS = 0;
	public static final int AUTO_CENTER_NETWORK = 1;
	public static final int AUTO_CENTER_MIDPOINT = 2;
	public static final int AUTO_CENTER_NONE = 3;

	public static final int UNITS_METERS = 0;
	public static final int UNITS_FEET = 1;
	public static final int UNITS_MILES = 2;

	public static final int LOCATION_FMT_DDM = 0;
	public static final int LOCATION_FMT_DDMS = 1;
	public static final int LOCATION_FMT_DD = 2;
	public static final int LOCATION_FMT_MGRS = 3;

	private Context mContext = null;
	private GeoPoint mCoarseLastGP = null;
	private GeoPoint mFineLastGP = null;
	private Location mCoarseLastLocation = null;
	private Location mFineLastLocation = null;
	private TextView mTvLocationCoarseName = null;
	private TextView mTvLocationCoarse = null;
	private TextView mTvLocationFine = null;
	private MapView mMapView = null;
	private String mCoarseLocationProvider = null;
	private String mFineLocationProvider = null;
	private ImageOverlay mCoarseLocationOverlay = null;
	private ImageOverlay mFineLocationOverlay = null;
	private LineOverlay mLineOverlay = null;
	private TextView mTvCellBearing = null;
	private TextView mTvCellDirection = null;
	private boolean mAutoZoom = false;
	private int mAutoCenter = 0;
	private int mUnits = 0;
	private int mLocationFmt = 0;
	private boolean mCompass = false;
	private LatLon2MGRUTM mMgrsConversion;
	private File mOutputFile = null;
	private MyPhoneStateListener mPhoneStateListener;
	private boolean mSaveData = false;
	// private boolean mDirectQuery = false;
	private MyLocationOverlay mMyLocationOverlay;
	private LinearLayout mZoomLayout = null;
	private LinearLayout mZoom = null;
	private static String sSaveDataFile;
	public static ArrayList<Coverage> mCoverage;

	private Projection mProjection;

	public MyLocationListener(Activity a,
			MyPhoneStateListener phoneStateListener) {
		mContext = a;
		mPhoneStateListener = phoneStateListener;

		mCoverage = new ArrayList<Coverage>();

		MainActivity cf = (MainActivity) a;
		mCoarseLocationProvider = cf.getCoarseLocationProvider();
		mFineLocationProvider = cf.getFineLocationProvider();

		mTvLocationCoarseName = (TextView) a.findViewById(R.id.provider_name);
		mTvLocationCoarseName.setText(mCoarseLocationProvider);

		// mTvLocationCoarse = (TextView)
		// a.findViewById(R.id.tv_location_coarse);
		// mTvLocationFine = (TextView) a.findViewById(R.id.tv_location_fine);
		mMapView = (MapView) a.findViewById(R.id.mapview);
		mMapView.setBuiltInZoomControls(true);

		// mZoomLayout = (LinearLayout) a.findViewById(R.id.layout_zoom);
		// mZoom = (LinearLayout) mMapView.getZoomControls();

		// mTvCellBearing = (TextView) a.findViewById(R.id.tv_cell_bearing);
		mTvCellDirection = (TextView) a.findViewById(R.id.cell_direction);

		mCoarseLocationOverlay = new ImageOverlay(a.getResources().getDrawable(
				R.drawable.star_small), null);

		// only set up the fine location overlay and line overlay if the
		// gps is enabled
		if (mFineLocationProvider != null) {
			mFineLocationOverlay = new ImageOverlay(a.getResources()
					.getDrawable(R.drawable.reticle), null);

			Paint paint = new Paint();
			paint.setStrokeWidth(3);
			paint.setColor(Color.parseColor("#00B000"));
			mLineOverlay = new LineOverlay(null, null, paint);
		}

		// set text labels for location providers
		// TextView tmp = (TextView) a.findViewById(R.id.tv_location_fine_name);
		// if(mFineLocationProvider != null) {
		// tmp.setText(StyledResourceHelper.GetStyledString(mContext,
		// R.string.bold_fmt, mFineLocationProvider));
		// } else {
		// // if we have no fine location provider, set the location
		// // text to disabled
		// tmp.setText(StyledResourceHelper.GetStyledString(mContext,
		// R.string.bold_fmt, a.getResources().getString(
		// R.string.provider_gps)));
		//				
		// mTvLocationFine.setText(R.string.provider_disabled);
		// }

		mMgrsConversion = new CoordinateConversion().new LatLon2MGRUTM();

		mProjection = mMapView.getProjection();
		UpdateOverlays();
	}

	public void onLocationChanged(Location location) {
		Double dist = null;
		GeoPoint gp = getGeoPoint(location);
		String prov = location.getProvider();
		if (!prov.equals("gps")) {
			mTvLocationCoarseName.setText("Prov != GPS. Ignoring.");
			return;
		}

		mTvLocationCoarseName.setText(prov);
		if (null != mFineLastLocation) {
			dist = RouteDistanceCount.getDistance(location.getLatitude(),
					location.getLongitude(), mFineLastLocation.getLatitude(),
					mFineLastLocation.getLongitude());
			if (!gp.equals(mFineLastGP)) {
				mFineLastLocation = location;

				Coverage geoPoint = new Coverage();
				geoPoint.sLatitude = Double.toString(location.getLatitude());
				geoPoint.sLongitude = Double.toString(location.getLongitude());
				geoPoint.cid = Integer.toString(mPhoneStateListener.getCid());

				mCoverage.add(geoPoint);
				if (10 < mCoverage.size()) {
					mCoverage.remove(0);
				}
				mTvCellDirection.setText(Integer.toString(mCoverage.size()));

				mFineLastGP = gp;

				UpdateOverlays();
				zoomAndCenterMap();
			}
		} else {
			mFineLastLocation = location;
		}

	}

	private GeoPoint getGeoPoint(Location location) {
		Double lat = location.getLatitude() * 1E6;
		Double lon = location.getLongitude() * 1E6;
		return new GeoPoint(lat.intValue(), lon.intValue());
	}

	public void onProviderDisabled(String provider) {
		TextView tv = getTextViewForProvider(provider);
		if (tv != null) {
			// tv.setText(mContext.getString(R.string.provider_disabled));
		}
	}

	public void onProviderEnabled(String provider) {
		TextView tv = getTextViewForProvider(provider);
		if (tv != null) {
			// tv.setText(mContext.getString(R.string.provider_waiting));
		}
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public void pause() {
		if (mMyLocationOverlay != null && mCompass) {
			mMyLocationOverlay.disableCompass();
		}
	}

	public void resume() {
		if (mMyLocationOverlay != null && mCompass) {
			mMyLocationOverlay.enableCompass();
			mMapView.invalidate();
		}
	}

	public void setAutoCenter(int autocenter) {
		mAutoCenter = autocenter;
		zoomAndCenterMap();
	}

	public void setAutoZoom(boolean enabled) {
		mAutoZoom = enabled;
		// mZoomLayout.removeView(mZoom);

		if (!mAutoZoom) {
			// add zoom control back if auto-zoom is disabled
			// mZoomLayout.addView(mZoom);
		}

		zoomAndCenterMap();
	}

	public void setLocationFormat(int fmt) {
		mLocationFmt = fmt;
	}

	public void setSatellite(boolean enabled) {
		mMapView.setSatellite(enabled);
		mMapView.invalidate();
	}

	public void setUnits(int units) {
		mUnits = units;
	}

	public void setCompass(boolean enabled) {
		mCompass = enabled;
		if (enabled) {
			mMyLocationOverlay = new MyLocationOverlay(mContext, mMapView);
			mMyLocationOverlay.disableMyLocation();
			mMyLocationOverlay.enableCompass();
		} else {
			mMyLocationOverlay = null;
		}
	}

	private String getDDM(double coord) {
		if (coord < 0)
			coord = -coord;

		int deg = (int) Math.floor(coord);
		coord -= deg;
		coord *= 60;

		DecimalFormat df = new DecimalFormat(mContext
				.getString(R.string.min_fmt));
		return String.format(mContext.getString(R.string.ddm_fmt), deg, df
				.format(coord));
	}

	private String getDDMS(double coord) {
		if (coord < 0)
			coord = -coord;

		int deg = (int) Math.floor(coord);
		coord -= deg;
		coord *= 60;

		int min = (int) Math.floor(coord);
		coord -= min;
		coord *= 60;

		DecimalFormat df = new DecimalFormat(mContext
				.getString(R.string.sec_fmt));
		return String.format(mContext.getString(R.string.ddms_fmt), deg, min,
				df.format(coord));
	}

	private String getDD(double coord) {
		if (coord < 0)
			coord = -coord;

		DecimalFormat df = new DecimalFormat(mContext
				.getString(R.string.deg_fmt));

		return String.format(mContext.getString(R.string.dd_fmt), df
				.format(coord));
	}

	private String getMGRS(double lat, double lon) {
		return mMgrsConversion.convertLatLonToMGRUTM(lat, lon);
	}

	private Location getLastLocationForProvider(String provider) {
		if (provider.equals(mFineLocationProvider)) {
			return mFineLastLocation;
		} else if (provider.equals(mCoarseLocationProvider)) {
			return mCoarseLastLocation;
		}

		return null;
	}

	private String getNiceLocation(Location loc) {
		StringBuilder sb = new StringBuilder();
		double lat = loc.getLatitude();
		double lon = loc.getLongitude();

		String latStr, lonStr;

		switch (mLocationFmt) {
		case LOCATION_FMT_DD:
			latStr = getDD(lat);
			lonStr = getDD(lon);
			break;
		case LOCATION_FMT_DDM:
			latStr = getDDM(lat);
			lonStr = getDDM(lon);
			break;
		case LOCATION_FMT_MGRS:
			sb.append(getMGRS(lat, lon));
		default: // LOCATION_FMT_DDMS
			latStr = getDDMS(lat);
			lonStr = getDDMS(lon);
			break;
		}

		if (mLocationFmt != LOCATION_FMT_MGRS) {
			sb.append(latStr);
			if (lat > 0)
				sb.append("N");
			else
				sb.append("S");

			sb.append(", ");
			sb.append(lonStr);
			if (lon > 0)
				sb.append("E");
			else
				sb.append("W");
		}

		return sb.toString();
	}

	private TextView getTextViewForProvider(String provider) {
		if (provider.equals(mFineLocationProvider)) {
			return mTvLocationFine;
		} else if (provider.equals(mCoarseLocationProvider)) {
			return mTvLocationCoarse;
		}

		return null;
	}

	private double metersToFeet(double meters) {
		return meters * 3.28;
	}

	private double metersToMiles(double meters) {
		return meters / 1609;
	}

	private void UpdateOverlays() {
		mCoarseLocationOverlay.setLocation(mCoarseLastGP);
		if (mFineLocationOverlay != null) {
			mFineLocationOverlay.setLocation(mFineLastGP);
			mLineOverlay.setPositions(mFineLastGP, mCoarseLastGP);
		}

		// add overlays
		List<Overlay> overlays = mMapView.getOverlays();
		overlays.clear();

		// only add the line and the fine overlay if the fine location overlay
		// exists
		if (mFineLocationOverlay != null) {
			overlays.add(mLineOverlay);
			overlays.add(mFineLocationOverlay);
		}

		overlays.add(mCoarseLocationOverlay);

		if (mMyLocationOverlay != null && mCompass) {
			overlays.add(mMyLocationOverlay);
		}
		zoomAndCenterMap();
		overlays.add(new RouteOverlay(mCoverage, mProjection));
		mMapView.invalidate();
	}

	private void zoomAndCenterMap() {
		MapController mc = mMapView.getController();

		if (mFineLastGP != null && mCoarseLastGP != null) {
			int lat1 = mFineLastGP.getLatitudeE6();
			int lon1 = mFineLastGP.getLongitudeE6();

			int lat2 = mCoarseLastGP.getLatitudeE6();
			int lon2 = mCoarseLastGP.getLongitudeE6();

			int latspan = Math.max(lat1, lat2) - Math.min(lat1, lat2);
			int lonspan = Math.max(lon1, lon2) - Math.min(lon1, lon2);

			// handle zooming
			if (mAutoZoom) {
				// mc.zoomToSpan(latspan, lonspan);

				// if we're using a centering option that isn't the midpoint, we
				// have to
				// zoom out one level to see both points
				// if (mAutoCenter != AUTO_CENTER_MIDPOINT)
				// mc.setZoom(mMapView.getZoomLevel() - 1);
			}

			if (mAutoCenter == AUTO_CENTER_GPS) {
				mc.setCenter(mFineLastGP);
			} else if (mAutoCenter == AUTO_CENTER_NETWORK) {
				mc.setCenter(mCoarseLastGP);
			} else if (mAutoCenter == AUTO_CENTER_MIDPOINT) {
				int latcen = (Math.max(lat1, lat2) + Math.min(lat1, lat2)) / 2;
				int loncen = (Math.max(lon1, lon2) + Math.min(lon1, lon2)) / 2;
				mc.setCenter(new GeoPoint(latcen, loncen));
			}
		} else if (mFineLastGP != null) {
			// auto center on gps if that's enabled
			if (mAutoCenter != AUTO_CENTER_NONE) {
				mc.setCenter(mFineLastGP);
			}

			// set zoom level to something reasonable - autozoom is designed
			// for when there's 2 points, doesn't really matter when there's
			// only one point
			if (mAutoZoom) {
				// mc.setZoom(mMapView.getMaxZoomLevel() - 2);
			}
		} else if (mCoarseLastGP != null) {
			// auto center on network if that's enabled
			if (mAutoCenter != AUTO_CENTER_NONE) {
				mc.setCenter(mCoarseLastGP);
			}

			if (mAutoZoom) {
				// mc.setZoom(mMapView.getMaxZoomLevel() - 2);
			}
		}
		// mc.setZoom(6);
	}

	public void setSaveData(boolean saveData) {
		Activity a = (Activity) mContext;
		if (saveData) {
			// a.setTitle(a.getString(R.string.app_name) +
			// a.getString(R.string.saving_data));

			// only create a new data file if the name has been null'd
			if (sSaveDataFile == null) {
				// sSaveDataFile = getSaveDataFilename();
			}
		} else {
			a.setTitle(a.getString(R.string.app_name));
			// sSaveDataFile = null;
		}

		// mSaveData = saveData;
	}

}
