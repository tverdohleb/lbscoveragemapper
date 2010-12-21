package lbs.map;

import java.util.ArrayList;
import java.util.List;

import lbs.map.listeners.MyLocationListener;
import lbs.map.listeners.MyPhoneStateListener;
import lbs.map.utils.ErrorDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Criteria;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class MainActivity extends MapActivity implements
		OnSharedPreferenceChangeListener, OnClickListener {

	public static final String APP_TAG = "COVERAGE_MAPPER";
	private FileHelper mLogFile;
	private String mCoarseLocationProvider;
	private SharedPreferences mPreferences;
	private LocationManager mLocationManager;
	private TelephonyManager mTelephonyManager;
	private MainActivity mThis = this;
	private String mFineLocationProvider;
	private MyLocationListener mLocationListener;
	private MyPhoneStateListener mPhoneStateListener;
	public ArrayList<Coverage> mCoverage;
	private Button buttonStart, buttonStop;
	private MapView mMapView = null;

	// location refresh time, in seconds
	private int mLocationRefresh = 30;

	// options menu items
	private static final int MENU_ITEM_SETTINGS = 0;
	private static final int MENU_ITEM_ABOUT = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// load preferences
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		mPreferences.registerOnSharedPreferenceChangeListener(this);

		// get the location manager reference
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		setContentView(R.layout.map);
		
		mMapView = (MapView) findViewById(R.id.mapview);
		mMapView.setBuiltInZoomControls(true);
		mMapView.getController().setZoom(15);

		startService(new Intent(this, DataManagerService.class));

		mLogFile = new FileHelper(this);

		sendData();

	    buttonStart = (Button) findViewById(R.id.buttonStart);
	    buttonStop = (Button) findViewById(R.id.buttonStop);

	    buttonStart.setOnClickListener(this);
	    buttonStop.setOnClickListener(this);
	}

	public void sendData() {
		Thread sendToServer = new Thread(new Runnable() {
			@Override
			public void run() {
				mLogFile.getFileList();
			}
		});
		sendToServer.start();
	}

	// onclicklistener to call finish() when the ok button on the error dialog
	// is pressed
	private DialogInterface.OnClickListener errorFinishListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			finish();
		}
	};

	// onclicklistener to just show a dialog box and ignore whatever the user
	// does
	private DialogInterface.OnClickListener errorContinueListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {

		}
	};

	private PhoneStateListener statusCheckListener = new PhoneStateListener() {
		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			// unregister this callback
			mTelephonyManager.listen(this, LISTEN_NONE);

			int state = serviceState.getState();
			switch (state) {
			case ServiceState.STATE_OUT_OF_SERVICE:
				ErrorDialog.Show(mThis,
						getString(R.string.service_error_no_service),
						errorFinishListener);
				break;
			case ServiceState.STATE_POWER_OFF:
				ErrorDialog.Show(mThis,
						getString(R.string.service_error_phone_off),
						errorFinishListener);
				break;
			}
		}
	};

	public String getCoarseLocationProvider() {
		return mCoarseLocationProvider;
	}

	public String getFineLocationProvider() {
		return mFineLocationProvider;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem settings = menu.add(Menu.NONE, MENU_ITEM_SETTINGS, Menu.NONE,
				R.string.menu_title_settings);
		settings.setIcon(android.R.drawable.ic_menu_preferences);

		MenuItem about = menu.add(Menu.NONE, MENU_ITEM_ABOUT, Menu.NONE,
				R.string.menu_title_about);
		about.setIcon(android.R.drawable.ic_menu_info_details);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SETTINGS:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case MENU_ITEM_ABOUT:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

//		loadPreferences();
	}

	// try to get the location provider with the given name. if that fails, go
	// for accuracy
	private String getEnabledLocationProvider(String name, int accuracy) {
		if (mLocationManager != null) {
			// first check the provider with the given name
			if (mLocationManager.isProviderEnabled(name))
				return name;

			// if no dice, search for one with the given accuracy
			Criteria criteria = new Criteria();
			criteria.setAccuracy(accuracy);

			List<String> providers = mLocationManager.getProviders(criteria,
					true);
			if (providers != null && !providers.isEmpty()) {
				return providers.get(0);
			}
		}

		// nothing? return null
		return null;
	}

	// try to get a requested location provider, but return an alternate enabled
	// one if found. generates error and warning dialogs if there are issues.
	// returns null if nothing is found.
	private String getLocationProvider(String provRequested,
			String accuracy_str, int accuracy_int) {

		String provFound = getEnabledLocationProvider(provRequested,
				accuracy_int);

		if (provFound != null) {
			// it's possible to request coarse accuracy but get a fine accuracy
			// provider, so check it
			// just in case.
			if (!provFound.equals(provRequested)) {
				LocationProvider locProv = mLocationManager
						.getProvider(provFound);
				if (locProv.getAccuracy() == accuracy_int) {
					// if we found an acceptable location provider, give the
					// user a friendly warning to
					// indicate it isn't the one we want
					String msg = String.format(
							getString(R.string.provider_warning),
							provRequested, provFound);
					// Log.d(CELLFINDER, msg);
					ErrorDialog.Show(this, msg, errorContinueListener);
				} else {
					// accuracy mismatch
					provFound = null;
				}
			}
		}

		// if(provFound != null)
		// Log.d(CELLFINDER, "using location provider: " + provFound);

		return provFound;
	}

	private void loadPreferences() {
		boolean autoZoom = mPreferences.getBoolean(
				getString(R.string.pref_title_auto_zoom), true);
		boolean satellite = mPreferences.getBoolean(
				getString(R.string.pref_title_satellite), true);
		String autoCenter = mPreferences.getString(
				getString(R.string.pref_title_auto_center), null);
		String units = mPreferences.getString(
				getString(R.string.pref_title_dist_unit), null);
		String locationRefresh = mPreferences.getString(
				getString(R.string.pref_title_location_refresh), null);
		boolean showCellData = mPreferences.getBoolean(
				getString(R.string.pref_title_show_cell_data), true);
		String coordFmt = mPreferences.getString(
				getString(R.string.pref_title_coord_fmt), null);
		boolean compass = mPreferences.getBoolean(
				getString(R.string.pref_title_compass), false);
		boolean saveData = mPreferences.getBoolean(
				getString(R.string.pref_title_savedata), false);
		boolean directQuery = mPreferences.getBoolean(
				getString(R.string.pref_title_directquery), false);

		// show or hide the cell info view
		// if (!showCellData) {
		// ((LinearLayout) findViewById(R.id.cell_info_view))
		// .setVisibility(View.GONE);
		// } else {
		// ((LinearLayout) findViewById(R.id.cell_info_view))
		// .setVisibility(View.VISIBLE);
		// }

		// look up location refresh, default to 60
		if (locationRefresh != null) {
			// this is stored as a string like "5 seconds" in the prefrence
			// database
			int idx = locationRefresh.indexOf(' ');
			if (idx != -1) {
				String num = locationRefresh.substring(0, idx);
				try {
					int newrefresh = Integer.parseInt(num);
					// if the refresh value changes, re-start the listeners
					if (mLocationRefresh != newrefresh) {
						mLocationRefresh = newrefresh;
						restartListeners();
					}
				} catch (NumberFormatException e) {
					// don't do anything
				}
			}
		}

		// Log.d(CELLFINDER,
		// String.format("Preferences loaded: zoom %b, satellite %b, auto-center %s",
		// mAutoZoom, mSatellite, mAutoCenter));

		// if the listener is running, update it with the current prefrences
		if (mLocationListener != null) {
			mLocationListener.setSatellite(satellite);
			mLocationListener.setAutoZoom(autoZoom);

			// set auto-centering preferences by integer becuase it's less
			// typing. also faster
			// when the map has to decide how to update
			if (getString(R.string.pref_center_option_midpoint).equals(
					autoCenter)) {
				mLocationListener
						.setAutoCenter(MyLocationListener.AUTO_CENTER_MIDPOINT);
			} else if (getString(R.string.pref_center_option_network).equals(
					autoCenter)) {
				mLocationListener
						.setAutoCenter(MyLocationListener.AUTO_CENTER_NETWORK);
			} else if (getString(R.string.pref_center_option_none).equals(
					autoCenter)) {
				mLocationListener
						.setAutoCenter(MyLocationListener.AUTO_CENTER_NONE);
			} else {
				// default to gps
				mLocationListener
						.setAutoCenter(MyLocationListener.AUTO_CENTER_GPS);
			}

			// set distance unit preferences
			if (getString(R.string.pref_value_dist_unit_feet).equals(units)) {
				mLocationListener.setUnits(MyLocationListener.UNITS_FEET);
			} else if (getString(R.string.pref_value_dist_unit_miles).equals(
					units)) {
				mLocationListener.setUnits(MyLocationListener.UNITS_MILES);
			} else {
				mLocationListener.setUnits(MyLocationListener.UNITS_METERS);
			}

			// set coordinate format preference
			if (getString(R.string.pref_value_coord_fmt_dd).equals(coordFmt)) {
				mLocationListener
						.setLocationFormat(MyLocationListener.LOCATION_FMT_DD);
			} else if (getString(R.string.pref_value_coord_fmt_ddms).equals(
					coordFmt)) {
				mLocationListener
						.setLocationFormat(MyLocationListener.LOCATION_FMT_DDMS);
			} else if (getString(R.string.pref_value_coord_fmt_mgrs).equals(
					coordFmt)) {
				mLocationListener
						.setLocationFormat(MyLocationListener.LOCATION_FMT_MGRS);
			} else {
				mLocationListener
						.setLocationFormat(MyLocationListener.LOCATION_FMT_DDM);
			}

			mLocationListener.setSaveData(saveData);

			// set direct query mode
//			mLocationListener.setDirectQueryMode(directQuery);
//			mPhoneStateListener.setDirectQueryMode(directQuery);
		}
	}

	private void setupListeners() {
		// look for the 'network' location provider or something else that might
		// be acceptable. if there are issues, error dialogs are generated. if
		// null is returned, this app won't work so just return
		mCoarseLocationProvider = getLocationProvider(
				getString(R.string.provider_network),
				getString(R.string.provider_coarse), Criteria.ACCURACY_COARSE);

		if (mCoarseLocationProvider == null) {
			ErrorDialog.Show(this, getString(R.string.provider_coarse_error),
					errorFinishListener);
			return;
		}

		// now look for the 'gps' location provider. if we don't have it, we'll
		// just do without
		mFineLocationProvider = getLocationProvider(
				getString(R.string.provider_gps),
				getString(R.string.provider_fine), Criteria.ACCURACY_FINE);

		// get phone listener and telephony manager, but don't start
		// anything yet
		mPhoneStateListener = new MyPhoneStateListener(this);
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		// set up location listener and manager, but don't start anything yet
		mLocationListener = new MyLocationListener(this, mPhoneStateListener);
		mPhoneStateListener.setLocationListener(mLocationListener);

		// check to see if the phone is turned on. this listener
		// checks for service and displays a dialog box if there are
		// any problems. the callback immediately unregisters itself.
		mTelephonyManager.listen(statusCheckListener,
				PhoneStateListener.LISTEN_SERVICE_STATE);
	}

	// start the two listeners - called during onResume and when refreshing
	// setting changes
	private void startListeners() {
		// start the phone state listener
		if (mTelephonyManager != null && mPhoneStateListener != null) {

			// get the location and call the listener location changed callback.
			// sometimes if the cell changes while the app is stopped, it won't
			// get the latest location when it resumes
			CellLocation location = mTelephonyManager.getCellLocation();
			mPhoneStateListener.onCellLocationChanged(location);

			mTelephonyManager.listen(mPhoneStateListener,
					PhoneStateListener.LISTEN_CELL_LOCATION
							| PhoneStateListener.LISTEN_SERVICE_STATE
							| PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
		}

		// start the location listeners
		if (mLocationListener != null) {
			if (mCoarseLocationProvider != null)
				mLocationManager.requestLocationUpdates(
						mCoarseLocationProvider, mLocationRefresh * 1000, 0,
						mLocationListener);

			if (mFineLocationProvider != null)
				mLocationManager.requestLocationUpdates(mFineLocationProvider,
						mLocationRefresh * 1000, 0, mLocationListener);
		}
	}

	// stop the listeners - called during onPause and when refreshing setting
	// changes
	private void stopListeners() {
		// stop the telephony listener
		if (mTelephonyManager != null && mPhoneStateListener != null) {
			mTelephonyManager.listen(mPhoneStateListener,
					PhoneStateListener.LISTEN_NONE);
		}

		// stop the location listener
		if (mLocationManager != null && mLocationListener != null) {
			mLocationManager.removeUpdates(mLocationListener);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}


	@Override
	protected void onResume() {
		// Log.d(CELLFINDER,
		// "CellFinder.onResume() - starting telephony and location listeners");
		super.onResume();
//
		setupListeners();
		loadPreferences();
		startListeners();
//
//		if (mLocationListener != null) {
//			mLocationListener.resume();
//		}
	}

	private void restartListeners() {
		stopListeners();
		setupListeners();
		loadPreferences();
		startListeners();
	}
	
	  public void onClick(View src) {
		    switch (src.getId()) {
		    case R.id.buttonStart:
//		      Log.d(TAG, "onClick: starting srvice");
		      startService(new Intent(this, DataManagerService.class));
		      break;
		    case R.id.buttonStop:
//		      Log.d(TAG, "onClick: stopping srvice");
		      stopService(new Intent(this, DataManagerService.class));
		      break;
		    }
		  }

}