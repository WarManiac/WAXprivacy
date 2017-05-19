package cx.ath.laghaim.waxprivacy;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static android.view.View.generateViewId;
import static java.lang.Thread.MAX_PRIORITY;

public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String KIWI = SettingsActivity.class.getPackage().getName();
    public static final String SETTINGS_ACTIVITY = SettingsActivity.class.getSimpleName();
    public static final String ACTION_PACK_ADD = KIWI+".pack_add";
    public static final String ACTION_PACK_REMOVE = KIWI+".pack_remove";
    public static final String ACTION_ALL_PACK = KIWI+".all_packs";
    public static final String ACTION_SEND_PACKS = KIWI+".send_packs";
    public static final String PACKAGES = "packages";
    public static final String PACKAGE = "package";
    public static final String PREFS_PATH = "/data/"+KIWI+"/shared_prefs";
    public static final String PREFS_NAME = "notoi.prefs";
    public static final String STATUS = "<status>";



    public Switch TEST;

    public  LinearLayout MAIN;
    private TextView     HIDDEN_N;
    private TextView     HIDDEN_P;
    private Switch       HIDDEN_S;
    public JSONObject C_PACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //ContactHelper Contact=new ContactHelper();


        C_PACK= getContacts();
        C_PACK = readPrefs();
        writePrefs();




        TEST=(Switch) findViewById(R.id.switch1);
        TEST.setOnCheckedChangeListener(this);

        MAIN=(LinearLayout) findViewById(R.id.main);

        HIDDEN_N =(TextView) findViewById(R.id.NAME);
        HIDDEN_P =(TextView) findViewById(R.id.PHONE);
        HIDDEN_S =(Switch)   findViewById(R.id.switch1);
        findViewById(R.id.Linear).setVisibility(View.GONE);
        HIDDEN_N.setVisibility(View.GONE);
        HIDDEN_P.setVisibility(View.GONE);
        HIDDEN_S.setVisibility(View.GONE);

        build_GUI();
    }

    public void build_GUI()
    {
        Iterator<String> all=C_PACK.keys();

        while (all.hasNext()) {
            String key = all.next();

            try {
                JSONObject tt = C_PACK.getJSONObject(key);
                Log.e("build_GUI", key);
                for (long i=0; i<tt.length(); i++)
                {
                    TextView NAME=new TextView(this);
                    NAME.setLayoutParams(HIDDEN_N.getLayoutParams());
                    NAME.setText(C_PACK.getJSONObject(key).getJSONObject(String.valueOf(i)).getString("NAME") );
                    MAIN.addView(NAME);
                    LinearLayout LTemp=new LinearLayout(this);
                    LTemp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    LTemp.setOrientation(LinearLayout.HORIZONTAL);

                    TextView NTemp=new TextView(this);
                    NTemp.setLayoutParams(HIDDEN_P.getLayoutParams());
                    NTemp.setTextSize(18);
                    NTemp.setText(C_PACK.getJSONObject(key).getJSONObject(String.valueOf(i)).getString("PHONE") );
                    LTemp.addView(NTemp);

                    Switch STemp=new Switch(this);
                    STemp.setLayoutParams(HIDDEN_S.getLayoutParams());

                    // not work generateViewId();
                    int id= findUnusedId();
                    STemp.setId(id);
                    if (C_PACK.getJSONObject(key).getJSONObject(String.valueOf(i)).getString("STATUS").equals("true"))
                        STemp.setChecked(true);

                    STemp.setOnCheckedChangeListener(this);
                    LTemp.addView(STemp);

                    NAME=new TextView(this);
                    NAME.setText(key+"|"+i);
                    NAME.setId(id+1);
                    LTemp.addView(NAME);
                    MAIN.addView(LTemp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

        int fID = 0;

    public int findUnusedId() {
        while( findViewById(++fID) != null );
        return fID;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        Log.d("Xposed",KIWI+" onCheckedChanged: >"+buttonView.getId());
        int ID=buttonView.getId()+1;
        TextView temp=(TextView) findViewById(ID);


        String ttt = (String) temp.getText();
        ttt.indexOf("|");
        String row  = ttt.substring(0,ttt.indexOf("|"));
        String colum=ttt.substring(ttt.indexOf("|")+1,ttt.length());
        Log.d("Xposed",KIWI+" onCheckedChanged: >"+buttonView.getId()+">>"+ temp.getText()+" "+row+" "+colum);

        try {
            if (isChecked)
                C_PACK.getJSONObject(row).getJSONObject(colum).put("STATUS",true);
            else
                C_PACK.getJSONObject(row).getJSONObject(colum).put("STATUS",false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        writePrefs();
    }

    public JSONObject getContacts() {

        JSONObject Contact    =new JSONObject();
        JSONObject ContactSubN=new JSONObject();
        JSONObject ContactSub =new JSONObject();

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {


            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
                Integer counter = 0;
                if (hasPhoneNumber > 0) {
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    while (phoneCursor.moveToNext())
                    {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        try {

                            ContactSub.put("ID", contact_id);
                            ContactSub.put("NAME", name);
                            ContactSub.put("PHONE", phoneNumber);
                            ContactSub.put("STATUS", false);
                            ContactSubN.put(String.valueOf(counter),ContactSub);
                            ContactSub=new JSONObject();
                        } catch (JSONException e) {

                        }
                        try {
                            Contact.getJSONObject(contact_id);
                            Contact.put(String.valueOf(contact_id), ContactSubN);
                        } catch (JSONException e) {
                            try {
                                Contact.put(String.valueOf(contact_id), ContactSubN);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                        counter++;
                    }
                    ContactSubN=new JSONObject();
                    phoneCursor.close();
                }
            }
        }
        return Contact;
    }

    public JSONObject readPrefs()
    {
        JSONObject tmp=new JSONObject();
        File dir = new File(Environment.getDataDirectory() + PREFS_PATH);
        File fi = new File(dir,PREFS_NAME);
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
                tmp = new JSONObject(sb.toString());
                Iterator<String> all=tmp.keys();
                while (all.hasNext()) {
                    String key = all.next();
                    JSONObject tt= C_PACK.getJSONObject(key);
                    for (Integer i=0; i<tt.length();i++)
                    {
                        if (tmp.getJSONObject(key).getJSONObject(String.valueOf(i)).get("STATUS").toString().equals("true"))
                        {
                            C_PACK.getJSONObject(key).getJSONObject(String.valueOf(i)).put("STATUS",true);
                        }
                    }
                }

            }
            catch (Exception e)
            {
                Log.e("Xposed", KIWI+" "+SETTINGS_ACTIVITY+" Exception reading prefs " + e);
            }
        }
        return C_PACK;
    }

    private void writePrefs()
    {
        Log.e("Xposed", KIWI+" WRITE "+SETTINGS_ACTIVITY+" " + C_PACK.toString());
        File dir = new File(Environment.getDataDirectory() + PREFS_PATH);
        dir.mkdirs();
        File fo = new File(dir,PREFS_NAME);
        try
        {
            FileWriter fw = new FileWriter(fo);
            fw.write(C_PACK.toString());
            fw.close();
            fo.setReadable(true,false);
            Log.e("Xposed", KIWI+" "+SETTINGS_ACTIVITY+" " + C_PACK.toString());
        }
        catch (Exception e)
        {
            Log.e("Xposed", KIWI+" "+SETTINGS_ACTIVITY+" Exception writing prefs " + e);
        }

        Intent i = new Intent(SettingsActivity.ACTION_PACK_ADD);
        i.putExtra(SettingsActivity.PACKAGE, C_PACK.toString());
        sendBroadcast(i);
    }
}
