package cx.ath.laghaim.waxprivacy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

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

    void DB_abfrage(int arg )
    {
        ArrayList<Integer> H_ROWID =new ArrayList<>()  ;
        ArrayList<Integer> H_ROWS  =new ArrayList<>()  ;
        ArrayList<String>  H_PHONE =new ArrayList<>()  ;
        ArrayList<String>  H_DATA1 =new ArrayList<>()  ;
        ArrayList<Integer> H_STATUS=new ArrayList<>()  ;


        cx.ath.laghaim.waxprivacy.DBHelper DB=new cx.ath.laghaim.waxprivacy.DBHelper(this);
        ArrayList<ArrayList <String> > re= DB.getAllCotacts();
        DB.close();
        for (int i=0; i<re.size(); i++)
        {
            H_ROWID.add( Integer.parseInt(re.get(i).get(0).toString() ));
            H_ROWS.add(  Integer.parseInt(re.get(i).get(1).toString() ));
            H_PHONE.add(                  re.get(i).get(3).toString() );
            H_DATA1.add(                  re.get(i).get(2).toString() );
            H_STATUS.add(Integer.parseInt(re.get(i).get(4).toString() ));
        }
        // hinzufügen und senden
        Intent i = null;
        if (arg==1) {
            i = new Intent(cx.ath.laghaim.waxprivacy.SettingsActivity.ACTION_SEND_REPLAY);
        }
        if (arg==2) {
            i = new Intent(cx.ath.laghaim.waxprivacy.SettingsActivity.ACTION_SEND_REPLAY_1);
        }

        i.putIntegerArrayListExtra(  cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_ROWID, H_ROWID);
        i.putIntegerArrayListExtra(  cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_ROWS, H_ROWS);
        i.putStringArrayListExtra(  cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_PHONE, H_PHONE);
        i.putStringArrayListExtra(  cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_DATA1, H_DATA1);
        i.putIntegerArrayListExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_STATUS, H_STATUS);

        i.putExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE, false);
        sendBroadcast(i);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getAction().equals(cx.ath.laghaim.waxprivacy.SettingsActivity.ACTION_SEND_CALL))
        {
            String _ID       = intent.getStringExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_ID);

            String PACKAGE_H_ROWS = intent.getStringExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_ROWS);
            String PACKAGE_H_PHONE= intent.getStringExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_PHONE);
            String PACKAGE_H_DATA1= intent.getStringExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_DATA1);
            String PACKAGE_H_STATUS= intent.getStringExtra(cx.ath.laghaim.waxprivacy.SettingsActivity.PACKAGE_H_STATUS);

            if (_ID.equals("all"))
            {
                DB_abfrage(1);
            }
            if (_ID.equals("Update"))
            {
                cx.ath.laghaim.waxprivacy.DBHelper DB=new cx.ath.laghaim.waxprivacy.DBHelper(this);



                DB.todb_i(Integer.parseInt(PACKAGE_H_ROWS),PACKAGE_H_DATA1,PACKAGE_H_PHONE,PACKAGE_H_STATUS);

                DB.close();

                DB_abfrage(2);
                Log.d("xposed", "UPDATE");
            }
        }
    }
}
