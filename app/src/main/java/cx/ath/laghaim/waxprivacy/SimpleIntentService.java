package cx.ath.laghaim.waxprivacy;

/**
 * Created by dante on 18.05.17.
 */

import android.app.IntentService;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * Created by dante on 17.05.17.
 */

public class SimpleIntentService extends IntentService {
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
    public static final String ACTION_RESP = "cx.ath.laghaim"+".intent.action.MESSAGE_PROCESSED";

    public SimpleIntentService() {
        super("SimpleIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String msg = intent.getStringExtra(PARAM_IN_MSG);

        String resultTxt = "" + DateFormat.format("MM/dd/yy hh:mm:ss", System.currentTimeMillis());

        Log.v("SimpleIntentService", "Handling msg: " + resultTxt);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(cx.ath.laghaim.waxprivacy.ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
        sendBroadcast(broadcastIntent);
    }



}
