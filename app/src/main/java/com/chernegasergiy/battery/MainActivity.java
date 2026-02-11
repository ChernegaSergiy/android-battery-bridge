package com.chernegasergiy.battery;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Gravity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("PHP Battery Bridge Installed.\n\nStatus: Listening for broadcasts.\nYou can close this app.");
        tv.setGravity(Gravity.CENTER);

        setContentView(tv);
    }
}
