
package com.referredlabs.kikbak.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.webkit.WebView;

import com.referredlabs.kikbak.R;

public class TermsDialog extends DialogFragment {

  private static final String ARG_URL = "url";

  public static TermsDialog newInstance(String url) {
    TermsDialog dialog = new TermsDialog();
    Bundle args = new Bundle();
    args.putString(ARG_URL, url);
    dialog.setArguments(args);
    return dialog;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    String url = getArguments().getString(ARG_URL);
    WebView webView = new WebView(getActivity());
    webView.loadUrl(url);
    AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
    b.setTitle(R.string.terms_title);
    b.setView(webView);
    b.setPositiveButton(R.string.accept_terms, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    return b.create();
  }

}
