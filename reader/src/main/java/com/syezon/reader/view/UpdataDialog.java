package com.syezon.reader.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.syezon.reader.R;

/**
 * Created by jin on 2017/2/10.
 */
public class UpdataDialog extends Dialog implements View.OnClickListener {
    private Button btn_update;

    private TextView tv_pro;
    private ImageView image_close;
    private RelativeLayout rl_button;

    public UpdataDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_update:
                upDialogListener.update();
//                btn_cancel.setEnabled(false);
                btn_update.setEnabled(false);
                break;



            case R.id.image_close:
                upDialogListener.cancel();
                break;
        }
    }

    private UpDialogListener upDialogListener;

    public void setUpDialogListener(UpDialogListener upDialogListener) {
        this.upDialogListener = upDialogListener;
    }

    public interface UpDialogListener {
        void update();

        void cancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_dialog);
        btn_update = (Button) findViewById(R.id.btn_update);

        image_close = (ImageView) findViewById(R.id.image_close);
        rl_button = (RelativeLayout) findViewById(R.id.rl_button);
        tv_pro = (TextView) findViewById(R.id.tv_pro);

        btn_update.setOnClickListener(this);
        image_close.setOnClickListener(this);
        //按空白处不能取消
        setCanceledOnTouchOutside(false);

    }

    public void setProgress(String progress) {
        tv_pro.setText(progress + "%");
    }

}
