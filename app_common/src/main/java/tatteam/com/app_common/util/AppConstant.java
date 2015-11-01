package tatteam.com.app_common.util;

/**
 * Created by ThanhNH-Mac on 10/31/15.
 */
public interface AppConstant {
    String DEFAULT_ADS_URL = "https://www.dropbox.com/s/2fy36ay5po304eo/my_ads.txt?dl=1";
    String DEFAULT_EXTRA_APP_URL = "https://www.dropbox.com/s/faa5s1wzl0izcg1/my_extra_apps.txt?dl=1";

    int RE_SYNC_INTERVAL = 3;

    enum AdsType {
        SMALL_BANNER_GAME("small_banner_game"),
        SMALL_BANNER_LANGUAGE_LEARNING("small_banner_language_learning"),
        SMALL_BANNER_DRIVING_TEST("small_banner_driving_test");
        private String type;

        private AdsType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
