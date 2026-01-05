package com.qq.xqf1001.androidtraining.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.qq.xqf1001.androidtraining.R;
import com.qq.xqf1001.androidtraining.base.OnFragmentKeyDownListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private NavController navController;
    private List<OnFragmentKeyDownListener> onFragmentKeyDownListenerList = new ArrayList<>();
    private long exitTime;

    public void setOnFragmentKeyDownListener(OnFragmentKeyDownListener onFragmentKeyDownListener) {
        onFragmentKeyDownListenerList.add(onFragmentKeyDownListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_chart, R.id.navigation_video,
                R.id.navigation_me).build();
        navController = Navigation.findNavController(this,
                R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController,
                appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        NavigationUI.navigateUp(navController, appBarConfiguration);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (onFragmentKeyDownListenerList.size() != 0) {
            onFragmentKeyDownListenerList.
                    remove(onFragmentKeyDownListenerList.size() - 1);
        }
        return NavigationUI.navigateUp(navController, appBarConfiguration);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if (onFragmentKeyDownListenerList.size() != 0) {
                if (onFragmentKeyDownListenerList.get(onFragmentKeyDownListenerList.size() - 1)
                        .onKeyDown(keyCode, event)) {
                    return true;
                } else {
                    onSupportNavigateUp();
                }
            } else if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(this, "再次按返回键退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                //假退出，返回桌面
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                //finish(); //真退出
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
