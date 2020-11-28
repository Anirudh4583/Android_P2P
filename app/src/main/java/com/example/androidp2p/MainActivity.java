package com.example.androidp2p;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.InetAddresses;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.SecureDirectoryStream;
import java.util.ArrayList;
import java.util.List;

/*
    activity that uses WiFi direct API to discover and connect to available
    devices.
    it uses interfaces to notify about the operations(success/failure)

    also a broadcast receiver is needed
    to register runtime events related to wifi state
 */

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1001;

    // Button and other field objects

    Button OnOff;                       // WiFi On/Off button
    Button searchPeer, sendMsg;         // search button, send message button
    TextView conStat, readMsg;          // connection status field, message o/p field
    ListView showPeer;                  // available devices list
    EditText typeMsg;                   // message i/p field
    ImageView goToSettings;             // to go to settings window
    TextView goToSettingsText;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;           // used to show device name in showPeer
    WifiP2pDevice[] deviceArray;        // this array is used to connect a device

    static final int READ_MESSAGE = 1;

    serverClass serverClass;
    clientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        goToSettings();
        exqListener();
    }

    Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch(message.what) {
                case READ_MESSAGE:
                    byte[] readBuff = (byte[]) message.obj;
                    String tempMessage = new String(readBuff, 0, message.arg1);
                    readMsg.setText(tempMessage);
                    break;
            }
            return true;
        }
    });

    public void goToSettings(){
        goToSettings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //Open Wifi settings
                startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
            }
        });
    }

    private void exqListener() {

//         !! the button won't work !!
    /* setWiFiEnabled(boolean) is deprecated in API level 29
        TODO: update the deprecated function
                with some other alternative

                community says: open settings
                                to let the user manually connect wifi direct
                use: public void goToSettings(){
    	                    goToSettings.setOnClickListener(new OnClickListener() {

			                @Override
			                public void onClick(View arg0) {

				                //Open Wifi settings
		                        startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
			                 }
		                });
    */
//
//        OnOff.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (wifiManager.isWifiEnabled()) {
//                    wifiManager.setWifiEnabled(false);
//                    OnOff.setText(R.string.wifi_on);
//                } else {
//                    wifiManager.setWifiEnabled(true);
//                    OnOff.setText(R.string.wifi_off);
//                }
//            }
//        });

        searchPeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Discovery of available peers to connect
                // this only detects the devices in range, provide NO other info
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MainActivity.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
                    // After this point, wait for callback in
                    // onRequestPermissionsResult(int, String[], int[]) overridden method
                }

                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // search started
                        conStat.setText(R.string.search_pass);
                    }

                    @Override
                    public void onFailure(int i) {
                        // search start failed
                        conStat.setText(R.string.search_fail);
                    }
                });
            }
        });

        showPeer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // connecting to selected peer
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = typeMsg.getText().toString();
                sendReceive.write(message.getBytes());
            }
        });
    }

    private void initialWork(){

        //*************************** buttons and fields initialisation ***************************

        searchPeer = (Button) findViewById(R.id.search_button);
        sendMsg = (Button) findViewById(R.id.send_button);
        conStat = (TextView) findViewById(R.id.connection_status);
        readMsg = (TextView) findViewById(R.id.message_output);
        showPeer = (ListView) findViewById(R.id.devices_ListView);
        typeMsg = (EditText) findViewById(R.id.message_input);
        goToSettings = findViewById(R.id.go_to_settings);

        // object initialisation with hardware check
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null); //this we'll later use to connect the app to the p2p framework
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        // Intent Filter
        mIntFilter = new IntentFilter();

        // required intent values
        // to be matched later
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    // discovering peer

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            // check if peers changed
            // peer list != current peers
            if(!peerList.getDeviceList().equals(peers)) {
                // clear peer list
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

                // array adapter for device list field
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                showPeer.setAdapter(adapter);
            }

            // if peer list is empty
            if(peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "No Devices Found :(", Toast.LENGTH_SHORT).show();
                //return;
            }
        }

    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            // when the device is host
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                conStat.setText(R.string.host);
                serverClass = new serverClass();
                serverClass.start();
            }
            // when device is client
            else if(wifiP2pInfo.groupFormed) {
                conStat.setText(R.string.client);
                clientClass = new clientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    // Registering the broadcast receiver with intent values
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntFilter); // Registering
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver); // Unregistering
    }

    // server thread class
    public class serverClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            super.run();
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // method used for sending and receiving messages
    private class SendReceive extends Thread {
        private  Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        // constructor
        public SendReceive(Socket s) {
            socket = s;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if(bytes > 0) {
                        handler.obtainMessage(READ_MESSAGE, bytes, -1).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // client thread class
    public class clientClass extends Thread {
        Socket socket;
        String hostAdd;

        public clientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            super.run();
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 450);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}