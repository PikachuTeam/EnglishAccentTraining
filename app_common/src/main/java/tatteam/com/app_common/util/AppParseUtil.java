package tatteam.com.app_common.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import tatteam.com.app_common.entity.AppConfigEntity;
import tatteam.com.app_common.entity.MyAdsEntity;
import tatteam.com.app_common.entity.MyAppEntity;
import tatteam.com.app_common.entity.MyExtraAppsEntity;

/**
 * Created by ThanhNH-Mac on 10/3/15.
 */
public class AppParseUtil {

    public static AppConfigEntity parseAppConfig(String json) {
        AppConfigEntity appConfig = null;
        if (json != null && !json.trim().isEmpty()) {
            try {
                Gson gson = new Gson();
                JsonElement element = gson.fromJson(json, JsonElement.class);
                JsonObject jsonObj = element.getAsJsonObject();
                appConfig = parseAppConfig(jsonObj);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return appConfig;
    }

    public static AppConfigEntity parseAppConfig(JsonObject jsonObj) {
        AppConfigEntity appConfig = null;
        if (jsonObj != null) {
            try {
                appConfig = new AppConfigEntity();

                JsonObject app = jsonObj.getAsJsonObject("app");
                appConfig.app.appName = app.get("app_name").getAsString();
                appConfig.app.appVer = app.get("app_version").getAsString();

                JsonObject ads = jsonObj.getAsJsonObject("ads");
                appConfig.ads.adsVer = ads.get("ads_version").getAsString();

                JsonObject google_ads = ads.getAsJsonObject("google_ads");
                appConfig.ads.googleAds.smallBannerId = google_ads.get("small_banner_id").getAsString();
                appConfig.ads.googleAds.biBannerId = google_ads.get("big_banner_id").getAsString();

                JsonArray jsonArray = ads.getAsJsonArray("my_ads");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject my_ads = jsonArray.get(i).getAsJsonObject();
                    MyAdsEntity myAdsObj = new MyAdsEntity();
                    myAdsObj.packageName = my_ads.get("package_name").getAsString();
                    myAdsObj.priority = my_ads.get("priority").getAsInt();
                    myAdsObj.urlImage = my_ads.get("image").getAsString();
                    myAdsObj.downLoad = my_ads.get("download").getAsString();
                    appConfig.ads.myAds.add(myAdsObj);
                }

                JsonObject my_extra_apps = jsonObj.getAsJsonObject("my_extra_apps");
                appConfig.myExtraApps.myExtraAppsVersion = my_extra_apps.get("my_extra_apps_version").getAsString();
                appConfig.myExtraApps.download = my_extra_apps.get("download").getAsString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return appConfig;
    }

    public static MyExtraAppsEntity parseExtraApps(String json) {
        MyExtraAppsEntity extraApps = null;
        if (json != null && !json.trim().isEmpty()) {
            try {
                Gson gson = new Gson();
                JsonElement element = gson.fromJson(json, JsonElement.class);
                JsonObject jsonObj = element.getAsJsonObject();
                extraApps = parseExtraApps(jsonObj);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return extraApps;

    }

    public static MyExtraAppsEntity parseExtraApps(JsonObject jsonObj) {
        MyExtraAppsEntity extraApps = null;
        if (jsonObj != null) {
            try {
                extraApps = new MyExtraAppsEntity();
                JsonArray my_apps = jsonObj.getAsJsonArray("my_apps");
                for (int i = 0; i < my_apps.size(); i++) {
                    JsonObject extra_app = my_apps.get(i).getAsJsonObject();
                    MyAppEntity myApp = new MyAppEntity();
                    myApp.name = extra_app.get("name").getAsString();
                    myApp.description = extra_app.get("description").getAsString();
                    myApp.image = extra_app.get("image").getAsString();
                    myApp.packageName = extra_app.get("package_name").getAsString();
                    extraApps.appList.add(myApp);
                }

                JsonArray my_games = jsonObj.getAsJsonArray("my_games");
                for (int i = 0; i < my_games.size(); i++) {
                    JsonObject my_game = my_games.get(i).getAsJsonObject();
                    MyAppEntity myGame = new MyAppEntity();
                    myGame.name = my_game.get("name").getAsString();
                    myGame.description = my_game.get("description").getAsString();
                    myGame.image = my_game.get("image").getAsString();
                    myGame.packageName = my_game.get("package_name").getAsString();
                    extraApps.gameList.add(myGame);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return extraApps;
    }
}
