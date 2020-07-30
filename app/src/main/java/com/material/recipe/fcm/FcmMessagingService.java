package com.material.recipe.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.material.recipe.ActivitySplash;
import com.material.recipe.R;
import com.material.recipe.data.AppConfig;
import com.material.recipe.data.SharedPref;
import com.material.recipe.model.FcmNotif;
import com.material.recipe.utils.PermissionUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Map;

public class FcmMessagingService extends FirebaseMessagingService {

    private static int VIBRATION_TIME = 500; // in millisecond
    private SharedPref sharedPref;
    private ImageLoader imgloader = ImageLoader.getInstance();

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sharedPref = new SharedPref(this);
        sharedPref.setFcmRegId(s);
        sharedPref.setOpenAppCounter(SharedPref.MAX_OPEN_COUNTER);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        sharedPref = new SharedPref(this);

        if(AppConfig.REFRESH_DATA_NOTIF){
            sharedPref.setRefreshRecipe(true);
            sharedPref.setRefreshCategory(true);
        }
        if (imgloader.isInited() && AppConfig.REFRESH_IMG_NOTIF) {
            imgloader.clearDiskCache();
            imgloader.clearMemoryCache();
        }

        if (sharedPref.getNotification() && PermissionUtil.isStorageGranted(this)) {
            if (remoteMessage.getData().size() == 0) return;
            Map<String, String> data = remoteMessage.getData();
            final FcmNotif fcmNotif = new FcmNotif();
            fcmNotif.title = data.get("title");
            fcmNotif.content = data.get("content");
            displayNotificationIntent(fcmNotif);
        }
    }

    private void displayNotificationIntent(FcmNotif fcmNotif) {
        playRingtoneVibrate(this);
        Intent intent = new Intent(this, ActivitySplash.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(fcmNotif.title);
        builder.setContentText(fcmNotif.content);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            builder.setPriority(Notification.PRIORITY_HIGH);

        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(fcmNotif.content));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        int unique_id = (int) System.currentTimeMillis();
        notificationManager.notify(unique_id, builder.build());
    }

    private void playRingtoneVibrate(Context context) {
        try {
            // play vibration
            if (sharedPref.getVibration()) {
                ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VIBRATION_TIME);
            }
            RingtoneManager.getRingtone(context, Uri.parse(sharedPref.getRingtone())).play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
