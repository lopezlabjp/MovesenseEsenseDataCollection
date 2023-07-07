package jp.lopezlab.aoyagi.movesensedatacollection.activity_01_connection;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

public enum ConnectingDialog {
    INSTANCE;

    private AlertDialog alertDialog;

    public void showDialog(Context context) {
        if (alertDialog == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("connecting...")
                    .setMessage("might be 30s");

            alertDialog = alertDialogBuilder.show();
        }
    }

    public void dismissDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }
}
