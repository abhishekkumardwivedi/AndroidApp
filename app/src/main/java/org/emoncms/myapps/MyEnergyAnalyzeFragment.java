package org.emoncms.myapps;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
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
    private static List<DeviceStat> deviceStat = new ArrayList<DeviceStat>();
    Activity uiThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        pieChart = new PieChart(mContext);
        statController = new MQTTController(mContext);
        statController.startEnergyUpdates();
        uiThread = getActivity();
        pieUpdater();
        deviceUpdater();
        dumpDeviceStat();
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

        drawPie();
        drawTable();
    }

    private void deviceUpdater() {
            statController.registerDeviceStateListener(new MQTTController.CallBack() {
                @Override
                public void updateMessage(String[] msg) {
                    Log.d(TAG, "dev: " + msg[0] + "|" + msg[1] + "|" + msg[2]);
                    int i;
                    for(i = 0; i < deviceStat.size(); i++) {
                        if(deviceStat.get(i).getId() == Integer.parseInt(msg[0])) {
                            DeviceStat device = deviceStat.get(i);
                            if(device.getState() == null || !device.getState().equals(msg[1])) {
                                device.setState(msg[1]);
                                deviceStat.remove(i);
                                deviceStat.add(device);
                                uiThread.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        drawTable();
                                        drawPie();
                                    }
                                });
                            }
                            break;
                        }
                    }
                    if(i == deviceStat.size()) {
                        DeviceStat device = new DeviceStat(msg[0], msg[1], msg[2]);
                        deviceStat.add(device);
                        uiThread.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawTable();
                                drawPie();
                            }
                        });
                    }
                    dumpDeviceStat();
                }
            });
        }

    private void pieUpdater() {
            statController.registerPChartDataListener(new MQTTController.CallBack() {
                @Override
                public void updateMessage(String[] msg) {
                    Log.d(TAG, "pie: " + msg[0] + "|" + msg[1]);
                    int i;
                    for(i = 0; i < deviceStat.size(); i++) {
                        if(deviceStat.get(i).getId() == Integer.parseInt(msg[0])) {
                            DeviceStat device = deviceStat.get(i);
                            if(device.getLoad() == -1 || !(device.getLoad() == Integer.parseInt(msg[1]))) {
                                device.setLoad(Integer.parseInt(msg[1]));
                                deviceStat.remove(i);
                                deviceStat.add(device);
                                uiThread.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        drawPie();
                                    }
                                });
                            }
                            break;
                        }
                    }
                    if(i == deviceStat.size()) {
                        DeviceStat device = new DeviceStat(msg[0], msg[1]);
                        deviceStat.add(device);
                        uiThread.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawPie();
                            }
                        });
                    }
                    dumpDeviceStat();
                }
            });
        }

    private void drawPie() {
        PieChart pieChart = (PieChart) getActivity().findViewById(R.id.pie);
        pieChart.removeAllViews();
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();
        Context context = getActivity();

        int rows = deviceStat.size();

        for (int i = 0; i < rows; i++) {
            DeviceStat stat = deviceStat.get(i);
            entries.add(new Entry(deviceStat.get(i).getLoad(), i));
            labels.add(deviceStat.get(i).getName());
        }

        PieDataSet dataset = new PieDataSet(entries, "");

        PieData data = new PieData(labels, dataset);
        pieChart.setData(data);
        pieChart.setDrawHoleEnabled(false);
        pieChart.getLegend().setPosition(Legend.LegendPosition.RIGHT_OF_CHART);

        pieChart.setDescription("");

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());
        dataset.setColors(colors);
        pieChart.animateY(200);
    }

    private void drawTable() {
        Context context = getActivity();
        TableLayout tableLayout = (TableLayout) getView().findViewById(R.id.tl);
        tableLayout.removeAllViews();

        tableLayout.addView(createTitleView());
        int rows = deviceStat.size();
        for (int i = 0; i < rows; i++) {
            DeviceStat stat = deviceStat.get(i);
            TableRow tbrow = new TableRow(context);
            tbrow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView c0 = new TextView(context);
            c0.setText("" + stat.getId());
            c0.setTextColor(Color.WHITE);
            c0.setGravity(Gravity.CENTER);
            c0.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));
            c0.setPadding(20,2, 20, 2);
            tbrow.addView(c0);

            final TextView c1 = new TextView(context);
            c1.setText("" + stat.getName());
            c1.setTextColor(Color.WHITE);
            c1.setGravity(Gravity.CENTER);
            c1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 6f));
            c1.setPadding(20,2, 20, 2);
            final int index = i;
            c1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    editDeviceName(index);
                    return true;
                }
            });
            tbrow.addView(c1);

            TextView c2 = new TextView(context);
            c2.setText("" + stat.getRating());
            c2.setTextColor(Color.WHITE);
            c2.setGravity(Gravity.CENTER);
            c2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f));
            c2.setPadding(20,2, 20, 2);
            tbrow.addView(c2);

            TextView c3 = new TextView(context);
            c3.setText("" + stat.getState());
            c3.setTextColor(Color.WHITE);
            c3.setGravity(Gravity.CENTER);
            c3.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f));
            c3.setPadding(20,2, 20, 2);
            tbrow.addView(c3);

            tableLayout.addView(tbrow);
        }
    }

    private void editDeviceName(final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Device Name");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeviceStat device = deviceStat.get(index);
                device.setName(input.getText().toString());
                deviceStat.set(index, device);
                uiThread.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawTable();
                        drawPie();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private View createTitleView() {
        Context context = getActivity();

            TableRow tbrow = new TableRow(context);
            tbrow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView c0 = new TextView(context);
            c0.setText("No");
            c0.setBackgroundColor(Color.BLACK);
            c0.setTextColor(Color.WHITE);
            c0.setGravity(Gravity.CENTER);
            c0.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f));
            c0.setPadding(20,20, 20, 20);
            tbrow.addView(c0);

            TextView c1 = new TextView(context);
            c1.setText("DeviceName");
            c1.setBackgroundColor(Color.BLACK);
            c1.setTextColor(Color.WHITE);
            c1.setGravity(Gravity.CENTER);
            c1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 6f));
            c1.setPadding(20,20, 20, 20);
            tbrow.addView(c1);

            TextView c2 = new TextView(context);
            c2.setText("Power");
            c2.setBackgroundColor(Color.BLACK);
            c2.setTextColor(Color.WHITE);
            c2.setGravity(Gravity.CENTER);
            c2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f));
            c2.setPadding(20,20, 20, 20);
            tbrow.addView(c2);

            TextView c3 = new TextView(context);
            c3.setText("Status");
            c3.setBackgroundColor(Color.BLACK);
            c3.setTextColor(Color.WHITE);
            c3.setGravity(Gravity.CENTER);
            c3.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f));
            c3.setPadding(20,20, 20, 20);
            tbrow.addView(c3);
        return tbrow;

    }

    private void dumpDeviceStat() {
        Log.d(TAG, "____________________");
        for (int i = 0; i < deviceStat.size(); i++) {
            DeviceStat device = deviceStat.get(i);
            Log.d(TAG, "ID:     " + device.getId());
            Log.d(TAG, "Name:   " + device.getName());
            Log.d(TAG, "Rating: " + device.getRating());
            Log.d(TAG, "State:  " + device.getState());
            Log.d(TAG, "Load:   " + device.getLoad());
            Log.d(TAG, "Color:  " + device.getColor());
            Log.d(TAG, "---------   ");
        }
        Log.d(TAG, "____________________");
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

    public class DeviceStat {
        int mId;
        String mName;
        int mLoadPercent = -1;
        String mState;
        int mRating;

        public DeviceStat(String id, String load) {
            mId = Integer.parseInt(id);
            mLoadPercent = Integer.parseInt(load);
            mName = "-";
            mState = "-";
            mRating = -1;

        }

        public DeviceStat(String id, String state, String rating) {
            mId = Integer.parseInt(id);
            mState = state;
            mRating = Integer.parseInt(rating);
            mLoadPercent = 0;
            mName = "-";
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
}