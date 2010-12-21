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

package lbs.map.utils;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;

public class StyledResourceHelper {
	public static CharSequence GetStyledString(Context context, int stringid,
			Object... args) {

		String str = context.getString(stringid);
		if (str != null) {
			// check for string args, then htmlEocde them
			for (int i = 0; i < args.length; i++) {
				if (args[i].getClass() == String.class)
					args[i] = TextUtils.htmlEncode((String) args[i]);
			}

			// format string, then convert to a CharSequence
			String strtxt = String.format(str, args);
			return Html.fromHtml(strtxt);
		} else {
			return null;
		}
	}
}
