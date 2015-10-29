package tatteam.com.app_common.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by ThanhNH on 10/17/2015.
 */
public class CommonUtil {
    public static void openGooglePlay(Context activity, String packageName) throws Exception{
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName));
        activity.startActivity(intent);
    }
}
