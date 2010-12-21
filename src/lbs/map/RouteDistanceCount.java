package lbs.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.android.maps.GeoPoint;

public class RouteDistanceCount {
        private final static double EARTH_RADIUS = 6378.137;
        private static final float MILLION = 1000000;


        private static double rad(double d) {
                return d * Math.PI / 180.0;
        }
       
        private List<Double> mDistancelist;
       
        public RouteDistanceCount(){
                mDistancelist = new ArrayList<Double>();
        }
       
        public static double getDistance(int lat1, int lng1, int lat2, int lng2){
                double lat1_ = lat1 / MILLION;
                double lng1_ = lng1 / MILLION;
                double lat2_ = lat2 / MILLION;
                double lng2_ = lng2 / MILLION;
                return getDistance(lat1_, lng1_, lat2_, lng2_);
        }
       
        public static double getDistance(double lat1, double lng1, double lat2,
                        double lng2) {
                double radLat1 = rad(lat1);
                double radLat2 = rad(lat2);
                double a = radLat1 - radLat2;
                double b = rad(lng1) - rad(lng2);
                double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                                + Math.cos(radLat1) * Math.cos(radLat2)
                                * Math.pow(Math.sin(b / 2), 2)));
                s = s * EARTH_RADIUS;
                s = Math.round(s * 10000) / 10000d;
                return s;
        }
       
        public void setPointOfRoute(GeoPoint g1, GeoPoint g2){
                double lat1 = int2double(g1.getLatitudeE6());
                double lng1 = int2double(g1.getLongitudeE6());
                double lat2 = int2double(g2.getLatitudeE6());
                double lng2 = int2double(g2.getLongitudeE6());
                double distanceMirco = getDistance(lat1, lng1, lat2, lng2);
                mDistancelist.add(distanceMirco);
        }
       
        public double getDistance(){
                double sumDistance = 0d;
                Iterator<Double> itr = mDistancelist.iterator();
                while(itr.hasNext()){
                        sumDistance += itr.next();
                }
                return Math.round(sumDistance*100)/100d ;
        }
       
        public void clearMircoDisBuff(){
                mDistancelist.clear();
        }
       
        private double int2double(int i){
                return (double) i /MILLION;
        }

}