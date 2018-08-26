package com.syezon.reader.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.syezon.reader.R;

/**
 * 清除缓存的对话框
 * Created by jin on 2016/9/16
 */
public class CleanCacheUtils extends PopupWindow {

    private View mConvertView;
    private PopupWindow mPopWindows;

    public interface IDialogClickListener {
        void onCancelClick();

        void onSureClick();
    }

    public void showCleanDialog(View parent, Context context, String title, String msg, final IDialogClickListener listener) {
        if (mPopWindows == null) {
            mConvertView = LayoutInflater.from(context).inflate(R.layout.view_dialog, null);
            TextView tv_title = (TextView) mConvertView.findViewById(R.id.dialog_title);
            TextView tv_msg = (TextView) mConvertView.findViewById(R.id.dialog_message);
            TextView tv_cancel = (TextView) mConvertView.findViewById(R.id.dialog_cancel);
            TextView tv_sure = (TextView) mConvertView.findViewById(R.id.dialog_sure);

            tv_title.setText(title);
            tv_msg.setText(msg);
            //按钮点击回调
            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopWindows.dismiss();
                    listener.onCancelClick();
                }
            });
            tv_sure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopWindows.dismiss();
                    listener.onSureClick();
                }
            });
            mPopWindows = new PopupWindow(mConvertView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            mPopWindows.setTouchable(true);
            mPopWindows.setBackgroundDrawable(new ColorDrawable(0x50505050));
        }
        mPopWindows.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }
}
