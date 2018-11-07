package com.example.windows_pc.anp;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Main2Activity extends AppCompatActivity implements OnNavigationItemSelectedListener {
    public static FragmentManager fstatic;

    public Main2Activity() {
        fstatic = getSupportFragmentManager();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0336R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(C0336R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(C0336R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, C0336R.string.navigation_drawer_open, C0336R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(C0336R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TextView nav_user = (TextView) navigationView.getHeaderView(null).findViewById(C0336R.id.profile_name);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("user : ");
        stringBuilder.append(getIntent().getStringExtra("name"));
        nav_user.setText(stringBuilder.toString());
        selectFrame(C0336R.id.show_OperLayout);
    }

    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(C0336R.id.drawer_layout);
        if (drawer.isDrawerOpen((int) GravityCompat.START)) {
            drawer.closeDrawer((int) GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0336R.menu.main2, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        selectFrame(item.getItemId());
        return true;
    }

    public void selectFrame(int id) {
        Fragment fragment = null;
        if (id == C0336R.id.show_equip) {
            fragment = new showEquipActivity();
        } else if (id == C0336R.id.show_pers) {
            fragment = new showPersActivity();
        } else if (id == C0336R.id.add_equipLayout) {
            fragment = new AddEquipActivity();
        } else if (id == C0336R.id.add_persLayout) {
            fragment = new AddPersActivity();
        } else if (id == C0336R.id.add_OperLayout) {
            fragment = new AddOperActivity();
        } else if (id == C0336R.id.show_OperLayout) {
            fragment = new showOperActivity();
        } else if (id == C0336R.id.quitter) {
            finish();
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(C0336R.id.fragment, fragment).commit();
        }
        ((DrawerLayout) findViewById(C0336R.id.drawer_layout)).closeDrawer((int) GravityCompat.START);
    }

    public static void Home() {
        fstatic.beginTransaction().replace(C0336R.id.fragment, new showOperActivity()).commit();
    }
}
