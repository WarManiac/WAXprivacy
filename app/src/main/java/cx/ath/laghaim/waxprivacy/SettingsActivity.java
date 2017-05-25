package cx.ath.laghaim.waxprivacy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dante on 20.05.17.
 */

public class SettingsActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public static final String APP_PACKET_NAME   = SettingsActivity.class.getPackage().getName();

    public static final String ACTION_SEND_CALL   = APP_PACKET_NAME+".send_call";
    public static final String ACTION_SEND_REPLAY   = APP_PACKET_NAME+".send_replay";
    public static final String ACTION_SEND_REPLAY_1 = APP_PACKET_NAME+".send_replay_1";
    public static final String ACTION_SEND_PACKS = APP_PACKET_NAME+".send_replay";


    public static final String PACKAGE_ID     = "ID";
    public static final String PACKAGE        = "package";

    public static final String PACKAGE_H_ROWID = "H_ROWS";
    public static final String PACKAGE_H_ROWS  = "H_ROWS";
    public static final String PACKAGE_H_PHONE ="H_PHONE";
    public static final String PACKAGE_H_DATA1 ="H_DATA1";
    public static final String PACKAGE_H_STATUS="H_STATUS";


    private Intent intent;
    public ArrayList<Integer> V_ID_S;
    public Map SVID = new HashMap();

    public static SettingsActivity _SettingsActivity;

    List<String> listDataHeader;
    HashMap<String, List<ArrayList<String>>> listDataChild;
    private HashMap<Integer, Integer> PVID=new HashMap<>();
    private HashMap<Integer, ArrayList<Integer>> NVID=new HashMap<>() ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        _SettingsActivity = this;

        V_ID_S=new ArrayList<>();
        intent = new Intent(this, cx.ath.laghaim.waxprivacy.BroadcastService.class);
        SVID = new HashMap();

        new Thread(new Runnable() {

            @Override
            public void run() {
                getContacts();
                Intent i = new Intent(SettingsActivity.ACTION_SEND_CALL);
                i.setPackage(APP_PACKET_NAME);
                i.putExtra(SettingsActivity.PACKAGE_ID, "all");
                _SettingsActivity.startService(i);
            }
        }).start();

    }

    public void getContacts() {
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String RID = ContactsContract.Contacts.NAME_RAW_CONTACT_ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        DBHelper DB = new DBHelper(this);
        ArrayList<Integer> RAW_CONTACT_IDs=new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Integer contact_id     = cursor.getInt(cursor.getColumnIndex(_ID));
                Integer RAW_CONTACT_ID = cursor.getInt(cursor.getColumnIndex(RID));
                RAW_CONTACT_IDs.add(RAW_CONTACT_ID);

                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{String.valueOf(contact_id)}, null);

                    while (phoneCursor.moveToNext()) {
                        String pNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        DB.todb(RAW_CONTACT_ID, name, pNumber);
                    }
                }
            }
        }
        DB.dell_old(RAW_CONTACT_IDs);
        DB.close();

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
        prepareListData();

    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        cx.ath.laghaim.waxprivacy.DBHelper DB=new cx.ath.laghaim.waxprivacy.DBHelper(this);
        ArrayList<ArrayList<String>> re=DB.getAllCotacts();
        DB.close();


        HashMap< Integer,HashMap >  re_map=new HashMap();
        for(int i=0; i<re.size();i++) {
            Integer H_ROWS=Integer.valueOf(re.get(i).get(1).toString());
            Integer H_ROWID =Integer.valueOf(re.get(i).get(0).toString());
            HashMap H_DATA1=new HashMap<>();
            HashMap H_PHONE=new HashMap<>();
            HashMap STATUS =new HashMap<>();
            H_DATA1.put(0,re.get(i).get(2).toString());
            H_PHONE.put(H_ROWID,re.get(i).get(3).toString());
            STATUS.put (H_ROWID,re.get(i).get(4).toString());
            if (!re_map.containsKey(H_ROWS))
            {
                HashMap tmp=new HashMap<>();
                tmp.put(2,H_DATA1);
                tmp.put(3,H_PHONE);
                tmp.put(4,STATUS);
                re_map.put(H_ROWS, tmp);
            } else
            {
                HashMap tmp=new HashMap<>();
                H_PHONE= (HashMap) re_map.get(H_ROWS).get(3);
                H_PHONE.put(H_ROWID, re.get(i).get(3).toString());
                STATUS= (HashMap) re_map.get(H_ROWS).get(4);
                STATUS.put(H_ROWID, re.get(i).get(4).toString());
                tmp.put(2,re_map.get(H_ROWS).get(2));
                tmp.put(3,H_PHONE);
                tmp.put(4,STATUS);
                re_map.put(H_ROWS, tmp);
            }
        }

        Set<Integer> all = re_map.keySet();

        int conter=0;
        for (Integer arg:all) {
            HashMap ttt= (HashMap) re_map.get(arg).get(3);
            Set<Integer> all1 = ttt.keySet();

            listDataHeader.add( ((HashMap) re_map.get(arg).get(2)).get(0).toString() );
            List<ArrayList<String>> name = new ArrayList<>();
            for (Integer arg1:all1)
            {
                ArrayList<String> tt=new ArrayList<>();
                tt.add(((HashMap) re_map.get(arg).get(3)).get(arg1).toString());
                tt.add(((HashMap) re_map.get(arg).get(4)).get(arg1).toString());
                tt.add(""+arg1);
                name.add( tt );
            }
            listDataChild.put(listDataHeader.get(conter), name); // Header, Child data
            conter++;
        }
        Collections.sort(listDataHeader);

        for (String NAMEN : listDataHeader)
        {
            TextView Name=new TextView(this);
            Name.setText(NAMEN);
            Name.setTextSize(24);
            int vv=findUnusedId();
            Name.setId(vv);
            Name.setPadding(32,10,0,10);
            Name.setTypeface(null, Typeface.BOLD);
            LinearLayout LPHONE=new LinearLayout(this);
            LPHONE.setOrientation(LinearLayout.VERTICAL);
            List<ArrayList<String>> TTT=listDataChild.get(NAMEN);
            ArrayList<Integer> nn=new ArrayList<>();
            for (int i=0; i<TTT.size(); i++) {
                Switch PHONE=new Switch(this);
                PHONE.setPadding(64,10,64,10);
                PHONE.setTextSize(18);
                PHONE.setText(TTT.get(i).get(0));
                PHONE.setVisibility(View.GONE);
                if (TTT.get(i).get(1).equals("1"))
                    PHONE.setChecked(true);
                else
                    PHONE.setChecked(false);

                if (PHONE.isChecked())
                    PHONE.setTextColor(Color.GREEN);
                else
                    PHONE.setTextColor(Color.RED);

                int v=findUnusedId();
                PHONE.setId(v);
                PVID.put(v, Integer.valueOf(TTT.get(i).get(2)));
                LPHONE.addView(PHONE);
                PHONE.setOnCheckedChangeListener(this);
                nn.add(v);
            }
            ((LinearLayout) findViewById(R.id.Main)).addView(Name);
            ((LinearLayout) findViewById(R.id.Main)).addView(LPHONE);
            NVID.put(vv,nn);
            Name.setOnClickListener(this);
        }

    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        Integer ROWID=PVID.get(buttonView.getId());

        DBHelper DB=new DBHelper(this);
        if (isChecked)
            DB.updateContact (ROWID, 1 );
        else
            DB.updateContact (ROWID, 0 );

        DB.close();

        if (buttonView.isChecked())
            buttonView.setTextColor(Color.GREEN);
        else
            buttonView.setTextColor(Color.RED);

        Intent i = new Intent(SettingsActivity.ACTION_SEND_CALL);
        i.setPackage(APP_PACKET_NAME);
        i.putExtra(SettingsActivity.PACKAGE_ID, "all");
        _SettingsActivity.startService(i);
    }

    @Override
    public void onClick(View v) {
        ArrayList<Integer> test = NVID.get(v.getId());
        for (int i=0; i<test.size();i++) {

            Switch Pswitch = (Switch) findViewById(test.get(i));
            if (Pswitch.getVisibility()==View.GONE)
                Pswitch.setVisibility(View.VISIBLE);
            else
                Pswitch.setVisibility(View.GONE);
        }
    }
}
