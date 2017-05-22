package cx.ath.laghaim.waxprivacy;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dante on 20.05.17.
 */

public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String APP_PACKET_NAME   = SettingsActivity.class.getPackage().getName();

    public static final String ACTION_SEND_CALL   = APP_PACKET_NAME+".send_call";
    public static final String ACTION_SEND_REPLAY = APP_PACKET_NAME+".send_replay";
    public static final String ACTION_SEND_PACKS = APP_PACKET_NAME+".send_replay";

    public static final String PACKAGE_ID     = "ID";
    public static final String PACKAGE        = "package";

    public static final String PACKAGE_H_ROWS  = "H_ROWS";
    public static final String PACKAGE_H_PHONE ="H_PHONE";
    public static final String PACKAGE_H_DATA1 ="H_DATA1";

    private Intent intent;

    public ArrayList<Integer> V_ID_S;

    public Map SVID = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        V_ID_S=new ArrayList<>();
        intent = new Intent(this, cx.ath.laghaim.waxprivacy.BroadcastService.class);
        SVID = new HashMap();

        new Thread(new Runnable() {

            @Override
            public void run() {
                getContacts();
            }
        }).start();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int tt= (int) SVID.get(buttonView.getId());
        cx.ath.laghaim.waxprivacy.DBHelper DB=new cx.ath.laghaim.waxprivacy.DBHelper(this);
        if (isChecked)
            DB.updateContact (tt, 1 );
        else
            DB.updateContact (tt, 0 );

        DB.update_to();
        DB.close();
    }

    public void getContacts() {
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Integer contact_id = cursor.getInt(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{String.valueOf(contact_id)}, null);

                    while (phoneCursor.moveToNext()) {
                        String pNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        cx.ath.laghaim.waxprivacy.DBHelper DB = new cx.ath.laghaim.waxprivacy.DBHelper(this);
                        DB.todb(contact_id, name, pNumber);
                        DB.close();
                    }
                }
            }
        }
        Intent signalIntent = new Intent(cx.ath.laghaim.waxprivacy.BroadcastService.BROADCAST_ACTION);
        sendBroadcast(signalIntent);
    }



    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        startService(intent);
        registerReceiver(broadcastReceiver, new IntentFilter(cx.ath.laghaim.waxprivacy.BroadcastService.BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        stopService(intent);
    }

    int fID = 0;
    public int findUnusedId() {
        while( findViewById(++fID) != null );
        return fID;
    }

    public void updateUI(Intent intent) {

        (findViewById(R.id.ROW_ID)).setVisibility(View.GONE);

        cx.ath.laghaim.waxprivacy.DBHelper DB=new cx.ath.laghaim.waxprivacy.DBHelper(this);
        ArrayList<ArrayList<String>> re=DB.getAllCotacts();
        DB.close();



        for(int i=0; i<re.size();i++) {

            String H_ROWID=re.get(i).get(0).toString();
            String H_ROWS =re.get(i).get(1).toString();
            String H_DATA1=re.get(i).get(2).toString();
            String H_PHONE=re.get(i).get(3).toString();
            String STATUS =re.get(i).get(4).toString();

            if (SVID.containsValue(Integer.parseInt(H_ROWID)))
            {

            } else {
                int r=Integer.parseInt(re.get(i).get(0).toString());
                int v=findUnusedId();

                SVID.put(v,r);

                LinearLayout ROW_ID= new LinearLayout(this);
                TextView     NAME  = new TextView(this);
                LinearLayout L2    = new LinearLayout(this);


                TextView PHONEID   = new TextView(this);
                PHONEID.setId(v+1);
                PHONEID.setLayoutParams(( findViewById(R.id.PHONE_ID)).getLayoutParams());
                PHONEID.setPadding((findViewById(R.id.PHONE_ID)).getPaddingLeft(),0,0,0);

                Switch SWITCH = new Switch(this);
                SWITCH.setId(v);
                SWITCH.setLayoutParams((findViewById(R.id.switch_ID)).getLayoutParams());


                ROW_ID.setId(v+4);
                NAME.setId(v+3);
                L2.setId(v+2);

                ROW_ID.setLayoutParams((findViewById(R.id.ROW_ID)).getLayoutParams());
                NAME.setLayoutParams((findViewById(R.id.NAME)).getLayoutParams());
                L2.setLayoutParams(( findViewById(R.id.L2)).getLayoutParams());
                PHONEID.setLayoutParams(( findViewById(R.id.PHONE_ID)).getLayoutParams());
                PHONEID.setPadding((findViewById(R.id.PHONE_ID)).getPaddingLeft(),0,0,0);

                SWITCH.setLayoutParams((findViewById(R.id.switch_ID)).getLayoutParams());
                NAME.setTypeface(null , Typeface.BOLD );
                NAME.setTextSize(24);
                PHONEID.setTextSize(16);

                //ROW_ID.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                ROW_ID.setOrientation(LinearLayout.VERTICAL);
                //L2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                L2.setOrientation(LinearLayout.HORIZONTAL);

                NAME.setText(re.get(i).get(2).toString());
                PHONEID.setText(re.get(i).get(3).toString());

                if (Integer.parseInt(re.get(i).get(4).toString())==0)
                    SWITCH.setChecked(false);
                else
                    SWITCH.setChecked(true);

                SWITCH.setOnCheckedChangeListener(this);

                LinearLayout MAIN=(LinearLayout)findViewById(R.id.Main);
                L2.addView(PHONEID);
                L2.addView(SWITCH);
                ROW_ID.addView(NAME);
                ROW_ID.addView(L2);
                MAIN.addView(ROW_ID);
            }
        }
    }
}
