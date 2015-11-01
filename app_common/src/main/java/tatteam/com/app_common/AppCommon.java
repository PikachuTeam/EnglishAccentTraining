package tatteam.com.app_common;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import tatteam.com.app_common.entity.AppConfigEntity;
import tatteam.com.app_common.ui.dialog.MoreAppsDialog;
import tatteam.com.app_common.util.AppConstant;
import tatteam.com.app_common.util.AppLocalSharedPreferences;
import tatteam.com.app_common.util.AppLog;
import tatteam.com.app_common.util.AppParseUtil;


/**
 * Created by ThanhNH-Mac on 10/4/15.
 */
public class AppCommon implements AppConstant {
    private static AppCommon instance;
    private Context context;
    private AppConfigEntity appLocalConfig;

    private AppCommon() {
    }

    public static AppCommon getInstance() {
        if (instance == null) {
            instance = new AppCommon();
        }
        return instance;
    }

    public void initIfNeeded(Context context) {
        if (this.context == null) {
            this.context = context;
            AppLocalSharedPreferences.getInstance().initIfNeeded(this.context);
        }
        this.refreshLocalAppConfig();
    }

    public void increaseLaunchTime() {
        AppLocalSharedPreferences.getInstance().increaseAppLaunchTime();
    }

    public AppConfigEntity getAppLocalConfig() {
        return appLocalConfig;
    }

    public AppConfigEntity refreshLocalAppConfig() {
        appLocalConfig = AppParseUtil.parseAppConfig(AppLocalSharedPreferences.getInstance().getLocalAppConfigString());
        return appLocalConfig;
    }

    public void syncNewConfigAppIfNeeded(String url) {
        if (context != null) {
            if (AppLocalSharedPreferences.getInstance().shouldSyncAppConfig(RE_SYNC_INTERVAL)) {
                AppLog.i(">>>> AppCommon # syncNewConfigApp");
                Ion.with(context)
                        .load(url)
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                handlerAppConfigResponse(result);
                            }
                        });
            }
        }
    }

    private void handlerAppConfigResponse(final JsonObject response) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                AppConfigEntity newConfig = AppParseUtil.parseAppConfig(response);
                if (appLocalConfig == null && newConfig != null) {
                    appLocalConfig = newConfig;
                    AppLocalSharedPreferences.getInstance().setLocalAppConfig(response.toString());
                    AppLocalSharedPreferences.getInstance().setSyncAppConfigInterval();
                    AppLog.i(">>>> AppCommon # syncNewConfigApp # New");
                } else if (appLocalConfig != null && newConfig != null) {
                    if (!appLocalConfig.equalTo(newConfig)) {
                        appLocalConfig = newConfig;
                        AppLocalSharedPreferences.getInstance().setLocalAppConfig(response.toString());
                        AppLocalSharedPreferences.getInstance().setSyncAppConfigInterval();
                        AppLog.i(">>>> AppCommon # syncNewConfigApp # New");
                    } else {
                        AppLocalSharedPreferences.getInstance().setSyncAppConfigInterval();
                        AppLog.i(">>>> AppCommon # syncNewConfigApp # Same");
                    }
                } else if (newConfig == null) {
                    AppLog.e(">>>> AppCommon # syncNewConfigApp # Fail");
                }
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public MoreAppsDialog openMoreAppDialog(Context activity) {
        MoreAppsDialog moreAppsDialog = new MoreAppsDialog(activity);
        moreAppsDialog.show();
        return moreAppsDialog;
    }

    public void syncAdsSmallBannerIfNeeded(final AdsType adsType) {
        if (AppLocalSharedPreferences.getInstance().shouldSyncAds(RE_SYNC_INTERVAL)) {
            Ion.with(context)
                    .load(DEFAULT_ADS_URL)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            try {
                                if (result != null) {
                                    String adsUnitId = result.get(adsType.getType()).getAsString();
                                    if (adsUnitId != null && !adsUnitId.trim().isEmpty()) {
                                        AppLocalSharedPreferences.getInstance().setAdsIdSmallBanner(adsUnitId);
                                    }
                                } else {
                                    AppLocalSharedPreferences.getInstance().removeAdsIdSmallBanner();
                                }
                            } catch (Exception ex) {
                                AppLocalSharedPreferences.getInstance().removeAdsIdSmallBanner();
                            }
                        }
                    });
        }
    }

    public void destroy() {
        instance = null;
    }

}
