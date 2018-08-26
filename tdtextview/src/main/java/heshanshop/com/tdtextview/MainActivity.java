package heshanshop.com.tdtextview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private CYTextView myText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myText = findViewById(R.id.myText);
        myText.setText("短发的啊地方爱的短发房贷爱的按，。，。，。到底都市。！3213---=发达省份的大爱的爱的啊安德森啊发啊打算打算放弃 去千万千万千万千万");

    }

    public void start(View v) {
        myText.startWriteMode();
    }
    public void start2(View v){
        myText.startReadMode();
    }
}
