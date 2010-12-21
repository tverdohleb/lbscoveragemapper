/**
 * 
 */
package lbs.map;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.os.Build;
import android.util.Log;

/**
 * @author dima yavdoshenko
 * 
 */
public class HttpHelper {

	private HttpClient mClient;
	private HttpGet mHttpGet;
	private BasicResponseHandler mRsponseHandler;

	public HttpHelper() {

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params,
				HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);

		try {
			String userAgent = "CMapper " + " (" + Build.MODEL + ", Android "
					+ Build.VERSION.RELEASE + ")";

			// System.out.println(userAgent);
			HttpProtocolParams.setUserAgent(params, userAgent);

		} catch (Exception e) {
			HttpProtocolParams.setUserAgent(params, "android unknown");
		}

		// Create and initialize scheme registry
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		registry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));

		// Create an HttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				registry);

		HttpConnectionParams.setConnectionTimeout(params, 30000);
		HttpConnectionParams.setSoTimeout(params, 30000);

		mClient = new DefaultHttpClient(cm, params);

		mHttpGet = new HttpGet();
		mRsponseHandler = new BasicResponseHandler();
	}

	public String sendToServer(Coverage coverage) {
		String openApi_response = null;

		try {

			// Build the url
			StringBuilder uri = new StringBuilder(
					"http://emsviking.name/cell/put.php");
			uri.append("?cellid=").append(coverage.cid);
			uri.append("&mnc=").append(coverage.mnc);
			uri.append("&mcc=").append(coverage.mcc);
			uri.append("&lac=").append(coverage.lac);
			uri.append("&latitude=").append(coverage.sLatitude);
			uri.append("&longitude=").append(coverage.sLongitude);
			uri.append("&time=").append(coverage.sTime);
			uri.append("&signal=").append(coverage.mSignalStrength);
			uri.append("&id=").append(coverage.mDeviceId);
			uri.append("&network_type=").append(coverage.mNetworkType);

			mHttpGet = new HttpGet(uri.toString());
			try {
				openApi_response = mClient.execute(mHttpGet, mRsponseHandler);
			} catch (Exception e) {
				e.getMessage();
				return "error";
			}

		} catch (Exception e) {
			Log.e("ERROR", e.getMessage());
			return "error";
		}
		return openApi_response.toString();
	}
}
