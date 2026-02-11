package com.chernegasergiy.battery;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(48, 48, 48, 48);

        TextView title = new TextView(this);
        title.setText("PHP Battery Bridge");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);

        TextView version = new TextView(this);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText("Version " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            version.setText("Version unknown");
        }
        version.setGravity(Gravity.CENTER);

        TextView author = new TextView(this);
        author.setText("\nAuthor: Chernega Sergiy\n");
        author.setGravity(Gravity.CENTER);

        TextView status = new TextView(this);
        status.setText("\nStatus: Listening for broadcasts.\nYou can close this app.");
        status.setGravity(Gravity.CENTER);

        layout.addView(title);
        layout.addView(version);
        layout.addView(author);
        layout.addView(status);

        setContentView(layout);
    }
}
