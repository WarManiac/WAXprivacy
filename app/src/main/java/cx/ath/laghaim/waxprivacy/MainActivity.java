package cx.ath.laghaim.waxprivacy;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

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

        // blockiert nicht das UI "Anwendung reageiert nicht" bei sehr vielen Kontackte
        //new Thread(new Runnable() {

        //    @Override
        //    public void run() {
                getContacts();
        //    }
        //}).start();
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

                        LinearLayout LTemp=new LinearLayout(this);
                        LTemp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        LTemp.setOrientation(LinearLayout.HORIZONTAL);

                        TextView NTemp=new TextView(this);
                        NTemp.setLayoutParams(TPHONE.getLayoutParams());
                        NTemp.setTextSize(18);

                        NTemp.setText(phoneNumber);
                        //NTemp.setId((Integer.parseInt(contact_id)*1000)+2);

                        Switch STemp=new Switch(this);
                        STemp.setLayoutParams(Tswitch.getLayoutParams());
                        //NTemp.setId((Integer.parseInt(contact_id)*1000));
                        LTemp.addView(NTemp);
                        LTemp.addView(STemp);
                        Main.addView(LTemp);
                    }
                    phoneCursor.close();
                }
            }
        }

    }

}