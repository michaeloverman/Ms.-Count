package tech.michaeloverman.android.mscount;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import tech.michaeloverman.android.mscount.utils.Metronome;
import tech.michaeloverman.android.mscount.utils.PrefUtils;
import timber.log.Timber;

public class MsCountActivity extends tech.michaeloverman.android.mscount.SingleFragmentActivity {

    public Metronome mMetronome;

    @Override
    protected Fragment createFragment() {
        Timber.d("MsCountActivity createFragment()");
        return MetronomeSelectorFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("MsCountActivity onCreate()");

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-9915736656105375~9633528243");

        checkIfWearableConnected();

        mMetronome = Metronome.getInstance();
        mMetronome.setContext(this);
    }

    private GoogleApiClient client;
    private static final long CONNECTION_TIME_OUT_MS = 3000;

    public void checkIfWearableConnected() {
        Timber.d("checking in wearable present");
        retrieveDeviceNode(new Callback() {
            @Override
            public void success(String nodeId) {
                Timber.d("Wear node detected");
                PrefUtils.saveWearStatus(MsCountActivity.this, true);
            }

            @Override
            public void failed(String message) {
                Timber.d("No Wear node detected");
                PrefUtils.saveWearStatus(MsCountActivity.this, false);
            }
        });

    }

    private GoogleApiClient getGoogleApiClient(Context context) {
        Timber.d("getting googleapiclient for checking wearable");
        if (client == null)
            client = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .build();
        return client;
    }

    private interface Callback {
        void success(final String nodeId);
        void failed(final String message);
    }

    private void retrieveDeviceNode(final Callback callback) {
        Timber.d("retrieving device nodes");
        final GoogleApiClient client = getGoogleApiClient(this);
        new Thread(new Runnable() {

            @Override
            public void run() {
                Timber.d("running device check thread");
                client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(client).await();
                Timber.d("result: " + result.toString());
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    String nodeId = nodes.get(0).getId();
                    callback.success(nodeId);
                } else {
                    callback.failed("no wearables found");
                }
                Timber.d("disconnecting client");
                client.disconnect();
            }
        }).start();
    }


}
