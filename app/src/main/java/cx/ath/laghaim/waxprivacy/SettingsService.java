package cx.ath.laghaim.waxprivacy;

import android.app.IntentService;
import android.content.Intent;

import java.util.ArrayList;

/**
 * Created by dante on 20.05.17.
 */

public class SettingsService extends IntentService {
    public static final String SETTINGS_SERVICE = "SettingsService";

    public SettingsService()
    {
        super(SETTINGS_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getAction().equals(cx.ath.laghaim.waxprivacy.SettingsActivity.ACTION_SEND_CALL))
        {
            String _ID       = intent.getStringExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_ID);

            if (_ID.equals("all"))
            {
                ArrayList<Integer> H_ROWS  =new ArrayList<>()  ;
                ArrayList<String>  H_PHONE =new ArrayList<>()  ;
                ArrayList<String>  H_DATA1 =new ArrayList<>()  ;

                cx.ath.laghaim.waxprivacy.DBHelper DB=new cx.ath.laghaim.waxprivacy.DBHelper(this);
                ArrayList<ArrayList <String> > re= DB.getAllCotacts_s();
                DB.close();
                for (int i=0; i<re.size(); i++)
                {
                    if (H_ROWS.indexOf(Integer.parseInt(re.get(i).get(1).toString()))==-1)
                        H_ROWS.add(Integer.parseInt(re.get(i).get(1).toString()));
                    if (H_PHONE.indexOf(re.get(i).get(3).toString())==-1)
                        H_PHONE.add(re.get(i).get(3).toString());
                    if (H_DATA1.indexOf(re.get(i).get(2).toString())==-1)
                        H_DATA1.add(re.get(i).get(2).toString());
                }
                // hinzufügen und senden
                Intent i = new Intent(cx.ath.laghaim.waxprivacy.SettingsActivity.ACTION_SEND_REPLAY);
                i.putIntegerArrayListExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_ROWS,H_ROWS);
                i.putStringArrayListExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_PHONE,H_PHONE);
                i.putStringArrayListExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_DATA1,H_DATA1);
                i.putExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE, false);
                sendBroadcast(i);

            }


        }
    }
}
