package com.example.androidp2p;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1001;

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

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

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
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    OnOff.setText("WiFi ON");
                } else {
                    wifiManager.setWifiEnabled(true);
                    OnOff.setText("WiFi OFF");
                }
            }
        });

        searchPeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Discovery of available peers to connect
                // this only detects the devices in range and provide NO other info
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MainActivity.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
                    // After this point you wait for callback in
                    // onRequestPermissionsResult(int, String[], int[]) overridden method
                }

                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        conStat.setText("Search Initiated");
                    }

                    @Override
                    public void onFailure(int i) {
                        conStat.setText("ERROR: Search Initiation Restricted");
                    }
                });
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

        // Intent Filter
        mIntFilter = new IntentFilter();
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                for(WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                ListView. (adapter);
            }

            if(peers.size == 0) {
                Toast.makeText(getApplicationContext(), "No Devices Found :(", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntFilter); // Registering the broadcast receiver
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver); // Unregistering the broadcast receiver
    }
}