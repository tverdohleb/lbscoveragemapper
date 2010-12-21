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

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Window;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		requestWindowFeature(Window.FEATURE_LEFT_ICON);
//		setContentView(R.layout.about);
//
//		Window window = getWindow();
//		window.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
//				R.drawable.icon_48x48);
//
//		PackageManager packageManager = getPackageManager();
//		PackageInfo packageInfo;
//		try {
//			String me = getString(R.string.app_package);
//			packageInfo = packageManager.getPackageInfo(me, 0);
//		} catch (NameNotFoundException e) {
//			// oh well
//			return;
//		}
//		
//		setTitle(String.format(getString(R.string.about_title_fmt, packageInfo.versionName)));
	}
}
