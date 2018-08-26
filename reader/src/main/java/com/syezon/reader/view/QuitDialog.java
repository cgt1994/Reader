package com.syezon.reader.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.syezon.reader.R;

/**
 * Created by jin on 2016/12/16.
 */
public class QuitDialog extends Dialog {
    private Button dialog_quit;
    private Button dialog_encourge;
    private Context context;
    private ImageView image_close;

    public QuitDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quit_dialog);
        dialog_quit = (Button) findViewById(R.id.dialog_quit);
        dialog_encourge = (Button) findViewById(R.id.dialog_encourge);
        image_close = (ImageView) findViewById(R.id.image_close);
        image_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        dialog_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.cancel();
            }
        });
        dialog_encourge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.sure();
            }
        });
        //按空白处不能取消
        setCanceledOnTouchOutside(false);

//        //初始化界面控件
//        initView();
//        //初始化界面数据
//        initData();
//        //初始化界面控件的事件
//        initEvent();

    }

    private DialogClickListener listener;

    public void setDialogClickListener(DialogClickListener listener) {
        this.listener = listener;
    }

    public interface DialogClickListener {
        void sure();

        void cancel();
    }


}
