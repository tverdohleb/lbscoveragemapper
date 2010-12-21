/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lbs.map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/**
 *
 * @author artiko
 */
public class Locate {
    private final String TAG="Locate";
    Activity act;
    Bitmap pointerImage;
    int imgAmount;
    float minLat, maxLat, minLong, maxLong, constLng, constLat;
    Locate(Activity act){
        this.act = act;
    }
    public void setConstant(float constLat, float constLng){
        this.constLat = constLat;
        this.constLng = constLng;
    }
    public void setAreaCoordinate(float minLat, float maxLat, float minLong, float maxLong){
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLong = minLong;
        this.maxLong = maxLong;
    }
    public void setPointerImage(int pointerResource){
        Bitmap pointer = BitmapFactory.decodeResource(act.getResources(), pointerResource);
        this.pointerImage = Bitmap.createScaledBitmap(pointer, 20, 20, true);
    }
    public Bitmap drawPointer(Bitmap mapImage, float x, float y){
        float h, w, xm, ym;
        Bitmap cloneImage = mapImage.copy(mapImage.getConfig(), true);
        h = cloneImage.getHeight();
        w = cloneImage.getWidth();
        xm = constLng*(x-minLong)/(maxLong-minLong)*w;
        ym = constLat*(maxLat-y)/(maxLat-minLat)*h;
        if(xm <= w && ym <=h) {
            Canvas c = new Canvas(cloneImage);
            c.drawBitmap(pointerImage, xm, ym, new Paint());
        }else{
            Log.e(TAG, "coordinate "+xm+", "+ym+" are not inside area : "+w+", "+h);
        }
        return cloneImage;
    }

    public float getMaxLat() {
        return maxLat;
    }

    public float getMaxLong() {
        return maxLong;
    }

    public float getMinLat() {
        return minLat;
    }

    public float getMinLong() {
        return minLong;
    }

}
