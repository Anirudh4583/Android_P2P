package com.example.androidp2p;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
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
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntFilter;


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
                    OnOff.setText("WiFi ON");
                }
                else {
                    wifiManager.setWifiEnabled(true);
                    OnOff.setText("WiFi OFF");
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

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null); //this we'll later use to connect the app to the p2p framework

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntFilter = new IntentFilter();
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}