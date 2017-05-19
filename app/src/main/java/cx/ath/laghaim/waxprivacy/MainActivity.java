package cx.ath.laghaim.waxprivacy;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String KIWI = MainActivity.class.getPackage().getName();
    public static final String SETTINGS_ACTIVITY = MainActivity.class.getSimpleName();
    public static final String ACTION_PACK_ADD = KIWI+".pack_add";
    public static final String ACTION_PACK_REMOVE = KIWI+".pack_remove";
    public static final String ACTION_ALL_PACK = KIWI+".all_packs";
    public static final String ACTION_SEND_PACKS = KIWI+".send_packs";
    public static final String PACKAGES = "packages";
    public static final String PACKAGE = "package";
    public static final String PREFS_PATH = "/data/"+KIWI+"/shared_prefs";
    public static final String PREFS_NAME = "notoi.prefs";
    public static final String STATUS = "<status>";


    public DBHelper DB;

    Cursor cursor;
    int counter;
    LinearLayout Main;

    TextView TNAME,TPHONE;
    Switch Tswitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DB=new DBHelper(this);
        Main=(LinearLayout) findViewById(R.id.xmain);


        TNAME    =(TextView) findViewById(R.id.TNAME);
        TPHONE   =(TextView) findViewById(R.id.TPHONE);
        Tswitch  =(Switch) findViewById(R.id.Tswitch);

        TNAME.setVisibility(View.GONE);
        TPHONE.setVisibility(View.GONE);
        Tswitch.setVisibility(View.GONE);

        getContacts();

    }

    public void getContacts() {

        String phoneNumber = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = getContentResolver();

        cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {

            counter = 0;
            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
                if (hasPhoneNumber > 0) {
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    TextView TTemp=new TextView(this);
                    TTemp.setId((Integer.parseInt(contact_id)*1000)+1);
                    TTemp.setLayoutParams(TNAME.getLayoutParams());
                    TTemp.setTextSize(30);
                    TTemp.setText(name);
                    Main.addView(TTemp);


                    while (phoneCursor.moveToNext()) {

                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));

                        ArrayList<ArrayList <String> > TT =  DB.contact(Integer.parseInt(contact_id),name,phoneNumber);

                        if (TT.size() > 1)
                        {
                            DB.deleteContact(Integer.parseInt(TT.get(0).get(0)));
                            Log.e("del",TT.get(0).get(3));
                        }
                        if (TT.size()==1)
                            Log.e("123456789",TT.get(0).get(3));
                        if (TT.size()==0)
                        {
                            DB.insertContact(Integer.parseInt(contact_id),Integer.parseInt(contact_id),name,phoneNumber,0);
                            TT =  DB.contact(Integer.parseInt(contact_id),name,phoneNumber);
                        }

                        for (int i=0; i<TT.size();i++)
                            for (int ii=0; ii<TT.get(i).size();ii++)
                                Log.e("out", ""+ii+">>"+TT.get(i).get(ii));

                        LinearLayout LTemp=new LinearLayout(this);
                        LTemp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        LTemp.setOrientation(LinearLayout.HORIZONTAL);

                        TextView NTemp=new TextView(this);
                        NTemp.setLayoutParams(TPHONE.getLayoutParams());
                        NTemp.setTextSize(18);
                        NTemp.setText(TT.get(0).get(4));

                        Switch STemp=new Switch(this);
                        STemp.setLayoutParams(Tswitch.getLayoutParams());
                        STemp.setId((Integer.parseInt(TT.get(0).get(0))*1000));
                        if (TT.get(0).get(5).equals("1"))
                            STemp.setChecked(true);

                        STemp.setOnCheckedChangeListener(this);

                        LTemp.addView(NTemp);
                        LTemp.addView(STemp);
                        Main.addView(LTemp);

                    }
                    phoneCursor.close();
                }
            }
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.e("Switch", ""+buttonView.getId()+" "+isChecked);
        ArrayList <String> re= DB.getrow(buttonView.getId()/1000);

        if (isChecked)
            DB.updateContact (buttonView.getId()/1000, Integer.parseInt(re.get(1)), Integer.parseInt(re.get(1)), re.get(3), re.get(4), 1);
        else
            DB.updateContact (buttonView.getId()/1000, Integer.parseInt(re.get(1)), Integer.parseInt(re.get(1)), re.get(3), re.get(4), 0);
    }
}