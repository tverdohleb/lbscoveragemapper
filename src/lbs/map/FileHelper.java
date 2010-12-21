package lbs.map;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lbs.map.listeners.MyLocationListener;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileHelper {

	private List<String> directoryEntries = new ArrayList<String>();
//	private double myProgress = 0, myProgressStep;

	// private ProgressDialog dialog;
	// private Context myContext;
	// private int increment;
	// private List<Coverage> aCoverage = new ArrayList<Coverage>();

	public FileHelper(Context context) {
		super();
		// myContext = context;
	}

	/**********************************************************************************************/
	public List<String> getFileList() {
		File testDir = null;

		File sdDir = new File(Environment.getExternalStorageDirectory()
				.getPath());

		if (sdDir.exists() && sdDir.canWrite()) {
			testDir = new File(sdDir.getAbsolutePath() + "/Coverage_Mapper");
			fill(testDir.listFiles());
		}

		return this.directoryEntries;
	}

	/*********************************************************************************************/
	private void fill(File[] files) {
		HttpHelper manager = new HttpHelper();
		for (File file : files) {
			this.directoryEntries.add(file.getPath());

			Boolean flag = true;
			List<Coverage> aCoverage = readFromFile(file.getPath());
			// myProgressStep = 100 / aCoverage.size();
			for (Coverage coverage : aCoverage) {
				if ("error" == manager.sendToServer(coverage)) {
					flag = false;
				}
				// myProgress += myProgressStep;
				// increment = (int)myProgress;
				// progressHandler.sendMessage(progressHandler.obtainMessage());
			}
			if (true == flag) {
				file.delete();
			}
		}
	}

	/*********************************************************************************************/
	public void writeToSd(String fileName, String outString) {

		BufferedWriter bufferedWriter = null;
		File mFile = null;
		File testDir = null;

		File sdDir = new File(Environment.getExternalStorageDirectory()
				.getPath());

		if (sdDir.exists() && sdDir.canWrite()) {
			testDir = new File(sdDir.getAbsolutePath() + "/Coverage_Mapper");
			testDir.mkdir();
			if (testDir.exists() && testDir.canWrite()) {
				mFile = new File(testDir.getAbsolutePath() + "/" + fileName);
			}
		}

		if ((!mFile.isFile() || (null == mFile)) && testDir.canWrite()) {
			try {
				mFile.createNewFile();
			} catch (IOException e) {
				Log.e(MainActivity.APP_TAG, "error creating file", e);
			}
		}
		if (mFile.exists() && mFile.canWrite()) {
			try {
				bufferedWriter = new BufferedWriter(new FileWriter(mFile, true));
				bufferedWriter.write(outString);
			} catch (Exception e) {
			} finally {
				try {
					if (bufferedWriter != null) {
						bufferedWriter.flush();
						bufferedWriter.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/*********************************************************************************************/
	public List<Coverage> readFromFile(String fileName) {
		List<Coverage> aCoverage = new ArrayList<Coverage>();

		try { // catches IOException below

			// ** File Read to SD OK
			File infile = new File(fileName);
			String string = new String();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(infile)));
			while ((string = reader.readLine()) != null) {
				Coverage mCoverage = new Coverage();
				String[] aCoverageSubStr = null;
				aCoverageSubStr = string.split(",");

				mCoverage.sTime = aCoverageSubStr[0];
				mCoverage.cid = aCoverageSubStr[1];
				mCoverage.lac = aCoverageSubStr[2];
				mCoverage.mcc = aCoverageSubStr[3];
				mCoverage.mnc = aCoverageSubStr[4];
				mCoverage.sLatitude = aCoverageSubStr[5];
				mCoverage.sLongitude = aCoverageSubStr[6];
				mCoverage.mSignalStrength = aCoverageSubStr[7];
				mCoverage.mDeviceId = aCoverageSubStr[8];
				mCoverage.mNetworkType = aCoverageSubStr[9];
				
//				MyLocationListener.mCoverage.add(mCoverage);
				aCoverage.add(mCoverage);
			}
			reader.close();
		} catch (Exception e) {
		}
		return aCoverage;
	}
}