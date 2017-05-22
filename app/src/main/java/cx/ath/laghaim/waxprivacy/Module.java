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

    public ArrayList<Integer> H_ROWS    =new ArrayList<>();
    public ArrayList<String>  H_PHONE   =new ArrayList<>();
    public ArrayList<String>  H_DATA1   =new ArrayList<>();
    public boolean abfrage=true;
    public boolean cx=false;

    public void logout(String info, String output)
    {
        XposedBridge.log(APP_PACKET_NAME+" >  " +info+  " :   "+ output);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (APP.equals(lpparam.packageName)) {
            int i_sdk = Build.VERSION.SDK_INT;
            logout("LOAD", String.valueOf(i_sdk));


            final Class<?> cResolver = findClass("android.content.ContentResolver", lpparam.classLoader);
            XposedBridge.hookAllMethods(cResolver, "query", new XC_MethodHook() {


                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    final Uri uri = (Uri) param.args[0];
                    //if (uri.toString().equals("content://com.android.contacts/raw_contacts"))
                    //logout("beforeHookedMethod",uri.toString());

                    if (cx==false) {
                        Context ctx = (Context) XposedHelpers.getObjectField(param.thisObject, M_CONTEXT);
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(SettingsActivity.ACTION_SEND_REPLAY);
                        filter.addAction(SettingsActivity.ACTION_SEND_CALL);
                        filter.addAction(SettingsActivity.ACTION_SEND_PACKS);
                        ctx.registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if (intent.getAction().equals(SettingsActivity.ACTION_SEND_REPLAY)) {

                                    H_ROWS = intent.getIntegerArrayListExtra(SettingsActivity.PACKAGE_H_ROWS);
                                    H_PHONE = intent.getStringArrayListExtra(SettingsActivity.PACKAGE_H_PHONE);
                                    H_DATA1 = intent.getStringArrayListExtra(SettingsActivity.PACKAGE_H_DATA1);

                                    for (int i = 0; i < H_ROWS.size(); i++)
                                        logout("replay:", "ROWID " + String.valueOf(H_ROWS.get(i)));
                                /*
                                for (int i=0; i<H_PHONE.size(); i++)
                                    logout("replay:",H_PHONE.get(i));
                                for (int i=0; i<H_DATA1.size(); i++)
                                    logout("replay:",H_DATA1.get(i));
                                */
                                    abfrage = false;
                                }
                            }
                        }, filter);
                        cx=true;
                        if (abfrage) {
                            Intent i = new Intent(SettingsActivity.ACTION_SEND_CALL);
                            i.setPackage(APP_PACKET_NAME);
                            i.putExtra(SettingsActivity.PACKAGE_ID, "all");
                            ctx.startService(i);
                        }
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String[] cNames;
                    Cursor cursor;
                    Uri uri;
                    MatrixCursor result;
                    boolean anderung=false;
                    try // Probleme mit KNOX und Samsunggeräte vorbeugen
                    {
                        cNames = ((Cursor) param.getResult()).getColumnNames();
                        cursor = (Cursor) param.getResult();
                        uri    = (Uri) param.args[0];
                        result= new MatrixCursor(cursor.getColumnNames());
                    } catch (NullPointerException e) {
                        return;
                    }

                    //logout("afterHookedMethod",uri.toString());

                    while (cursor.moveToNext()) {
                        String ID="";
                        String PHONE="";

                        // debug cursed
                        String debug="";

                        int cNames_l_max=0;
                        if (    uri.toString().equals("content://com.android.contacts/raw_contacts")||
                                uri.toString().equals("content://com.android.contacts/data/phones")||
                                uri.toString().equals("content://com.android.contacts/data")
                           )
                        {
                            if (cursor.getCount() > 0) {
                                for (int i = 0; i < cNames.length; i++) {
                                    if (cNames_l_max<cNames[i].length())
                                        cNames_l_max=cNames[i].length();
                                }
                                for (int i = 0; i < cNames.length; i++) {

                                    String tmp="[" + cNames[i] + "]";

                                    debug=debug+ String.format("%"+(cNames_l_max+2)+"s", tmp)     +"\t=\t" + cursor.getString(i)+"\n";
                                }
                            }
                        }

                        //

                        if (uri.toString().equals("content://com.android.contacts/raw_contacts"))
                        {
                            XposedBridge.log("cursor: \n"+debug);
                            if (cursor.getColumnIndex("_id")>-1) {
                                ID = cursor.getString(cursor.getColumnIndex("_id"));
                                if (H_ROWS.indexOf(Integer.parseInt(ID) ) > -1 ) {
                                    copyColumns(cursor, result);
                                    anderung=true;
                                }
                            }
                        }

                        if (uri.toString().equals("content://com.android.contacts/data/phones"))
                        {
                            XposedBridge.log("cursor: \n"+debug);
                            if (cursor.getColumnIndex("raw_contact_id")>-1) {
                                ID = cursor.getString(cursor.getColumnIndex("raw_contact_id"));
                                PHONE=cursor.getString(cursor.getColumnIndex("data1"));
                                if (H_ROWS.indexOf(Integer.parseInt(ID) ) > -1 && H_PHONE.indexOf(PHONE) > -1) {
                                    copyColumns(cursor, result);
                                    anderung=true;
                                }
                            }
                        }

                        if (uri.toString().equals("content://com.android.contacts/data"))
                        {
                            XposedBridge.log("cursor: \n"+debug);
                            if (cursor.getColumnIndex("data1")>-1) {
                                ID = cursor.getString(cursor.getColumnIndex("data1"));
                                if (H_DATA1.indexOf(ID) > -1) {
                                    copyColumns(cursor, result);
                                    //add syn-> raw_contact_id
                                    H_ROWS.add(cursor.getInt(cursor.getColumnIndex("raw_contact_id")));
                                    anderung=true;
                                }
                            }
                        }

                        if (uri.toString().equals("content://com.android.contacts/raw_contacts?account_name=WhatsApp&account_type=com.whatsapp&caller_is_syncadapter=true"))
                        {
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
                        }

                        if (uri.toString().equals("content://com.android.contacts/data?account_name=WhatsApp&account_type=com.whatsapp&caller_is_syncadapter=true"))
                        {
                            //XposedBridge.log("cursor: \n"+debug);
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