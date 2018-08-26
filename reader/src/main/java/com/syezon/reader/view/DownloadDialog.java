package com.syezon.reader.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.syezon.reader.R;

/**
 * Created by jin on 2017/2/23.
 */
public class DownloadDialog extends Dialog {
    public DownloadDialog(Context context) {
        super(context);
    }

    public DownloadDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_dialog);
    }
}
