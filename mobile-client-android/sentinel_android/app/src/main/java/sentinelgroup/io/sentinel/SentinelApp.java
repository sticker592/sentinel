package sentinelgroup.io.sentinel;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.bugsnag.android.Bugsnag;
import com.google.android.gms.security.ProviderInstaller;

import java.io.File;

import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.PRNGFixes;
import de.blinkt.openvpn.core.StatusListener;
import sentinelgroup.io.sentinel.util.AppConstants;
import sentinelgroup.io.sentinel.util.AppPreferences;

public class SentinelApp extends MultiDexApplication {
    public static final String TAG = SentinelApp.class.getSimpleName();

    private static SentinelApp sInstance;
    public static boolean isStart;

    @Override
    public void onCreate() {
        super.onCreate();
        PRNGFixes.apply();
        Bugsnag.init(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels();
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            /* enable SSL compatibility in pre-lollipop devices */
            upgradeSecurityProvider();
        }
        StatusListener mStatus = new StatusListener();
        mStatus.init(getApplicationContext());
        sInstance = this;
        MultiDex.install(this);
        if (AppPreferences.getInstance().getString(AppConstants.PREFS_FILE_PATH).isEmpty()) {
            String aFilePath = new File(getFilesDir(), AppConstants.FILE_NAME).getAbsolutePath();
            AppPreferences.getInstance().saveString(AppConstants.PREFS_FILE_PATH, aFilePath);
        }
        if (AppPreferences.getInstance().getString(AppConstants.PREFS_CONFIG_PATH).isEmpty()) {
            String aConfigPath = new File(getFilesDir(), AppConstants.CONFIG_NAME).getAbsolutePath();
            AppPreferences.getInstance().saveString(AppConstants.PREFS_CONFIG_PATH, aConfigPath);
        }
    }

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }

    public static boolean isDebugEnabled() {
        return BuildConfig.DEBUG;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Background message
        CharSequence name = getString(R.string.channel_name_background);
        NotificationChannel mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID, name, NotificationManager.IMPORTANCE_MIN);
        mChannel.setDescription(getString(R.string.channel_description_background));
        mChannel.enableLights(false);
        mChannel.setLightColor(Color.DKGRAY);
        mNotificationManager.createNotificationChannel(mChannel);
        // Connection status change messages
        name = getString(R.string.channel_name_status);
        mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
        mChannel.setDescription(getString(R.string.channel_description_status));
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private void upgradeSecurityProvider() {
        try {
            ProviderInstaller.installIfNeededAsync(this, new ProviderInstaller.ProviderInstallListener() {
                @Override
                public void onProviderInstalled() {
                    Log.e(TAG, "New security provider installed.");
                }

                @Override
                public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
                    Log.e(TAG, "New security provider install failed.");
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "Unknown issue trying to install a new security provider", ex);
        }
    }
}
