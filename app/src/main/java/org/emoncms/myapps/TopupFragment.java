package org.emoncms.myapps;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TopupFragment extends Fragment {

    private static final String TAG = TopupFragment.class.getName();
    private MQTTController statController;
    private Button viewBtn;
    private EditText viewTopup;
    private TextView label;
    private TextView msgView;
    private Context mContext;
    private Activity uiThread;
    private static String credit;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        uiThread = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.topup_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewBtn = (Button) getView().findViewById(R.id.regButton);
        viewTopup = (EditText) getView().findViewById(R.id.topup);
        label = (TextView) getView().findViewById(R.id.textView);
        msgView = (TextView) getView().findViewById(R.id.msgView);
        statController = new MQTTController(mContext);

        viewBtn.setVisibility(View.VISIBLE);
        viewTopup.setVisibility(View.VISIBLE);
        label.setVisibility(View.VISIBLE);
        msgView.setVisibility(View.GONE);

        viewBtn.setEnabled(true);
        viewTopup.setEnabled(true);

        statController.registerTopupCreditListener(new MQTTController.CallBack() {
            @Override
            public void updateMessage(String[] msg) {
                Log.d(TAG, "Got message :" + msg[0]);
                credit = msg[0];
                uiThread.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewTopup.setVisibility(View.GONE);
                        viewBtn.setVisibility(View.GONE);
                        label.setVisibility(View.GONE);
                        msgView.setText("Credit succeeded with value: " + credit);
                        msgView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable key = viewTopup.getText();
                if(key.length() != 20) {
                    Toast.makeText(getActivity(), "Enter valied top-up key", Toast.LENGTH_SHORT).show();
                } else {
                    MQTTHelper.publish(MQTTController.TOPIC_TOKEN_KEY, key.toString());
                    viewTopup.setEnabled(false);
                    viewBtn.setEnabled(false);
                }
            }
        });

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.topup);
    }
}
