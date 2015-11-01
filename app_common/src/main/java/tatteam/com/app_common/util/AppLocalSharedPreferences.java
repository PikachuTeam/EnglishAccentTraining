package tatteam.com.app_common.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Created by ThanhNH on 5/2/2015.
 */
public class AppLocalSharedPreferences {
    private static final String PREF_NAME = "app_setting_prefer";
    private static final String PREF_LAUNCH_TIME = "app_launch_time";
    private static final String PREF_RATE_APP = "app_rate";
    private static final String PREF_RATE_INTERVAL = "app_rate_interval";
    private static final String PREF_APP_CONFIG = "app_config";
    private static final String PREF_APP_CONFIG_INTERVAL = "app_config_interval";
    private static final String PREF_MY_EXTRA_APPS = "my_extra_apps";
    private static final String PREF_ADS_ID_SMALL_BANNER = "ads_id_small_banner";

    private static AppLocalSharedPreferences instance;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private AppLocalSharedPreferences() {
    }

    public static AppLocalSharedPreferences getInstance() {
        if (instance == null) {
            instance = new AppLocalSharedPreferences();
        }
        return instance;
    }

    public void initIfNeeded(Context context) {
        if (pref == null) {
            pref = context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
            editor = pref.edit();
        }
    }

    public String getLocalAppConfigString() {
        return pref.getString(PREF_APP_CONFIG, null);
    }

    public void setLocalAppConfig(String jSon) {
        editor.putString(PREF_APP_CONFIG, jSon);
        editor.commit();
    }

    public String getMyExtraAppsString() {
        return pref.getString(PREF_MY_EXTRA_APPS, null);
    }

    public void setMyExtraApps(String jSon) {
        editor.putString(PREF_MY_EXTRA_APPS, jSon);
        editor.commit();
    }

    //launch app
    public void increaseAppLaunchTime() {
        long launchTime = getAppLaunchTime() + 1;
        editor.putLong(PREF_LAUNCH_TIME, (launchTime));
        editor.commit();
    }

    public long getAppLaunchTime() {
        return pref.getLong(PREF_LAUNCH_TIME, 0L);
    }

    public void resetAppLaunchTime() {
        editor.putLong(PREF_LAUNCH_TIME, (0));
        editor.commit();
    }


    //rate app
    public void setIsRateApp(boolean isRateApp) {
        editor.putBoolean(PREF_RATE_APP, isRateApp);
        editor.commit();
    }

    public boolean isRatedApp() {
        return pref.getBoolean(PREF_RATE_APP, false);
    }

    public void setRateAppRemindInterval() {
        editor.putLong(PREF_RATE_INTERVAL, (new Date()).getTime());
        editor.commit();
    }

    public long getRateAppRemindInterval() {
        return pref.getLong(PREF_RATE_INTERVAL, 0L);
    }

    public boolean isRateAppOverDate(int threshold) {
        return (new Date()).getTime() - getRateAppRemindInterval() >= (long) (threshold * 24 * 60 * 60 * 1000);
    }

    public boolean isRateAppOverLaunchTime(int threshold) {
        return getAppLaunchTime() >= threshold;
    }

    //app config
    public void setSyncAppConfigInterval() {
        editor.putLong(PREF_APP_CONFIG_INTERVAL, (new Date()).getTime());
        editor.commit();
    }

    public long getSyncAppConfigInterval() {
        return pref.getLong(PREF_APP_CONFIG_INTERVAL, 0L);
    }

    public boolean isSyncAppConfigOverDate(int threshold) {
        return (new Date()).getTime() - getSyncAppConfigInterval() >= (long) (threshold * 24 * 60 * 60 * 1000);
    }

    public boolean shouldSyncAppConfig(int threshold) {
        return (getLocalAppConfigString() == null || isSyncAppConfigOverDate(threshold));
    }

    // ads
    public void setAdsIdSmallBanner(String adsId) {
        editor.putString(PREF_ADS_ID_SMALL_BANNER, adsId);
        editor.commit();
    }

    public String getAdsIdSmallBanner() {
        return pref.getString(PREF_ADS_ID_SMALL_BANNER, "");
    }

    public boolean shouldSyncAds(int afterEachLaunchTime) {
        return getAdsIdSmallBanner().trim().isEmpty() || (getAppLaunchTime() % afterEachLaunchTime == 0);
    }

    public void removeAdsIdSmallBanner(){
        setAdsIdSmallBanner("");
    }


    public void destroy() {
        instance = null;
    }

}
