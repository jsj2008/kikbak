
package com.referredlabs.kikbak.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.referredlabs.kikbak.R;
import com.referredlabs.kikbak.data.ClientOfferType;
import com.referredlabs.kikbak.data.GiftType;
import com.referredlabs.kikbak.data.ValidationType;
import com.referredlabs.kikbak.store.DataStore;
import com.referredlabs.kikbak.utils.BarcodeGenerator;

public class RedeemGiftSuccessFragment extends Fragment implements OnClickListener {

  private GiftType mGift;

  private Bitmap mBarcodeBitmap;
  private String mBarcode;

  private TextView mName;
  private TextView mNoteSecond;
  private TextView mValue;
  private TextView mDesc;
  private ImageView mCode;
  private TextView mTextCode;
  private Button mGive;

  public static RedeemGiftSuccessFragment newInstance() {
    RedeemGiftSuccessFragment fragment = new RedeemGiftSuccessFragment();
    return fragment;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    Bundle args = getActivity().getIntent().getExtras();
    String data = args.getString(SuccessActivity.ARG_GIFT);
    mGift = new Gson().fromJson(data, GiftType.class);
    mBarcodeBitmap = args.getParcelable(SuccessActivity.ARG_BARCODE_BITMAP);
    mBarcode = args.getString(SuccessActivity.ARG_BARCODE);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_redeem_gift_success, container, false);
    mName = (TextView) root.findViewById(R.id.name);
    mNoteSecond = (TextView) root.findViewById(R.id.redeem_note_second);
    mValue = (TextView) root.findViewById(R.id.redeem_value);
    mDesc = (TextView) root.findViewById(R.id.redeem_desc);
    mGive = (Button) root.findViewById(R.id.give);
    mGive.setOnClickListener(this);

    ViewStub stub = (ViewStub) root.findViewById(R.id.barcode_area);
    stub.setLayoutResource(getBarcodeLayout());
    stub.inflate();

    mCode = (ImageView) root.findViewById(R.id.code);
    mTextCode = (TextView) root.findViewById(R.id.text_code);

    setupViews();
    return root;
  }

  private int getBarcodeLayout() {
    String validationType = mGift.validationType;
    return ValidationType.BARCODE.equals(validationType) ? R.layout.fragment_redeem_success_barcode
        : R.layout.fragment_redeem_success_qrcode;
  }

  private void setupViews() {
    setupGiftViews();
    Bitmap bmp = mBarcodeBitmap;
    if (bmp == null) {
      bmp = BarcodeGenerator.generateQrCode(getActivity(), mBarcode);
    }
    mCode.setImageBitmap(bmp);
    mTextCode.setText(mBarcode);
  }

  private void setupGiftViews() {
    mName.setText(mGift.merchant.name);
    if (ValidationType.QRCODE.equals(mGift.validationType))
      mNoteSecond.setText(R.string.redeem_success_note_second);
    else
      mNoteSecond.setText(R.string.redeem_success_note_second_integrated);
    mValue.setText(mGift.desc);
    mDesc.setText(mGift.detailedDesc);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.give:
        onGiveClicked();
        break;
    }
  }

  private void onGiveClicked() {
    getActivity().finish();
    ClientOfferType offer = DataStore.getInstance().getOffer(mGift.offerId);
    if (offer != null) {
      Intent intent = new Intent(getActivity(), GiveActivity.class);
      Gson gson = new Gson();
      String data = gson.toJson(offer);
      intent.putExtra(GiveActivity.ARG_OFFER, data);
      startActivity(intent);
    }
  }
}
