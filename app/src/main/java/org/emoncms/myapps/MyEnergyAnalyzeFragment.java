package org.emoncms.myapps;

import com.github.mikephil.charting.charts.PieChart;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.HashMap;

public class MyEnergyAnalyzeFragment extends android.app.Fragment {

    private static final String TAG = MyEnergyAnalyzeFragment.class.getName();

    private Context mContext;
    private PieChart pieChart;
    private Switch mSwitch;
    private EnergyStatUpdateController statController;
    HashMap<String, DeviceStat> deviceStat = new HashMap<String, DeviceStat>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        pieChart = new PieChart(mContext);
        statController = new EnergyStatUpdateController(mContext);
        statController.startEnergyUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private CompoundButton.OnCheckedChangeListener btnListener =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            statController.setInvestigationMode(isChecked);
                        }
                    }).start();
                }
            };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwitch = (Switch) getView().findViewById(R.id.switch1);
        mSwitch.setOnCheckedChangeListener(btnListener);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.analyzer_title);
    }

    public class DeviceStat {
        int mId;
        String mName;
        int mLoadPercent;
        String mState;
        int mRating;

        public DeviceStat(String id, String load) {
            mId = Integer.parseInt(id);
            mLoadPercent = Integer.parseInt(load);
        }

        public DeviceStat(String id, String state, String rating) {
            mId = Integer.parseInt(id);
            mState = state;
            mRating = Integer.parseInt(rating);
        }

        public void setState(String state) {
            mState = state;
        }

        public void setName(String name) {
            mName = name;
        }

        public int getId() {
            return mId;
        }

        public String getName() {
            return mName;
        }

        public int getLoad() {
            return mLoadPercent;
        }

        public void setLoad(int load) {
            mLoadPercent = load;
        }

        public String getState() {
            return mState;
        }

        public String getColor() {
            return null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ea_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}