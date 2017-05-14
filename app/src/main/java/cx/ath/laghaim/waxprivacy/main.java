package cx.ath.laghaim.waxprivacy;


import static de.robv.android.xposed.XposedHelpers.findClass;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class main implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.whatsapp"))
            return;

        //XposedBridge.log("Loaded app: " + lpparam.packageName);

        try {
            final Class<?> cResolver = findClass("android.content.ContentResolver", lpparam.classLoader);

            XposedBridge.hookAllMethods(cResolver, "query", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

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

                    XposedBridge.log("beforeHookedMethod:");
                    XposedBridge.log("  >Hooked    uri: " + uri);
                    XposedBridge.log("  >param.args[0]='' ");
                }

                private void debug(MethodHookParam param) {
                    String[] cNames = ((Cursor) param.getResult()).getColumnNames();
                    for (int i = 0; i < cNames.length; i++) {
                        XposedBridge.log(" >>Field[" + i + "] = " + cNames[i]);
                    }
                }

                //TODO: 1. _id/raw_contact_id speichern suchen ausgeben lassen bei erlaubt
                private boolean pruefen(String arg) {
                    return false;
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    boolean copy = false;

                    //TODO: 1. _id/raw_contact_id speichern suchen ausgeben lassen bei erlaubt
                    //TODO: 2. Datensätze bereinigen (nur Nummer und Name ggf. Profilbild)

                    Uri uri = (Uri) param.args[0];
                    XposedBridge.log("afterHookedMethod:");
                    XposedBridge.log("   uri=" + uri + "-->" + uri.getHost() + uri.getPath());

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
                                    if (cursor.getString(cursor.getColumnIndex("_id")).equals("2"))
                                        copy = true;
                                    else copy = false;

                                    // for ... für XposedBridge.log >> debug
                                    for (int i = 0; i < cNames.length; i++) {
                                        XposedBridge.log(" >>>" + i + ": " + cNames[i] + " = " + cursor.getString(cursor.getColumnIndex(cNames[i])));
                                    }

                                    // wenn gestattet Kopier daten satz
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
                    if (uri.toString().equals("content://com.android.contacts/data/phones")) {
                        debug(param);
                        String[] cNames = ((Cursor) param.getResult()).getColumnNames();
                        if (cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {
                                // ob es FIELD "raw_contact_id" gibt
                                if (cursor.getColumnIndex("raw_contact_id") > -1) {
                                    //TODO 1. 2. function true oder false
                                    if (cursor.getString(cursor.getColumnIndex("raw_contact_id")).equals("2"))
                                        copy = true;
                                    else copy = false;
                                    for (int i = 0; i < cNames.length; i++) {
                                        XposedBridge.log(" >>>" + i + ": " + cNames[i] + " = " + cursor.getString(cursor.getColumnIndex(cNames[i])));
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
                                    if (cursor.getString(cursor.getColumnIndex("raw_contact_id")).equals("2"))
                                        copy = true;
                                    else copy = false;
                                    for (int i = 0; i < cNames.length; i++) {
                                        XposedBridge.log(" >>>" + i + ": " + cNames[i] + " = " + cursor.getString(cursor.getColumnIndex(cNames[i])));
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
