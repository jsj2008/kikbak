
package com.referredlabs.kikbak.ui;

import java.io.IOException;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.referredlabs.kikbak.R;
import com.referredlabs.kikbak.data.AvailableCreditType;
import com.referredlabs.kikbak.data.ClaimCreditRequest;
import com.referredlabs.kikbak.data.ClaimCreditResponse;
import com.referredlabs.kikbak.data.ClaimType;
import com.referredlabs.kikbak.http.Http;
import com.referredlabs.kikbak.store.DataStore;
import com.referredlabs.kikbak.tasks.TaskEx;
import com.referredlabs.kikbak.utils.Register;
import com.squareup.picasso.Picasso;

public class ClaimInputFragment extends KikbakFragment implements OnClickListener {
  private static final String TAG_INVALID_INFO = "tag_invalid_info";
  private static final String TAG_CLAIM_SUBMITTED = "tag_claim_submitted";

  public interface ClaimCompletedCallback {
    void onClaimCompleted();
  }

  private AvailableCreditType mCredit;

  private TextView mRewardValueTv;
  private TextView mCompanyNameTv;
  private ImageView mImage;
  private Button mButton;

  private TextView mPhoneTv;
  private TextView mNameTv;
  private TextView mStreetTv;
  private TextView mAptTv;
  private TextView mCityTv;
  private TextView mStateTv;
  private TextView mZipTv;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    String data = activity.getIntent().getStringExtra(RedeemCreditActivity.EXTRA_CREDIT);
    mCredit = new Gson().fromJson(data, AvailableCreditType.class);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_claim_reward, container, false);

    mButton = (Button) root.findViewById(R.id.button_submit);
    mButton.setOnClickListener(this);

    mRewardValueTv = (TextView) root.findViewById(R.id.reward_value);
    mCompanyNameTv = (TextView) root.findViewById(R.id.company_name);
    mImage = (ImageView) root.findViewById(R.id.image);

    mPhoneTv = (TextView) root.findViewById(R.id.phone_number);
    mNameTv = (TextView) root.findViewById(R.id.first_last_name);
    mStreetTv = (TextView) root.findViewById(R.id.address_street);
    mAptTv = (TextView) root.findViewById(R.id.address_apartment);
    mCityTv = (TextView) root.findViewById(R.id.address_city);
    mStateTv = (TextView) root.findViewById(R.id.address_state);
    mZipTv = (TextView) root.findViewById(R.id.address_zip);

    setupViews();

    return root;
  }

  private void setupViews() {
    mCompanyNameTv.setText(mCredit.merchant.name);
    Uri uri = Uri.parse(mCredit.imageUrl);
    Picasso.with(getActivity()).load(uri).into(mImage);
    setCreditAmount(mCredit.value);
  }

  private void setCreditAmount(double value) {
    String txt = getString(R.string.gift_card_amount_fmt, value);
    mRewardValueTv.setText(txt);
  }

  void onClaimSuccess() {
    DataStore.getInstance().creditUsed(mCredit.id, mCredit.value);
    showClaimSubmittedPopup();
  }

  void onClaimFailed() {
    showInvalidInfoPopup();
    mButton.setEnabled(true);
  }

  @Override
  public void onClick(View v) {
    if (validateData()) {
      mButton.setEnabled(false);
      resetTask();
      mTask = new ClaimTask();
      mTask.execute();
    }
  }

  private boolean validateData() {
    boolean isOk = true;
    isOk &= validateNotEmpty(mZipTv, R.string.claim_zip_empty)
        && validateZipCode(mZipTv, R.string.claim_zip_wrong_format);
    isOk &= validateNotEmpty(mStateTv, R.string.claim_state_empty);
    isOk &= validateNotEmpty(mCityTv, R.string.claim_city_empty);
    isOk &= validateNotEmpty(mStreetTv, R.string.claim_street_empty);
    isOk &= validateNotEmpty(mNameTv, R.string.claim_name_empty);
    isOk &= validateNotEmpty(mPhoneTv, R.string.claim_phone_empty)
        && validatePhone(mPhoneTv, R.string.claim_phone_wrong_format);
    return isOk;
  }

  boolean validateNotEmpty(TextView tv, int error) {
    if (tv.getText().toString().trim().length() == 0) {
      tv.setError(getString(error));
      tv.requestFocus();
      return false;
    }
    tv.setError(null);
    return true;
  }

  boolean validateZipCode(TextView tv, int error) {
    String zip = tv.getText().toString().trim();
    String regex = "^\\d{5}(-\\d{4})?$";
    if (!Pattern.matches(regex, zip)) {
      tv.setError(getString(R.string.claim_zip_wrong_format));
      tv.requestFocus();
      return false;
    }
    tv.setError(null);
    return true;
  }

  boolean validatePhone(TextView tv, int error) {
    if (!PhoneNumberUtils.isGlobalPhoneNumber(tv.getText().toString())) {
      tv.setError(getString(R.string.claim_phone_wrong_format));
      tv.requestFocus();
      return false;
    }
    tv.setError(null);
    return true;
  }

  private void showInvalidInfoPopup() {
    DialogFragment popup = new InvalidInfoPopup();
    popup.show(getChildFragmentManager(), TAG_INVALID_INFO);
  }

  private void showClaimSubmittedPopup() {
    DialogFragment popup = new ClaimSubmittedPopup();
    popup.show(getChildFragmentManager(), TAG_CLAIM_SUBMITTED);
  }

  public static class InvalidInfoPopup extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      return new AlertDialog.Builder(getActivity())
          .setMessage(R.string.claim_failed)
          .setPositiveButton(R.string.claim_failed_retry, null)
          .create();
    }
  }

  public static class ClaimSubmittedPopup extends DialogFragment {
    ClaimCompletedCallback mCallback;

    @Override
    public void onAttach(Activity activity) {
      super.onAttach(activity);
      mCallback = (ClaimCompletedCallback) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      AlertDialog dialog = new AlertDialog.Builder(getActivity())
          .setTitle(R.string.claim_success_title)
          .setMessage(R.string.claim_success_msg)
          .setPositiveButton(android.R.string.ok, null)
          .create();
      return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
      super.onDismiss(dialog);
      mCallback.onClaimCompleted();
    }
  }

  private class ClaimTask extends TaskEx {

    private ClaimCreditRequest mRequest;
    private long mUserId;

    ClaimTask() {
      mRequest = new ClaimCreditRequest();
      mRequest.claim = new ClaimType();
      mRequest.claim.creditId = mCredit.id;
      mRequest.claim.phoneNumber =
          PhoneNumberUtils.stripSeparators(mPhoneTv.getText().toString());
      mRequest.claim.name = mNameTv.getText().toString();
      mRequest.claim.street = mStreetTv.getText().toString();
      mRequest.claim.apt = mAptTv.getText().toString();
      mRequest.claim.city = mCityTv.getText().toString();
      mRequest.claim.state = mStateTv.getText().toString();
      mRequest.claim.zipcode = mZipTv.getText().toString();

      mUserId = Register.getInstance().getUserId();
    }

    @Override
    protected void doInBackground() throws IOException {
      String uri = Http.getUri(ClaimCreditRequest.PATH + mUserId + "/");
      Http.execute(uri, mRequest, ClaimCreditResponse.class);
    }

    @Override
    protected void onSuccess() {
      onClaimSuccess();
    }

    @Override
    protected void onFailed(Exception exception) {
      onClaimFailed();
    }
  }
}
