package tatteam.com.app_common.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ThanhNH on 10/8/2015.
 */
public class MyExtraAppsEntity {
    public String myExtraAppsVersion;
    public String download;

    public String myPubName;
    public List<MyAppEntity> appList = new ArrayList<>();
    public List<MyAppEntity> gameList = new ArrayList<>();
}
