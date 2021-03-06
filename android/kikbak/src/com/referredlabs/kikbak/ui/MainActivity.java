
package com.referredlabs.kikbak.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.referredlabs.kikbak.BuildConfig;
import com.referredlabs.kikbak.D;
import com.referredlabs.kikbak.R;
import com.referredlabs.kikbak.data.AvailableCreditType;
import com.referredlabs.kikbak.data.GiftType;
import com.referredlabs.kikbak.gcm.GcmHelper;
import com.referredlabs.kikbak.log.Log;
import com.referredlabs.kikbak.service.LocationFinder;
import com.referredlabs.kikbak.service.LocationFinder.LocationFinderListener;
import com.referredlabs.kikbak.store.DataStore;
import com.referredlabs.kikbak.store.TheOffer;
import com.referredlabs.kikbak.store.TheReward;
import com.referredlabs.kikbak.twitter.TwitterHelper;
import com.referredlabs.kikbak.ui.OfferListFragment.OnOfferClickedListener;
import com.referredlabs.kikbak.ui.RewardListFragment.OnRedeemListener;
import com.referredlabs.kikbak.utils.Register;

public class MainActivity extends KikbakActivity implements ActionBar.TabListener,
    OnOfferClickedListener, OnRedeemListener,
    LocationFinderListener {

  public static final String ARG_SHOW_REWARDS = "rewards";

  SectionsPagerAdapter mSectionsPagerAdapter;
  ViewFlipper mViewFlipper;
  ViewPager mViewPager;
  LocationFinder mLocationFinder;
  boolean mLocationWarningShown = false;
  TheReward mSelectedReward;

  BroadcastReceiver mNetworkStateReceiver;
  boolean mIsConnected;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!Register.getInstance().isRegistered()) {
      startActivity(new Intent(this, LoginActivity.class));
      finish();
      return;
    }
    setContentView(R.layout.activity_main);
    GcmHelper.getInstance().registerIfNeeded();
    mLocationFinder = new LocationFinder(this);
    setupViews();
    DataStore.getInstance().refreshOffers();
    DataStore.getInstance().refreshRewards();
  }

  void setupViews() {
    mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

    mViewPager = (ViewPager) findViewById(R.id.pager);
    mViewPager.setAdapter(mSectionsPagerAdapter);

    if (getIntent().getBooleanExtra(ARG_SHOW_REWARDS, false)) {
      mViewPager.setCurrentItem(1);
    }

    mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);
    setupActionBar();

    mIsConnected = isConnected();
    if (!mIsConnected && DataStore.getInstance().isEmpty()) {
      mViewFlipper.setDisplayedChild(1);
    }
  }

  void setupActionBar() {
    final ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        FlurryAgent.logEvent(position == 0 ? Log.EVENT_OFFERS_LIST : Log.EVENT_REWARDS_LIST);
        actionBar.setSelectedNavigationItem(position);
        RewardListFragment rl = mSectionsPagerAdapter.getRewardFragment();
        if (rl != null)
          rl.setActive(position == 1);
      }
    });

    for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
      Tab tab = actionBar.newTab();
      tab.setText(mSectionsPagerAdapter.getPageTitle(i));
      tab.setTabListener(this);
      actionBar.addTab(tab, false);
    }
    actionBar.setSelectedNavigationItem(mViewPager.getCurrentItem());
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerNetworkStateReceiver();
    mLocationFinder.startLocating();
    if (!mLocationWarningShown && !LocationFinder.isLocationEnabled()) {
      mLocationWarningShown = true;
      showLocationWarningDialog();
    }
  }

  private void showLocationWarningDialog() {
    AlertDialog.Builder b = new AlertDialog.Builder(this);
    b.setTitle(R.string.location_disabled_title);
    b.setMessage(R.string.location_disabled_message);
    b.setPositiveButton(R.string.ok, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        try {
          startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        } catch (Exception e) {
          // ignore
        }
      }
    });
    b.show();
  }

  @Override
  protected void onPause() {
    super.onPause();
    unregisterNetworkStateReceiver();
    mLocationFinder.stopLocating();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    if (BuildConfig.DEBUG) {
      getMenuInflater().inflate(R.menu.debug, menu);
    }
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem item = menu.findItem(R.id.action_debug_fixed_location);
    if (item != null)
      item.setChecked(D.USE_FIXED_LOCATION);

    item = menu.findItem(R.id.action_debug_bypass_geo_fence);
    if (item != null)
      item.setChecked(D.BYPASS_STORE_CHECK);

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_suggest:
        onSuggestClicked();
        break;

      case R.id.action_debug_clear_registration:
        Register.getInstance().clear();
        GcmHelper.getInstance().clear();
        TwitterHelper.getInstance().resetAuthorization();
        finish();
        return true;

      case R.id.action_debug_gcm:
        debugGcm();
        return true;

      case R.id.action_debug_fixed_location:
        D.USE_FIXED_LOCATION = !D.USE_FIXED_LOCATION;
        item.setChecked(D.USE_FIXED_LOCATION);
        DataStore.getInstance().refreshRewards();
        DataStore.getInstance().refreshOffers();
        return true;

      case R.id.action_debug_bypass_geo_fence:
        D.BYPASS_STORE_CHECK = !D.BYPASS_STORE_CHECK;
        item.setChecked(D.BYPASS_STORE_CHECK);
        return true;

      case R.id.action_debug_who:
        debugWhoAmI();
        return true;

    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    mViewPager.setCurrentItem(tab.getPosition());
  }

  @Override
  public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
  }

  @Override
  public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
  }

  @Override
  public void onOfferClicked(TheOffer theOffer) {
    give(theOffer);
  }

  @Override
  public void onSuggestClicked() {
    Intent intent = new Intent(this, SuggestBusinessActivity.class);
    startActivity(intent);
  }

  private void give(TheOffer theOffer) {
    Intent intent = new Intent(this, GiveActivity.class);
    Gson gson = new Gson();
    String data = gson.toJson(theOffer.getOffer());
    intent.putExtra(GiveActivity.ARG_OFFER, data);
    startActivity(intent);
  }

  @Override
  public void onRedeemGift(GiftType gift, int shareIdx) {
    redeemGift(gift, shareIdx);
  }

  @Override
  public void onRedeemCredit(AvailableCreditType credit) {
    redeemCredit(credit);
  }

  @Override
  public void onShowOffersCliked() {
    getSupportActionBar().setSelectedNavigationItem(0);
  }

  private void redeemGift(GiftType gift, int shareIdx) {
    Intent intent = new Intent(this, RedeemGiftActivity.class);
    String data = new Gson().toJson(gift);
    intent.putExtra(RedeemGiftActivity.EXTRA_GIFT, data);
    intent.putExtra(RedeemGiftActivity.EXTRA_SHARE_IDX, shareIdx);
    startActivity(intent);
  }

  private void redeemCredit(AvailableCreditType credit) {
    Intent intent = new Intent(this, RedeemCreditActivity.class);
    String data = new Gson().toJson(credit);
    intent.putExtra(RedeemCreditActivity.EXTRA_CREDIT, data);
    startActivity(intent);
  }

  boolean isConnected() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    return isConnected;
  }

  void unregisterNetworkStateReceiver() {
    if (mNetworkStateReceiver != null) {
      unregisterReceiver(mNetworkStateReceiver);
      mNetworkStateReceiver = null;
    }
  }

  void registerNetworkStateReceiver() {
    mNetworkStateReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        connectionChanged();
      }
    };

    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    registerReceiver(mNetworkStateReceiver, filter);
  }

  private void connectionChanged() {
    boolean wasConnected = mIsConnected;
    mIsConnected = isConnected();

    if (wasConnected && !mIsConnected) {
      // disconnected
      if (DataStore.getInstance().isEmpty()) {
        // no connection and no offers or rewards, let show no connection
        mViewFlipper.setDisplayedChild(1);
      }
    }
    if (!wasConnected && mIsConnected) {
      // re-connected
      mViewFlipper.setDisplayedChild(0);
      DataStore.getInstance().refreshOffers();
      DataStore.getInstance().refreshRewards();
    }
  }

  @Override
  public void onLocationUpdated(Location location) {
    OfferListFragment frag = mSectionsPagerAdapter.getOfferFragment();
    if (frag != null)
      frag.setUserLocation(location);
  }

  private void debugGcm() {
    GcmHelper helper = GcmHelper.getInstance();
    String regId = helper.getRegistrationId();
    if (regId != null) {
      Toast.makeText(this, regId, Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(this, "Not yet registered!", Toast.LENGTH_SHORT).show();
    }
  }

  private void debugWhoAmI() {
    String user = Register.getInstance().getFullName();
    long id = Register.getInstance().getUserId();
    Toast.makeText(this, user + " (" + id + ")", Toast.LENGTH_LONG).show();
  }

  // ------------------------------------------------------

  private class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public int getCount() {
      return 2;
    }

    @Override
    public Fragment getItem(int position) {
      if (position == 0)
        return new OfferListFragment();
      if (position == 1)
        return new RewardListFragment();

      throw new IllegalArgumentException();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      if (position == 0)
        return getString(R.string.title_offers);
      if (position == 1)
        return getString(R.string.title_redeem);
      throw new IllegalArgumentException();
    }

    public Fragment getFragmentByPosition(int position) {
      return getSupportFragmentManager().findFragmentByTag(
          "android:switcher:" + mViewPager.getId() + ":" + getItemId(position));
    }

    public OfferListFragment getOfferFragment() {
      return (OfferListFragment) getFragmentByPosition(0);
    }

    public RewardListFragment getRewardFragment() {
      return (RewardListFragment) getFragmentByPosition(1);
    }

  }
}
