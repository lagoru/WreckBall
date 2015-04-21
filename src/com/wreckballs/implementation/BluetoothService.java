package com.wreckballs.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for incoming
 * connections, a thread for connecting with a device, and a thread for
 * performing data transmissions when connected.
 */
public class BluetoothService {
	public enum TypeOfConnection{
		SERVER,
		CLIENT,
	}
	private TypeOfConnection mConnectionType;
	// Debugging
	private static final String TAG = "BluetoothService iBT";
	private static final boolean D = true;
	private static final int REQUEST_ENABLE_BT = 1;
	private static boolean mIsReceiverRegistered = false;

	// Name for the SDP record when creating server socket
	private static final String NAME = "i BT";

	// Unique UUID for this application
	private static UUID MY_UUID;

	// Member fields
	private final BluetoothAdapter mAdapter;
	//private final Handler mHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	private String mDeviceAddress;
	private String mDeviceName;
	private ConnectedThread mConnThread;
	private BluetoothSocket mSocket;

	private List<Pair<String,String> > mAvailableDevices;
	private boolean isServer = true;
	public void setIsServer(boolean is){
		isServer = is;
	}
	/**
	 * A bluetooth piconet can support up to 7 connections. This array holds 7
	 * unique UUIDs. When attempting to make a connection, the UUID on the
	 * client must match one that the server is listening for. When accepting
	 * incoming connections server listens for all 7 UUIDs. When trying to form
	 * an outgoing connection, the client tries each UUID one at a time.
	 */
	private ArrayList<UUID> mUuids;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
	// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
	// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
	// device

	//register for found bluetooth devices
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Add the name and address to an array adapter to show in a ListView
				mAvailableDevices.add(Pair.create(device.getName(),device.getAddress()));
			}
		}
	};
	private Activity mActivity;

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public BluetoothService(Activity activity) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mAvailableDevices = new ArrayList<Pair<String,String>>();
		mActivity = activity;
		//turn on bluetooth if needed 
		if (!mAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	public synchronized void unregister(){
		if(!mIsReceiverRegistered){
			return;
		}
		mIsReceiverRegistered = false;
		mActivity.unregisterReceiver(mReceiver);
	}

	/**Enables bluetooth, and its visibility
	 *
	 */
	public synchronized void register(){
		if(mIsReceiverRegistered){
			return;
		}
		mIsReceiverRegistered = true;
		//make discoverable for 300 seconds
		Intent discoverableIntent = new
				Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		mActivity.startActivity(discoverableIntent);

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		mActivity.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
	}

	public void startDiscovery(){
		mAdapter.startDiscovery();
	}

	public void stopDiscovery(){
		mAdapter.cancelDiscovery();
	}

	public static UUID getMY_UUID() {
		return MY_UUID;
	}

	public List<Pair<String,String>> getAvailableDevices(){
		return mAvailableDevices;
	}

	public static void setMY_UUID(UUID mY_UUID) {
		MY_UUID = mY_UUID;
	}

	public boolean isDeviceConnected() {
		if (mConnectedThread != null) {
			return true;
		}
		return false;
	}

	public String getmDeviceName() {
		return this.mDeviceName;
	}

	public void setmDeviceName(String mDeviceName) {
		this.mDeviceName = mDeviceName;
	}

	public String getmDeviceAddress() {
		return mDeviceAddress;
	}

	public void setmDeviceAddress(String mDeviceAddress) {
		this.mDeviceAddress = mDeviceAddress;
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;

		//TODO poinformowac o stanie polaczenia
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start() {
		if (D)
			Log.d(TAG, "start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to listen on a BluetoothServerSocket
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
		setState(STATE_LISTEN);
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param deviceAddress
	 *            Device hardware address
	 */
	public synchronized void connect(String deviceAddress) {
		BluetoothDevice device = mAdapter.getRemoteDevice(deviceAddress);
		mConnectionType = TypeOfConnection.CLIENT;
		//check if already connected
		if (mDeviceAddress != device.getAddress()) {
			if (D)
				Log.d(TAG, "connect to: " + device);

			// Cancel any thread attempting to make a connection
			if (mState == STATE_CONNECTING) {
				if (mConnectThread != null) {
					mConnectThread.cancel();
					mConnectThread = null;
				}
			}
			if(mConnThread != null){
				mConnThread.cancel();
				mConnThread = null;
			}

			// Create a new thread and attempt to connect to each UUID
			// one-by-one.
			try {

				// String
				// s="00001101-0000-1000-8000"+device.getAddress().split(":");
				ConnectThread mConnectThread = new ConnectThread(device,
						UUID.fromString("00001101-0000-1000-8000-"
								+ device.getAddress().replace(":", "")));
				Log.i(TAG, "uuid-string at server side"
						+ ("00001101-0000-1000-8000" + device.getAddress()
								.replace(":", "")));
				mConnectThread.start();
				setState(STATE_CONNECTING);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connected");

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		mDeviceName = null;
		mDeviceAddress = null;
		mSocket =null;

		if(mConnThread != null){
			mConnThread.cancel();
			mConnThread=null;
		}

		setState(STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(String out) {
		while(mConnectedThread == null){

		}
		mConnectedThread.write(out);
	}

	/**Read from ConnectedThread
	 * blocking read
	 * @return
	 */
	public String read(){
		if(mConnectedThread != null){
			return mConnectedThread.read();
		}
		return null;
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		setState(STATE_LISTEN);
		Log.d(TAG, "connection failed");
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost(BluetoothDevice device) {
		// setState(STATE_LISTEN);

		mDeviceAddress= null;
		mDeviceName=null;
		mConnThread=null;
		// Send a failure message back to the Activity
	}

	private class AcceptThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			// Create a new listening server socket
			try {

				if (mAdapter.isEnabled()) {
					BluetoothService.setMY_UUID(UUID
							.fromString("00001101-0000-1000-8000-"
									+ mAdapter.getAddress().replace(":", "")));
				}
				Log.i(TAG, "MY_UUID.toString()=="
						+ BluetoothService.getMY_UUID().toString());

				tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME,
						BluetoothService.getMY_UUID());
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
			mmServerSocket = tmp;
		}

		public void run() {
			if (D)
				Log.d(TAG, "BEGIN mAcceptThread" + this);
			setName("AcceptThread");
			BluetoothSocket socket = null;
			Log.i(TAG, "mState in acceptThread==" + mState);
			// Listen to the server socket if we're not connected
			while (mState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "accept() failed", e);
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					synchronized (BluetoothService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							mConnectionType = TypeOfConnection.SERVER;
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate
							// new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
			if (D)
				Log.i(TAG, "END mAcceptThread");
		}

		public void cancel() {
			if (D)
				Log.d(TAG, "cancel " + this);
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private UUID tempUuid;

		public ConnectThread(BluetoothDevice device, UUID uuidToTry) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			tempUuid = uuidToTry;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(uuidToTry);
			} catch (IOException e) {
				Log.e(TAG, "create() failed", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {

				connectionFailed();
				Log.e("Connection Failed", e.getMessage());
				//e.printStackTrace();

				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG,
							"unable to close() socket during connection failure",
							e2);
				}
				// Start the service over to restart listening mode
				//BluetoothService.this.start();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}
			mDeviceAddress= mmDevice.getAddress();
			mDeviceName=mmDevice.getName();
			// Start the connected thread
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		byte[] buffer = new byte[1024];
		int bytes;
		String income;

		public ConnectedThread(BluetoothSocket socket) {
			Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			
			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					if(mmInStream.available() ==0 || mmInStream.available() == -1 || income != null){
						Thread.sleep(50);
						continue;
					}
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					
					income = new String(buffer,0,bytes,"UTF-8");
					
				} catch (IOException e) {
					Log.e("adrian", "disconnected", e);
					connectionLost(mmSocket.getRemoteDevice());
				} catch (InterruptedException e) {
					Log.e("adrian", "disconnected", e);
					//e.printStackTrace();
				}
			}
		}

		public String read(){
			while(income == null){

			}
			String tmp = income;
			income = null;
			Log.d("adrian","read: " + tmp);
			return tmp;
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(String string) {
			try {
				mmOutStream.write(string.getBytes());
				Log.d("adrian","write :" + string);
			} catch (IOException e) {
				Log.e("adrian", "error during write");
			}
		}

		public void cancel() {
			Log.d("adrian", "cancel");
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	public boolean isRegistered() {
		return mIsReceiverRegistered;
	}

	public TypeOfConnection getTypeOfConnection(){
		return mConnectionType;
	}
}