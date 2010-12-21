/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lbs.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class SplashLBS extends Activity {
    protected boolean _active = true;
    protected int _splashTime = 3000; // time to display the splash screen in ms
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // ToDo add your GUI initialization code here
        setContentView(R.layout.splash);

        new Thread() {
        @Override
        public void run() {
            try {
                int waited = 0;
                while(_active && (waited < _splashTime)) {
                    sleep(100);
                    if(_active) {
                        waited += 100;
                    }
                }
            } catch(InterruptedException e) {
                // do nothing
            } finally {
                finish();
                startActivity(new Intent("lbs.map.MainActivity"));
//                startActivity(new Intent("lbs.map.MyMapActivity"));
                stop();
            }
        }
    }.start();

    }

}
