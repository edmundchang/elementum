package com.example.elementum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.example.elementum.MainApplication.ConnectionOptions;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

		Messenger messenger = new Messenger(MainActivity.mHandler);
		Message m = Message.obtain(MainActivity.mHandler,
				ConnectionOptions.CONNECTED.getValue(), 0, 0);
		m.replyTo = messenger;

		if (activeNetInfo != null && activeNetInfo.isConnected()) {
			// tell activity we're connected
			try {
				m.what = ConnectionOptions.CONNECTED.getValue();
				messenger.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			// tell activity we're not connected
			try {
				m.what = ConnectionOptions.DISCONNECTED.getValue();
				messenger.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}
