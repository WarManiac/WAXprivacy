package cx.ath.laghaim.waxprivacy;

import static de.robv.android.xposed.XposedHelpers.findClass;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringTokenizer;

public class main implements IXposedHookLoadPackage {

    public static final String KIWI = main.class.getPackage().getName();
    public static final String STATUS = "<status>";
    //package and classes
    public static final String SYSTEMUI = "com.android.systemui";
    public static final String PHONE_STATUS_BAR = SYSTEMUI +".statusbar.phone.PhoneStatusBar";
    public static final String BASE_STATUS_BAR = SYSTEMUI +".statusbar.BaseStatusBar";
    //hooked methods
    public static final String MAKE_STATUS_BAR_VIEW = "makeStatusBarView";
    public static final String ADD_NOTIFICATION = "addNotification";
    public static final String REMOVE_NOTIFICATION = "removeNotification";
    public static final String UPDATE_NOTIFICATION = "updateNotification";
    public static final String UPDATE_NOTIFICATION_ICONS = "updateNotificationIcons";

    //accessed methods and fields
    public static final String CREATE_NOTIFICATION_VIEWS = "createNotificationViews";
    public static final String M_CONTEXT = "mContext";
    public static final String M_NOTIFICATION_ICONS = "mNotificationIcons";
    public static final String M_SYSTEM_ICON_AREA = "mSystemIconArea";
    public static final String ICON = "icon";

    private Set<String> i_pack = new HashSet<>();
    private LinkedHashMap<String, View> i_icons = new LinkedHashMap<>();
    private LinearLayout i_status;
    private int i_sdk;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {


        if (!lpparam.packageName.equals("com.whatsapp"))
            return;



        try {
            final Class<?> cResolver = findClass("android.content.ContentResolver", lpparam.classLoader);

            XposedBridge.hookAllMethods(cResolver, "query", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                    final Context ctx = (Context) XposedHelpers.getObjectField(param.thisObject, M_CONTEXT);
                    final Object psb = param.thisObject;
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(SimpleIntentService.ACTION_RESP);
                    filter.addCategory(Intent.CATEGORY_DEFAULT);

                    ctx.registerReceiver(new BroadcastReceiver()
                    {
                        @Override
                        public void onReceive(Context context, Intent intent)
                        {
                            String text = intent.getStringExtra(SimpleIntentService.PARAM_OUT_MSG);
                            XposedBridge.log("onReceive:83 > "+text);
                        }
                    }, filter);
                    try
                    {
                        Intent i = new Intent(MainActivity.ACTION_SEND_PACKS);
                        i.setPackage(KIWI);
                        ctx.startService(i);
                        XposedBridge.log(KIWI + " " + MAKE_STATUS_BAR_VIEW + " sent intent...");
                    }
                    catch (Throwable t)
                    {
                        XposedBridge.log(KIWI + " " + MAKE_STATUS_BAR_VIEW + " failed intent: " + t);
                    }

                    Uri uri = (Uri) param.args[0];
                    // benötig für WA
                    if (uri.toString().equals("content://com.android.contacts/raw_contacts")) {
                        return;
                    }
                    // benötig für WA
                    if (uri.toString().equals("content://com.android.contacts/data/phones")) {
                        return;
                    }
                    // benötig für WA
                    if (uri.toString().equals("content://com.android.contacts/data")) {
                        return;
                    }

                    // was WA alles ab Fragt im zusammenhang android.content.ContentResolver
                    //XposedBridge.log("beforeHookedMethod:");
                    //XposedBridge.log("  >Hooked    uri: " + uri);
                    //XposedBridge.log("  >param.args[0]='' ");
                    //param.args[0]="";
                }

                private void debug(MethodHookParam param) {
                    String[] cNames = ((Cursor) param.getResult()).getColumnNames();
                    for (int i = 0; i < cNames.length; i++) {
                        //XposedBridge.log(" >>Field[" + i + "] = " + cNames[i]);
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    boolean copy = false;



                    //TODO: 1. _id/raw_contact_id speichern suchen ausgeben lassen bei erlaubt SQLite
                    //TODO: 2. Datensätze bereinigen (nur Nummer und Name ggf. Profilbild)

                    Uri uri = (Uri) param.args[0];
                    //XposedBridge.log("afterHookedMethod:");
                    //XposedBridge.log("   uri=" + uri + "-->" + uri.getHost() + uri.getPath());
                    Cursor cursor;

                    try // Probleme mit KNOX und Samsunggeräte vorbeugen
                    {
                        ((Cursor) param.getResult()).getColumnNames();
                    } catch (NullPointerException e) {
                        return;
                    }


                    cursor = (Cursor) param.getResult();
                    MatrixCursor result = new MatrixCursor(cursor.getColumnNames());

                    /*
                     * erst abfrage von WA immer alle raw_contacts
                     */
                    if (uri.toString().equals("content://com.android.contacts/raw_contacts")) {
                        // nur function debug()
                        debug(param);

                        String[] cNames = ((Cursor) param.getResult()).getColumnNames();
                        // prüfen ob datensätze vorhanden
                        if (cursor.getCount() > 0) {

                            while (cursor.moveToNext()) {
                                // ob es FIELD "_id" gibt
                                if (cursor.getColumnIndex("_id") > -1) {

                                    //TODO 1. function true oder false

                                    /*
                                    // hole _id als INT
                                    int ID=cursor.getInt(cursor.getColumnIndex("_id"));
                                    XposedBridge.log(" >>>DB>>>"+ID);

                                    if (cursor.getString(cursor.getColumnIndex("_id")).equals("1"))
                                        copy = true;
                                    else copy = false;

                                    // for ... für XposedBridge.log >> debug
                                    for (int i = 0; i < cNames.length; i++) {
                                        XposedBridge.log(" raw_contacts>>>" + i + ": " + cNames[i] + " = " + cursor.getString(cursor.getColumnIndex(cNames[i])));
                                    }

                                    // wenn gestattet Kopier daten satz
                                    if (copy)
                                        */
                                        copyColumns(cursor, result);
                                }
                            }
                        }
                        result.respond(cursor.getExtras());
                        param.setResult(result);
                        cursor.close();
                        return;
                    }

                    // siehe -> * erst abfrage von WA immer alle raw_contacts
                    if (uri.toString().equals("content://com.android.contacts/data/phones")) {
                        debug(param);
                        String[] cNames = ((Cursor) param.getResult()).getColumnNames();
                        if (cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {
                                // ob es FIELD "raw_contact_id" gibt
                                if (cursor.getColumnIndex("raw_contact_id") > -1) {
                                    //TODO 1. 2. function true oder false
                                    if (
                                            cursor.getString(cursor.getColumnIndex("data2")).equals("0") &&
                                            cursor.getString(cursor.getColumnIndex("data3")).equals("WA")
                                       ) {
                                        copy = true;
                                        for (int i = 0; i < cNames.length; i++) {
                                            //XposedBridge.log(" phones>>>" + i + ": " + cNames[i] + " = " + cursor.getString(cursor.getColumnIndex(cNames[i])));
                                        }
                                    }
                                    else {
                                        copy = false;
                                    }


                                    if (copy) copyColumns(cursor, result);
                                }
                            }
                        }
                        result.respond(cursor.getExtras());
                        param.setResult(result);
                        cursor.close();
                        return;
                    }

                    // siehe -> * erst abfrage von WA immer alle raw_contacts
                    if (uri.toString().equals("content://com.android.contacts/data")) {
                        debug(param);
                        String[] cNames = ((Cursor) param.getResult()).getColumnNames();
                        if (cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {

                                // ob es FIELD "raw_contact_id" gibt
                                if (cursor.getColumnIndex("raw_contact_id") > -1) {
                                    //TODO 1. 2. function true oder false

                                    /*
                                    if (cursor.getString(cursor.getColumnIndex("raw_contact_id")).equals("1"))
                                        copy = true;
                                    else copy = false;
                                    for (int i = 0; i < cNames.length; i++) {
                                        XposedBridge.log(" data>>>" + i + ": " + cNames[i] + " = " + cursor.getString(cursor.getColumnIndex(cNames[i])));
                                    }
                                    if (copy) */
                                        copyColumns(cursor, result);
                                }
                            }
                        }
                        result.respond(cursor.getExtras());
                        param.setResult(result);
                        cursor.close();
                        return;
                    }
                }
            });
        } catch (Throwable t) {
            throw t;
        }
    }

    /*
     * Funktion copyColumns von M66B:
     * https://github.com/M66B/XPrivacy/blob/master/src/biz/bokhorst/xprivacy/XContentResolver.java ab Zeile: 627
     * Stand (a8faf99  on 23 May 2015)
     * Änderung Util.log() zu Log.W()
     */
    private void copyColumns(Cursor cursor, MatrixCursor result) {
        copyColumns(cursor, result, cursor.getColumnCount());
    }

    private void copyColumns(Cursor cursor, MatrixCursor result, int count) {
        try {
            Object[] columns = new Object[count];
            for (int i = 0; i < count; i++)
                switch (cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_NULL:
                        columns[i] = null;
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        columns[i] = cursor.getInt(i);
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        columns[i] = cursor.getFloat(i);
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        columns[i] = cursor.getString(i);
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        columns[i] = cursor.getBlob(i);
                        break;
                    default:
                        Log.w("tttt", "Unknown cursor data type=" + cursor.getType(i));
                }
            result.addRow(columns);
        } catch (Throwable ex) {
            Log.e("ttt", "", ex);
        }
    }



   }


   /*
   package cx.ath.laghaim.waxprivacy;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import android.widget.Switch;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public DBHelper DB;
    ArrayList<String> contactList;
    Cursor cursor;
    ScrollView Main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DB=new DBHelper(this);
        Main=(ScrollView) findViewById(R.id.Main);

        // blockiert nicht das UI "Anwendung reageiert nicht" bei sehr vielen Kontackte
        new Thread(new Runnable() {
            @Override
            public void run() {
                getContacts();
            }
        }).start();
    }


    public void getContacts() {
        contactList = new ArrayList<String>();

        String phoneNumber = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;


        StringBuffer output;
        ContentResolver contentResolver = getContentResolver();
        cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        if (cursor.getCount() > 0) {

            //EditText
            // LinearLayout
            //   Switch
            //   Switch
            // TODO: 16.05.17 erstelle gui einträge!!
            String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
            String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
            int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

            if (hasPhoneNumber > 0) {
                int counter=1;
                EditText temp= new EditText(this);
                temp.setId(Integer.parseInt(_ID)*1000);
                temp.setText(DISPLAY_NAME);
                Main.addView(temp);

                LinearLayout Ltemp=new LinearLayout(this);
                Ltemp.setOrientation(LinearLayout.VERTICAL);
                Ltemp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                Ltemp.setId( (Integer.parseInt(_ID)*1000)+counter );
                Main.addView(Ltemp);

                Switch Ptemp=new Switch(this);


                //This is to read multiple phone numbers associated with the same contact
                Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                while (phoneCursor.moveToNext()) {
                    phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                    Ptemp.setId( (Integer.parseInt(_ID)*1000)+counter );
                    Ptemp.setText(phoneNumber);
                    counter++;
                    // TODO: 16.05.17 datenbanke abgleich eintragen Einstellung usw.
                }
                phoneCursor.close();
                Main.addView(Ptemp);
            }
        }
    }
}

    */