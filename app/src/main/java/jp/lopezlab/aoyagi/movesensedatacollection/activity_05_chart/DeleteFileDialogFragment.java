package jp.lopezlab.aoyagi.movesensedatacollection.activity_05_chart;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import jp.lopezlab.aoyagi.movesensedatacollection.R;
import jp.lopezlab.aoyagi.movesensedatacollection.activity_04_file_list.FileListActivity;

public class DeleteFileDialogFragment extends DialogFragment {

    private String filename;
    //trueなら削除、キャンセルならfalse
    protected boolean isDelete;
    private AppCompatActivity appCompatActivity;

    public DeleteFileDialogFragment(String name,AppCompatActivity activity) {
        this.filename=name;
        this.appCompatActivity=activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //ダイアログビルダーを生成
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //ダイアログのタイトル
        builder.setTitle(R.string.dialog_title);
        //削除するファイルの内容
        builder.setMessage(filename+"を削除しますか");
        //削除ボタン
        builder.setPositiveButton(R.string.dialog_btn_delete,new DialogButtonClickListener());
        //キャンセルボタン
        builder.setNegativeButton(R.string.dialog_btn_cancel,new DialogButtonClickListener());
        AlertDialog dialog=builder.create();
        return dialog;
    }

    private class DialogButtonClickListener implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog,int which){
            //削除するかどうか
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    isDelete=true;
                    Intent backIntent = new Intent();
                    backIntent.putExtra("FILE_DELETE", true);
                    backIntent.putExtra("FILE_NAME", filename);
                    appCompatActivity.setResult(Activity.RESULT_OK,backIntent);
                    appCompatActivity.finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    isDelete=false;
                    break;

            }
        }
    }
}
