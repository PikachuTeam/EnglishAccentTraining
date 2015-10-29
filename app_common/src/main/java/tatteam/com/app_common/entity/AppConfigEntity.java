package tatteam.com.app_common.entity;

/**
 * Created by Thanh on 03/10/2015.
 */
public class AppConfigEntity {
    public AdsEntity ads = new AdsEntity();
    public AppEntity app = new AppEntity();
    public MyExtraAppsEntity myExtraApps = new MyExtraAppsEntity();

    public boolean equalTo(AppConfigEntity newConfig) {
        return this.app.appVer.equalsIgnoreCase(newConfig.app.appVer) &&
                this.ads.adsVer.equalsIgnoreCase(newConfig.ads.adsVer) &&
                this.myExtraApps.myExtraAppsVersion.equalsIgnoreCase(newConfig.myExtraApps.myExtraAppsVersion);
    }

    public boolean compareExtraApps(AppConfigEntity newConfig){
        return this.myExtraApps.myExtraAppsVersion.equalsIgnoreCase(newConfig.myExtraApps.myExtraAppsVersion);
    }

}
