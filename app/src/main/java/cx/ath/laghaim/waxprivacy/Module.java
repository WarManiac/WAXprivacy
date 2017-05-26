package cx.ath.laghaim.waxprivacy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.WebView;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by dante on 20.05.17.
 */

public class Module implements IXposedHookLoadPackage {

    public static final String APP_PACKET_NAME   = SettingsActivity.class.getPackage().getName();

    public static final String APP = "com.whatsapp";
    public static final String M_CONTEXT = "mContext";

    private static volatile Activity currentActivity;
    private static Context context;
    public Context ctx;
    public Context afterHookedMethod_ctx;

    public ArrayList<Integer> H_ROWID =new ArrayList<>()  ;
    public ArrayList<Integer> H_ROWS  =new ArrayList<>()  ;
    public ArrayList<String>  H_PHONE =new ArrayList<>()  ;
    public ArrayList<String>  H_DATA1 =new ArrayList<>()  ;
    public ArrayList<Integer> H_STATUS=new ArrayList<>()  ;

    public boolean abfrage=true;
    public boolean cx=false;
    public boolean afterHookedMethod_cx=false;

    public void logout(String info, String output)
    {
        XposedBridge.log(APP_PACKET_NAME+" >  " +info+  " :   "+ output);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        XposedHelpers.findAndHookMethod("android.app.Instrumentation", lpparam.classLoader, "newActivity", ClassLoader.class, String.class, Intent.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Module.currentActivity = (Activity) param.getResult();
            }
        });

        if (APP.equals(lpparam.packageName)) {
            int i_sdk = Build.VERSION.SDK_INT;
            logout("LOAD", String.valueOf(i_sdk));


            final Class<?> cResolver = findClass("android.content.ContentResolver", lpparam.classLoader);
            XposedBridge.hookAllMethods(cResolver, "query", new XC_MethodHook() {


                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    final Uri uri = (Uri) param.args[0];
                    //if (uri.toString().equals("content://com.android.contacts/raw_contacts"))
                    //logout("beforeHookedMethod",uri.toString());

                    if (cx==false) {
                        ctx = (Context) XposedHelpers.getObjectField(param.thisObject, M_CONTEXT);
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(SettingsActivity.ACTION_SEND_REPLAY);
                        filter.addAction(SettingsActivity.ACTION_SEND_CALL);
                        filter.addAction(SettingsActivity.ACTION_SEND_PACKS);
                        ctx.registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {

                                if (intent.getAction().equals(SettingsActivity.ACTION_SEND_REPLAY) || intent.getAction().equals(SettingsActivity.ACTION_SEND_REPLAY_1) ) {

                                    H_ROWID= intent.getIntegerArrayListExtra(SettingsActivity.PACKAGE_H_ROWID);
                                    H_ROWS = intent.getIntegerArrayListExtra(SettingsActivity.PACKAGE_H_ROWS);
                                    H_PHONE = intent.getStringArrayListExtra(SettingsActivity.PACKAGE_H_PHONE);
                                    H_DATA1 = intent.getStringArrayListExtra(SettingsActivity.PACKAGE_H_DATA1);
                                    H_STATUS=intent.getIntegerArrayListExtra(SettingsActivity.PACKAGE_H_STATUS);
                                    abfrage = false;
                                    //for (int i=0; i<H_ROWS.size(); i++)
                                    //    XposedBridge.log ("onReceive "+H_ROWS.get(i)+" "+H_DATA1.get(i)+" "+H_STATUS.get(i));
                                }
                            }
                        }, filter);
                        cx=true;

                    }
                    if (abfrage && cx) {
                        Intent i = new Intent(SettingsActivity.ACTION_SEND_CALL);
                        i.setPackage(APP_PACKET_NAME);
                        i.putExtra(SettingsActivity.PACKAGE_ID, "all");
                        ctx.startService(i);
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    String[] cNames = new String[0];
                    Cursor cursor = null;
                    Uri uri;
                    MatrixCursor result=null;
                    boolean anderung=false;
                    uri    = (Uri) param.args[0];

                        try // Probleme mit KNOX und Samsunggeräte vorbeugen
                        {
                            cNames = ((Cursor) param.getResult()).getColumnNames();
                            cursor = (Cursor) param.getResult();

                            result = new MatrixCursor(cursor.getColumnNames());
                        } catch (NullPointerException e) {
                            return;
                        }


                        logout("afterHookedMethod",uri.toString());

                        while (cursor.moveToNext()) {
                            String ID="";
                            String PHONE="";

                            // debug cursed
                            String debug="";

                            int cNames_l_max=0;

                                if (cursor.getCount() > 0) {
                                    for (int i = 0; i < cNames.length; i++) {
                                        if (cNames_l_max<cNames[i].length())
                                            cNames_l_max=cNames[i].length();
                                    }
                                    for (int i = 0; i < cNames.length; i++) {

                                        String tmp="[" + cNames[i] + "]";

                                        try {
                                            debug = debug + String.format("%" + (cNames_l_max + 2) + "s", tmp) + "\t=\t" + cursor.getString(i) + "\n";
                                        } catch (SQLiteException e )
                                        {

                                        }
                                    }
                                }


                            if (uri.toString().equals("content://com.android.contacts/raw_contacts"))
                            {
                                if (cursor.getColumnIndex("_id")>-1) {
                                    ID = cursor.getString(cursor.getColumnIndex("_id"));

                                    ArrayList<Integer> suche=new ArrayList<>();
                                    for (int i=0; i<H_ROWS.size(); i++)
                                    {
                                        if (H_ROWS.get(i) == Integer.parseInt(ID) && H_STATUS.get(i)==1 ) {
                                            copyColumns(cursor, result);
                                            anderung = true;
                                            XposedBridge.log ("raw_contacts copy "+H_ROWS.get(i)+" "+H_DATA1.get(i)+" "+H_STATUS.get(i));
                                            i=H_ROWS.size()+1;
                                        }
                                    }
                                }
                            }

                            if (uri.toString().equals("content://com.android.contacts/data/phones"))
                            {
                                if (cursor.getColumnIndex("raw_contact_id")>-1) {
                                    ID = cursor.getString(cursor.getColumnIndex("raw_contact_id"));
                                    PHONE=cursor.getString(cursor.getColumnIndex("data1"));
                                    String data1 = cursor.getString(cursor.getColumnIndex("display_name"));
                                    ArrayList<Integer> suche=new ArrayList<>();

                                    for (int i=0; i<H_ROWS.size(); i++)
                                    {
                                        if (H_ROWS.get(i) == Integer.parseInt(ID)) {
                                            suche.add(i);
                                        }
                                    }
                                    for (int i=0; i<suche.size();i++) {
                                        if (H_STATUS.get(suche.get(i)) == 1 && H_DATA1.get(suche.get(i)).equals(data1) && H_PHONE.get(suche.get(i)).equals(PHONE)) {
                                            copyColumns(cursor, result);
                                            anderung = true;
                                            i=suche.size()+1;
                                        }
                                    }
                                    if (suche.size()==0){
                                        if (Module.currentActivity!=null) {
                                            final String finalID = ID;
                                            final String finalPHONE = PHONE;
                                            final String finaldata1 = data1;
                                            final MethodHookParam fparam=param;
                                            final AlertDialog.Builder dialog=new AlertDialog.Builder(Module.currentActivity)
                                                    .setTitle("Write access denied")
                                                    .setMessage("Grant permission "+finalID+" "+finalPHONE+" "+finaldata1)
                                                    .setPositiveButton("Zulassen", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Intent i = new Intent(SettingsActivity.ACTION_SEND_CALL);
                                                            i.setPackage(APP_PACKET_NAME);
                                                            i.putExtra(SettingsActivity.PACKAGE_ID, "Update");
                                                            i.putExtra(SettingsActivity.PACKAGE_H_ROWS,finalID);
                                                            i.putExtra(SettingsActivity.PACKAGE_H_PHONE,finalPHONE);
                                                            i.putExtra(SettingsActivity.PACKAGE_H_DATA1,finaldata1);
                                                            i.putExtra(SettingsActivity.PACKAGE_H_STATUS,""+1);
                                                            ctx.startService(i);
                                                            dialog.dismiss();
                                                            abfrage=true;
                                                        }
                                                    })
                                                    .setNegativeButton("Verweigern", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            Intent i = new Intent(SettingsActivity.ACTION_SEND_CALL);
                                                            i.setPackage(APP_PACKET_NAME);
                                                            i.putExtra(SettingsActivity.PACKAGE_ID, "Update");
                                                            i.putExtra(SettingsActivity.PACKAGE_H_ROWS,finalID);
                                                            i.putExtra(SettingsActivity.PACKAGE_H_PHONE,finalPHONE);
                                                            i.putExtra(SettingsActivity.PACKAGE_H_DATA1,finaldata1);
                                                            i.putExtra(SettingsActivity.PACKAGE_H_STATUS,""+0);
                                                            ctx.startService(i);
                                                            dialog.cancel();
                                                            abfrage=true;
                                                        }
                                                    });

                                            final AlertDialog alert = dialog.create();
                                            alert.show();

                                            final Handler handler  = new Handler();
                                            final Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (alert.isShowing()) {
                                                        alert.dismiss();
                                                    }
                                                }

                                            };

                                            alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    handler.removeCallbacks(runnable);
                                                }
                                            });

                                            handler.postDelayed(runnable, 60000);
                                        }
                                    }
                                }
                            }

                            if (uri.toString().equals("content://com.android.contacts/data"))
                            {
                                logout("data",debug);
                                if (cursor.getColumnIndex("data1")>-1) {
                                    String data1 = cursor.getString(cursor.getColumnIndex("data1"));
                                    ID = cursor.getString(cursor.getColumnIndex("raw_contact_id"));

                                    ArrayList<Integer> suche=new ArrayList<>();
                                    for (int i=0; i<H_ROWS.size(); i++)
                                    {
                                        if (H_ROWS.get(i)== Integer.parseInt(ID) ) {
                                            suche.add(i);
                                        }
                                    }
                                    for (int i=0; i<suche.size();i++) {
                                        if (H_STATUS.get(suche.get(i))==1 && H_DATA1.get(suche.get(i)).equals(data1)){
                                            //XposedBridge.log ("data Found permission "+H_ROWS.get(i)+" "+H_DATA1.get(i)+" "+H_STATUS.get(i));
                                            copyColumns(cursor, result);
                                            anderung=true;
                                            i=suche.size()+1;
                                        }
                                    }
                                }
                            }

                            if (uri.toString().equals("content://com.android.contacts/raw_contacts?account_name=WhatsApp&account_type=com.whatsapp&caller_is_syncadapter=true"))
                            {
                                XposedBridge.log("uri "+uri.toString());
                                logout("caller_is_syncadapter",debug);
                                copyColumns(cursor, result);
                                anderung=true;

                                if (cursor.getColumnIndex("data1")>-1) {
                                    ID = cursor.getString(cursor.getColumnIndex("_id"));
                                    ArrayList<Integer> suche = new ArrayList<>();
                                    for (int i = 0; i < H_ROWS.size(); i++) {
                                        if (H_ROWS.get(i) == Integer.parseInt(ID) && H_STATUS.get(i)==1) {
                                            suche.add(i);
                                        }
                                    }
                                if (suche.size()==0)
                                {
                                    copyColumns_c(cursor, result, "deleted","1");
                                    anderung=true;
                                } else
                                {
                                    copyColumns(cursor, result);
                                    anderung=true;
                                }
                                }
                                /*

                                if (cursor.getColumnIndex("_id")>-1) {
                                    ID = cursor.getString(cursor.getColumnIndex("_id"));
                                    if (H_ROWS.indexOf(ID) > -1) {
                                        copyColumns(cursor, result);
                                        anderung=true;
                                    } else
                                    {
                                        copyColumns_c(cursor, result, "deleted","1");
                                        anderung=true;
                                    }
                                }
                                */
                            }

                            if (uri.toString().equals("content://com.android.contacts/data?account_name=WhatsApp&account_type=com.whatsapp&caller_is_syncadapter=true"))
                            {
                                //XposedBridge.log("cursor: \n"+debug);
                                copyColumns(cursor, result);
                                anderung=true;
                            }
                        }
                    if (anderung) {
                        result.respond(cursor.getExtras());
                        param.setResult(result);
                        cursor.close();
                    }

                }

                private  void copyColumns_c(Cursor cursor, MatrixCursor result, String colum, String arg) {
                    copyColumns_c(cursor, result, cursor.getColumnCount(),colum,arg);
                }


                private void copyColumns(Cursor cursor, MatrixCursor result) {
                    copyColumns(cursor, result, cursor.getColumnCount());
                }

                private void copyColumns_c(Cursor cursor, MatrixCursor result, int count, String colum, String arg) {
                    boolean change = false;
                    try {
                        Object[] columns = new Object[count];
                        for (int i = 0; i < count; i++) {
                            change = false;
                            if (cursor.getColumnIndex("colum") > -1)
                                change = true;
                            switch (cursor.getType(i)) {
                                case Cursor.FIELD_TYPE_NULL:
                                    columns[i] = null;
                                    break;
                                case Cursor.FIELD_TYPE_INTEGER:
                                    if (change)
                                        columns[i] = Integer.valueOf(arg);
                                    else
                                        columns[i] = cursor.getInt(i);
                                    break;
                                case Cursor.FIELD_TYPE_FLOAT:
                                    if (change)
                                        columns[i] = Float.valueOf(arg);
                                    else
                                        columns[i] = cursor.getFloat(i);
                                    break;
                                case Cursor.FIELD_TYPE_STRING:
                                    if (change)
                                        columns[i] = arg;
                                    else
                                        columns[i] = cursor.getString(i);
                                    break;
                                case Cursor.FIELD_TYPE_BLOB:
                                    columns[i] = cursor.getBlob(i);
                                    break;
                                default:
                                    Log.w("tttt", "Unknown cursor data type=" + cursor.getType(i));
                            }
                        }
                        result.addRow(columns);

                    } catch (Throwable ex) {
                        Log.e("ttt", "", ex);
                    }
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
            });

        }
    }
}