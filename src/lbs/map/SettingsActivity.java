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

package lbs.map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		refreshSettings();
	}

	private void refreshSettings() {
		Resources resources = getResources();
		
		ListPreference center = (ListPreference) findPreference(
				resources.getString(R.string.pref_title_auto_center));
		center.setSummary(center.getEntry());

		ListPreference units = (ListPreference) findPreference(
				resources.getString(R.string.pref_title_dist_unit));
		units.setSummary(units.getEntry());

		ListPreference refresh = (ListPreference) findPreference(
				resources.getString(R.string.pref_title_location_refresh));
		refresh.setSummary(refresh.getEntry());
		
		ListPreference coordfmt = (ListPreference) findPreference(
				resources.getString(R.string.pref_title_coord_fmt));
		coordfmt.setSummary(coordfmt.getEntry());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		// register for updates
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshSettings();
	}
}
