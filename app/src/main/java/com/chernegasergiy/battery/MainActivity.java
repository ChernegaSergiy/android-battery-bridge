package com.chernegasergiy.battery;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.chernegasergiy.battery.fragment.StatusFragment;
import com.chernegasergiy.battery.fragment.AboutFragment;

public class MainActivity extends AppCompatActivity {
    private StatusFragment statusFragment;
    private AboutFragment aboutFragment;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        statusFragment = new StatusFragment();
        aboutFragment = new AboutFragment();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_status) {
                loadFragment(statusFragment, "Battery Status");
                return true;
            } else if (itemId == R.id.nav_about) {
                loadFragment(aboutFragment, "About");
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            loadFragment(statusFragment, "Battery Status");
        }
    }

    private void loadFragment(Fragment fragment, String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
