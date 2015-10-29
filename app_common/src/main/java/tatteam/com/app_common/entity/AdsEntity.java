package tatteam.com.app_common.entity;

import java.util.ArrayList;

/**
 * Created by Thanh on 03/10/2015.
 */
public class AdsEntity {
    public String adsVer;
    public GoogleAdsEntity googleAds = new GoogleAdsEntity();
    public ArrayList<MyAdsEntity> myAds = new ArrayList<>();
}
