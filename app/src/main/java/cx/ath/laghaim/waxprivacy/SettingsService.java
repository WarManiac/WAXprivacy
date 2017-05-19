package cx.ath.laghaim.waxprivacy;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

import static cx.ath.laghaim.waxprivacy.SettingsActivity.KIWI;
import static cx.ath.laghaim.waxprivacy.SettingsActivity.SETTINGS_ACTIVITY;

public class SettingsService extends IntentService
{
    public static final String SETTINGS_SERVICE = "SettingsService";

    public SettingsService()
    {
        super(SETTINGS_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.v("Xposed >>> ", intent.getAction());

            String s = intent.getStringExtra(SettingsActivity.PACKAGE);
            String tmp=readPrefs();
            Intent i = new Intent(SettingsActivity.ACTION_PACK_ADD);
            i.putExtra(SettingsActivity.PACKAGE, tmp);
            sendBroadcast(i);
        }
    }

    public static String readPrefs()
    {
        String tmp="";
        File dir = new File(Environment.getDataDirectory() + SettingsActivity.PREFS_PATH);
        File fi = new File(dir,SettingsActivity.PREFS_NAME);
        if (fi.exists())
        {
            try
            {
                FileReader fr = new FileReader(fi);
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[1024];
                int r = fr.read(buffer);
                while (r > 0)
                {
                    sb.append(buffer, 0, r);
                    r = fr.read(buffer);
                }
                fr.close();
                tmp = sb.toString();
            }
            catch (Exception e)
            {
                Log.e("Xposed", KIWI+" "+SETTINGS_ACTIVITY+" Exception reading prefs " + e);
            }
        }
        return tmp;
    }
}
