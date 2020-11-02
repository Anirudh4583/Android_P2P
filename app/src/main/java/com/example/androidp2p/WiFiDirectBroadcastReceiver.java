package com.example.androidp2p;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// WiFiDirectBroadcastReceiver is a broadcast receiver specifically
// used to register different system and application events related
// to wifi state as they happen during android runtime

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager xManager, WifiP2pManager.Channel xChannel, MainActivity xActivity) {
        this.mManager = xManager;
        this.mChannel = xChannel;
        this.mActivity = xActivity;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // CHECKING INTENTS

        // check if wifi p2p state enabled/disabled
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            // check if its enabled
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // WiFi P2P is enabled
                Toast.makeText(mActivity, "WiFi P2p is supported", Toast.LENGTH_SHORT).show();
            } else {
                // WiFi p2p is disabled
                Toast.makeText(mActivity, "WiFi P2p is not supported", Toast.LENGTH_SHORT).show();
            }
        }

        // check if the peers in the range have changed
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                mManager.requestPeers(mChannel, mActivity.peerListListener);
            }

        // check if the connection with the peer has changed
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            // check if the device is connected with peer
            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
            } else {
                mActivity.conStat.setText(R.string.discon);
            }
            /* NetworkInfo class is deprecated but works for some cases
                TODO: update the deprecated class/function
                        with some other alternative

                community says:
                use:
            */
        }
    }
}
