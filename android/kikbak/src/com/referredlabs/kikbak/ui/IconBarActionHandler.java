
package com.referredlabs.kikbak.ui;

import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import com.flurry.android.FlurryAgent;
import com.referredlabs.kikbak.data.MerchantLocationType;
import com.referredlabs.kikbak.log.Log;
import com.referredlabs.kikbak.ui.IconBarHelper.IconBarListener;
import com.referredlabs.kikbak.utils.Nearest;

public class IconBarActionHandler implements IconBarListener {

  Activity mActivity;

  IconBarActionHandler(Activity activity) {
    mActivity = activity;
  }

  @Override
  public void onMapIconClicked(Nearest nearest) {
    MerchantLocationType loc = nearest.get();
    String address = String.format("%s, %s, %s", loc.address1, loc.city, loc.state);
    String uri = String.format(Locale.US, "geo:0,0?q=%s(%s)", Uri.encode(address),
        Uri.encode(nearest.getMerchantName()));
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    start(intent);
  }

  @Override
  public void onPhoneIconClicked(String phone) {
    String uri = "tel:" + phone;
    // ACTION_CALL and android.permission.CALL_PHONE to call directly
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    start(intent);
  }

  @Override
  public void onWebIconClicked(String url) {
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    start(intent);
  }

  private void start(Intent intent) {
    try {
      mActivity.startActivity(intent);
    } catch (ActivityNotFoundException e) {
      FlurryAgent.onError(Log.E_START_ACTIVITY, e.toString(), Log.CLASS_LOCAL);
    }
  }

}
