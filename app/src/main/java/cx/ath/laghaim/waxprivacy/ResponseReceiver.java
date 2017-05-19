package cx.ath.laghaim.waxprivacy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

/**
 * Created by dante on 18.05.17.
 */

public class ResponseReceiver extends BroadcastReceiver {
    public static final String ACTION_RESP = "cx.ath.laghaim"+".intent.action.MESSAGE_PROCESSED";

    public  MainActivity tt;

    public ResponseReceiver(MainActivity mainActivity) {
        tt=mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Update UI, new "message" processed by SimpleIntentService
        String text = intent.getStringExtra(SimpleIntentService.PARAM_OUT_MSG);

    }

}
