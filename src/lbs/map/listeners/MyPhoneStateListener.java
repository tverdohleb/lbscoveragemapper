/*
 * Copyright (C) 2008 Jon Larimer <jlarimer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lbs.map.listeners;

import java.util.Hashtable;

import lbs.map.R;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.gsm.GsmCellLocation;
import android.widget.TextView;

public class MyPhoneStateListener extends PhoneStateListener {
	public static String LOCTYPE_CENTROID = "centroid";
	public static String LOCTYPE_TOWER = "tower";
	public static String LOCTYPE_UNKNOWN = "unknown";
	
	// the "waiting" location type means we made a request but are 
	// still waiting on a response
	public static String LOCTYPE_WAITING = "waiting";  
	
	private TextView mTvOperText = null;
	private TextView mTvSignalStr = null;
	private TextView mTvCellCid = null;
	private TextView mTvCellLac = null;
	private Context mContext = null;
	private LocationCache mLocationCache = null;
	private Location mWaitingLocation = null;
	private MyLocationListener mLocationListener = null;

	private String mOperStr = "";
	private int mCid = -1;
	private int mLac = -1;
	private int mDbm = -1;
	private int mMcc = -1;
	private int mMnc = -1;
	
	public MyPhoneStateListener(Activity a) {
		// initialize our textviews
		mTvOperText = (TextView) a.findViewById(R.id.operator_name);
//		mTvOperNum = (TextView) a.findViewById(R.id.tv_oper_num);
		mTvSignalStr = (TextView) a.findViewById(R.id.signal_str);
		mTvCellCid = (TextView) a.findViewById(R.id.cell_cid);
		mTvCellLac = (TextView) a.findViewById(R.id.cell_lac);

		// we need the context to get various string values
		mContext = a;
	}

	public void setLocationListener(MyLocationListener listener) {
		mLocationListener = listener;
	}
	
	@Override
	public void onCellLocationChanged(CellLocation location) {
		//Log.d(CellFinderMapActivity.CELLFINDER,
		//		"MyPhoneStateListener.OnCellLocationChanged()");
		super.onCellLocationChanged(location);

		if (location.getClass() == GsmCellLocation.class) {
			GsmCellLocation gsmloc = (GsmCellLocation) location;

			final int cid = mCid = gsmloc.getCid();
			final int lac = mLac = gsmloc.getLac();
			String unknown = mContext.getString(R.string.unknown);

			// don't print a -1 in the UI, that's hella weak
			String cidStr = cid == -1 ? unknown : Integer.toString(cid);
			String lacStr = lac == -1 ? unknown : Integer.toString(lac);
			
			// get short cid - lower 16 bits
//			int shortcid = cid & 0xffff;
//			String shortCidStr = cid == -1 ? unknown : Integer.toString(shortcid);

			mTvCellCid.setText(cidStr);
			mTvCellLac.setText(lacStr);
			
			// do direct query if it's enabled and we have enough info
		}
	}

	@Override
	public void onServiceStateChanged(ServiceState serviceState) {
		// Log.d(CellFinderMapActivity.CELLFINDER,
		// "MyPhoneStateListener.onServiceStateChanged()");
		super.onServiceStateChanged(serviceState);

		int state = serviceState.getState();
		switch (state) {
		case ServiceState.STATE_IN_SERVICE:
		case ServiceState.STATE_EMERGENCY_ONLY:
			mOperStr = serviceState.getOperatorAlphaLong();
			mTvOperText.setText(mOperStr);
			

			String op = serviceState.getOperatorNumeric();
			
			if (op.length() > 3) {
				String mccStr = op.substring(0, 3);
				String mncStr = op.substring(3);
				
				try {
					mMcc = Integer.parseInt(mccStr);
					mMnc = Integer.parseInt(mncStr);
				} catch (Exception e) { }
				
//				mTvOperNum.setText(StyledResourceHelper.GetStyledString(
//						mContext, R.string.opernum_fmt, mccStr, mncStr));
			} else {
//				mTvOperNum.setText(op);
			}

			break;
		case ServiceState.STATE_POWER_OFF:
			clearTextViews();
			mOperStr = mContext.getString(R.string.service_state_poweroff);
//			mTvOperText.setText(mOperStr);
			break;
		case ServiceState.STATE_OUT_OF_SERVICE:
			clearTextViews();
			
			mOperStr = mContext.getString(R.string.service_state_noservice);
//			mTvOperText.setText(mOperStr);
			
			break;
		}
	}

	@Override
	public void onSignalStrengthChanged(int asu) {
		// Log.d(CellFinderMapActivity.CELLFINDER,
		// "MyPhoneStateListener.onSignalStrengthChanged()");
		super.onSignalStrengthChanged(asu);

		// dBm calculation comes from PhoneStateIntentReceiver.java in the 
		// Android source code
		String dbmStr = "Unknown";
		
		if(asu != -1) {
			mDbm = ((Integer)(-113 + 2*asu));
			
			if(mDbm == -113) dbmStr = "-113 or less";
			else if (mDbm >= -51) dbmStr = "-51 or greater";
			else {
				dbmStr = Integer.toString(mDbm);
			}
		}
		
		mTvSignalStr.setText(dbmStr);
	}

	private void clearTextViews() {
//		mTvOperText.setText("");
//		mTvOperNum.setText("");
//		mTvCellCidLac.setText("");
//		mTvSignalStr.setText("");
	}

	public String getOperStr() {
		return mOperStr;
	}

	public int getCid() {
		return mCid;
	}

	public int getLac() {
		return mLac;
	}

	public int getDbm() {
		return mDbm;
	}

	public int getMcc() {
		return mMcc;
	}

	public int getMnc() {
		return mMnc;
	}
	
	private static class LocationCache {
		private Hashtable<String, Location> _hash = new Hashtable<String, Location>();
		
		public LocationCache() { }
		
		public Location GetLocation(int mcc, int mnc, int cid, int lac) {
			String key = getKey(mcc, mnc, cid, lac);
			if(_hash.containsKey(key)) {
				return _hash.get(key);
			} else {
				return null;
			}
		}
		
		public void SetLocation(int mcc, int mnc, int cid, int lac, Location location) {
			_hash.put(getKey(mcc, mnc, cid, lac), location);
		}
		
		private static String getKey(int mcc, int mnc, int cid, int lac) {
			return String.format("%d:%d:%d:%d", mcc, mnc, cid, lac);
		}
	}
}
