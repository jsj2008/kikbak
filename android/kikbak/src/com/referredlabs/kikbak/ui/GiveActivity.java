
package com.referredlabs.kikbak.ui;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.referredlabs.kikbak.R;
import com.referredlabs.kikbak.data.ClientOfferType;
import com.referredlabs.kikbak.service.LocationFinder;
import com.referredlabs.kikbak.ui.PublishFragment.ShareStatusListener;
import com.referredlabs.kikbak.ui.ShareOptionsFragment.OnShareMethodSelectedListener;
import com.referredlabs.kikbak.utils.Nearest;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class GiveActivity extends FragmentActivity implements OnClickListener,
    OnShareMethodSelectedListener,
    ShareStatusListener {

  private static final String STATE_PHOTO_URI = "photo_uri";
  private static final String STATE_CROP_URI = "crop_uri";

  private static final int REQUEST_TAKE_PHOTO = 1;
  private static final int REQUEST_CROP_PHOTO = 2;

  private ClientOfferType mOffer;
  private ImageView mImage;
  private View mTakePhoto;
  private EditText mComment;
  private Uri mPhotoUri;
  private Uri mCroppedPhotoUri;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_give);
    mImage = (ImageView) findViewById(R.id.image);
    mComment = (EditText) findViewById(R.id.comment);
    mTakePhoto = findViewById(R.id.take_photo);
    mTakePhoto.setOnClickListener(this);

    findViewById(R.id.give).setOnClickListener(this);
    findViewById(R.id.terms).setOnClickListener(this);
    String data = getIntent().getStringExtra("data");
    mOffer = new Gson().fromJson(data, ClientOfferType.class);
    setupViews();
  }

  private void setupViews() {
    Uri uri = Uri.parse(mOffer.giveImageUrl);
    Picasso.with(this).load(uri).into(mImage);
    ((TextView) findViewById(R.id.name)).setText(mOffer.merchantName);
    ((TextView) findViewById(R.id.gift_desc)).setText(mOffer.giftDesc);
    ((TextView) findViewById(R.id.gift_desc_opt)).setText(mOffer.giftDetailedDesc);
    ((TextView) findViewById(R.id.reward_desc)).setText(mOffer.kikbakDesc);
    ((TextView) findViewById(R.id.reward_desc_opt)).setText(mOffer.kikbakDetailedDesc);

    IconBarHelper iconBar = new IconBarHelper(findViewById(R.id.icon_bar),
        new IconBarActionHandler(this));
    iconBar.setLink(mOffer.merchantUrl);

    Location location = LocationFinder.getLastLocation();
    double latitude = location.getLatitude();
    double longitude = location.getLongitude();
    Nearest nearest = new Nearest(mOffer.locations);
    nearest.determineNearestLocation(latitude, longitude);
    iconBar.setPhone(Long.toString(nearest.getPhoneNumber()));
    iconBar.setLocation(nearest);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.take_photo:
        onTakePhotoClicked();
        break;

      case R.id.give:
        onShareClicked();
        break;

      case R.id.terms:
        onTermsClicked();
        break;
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mPhotoUri != null)
      outState.putParcelable(STATE_PHOTO_URI, mPhotoUri);
    if (mCroppedPhotoUri != null)
      outState.putParcelable(STATE_CROP_URI, mCroppedPhotoUri);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    mPhotoUri = savedInstanceState.getParcelable(STATE_PHOTO_URI);
    mCroppedPhotoUri = savedInstanceState.getParcelable(STATE_CROP_URI);

    if (mCroppedPhotoUri != null) {
      onPhotoTaken();
    }
  }

  protected void onTakePhotoClicked() {
    try {
      File file = getTempFile();
      mPhotoUri = Uri.fromFile(file);
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
      startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void onPhotoTakenAsync() {
    mImage.post(new Runnable() {
      @Override
      public void run() {
        onPhotoTaken();
      }
    });
  }

  protected void onPhotoTaken() {
    mTakePhoto.setVisibility(View.INVISIBLE);
    mImage.setImageURI(mCroppedPhotoUri);
  }

  protected void onTermsClicked() {
    String title = getString(R.string.terms_title);
    String url = mOffer.tosUrl;
    NoteDialog dialog = NoteDialog.newInstance(title, url);
    dialog.show(getSupportFragmentManager(), null);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
      try {
        File file = getTempFile();
        mCroppedPhotoUri = Uri.fromFile(file);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mPhotoUri, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCroppedPhotoUri);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        startActivityForResult(intent, REQUEST_CROP_PHOTO);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return;
    }

    if (requestCode == REQUEST_CROP_PHOTO && resultCode == RESULT_OK) {
      removeFile(mPhotoUri);
      mPhotoUri = null;
      onPhotoTakenAsync();
    }
  }

  protected void onShareClicked() {
    ShareOptionsFragment dialog = ShareOptionsFragment.newInstance();
    dialog.show(getFragmentManager(), "");
  }

  private File getTempFile() throws IOException {
    // TODO: what if there is no sd card ?
    // need a way to grant access to a file in app space
    return File.createTempFile("kikbak", ".jpg", getExternalCacheDir());
  }

  private static void removeFile(Uri uri) {
    if (uri != null) {
      File f = new File(uri.getPath());
      f.delete();
    }
  }

  @Override
  public void onSendViaEmail() {
    Intent intent = new Intent(this, ShareViaEmailActivity.class);
    intent.putExtra(ShareViaEmailActivity.ARG_COMMENT, mComment.getText().toString());
    intent.putExtra(ShareViaEmailActivity.ARG_OFFER_ID, mOffer.id);
    intent.putExtra(ShareViaEmailActivity.ARG_MERCHANT_ID, mOffer.merchantId);
    intent.putExtra(ShareViaEmailActivity.ARG_LOCATION_ID, mOffer.locations[0].locationId); // FIXME
    intent.putExtra(ShareViaEmailActivity.ARG_DEFAULT_PHOTO, mOffer.giveImageUrl);
    if (mCroppedPhotoUri != null) {
      intent.putExtra(ShareViaEmailActivity.ARG_USER_PHOTO, mCroppedPhotoUri.getPath());
    }
    startActivity(intent);
  }

  @Override
  public void onSendViaFacebook() {
    Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onPostOnFacebook() {
    android.util.Log.d("MMM", "post on timeline");
    if (mCroppedPhotoUri == null) {
      // TODO: extract original photo
      Toast.makeText(this, "Take photo first!", Toast.LENGTH_LONG).show();
      return;
    }

    String path = mCroppedPhotoUri.getPath();
    String comment = mComment.getText().toString();
    long locationId = mOffer.locations[0].locationId;
    PublishFragment publish = PublishFragment.newInstance(comment, path, mOffer.id,
        mOffer.merchantId, locationId);
    publish.show(getSupportFragmentManager(), "");
  }

  @Override
  public void onShareFinished(boolean success) {
    String txt = "You have shared a gift " + (success ? "succesfully" : "unsuccesfully");
    Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
    finish();
  }
}
