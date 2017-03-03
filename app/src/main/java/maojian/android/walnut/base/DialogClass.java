package maojian.android.walnut.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import maojian.android.walnut.R;

/**
 * @author hezuzhi
 * @Description: ()
 * @date 2017/2/28  16:26.
 * @version: 1.0
 */
public class DialogClass {
    public static void LoginDialog(Context context) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.include_login, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context).setView(layout);
        final Dialog newDialog = dialog.create();
        layout.findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDialog.dismiss();
            }
        });

//                .setPositiveButton("确定", null).setNegativeButton("取消", null)
        newDialog.show();
    }
}
