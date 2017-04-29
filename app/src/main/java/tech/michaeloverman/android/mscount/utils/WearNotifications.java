package tech.michaeloverman.android.mscount.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import tech.michaeloverman.android.mscount.R;
import tech.michaeloverman.android.mscount.programmed.ProgrammedMetronomeFragment;
import timber.log.Timber;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

/**
 * Created by Michael on 4/29/2017.
 */

public class WearNotifications {

    private static final String EXTRA_WEAR_INTENT_ID = "tech.michaeloverman.android.mscount.wearable.extra_message";

    private PendingIntent getExamplePendingIntent(Context context) {
        Intent intent = new Intent("tech.michaeloverman.android.mscount.wearable.pendingintent")
                .setClass(context, ProgrammedMetronomeFragment.class);
        intent.putExtra(EXTRA_MESSAGE, "extra pending intent message: here it is");
        Timber.d("returning example pending intent");
        return PendingIntent.getBroadcast(context, 007, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public static void sendStartStopNotifToWear(Context context, String title, String message) {
        Timber.d("sendStartStopNotifToWear()");
        int notifId = 3435;
        Intent wearIntent = new Intent(Metronome.ACTION_METRONOME_START_STOP);
//        wearIntent.putExtra(EXTRA_WEAR_INTENT_ID, 42);
        PendingIntent wearPendingIntent = PendingIntent.getBroadcast(
                context, notifId, wearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Timber.d("Pending Intent created");

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "Start/Stop",
                wearPendingIntent)
                .build();
        Timber.d("action created");

        NotificationCompat.WearableExtender wearExtend = new NotificationCompat.WearableExtender()
                .setContentAction(0)
                .setHintHideIcon(true)
                .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_FULL_SCREEN)
                .setHintScreenTimeout(NotificationCompat.WearableExtender.SCREEN_TIMEOUT_LONG)
                .setBackground(BitmapFactory.decodeResource(
                        context.getResources(), R.mipmap.ic_launcher))
                .setContentIntentAvailableOffline(false)
//                .setHintAmbientBigPicture(true)
                .addAction(action);

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(title)
                .setContentText(message)
                .setVibrate(new long[] { 50, 50, 500 })
//                .addAction(action)
                .setContentIntent(wearPendingIntent)
                .extend(wearExtend);
//                .addAction(android.R.drawable.ic_media_play, playPauseIntent);
        Timber.d("notifBuilder created");

        Notification notif = notifBuilder.build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(notifId, notif);
        Timber.d("manager has notified...");
    }
}
