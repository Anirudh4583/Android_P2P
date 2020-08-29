package com.example.androidp2p;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button OnOff;
    Button searchPeer, sendMsg;
    TextView conStat, readMsg;
    ListView showPeer;
    EditText typeMsg;

    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        exqListener();
    }

    private void exqListener() {
        OnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                }
                else {
                    wifiManager.setWifiEnabled(true);
                }
            }
        });
    }

    private void initialWork(){
        searchPeer = (Button) findViewById(R.id.search_button);
        sendMsg = (Button) findViewById(R.id.send_button);
        conStat = (TextView) findViewById(R.id.connection_status);
        readMsg = (TextView) findViewById(R.id.message_output);
        OnOff = (Button) findViewById(R.id.on_off_switch);
        showPeer = (ListView) findViewById(R.id.devices_ListView);
        typeMsg = (EditText) findViewById(R.id.message_input);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }
}