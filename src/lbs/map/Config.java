/*
 * Config File Handler
 */

package lbs.map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.google.common.android.AndroidConfig;

import android.util.Log;

/**
 *
 * @author artiko
 */
public class Config {
    private final String TAG="Config";
    ArrayList<String> configFileList = new ArrayList<String>();
    public void readConfig(String configFileName){
        try{
            BufferedReader br = new BufferedReader(new FileReader(configFileName));
            String line;
            while((line = br.readLine())!=null){
                configFileList.add(line);
            }
            Log.v(TAG, "config size : "+configFileList.size());
        }catch(IOException ioe){
            Log.e(TAG, "readConfig Exception : "+ioe);
        }
    }
	public static void setConfig(AndroidConfig config) {
		// TODO Auto-generated method stub
		
	}
}
