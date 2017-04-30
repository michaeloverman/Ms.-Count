package tech.michaeloverman.android.mscount.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import tech.michaeloverman.android.mscount.R;
import timber.log.Timber;

/**
 * Created by Michael on 4/29/2017.
 */

public class WearNotificationManager extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 3233;
    private static final int REQUEST_CODE = 322;

    private static final String ACTION_PLAY = "tech.michaeloverman.android.mscount.play";
    private static final String ACTION_PAUSE = "tech.michaeloverman.android.mscount.pause";

    private final Context mContext;
    private final MetronomeListener mListener;
    private String mTitle;
    private String mMessage;

    private MediaSessionCompat mSession;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;
    private PlaybackStateCompat mPlaybackState;
    private final NotificationManagerCompat mNotificationManager;

    private final PendingIntent mPlayIntent;
    private final PendingIntent mPauseIntent;

    private boolean mStarted = false;

    public WearNotificationManager(Context context, MetronomeListener ml,
                                   String title, String message) throws RemoteException {
        mContext = context;
        mListener = ml;
        mTitle = title;
        mMessage = message;

//        updateSessionToken();

        mNotificationManager = NotificationManagerCompat.from(mContext);

        mPlayIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT);
        mPauseIntent = PendingIntent.getBroadcast(context, REQUEST_CODE,
                new Intent(ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationManager.cancelAll();

    }

    public void startNotification() {
        if (!mStarted) {
//            mPlaybackState = mController.getPlaybackState();

            // The notification must be updated after setting started to true
            Notification notification = getStartStopNotif();
            if (notification != null) {
//                mController.registerCallback(mCb);
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                mListener.registerReceiver(this, filter);

                Timber.d("...gotten this far...");
//                mService.startForeground(NOTIFICATION_ID, notification);
                mNotificationManager.notify(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

//    private void updateSessionToken() throws RemoteException {
//        mSession = new MediaSessionCompat(mContext, "MetronomeSession");
//        mSessionToken = mSession.getSessionToken();
//        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
//        mController = new MediaControllerCompat(mContext, mSessionToken);
//        mController.registerCallback(mCb);
//    }

//    private final MediaControllerCompat.Callback mCb = new MediaControllerCompat.Callback() {
//        @Override
//        public void onPlaybackStateChanged(PlaybackStateCompat state) {
//            mPlaybackState = state;
//            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
//                    state.getState() == PlaybackStateCompat.STATE_NONE) {
//                stopNotification();
//            } else {
//                Notification notification = getStartStopNotif();
//                if (notification != null) {
//                    mNotificationManager.notify(NOTIFICATION_ID, notification);
//                }
//            }
//        }
//    };

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
//            mController.unregisterCallback(mCb);
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mListener.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
//            mListener.stopForeground(true);
        }
    }

//    private PendingIntent getExamplePendingIntent(Context context) {
//        Intent intent = new Intent("tech.michaeloverman.android.mscount.wearable.pendingintent")
//                .setClass(context, ProgrammedMetronomeFragment.class);
//        intent.putExtra(EXTRA_MESSAGE, "extra pending intent message: here it is");
//        Timber.d("returning example pending intent");
//        return PendingIntent.getBroadcast(context, 007, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }
    public Notification getStartStopNotif() {
        Timber.d("sendStartStopNotifToWear()");
        int notifId = 3435;

        String label;
        int icon;
        PendingIntent intent;
//        if(mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
        if(mStarted) {
            label = "Pause";
            icon = R.drawable.ic_pause;
            intent = mPauseIntent;
        } else {
            label = "Play";
            icon = R.drawable.ic_play_arrow;
            intent = mPlayIntent;
        }







        Intent wearIntent = new Intent(Metronome.ACTION_METRONOME_START_STOP);
//        wearIntent.putExtra(EXTRA_WEAR_INTENT_ID, 42);
        PendingIntent wearPendingIntent = PendingIntent.getBroadcast(
                mContext, notifId, wearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Timber.d("Pending Intent created");

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                icon,
                label,
                intent)
                .build();
        Timber.d("action created");

        NotificationCompat.WearableExtender wearExtend = new NotificationCompat.WearableExtender()
                .setContentAction(0)
                .setHintHideIcon(true)
                .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_FULL_SCREEN)
                .setHintScreenTimeout(NotificationCompat.WearableExtender.SCREEN_TIMEOUT_LONG)
                .setBackground(BitmapFactory.decodeResource(
                        mContext.getResources(), R.mipmap.ic_launcher))
                .setContentIntentAvailableOffline(false)
                .setHintAmbientBigPicture(true)
                .addAction(action);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(mContext)
                .setStyle(new android.support.v7.app.NotificationCompat.MediaStyle()
//                    .setShowActionsInCompactView(action)  //TODO this
                    .setMediaSession(mSessionToken))
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(mTitle)
                .setContentText(mMessage)
                .setOnlyAlertOnce(true)
//                .setGroup("GROUP_KEY")
                .setVibrate(new long[] { 50, 50, 500 })
//                .addAction(action)
                .setContentIntent(wearPendingIntent)
                .extend(wearExtend);
//                .addAction(android.R.drawable.ic_media_play, playPauseIntent);
        Timber.d("notifBuilder created");

        Notification notif = notifBuilder.build();

        return notif;


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch(action) {
            case ACTION_PAUSE:
                mListener.metronomeStartStop();
//                mTransportControls.pause();
                mStarted = false;
                break;
            case ACTION_PLAY:
                mListener.metronomeStartStop();
//                mTransportControls.play();
                mStarted = true;
                break;
            case Metronome.ACTION_METRONOME_START_STOP:
                Timber.d("intent received, but not the one I thought...");
                break;
            default:
                Timber.d("Unknown intent ignored. Action: " + action);
        }
    }
}
