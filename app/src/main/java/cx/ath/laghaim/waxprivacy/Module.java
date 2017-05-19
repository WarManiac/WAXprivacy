package cx.ath.laghaim.waxprivacy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findClass;


/**
 * Created by dante on 18.05.17.
 */

public class Module implements IXposedHookLoadPackage {

    public static final String KIWI = Module.class.getPackage().getName();
    public static final String STATUS = "<status>";

    public static final String APP = "com.whatsapp";
    public static final String INFO = Module.class.getPackage().getName();

    public static final String M_CONTEXT = "mContext";
    public static final String UPDATE_NOTIFICATION_ICONS = "updateNotificationIcons";
    public static final String MAKE_STATUS_BAR_VIEW = "makeStatusBarView";
    public JSONObject C_PACK = new JSONObject();
    boolean first=false;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {



        if (APP.equals(lpparam.packageName))
        {
            int i_sdk = Build.VERSION.SDK_INT;
            XposedBridge.log(INFO + "> started on Sdk " + i_sdk);


            final Class<?> cResolver = findClass("android.content.ContentResolver", lpparam.classLoader);
            XposedBridge.hookAllMethods(cResolver, "query", new XC_MethodHook()
            {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Uri uri = (Uri) param.args[0];
                    XposedBridge.log(INFO + "> "+ uri);

                    final Context ctx = (Context) XposedHelpers.getObjectField(param.thisObject, M_CONTEXT);
                    final Object psb = param.thisObject;
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(SettingsActivity.ACTION_PACK_ADD);

                    ctx.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (intent.getAction().equals(SettingsActivity.ACTION_PACK_ADD))
                            {
                                String s = intent.getStringExtra(SettingsActivity.PACKAGE);
                                try {
                                    C_PACK= new JSONObject(s);
                                    XposedBridge.log(KIWI + " Broadcast add got " + C_PACK.length());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, filter);

                    try
                    {
                        XposedBridge.log(KIWI + " Broadcast add got " + C_PACK.length());
                        if (C_PACK.length()==0) {
                            Intent i = new Intent(SettingsActivity.ACTION_SEND_PACKS);
                            i.setPackage(KIWI);
                            i.putExtra(SettingsActivity.PACKAGE, "ALL");
                            ctx.startService(i);
                            first=true;
                        }
                    }
                    catch (Throwable t)
                    {
                        XposedBridge.log(KIWI + " " + MAKE_STATUS_BAR_VIEW + " failed intent: " + t);
                    }

                    if (uri.toString().equals("content://com.android.contacts/raw_contacts?account_name=WhatsApp&account_type=com.whatsapp&caller_is_syncadapter=true"))
                    {
                        param.args[0]="content://com.android.contacts/raw_contacts?account_name=WhatsApp&account_type=com.whatsapp&caller_is_syncadapter=false";
                    }
                    if (uri.toString().equals("content://com.android.contacts/data?account_name=WhatsApp&account_type=com.whatsapp&caller_is_syncadapter=true"))
                    {
                        param.args[0]="content://com.android.contacts/data?account_name=WhatsApp&account_type=com.whatsapp&caller_is_syncadapter=false";
                    }
                    XposedBridge.log(INFO + "> "+ param.args[0].toString());
                }

                private void debug(MethodHookParam param) {
                    String[] cNames = ((Cursor) param.getResult()).getColumnNames();
                    for (int i = 0; i < cNames.length; i++) {
                        XposedBridge.log(" >>Field[" + i + "] = " + cNames[i]);
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Cursor cursor;
                    try // Probleme mit KNOX und Samsunggeräte vorbeugen
                    {
                        ((Cursor) param.getResult()).getColumnNames();
                    } catch (NullPointerException e) {
                        return;
                    }
                    Uri uri = (Uri) param.args[0];
                    XposedBridge.log(INFO + "afterHookedMethod "+"> "+ uri);
                    cursor = (Cursor) param.getResult();
                    MatrixCursor result = new MatrixCursor(cursor.getColumnNames());

                    //*
                    // * erst abfrage von WA immer alle raw_contacts
                    ///
                    if (uri.toString().equals("content://com.android.contacts/raw_contacts")) {
                        // nur function debug()
                        debug(param);
                        // prüfen ob datensätze vorhanden
                        if (cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {
                                if (cursor.getColumnIndex("_id") > -1) {
                                    String ID = cursor.getString(0);
                                    try {
                                        JSONObject tmp=C_PACK.getJSONObject(ID);
                                        for (Integer i=0; i<tmp.length();i++)
                                        {
                                            if (ID.equals(C_PACK.getJSONObject(ID).getJSONObject(String.valueOf(i)).getString("ID").toString()))
                                            {
                                                if (C_PACK.getJSONObject(ID).getJSONObject(String.valueOf(i)).getBoolean("STATUS"))
                                                    copyColumns(cursor, result);
                                            }
                                        }
                                    } catch (JSONException e) {}
                                }
                            }
                        }
                        result.respond(cursor.getExtras());
                        param.setResult(result);
                        cursor.close();
                        return;
                    }

                    // siehe -> * erst abfrage von WA immer alle raw_contacts
                    if (uri.toString().equals("content://com.android.contacts/data/phones") || uri.toString().equals("content://com.android.contacts/data"))
                    {
                        debug(param);
                        String[] cNames = ((Cursor) param.getResult()).getColumnNames();
                        if (cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {
                                // ob es FIELD "raw_contact_id" gibt
                                if (cursor.getColumnIndex("raw_contact_id") > -1) {
                                    String ID          = cursor.getString(0);
                                    String display_name= cursor.getString(1);
                                    String data1       = cursor.getString(2);
                                    String data2       = cursor.getString(3);
                                    for (int i = 0; i < cNames.length; i++)
                                        XposedBridge.log(" >>Field[" + i + "] = " + cNames[i] +" > : "+ cursor.getString(i));

                                    //TODO 1. 2. function true oder false
                                    try {
                                        JSONObject tmp=C_PACK.getJSONObject(ID);
                                        for (Integer i=0; i<tmp.length();i++)
                                        {
                                            if (ID.equals(C_PACK.getJSONObject(ID).getJSONObject(String.valueOf(i)).getString("ID").toString()))
                                            {
                                                if (C_PACK.getJSONObject(ID).getJSONObject(String.valueOf(i)).getBoolean("STATUS"))
                                                    copyColumns(cursor, result);
                                            }
                                        }


                                    } catch (JSONException e) {}


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
        }


    }

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
