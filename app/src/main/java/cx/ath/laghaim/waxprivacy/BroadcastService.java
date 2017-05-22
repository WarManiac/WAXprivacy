package cx.ath.laghaim.waxprivacy;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/**
 * Created by dante on 21.05.17.
 */

public class BroadcastService  extends Service {
    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = " cx.ath.laghaim.waxprivacy.displayevent";
    private final Handler handler = new Handler();
    Intent intent;
    int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        intent = new Intent(BROADCAST_ACTION);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            handler.postDelayed(this, 10000); // 10 seconds
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
    }
}
