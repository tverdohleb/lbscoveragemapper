//package lbs.map;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.view.KeyEvent;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//
//public class OptionsMenuHandler {
//    
//    /**
//     * Creates option menu.
//     * @param activity Parent activity.
//     * @param menu Menu.
//     * @return true.
//     */
//    static public boolean createOptionMenu(Activity activity, Menu menu) {
//        new MenuInflater(activity.getApplication()).inflate(R.menu.main_menu, menu);
//        return true;
//    }
//    
//    /**
//     * Handles option menu item selection.
//     * @param activity Parent activity.
//     * @param item Selected menu item.
//     * @return true if processed, false if not.
//     */
//    static public boolean handleOptionMenuItem(Activity activity, MenuItem item) {
//        Class<?> activityToStart;
//        Class<?> currentActivity = activity.getClass();
//        switch (item.getItemId()) {
//        case R.id.showMapItem: {
//            // show nearby categories
//            activityToStart = CategoriesActivity.class;
//            break;
//        }
//        case R.id.settingsMenuItem: {
//            activityToStart = SettingsActivity.class;
//            break;
//        }
//        case R.id.bookmarksMenuItem: {
//            activityToStart = BookmarksTabsActivity.class;
//            break;
//        }
//        case R.id.searchMenuItem: {
//            SearchDialog searchDialoge = new SearchDialog(activity);
//            searchDialoge.setOwnerActivity(activity);
//            searchDialoge.show();
//            return true;
//        }
//        default:
//            activityToStart = null;
//            break;
//        }
//        
//        if (activityToStart == null || currentActivity.equals(activityToStart)) {
//            return false;
//        }
//        else {
//            Intent intent = new Intent(activity, activityToStart);
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            activity.startActivity(intent);
//            return true;
//        }
//        
//    }
//    
//    /**
//     * Handles key down event.
//     * @param activity Parent activity.
//     * @param keyCode Key code.
//     * @param event Key event.
//     * @return True if event was handled, false otherwise.
//     */
//    public static boolean handleKeyDown(Activity activity, int keyCode, KeyEvent event) {
//        switch (keyCode) {
//        case KeyEvent.KEYCODE_SEARCH:
//            // show search dialog
//            SearchDialog searchDialoge = new SearchDialog(activity);
//            searchDialoge.setOwnerActivity(activity);
//            searchDialoge.show();
//            return true;
//        default:
//            break;
//        }
//        return false;
//    }
//}