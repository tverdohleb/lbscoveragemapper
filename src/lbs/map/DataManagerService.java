package lbs.map;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.Gravity;
import android.widget.Toast;

public class DataManagerService extends Service implements LocationListener {

	private TelephonyManager tMgr;
	private String sLatitude = "-1", sLongitude = "-1", cid, lac, sTime,
			mSignalStrength, mnc, mcc, mDeviceId, mNetworkType;
	private Integer iNetworkType;
	private GsmCellLocation location;
	private TelephonyManager tm;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	private Double dLatitude, dLongitude;
	private FileHelper mLogFile;
	private Long lTime;
	private NotificationManager mNotificationManager;
	private int NOTIFICATION_ID;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Toast.makeText(this, "Coverage Mapper Service Created", Toast.LENGTH_LONG).show();

		// Get Telephone Manager and other info..
		tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mDeviceId = tMgr.getDeviceId();

		PhoneStateListener phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCellLocationChanged(CellLocation location) {
				// TODO Auto-generated method stub
				super.onCellLocationChanged(location);
				updateData();
			}

			@Override
			public void onCallStateChanged(final int state,
					final String incomingNumber) {
				super.onCallStateChanged(state, incomingNumber);
			}

			@Override
			public void onSignalStrengthChanged(int asu) {
				Integer iAsu = asu;
				mSignalStrength = iAsu.toString();
				updateData();
			}

		};

		tMgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION
				| PhoneStateListener.LISTEN_SIGNAL_STRENGTH);

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener ll = new mylocationlistener();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1500, 5, ll);

		mLogFile = new FileHelper(this);
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "Coverage Mapper Service Stopped", Toast.LENGTH_LONG).show();
		mNotificationManager.cancel(NOTIFICATION_ID);
	}

	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "Coverage Mapper Service Started", Toast.LENGTH_LONG).show();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification note = new Notification(android.R.drawable.ic_menu_mylocation, 
			    getResources().getString(R.string.app_name),
			    System.currentTimeMillis());
			note.flags |= Notification.FLAG_ONGOING_EVENT;
			PendingIntent i = PendingIntent.getActivity(this, 0, 
			    new Intent(this, MainActivity.class), 0);
			note.setLatestEventInfo(this, getResources().getString(R.string.app_name), 
			    "Coverage Mapper Service Started", i);
			mNotificationManager.notify(NOTIFICATION_ID, note);
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	protected void updateData() {
		iNetworkType = tMgr.getNetworkType();
		mNetworkType = iNetworkType.toString();

		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		location = (GsmCellLocation) tm.getCellLocation();

		Integer i_cid = location.getCid();
		Integer i_lac = location.getLac();

		cid = i_cid.toString();
		lac = i_lac.toString();

		String networkOperator = tm.getNetworkOperator();
		if (networkOperator != null && networkOperator.length() > 0) {
			try {
				mcc = getPaddedInt(Integer.parseInt(networkOperator.substring(
						0, 3)), 3);
				mnc = getPaddedInt(Integer.parseInt(networkOperator
						.substring(3)), 2);
			} catch (NumberFormatException e) {
			}
		}

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
			writeFile();
			if (true) {
				Toast t = Toast.makeText(getApplicationContext(),
						"Data Update", Toast.LENGTH_SHORT);
				t.setGravity(Gravity.BOTTOM, 0, 0);
				t.show();
			}
		} 
	}

	private void writeFile() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_kk");
		Date currentTime = new Date();
		String dateString = formatter.format(currentTime);

		lTime = System.currentTimeMillis();
		sTime = lTime.toString();
		String fileName = "out_" + dateString + ".txt";
		// String str =
		// "Time, CellID, Lac, Mcc, Mnc, Latitude, Longitude, Signal, DeviceId, NetworkType \n";
		String str = sTime + "," + cid + "," + lac + "," + mcc + "," + mnc
				+ "," + sLatitude + "," + sLongitude + "," + mSignalStrength
				+ "," + mDeviceId + "," + mNetworkType + "\n";
		mLogFile.writeToSd(fileName, str);
		
		Intent in = new Intent("ololo");
		in.putExtra("load", str);
		sendBroadcast(in);
	}

	private class mylocationlistener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				dLatitude = location.getLatitude();
				dLongitude = location.getLongitude();
				sLatitude = dLatitude.toString();
				sLongitude = dLongitude.toString();
				updateData();
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	}

	/**
	 * Convert an int to an hex String and pad with 0's up to minLen.
	 */
	String getPaddedHex(int nr, int minLen) {
		String str = Integer.toHexString(nr);
		if (str != null) {
			while (str.length() < minLen) {
				str = "0" + str;
			}
		}
		return str;
	}

	/**
	 * Convert an int to String and pad with 0's up to minLen.
	 */
	String getPaddedInt(int nr, int minLen) {
		String str = Integer.toString(nr);
		if (str != null) {
			while (str.length() < minLen) {
				str = "0" + str;
			}
		}
		return str;
	}
}
