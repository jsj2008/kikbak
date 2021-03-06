
package com.referredlabs.kikbak.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.referredlabs.kikbak.store.DataStore;

public class GcmBroadcastReceiver extends BroadcastReceiver {

  private static final String KEY_TYPE = "type";
  private static final String KEY_TITLE = "title";
  private static final String KEY_MSG = "msg";
  private static final String KEY_OFFER_ID = "offerId";
  private static final String KEY_FROM = "from";
  private static final String KEY_TO = "to";

  @Override
  public void onReceive(Context context, Intent intent) {
    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
    String messageType = gcm.getMessageType(intent);
    if (messageType != null) {
      String type = intent.getStringExtra(KEY_TYPE);
      String title = intent.getStringExtra(KEY_TITLE);
      String msg = intent.getStringExtra(KEY_MSG);

      NotificationController.getInstance().showNotification(type, title, msg);
      DataStore.getInstance().refreshRewards();
    }
    setResultCode(Activity.RESULT_OK);
  }
}
