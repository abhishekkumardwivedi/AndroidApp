package org.emoncms.myapps;

import com.github.mikephil.charting.charts.PieChart;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyEnergyAnalyzeFragment extends android.app.Fragment {

    private static final String TAG = MyEnergyAnalyzeFragment.class.getName();

    private Context mContext;
    private PieChart pieChart;
    private Switch mSwitch;
    private MQTTController statController;
  //  HashMap<String, DeviceStat> deviceStat = new HashMap<String, DeviceStat>();
    List<DeviceStat> deviceStat = new ArrayList<DeviceStat>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        pieChart = new PieChart(mContext);
        statController = new MQTTController(mContext);
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

        drawTable();
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

        public int getRating() {
            return mRating;
        }

        public String getColor() {
            return null;
        }
    }

    private  void drawTable() {
        Context context = getActivity();
        TableLayout stk = (TableLayout) getView().findViewById(R.id.tl);
        int rows = deviceStat.size();
        for (int i = 0; i < rows; i++) {
            DeviceStat stat = deviceStat.get(i);
            TableRow tbrow = new TableRow(context);
            TextView c0 = new TextView(context);
            c0.setText("" + stat.getId());
            c0.setTextColor(Color.WHITE);
            c0.setGravity(Gravity.CENTER);
            tbrow.addView(c0);
            TextView c1 = new TextView(context);
            c1.setText("" + stat.getName());
            c1.setTextColor(Color.WHITE);
            c1.setGravity(Gravity.CENTER);
            tbrow.addView(c1);
            TextView c2 = new TextView(context);
            c2.setText("" + stat.getRating());
            c2.setTextColor(Color.WHITE);
            c2.setGravity(Gravity.CENTER);
            tbrow.addView(c2);
            TextView c3 = new TextView(context);
            c3.setText("" + stat.getState());
            c2.setTextColor(Color.WHITE);
            c3.setGravity(Gravity.CENTER);
            tbrow.addView(c3);
            stk.addView(tbrow);
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