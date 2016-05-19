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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TopupFragment extends Fragment {

    private Button viewBtn;
    private EditText viewTopup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable key = viewTopup.getText();
                if(key.length() < 20) { //TODO: Add valied condition for key values.
                    Toast.makeText(getActivity(), "Enter valied top-up key", Toast.LENGTH_SHORT).show();
                } else {
                    MQTTHelper.publish(MQTTController.TOPIC_TOKEN_KEY, key.toString());
                }
            }
        });
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(R.string.topup);
    }
}
