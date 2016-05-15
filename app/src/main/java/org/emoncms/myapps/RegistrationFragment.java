package org.emoncms.myapps;

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
import android.widget.Switch;
import android.widget.Toast;

public class RegistrationFragment extends Fragment {

    private Button viewBtn;
    private EditText viewKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reg_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewBtn = (Button) getView().findViewById(R.id.regButton);
        viewKey = (EditText) getView().findViewById(R.id.keyText);
        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable key = viewKey.getText();
                if(key.length() > 5) { //TODO: Add valied condition for key values.
                    Toast.makeText(getActivity(), "Enter valied key", Toast.LENGTH_SHORT).show();
                } else {
                    WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wInfo = wifiManager.getConnectionInfo();
                    String macAddress = wInfo.getMacAddress(); //TODO: To verify if this gives correct mac
                    String msgKey = key + macAddress;
                    MQTTHelper.publish(MQTTController.TOPIC_DEVICE_REGISTER, msgKey);
                }
            }
        });
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.register);
    }
}
