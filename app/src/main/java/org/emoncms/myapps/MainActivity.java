package org.emoncms.myapps;

import org.eclipse.paho.client.mqttv3.MqttClient;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

public class MainActivity extends AppCompatActivity
{
    Toolbar mToolbar;
    DrawerLayout mDrawer;

//    int TITLE_IDS[] = {R.string.me_title, R.string.ms_title, R.string.settings};
//    int ICONS[] = {R.drawable.ic_my_electric_white_36dp, R.drawable.ic_my_electric_white_36dp, R.drawable.ic_settings_applications_white_36dp};

    int TITLE_IDS[] = {R.string.me_title, R.string.analyzer_title, R.string.register,  R.string.settings};
    int ICONS[] = {R.drawable.ic_my_electric_white_36dp,
            R.drawable.ic_my_electric_white_36dp,
            R.drawable.ic_settings_applications_white_36dp,
            R.drawable.ic_settings_applications_white_36dp};

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    boolean fullScreenRequested;
    boolean isFirstRun;
    Handler mFullscreenHandler = new Handler();

    private static final String PREF_APP_FIRST_RUN = "app_first_run";

    public enum MyAppViews {
        MyElectricView,
        MyElectricSettingsView,
        MySolarView,
        MySolarSettingsView,
        MyEnergyAnalyzeView,
        registrationView,
        SettingsView
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        UpgradeManager.doUpgrade(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        isFirstRun = sp.getBoolean(PREF_APP_FIRST_RUN, true);
        sp.edit().putBoolean(PREF_APP_FIRST_RUN, false).apply();

        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.main_preferences, false);
        PreferenceManager.setDefaultValues(this, R.xml.me_preferences, false);

        setKeepScreenOn(sp.getBoolean("keep_screen_on", false));

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);

        if (mRecyclerView != null)
            mRecyclerView.setHasFixedSize(true);

        mAdapter = new NavigationDrawerAdapter(this, TITLE_IDS, ICONS);
        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(),motionEvent.getY());

                if(child!=null && mGestureDetector.onTouchEvent(motionEvent)){
                    int position = recyclerView.getChildAdapterPosition(child);
                    setSelectedNavigationItem(position);
                    mDrawer.closeDrawers();

                    switch (position) {
                        case 0:
                            showFragment(MyAppViews.MyElectricView);
                            break;
                        case 1:
                            showFragment(MyAppViews.MyEnergyAnalyzeView);
                            break;
                        case 2:
                            showFragment(MyAppViews.registrationView);
                            break;
                        case 3:
//                            showFragment(MyAppViews.MySolarView);
//                            break;
//                        case 2:
                            showFragment(MyAppViews.SettingsView);
                            break;
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer,mToolbar, R.string.openDrawer, R.string.closeDrawer);
        //mDrawer.setDrawerListener(mDrawerToggle);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        showFragment(MyAppViews.MyElectricView);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(mOnSystemUiVisibilityChangeListener);

        if (isFirstRun)
            mDrawer.openDrawer(GravityCompat.START);
    }

    public boolean setFullScreen() {

        if (fullScreenRequested)
            mFullscreenHandler.removeCallbacksAndMessages(null);
        else
            mFullscreenHandler.post(mSetFullScreenRunner);

        fullScreenRequested = !fullScreenRequested;

        return fullScreenRequested;
    }

    private Runnable mSetFullScreenRunner = new Runnable()
    {
        @Override
        public void run()
        {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE);
            }
            else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
            else
            {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LOW_PROFILE |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };

    public void setKeepScreenOn(boolean keep_screen_on) {
        if (keep_screen_on)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private View.OnSystemUiVisibilityChangeListener mOnSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            ActionBar ab = getSupportActionBar();
            if (ab == null)
                return;

            if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.VISIBLE)
            {
                mToolbar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down));
                ab.show();
                if (fullScreenRequested)
                    mFullscreenHandler.postDelayed(mSetFullScreenRunner, 5000);
            }
            else
            {
                mToolbar.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up));
                ab.hide();
            }
        }
    };

    private void setSelectedNavigationItem(int position) {
        View selected_child = mRecyclerView.getChildAt(((NavigationDrawerAdapter) mAdapter).getSelectedItem());
        if (selected_child != null) selected_child.setSelected(false);
        ((NavigationDrawerAdapter) mAdapter).setSelectedItem(position);
        selected_child = mRecyclerView.getChildAt(position);
        selected_child.setSelected(true);
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().findFragmentByTag(getResources().getString(R.string.tag_me_fragment)) == null)
        {
            showFragment(MyAppViews.MyElectricView);

            setSelectedNavigationItem(0);
        }
        else
            super.onBackPressed();
    }

    public void showFragment(MyAppViews appView) {
        Fragment frag;
        String tag;
        switch (appView) {
            case MyElectricSettingsView:
                frag = new MyElectricSettingsFragment();
                tag = getResources().getString(R.string.tag_me_settings_fragment);
                break;
            case SettingsView:
                frag = new SettingsFragment();
                tag = getResources().getString(R.string.tag_settings_fragment);
                break;
            case MySolarView:
                frag = new MySolarMainFragement();
                tag = getResources().getString(R.string.tag_ms_fragment);
                break;
            case MySolarSettingsView:
                frag = new MySolarSettingsFragment();
                tag = getResources().getString(R.string.tag_ms_settings_fragment);
                break;
            case MyEnergyAnalyzeView:
                frag = new MyEnergyAnalyzeFragment();
                tag = "myenergy_analyze_fragment";
                break;
            case registrationView:
                frag = new RegistrationFragment();
                tag = "registration_fragment";
            default:
                frag = new MyElectricMainFragment();
                tag = getResources().getString(R.string.tag_me_fragment);
                break;
        }

        if (fullScreenRequested)
        {
            mFullscreenHandler.removeCallbacksAndMessages(null);
            fullScreenRequested = false;
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.container, frag, tag)
                .commit();
    }
}