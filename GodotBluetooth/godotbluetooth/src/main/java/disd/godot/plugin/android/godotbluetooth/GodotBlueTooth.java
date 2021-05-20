package disd.godot.plugin.android.godotbluetooth;

/**
* Created by Rodrigo Favarete, Mad Forest Games' Lead Game Developer, on September 8, 2017
* Forked by Webbyninja, on May 18th 2021
*/

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.content.DialogInterface;
import android.util.Log;
import android.content.Intent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.UUID;
import java.util.Set;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ConcurrentModificationException;


import org.godotengine.godot.Godot;
import org.godotengine.godot.Dictionary;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import android.bluetooth.BluetoothServerSocket;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;


public class GodotBluetooth extends GodotPlugin {

    protected Activity activity;
	
	private static final String TAG = "godot";
	
	private static final int STATUS_UPDATE = 0;
	private static final int STATUS_ERROR = 1;

    private boolean initialized = false;
    boolean connected = false;
    boolean bluetoothRequired = true;
	
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MESSAGE_READ = 2;
	
    AcceptThread aThread;
    ConnectedThread cThreadClient;
    ConnectedThread cThreadServer;
	
    private Handler localHandler;

    StringBuilder receivedData = new StringBuilder();
	
    private static String macAdress;
    String remoteBluetoothName;
    String remoteBluetoothAddress;
    String[] externalDevicesDialogAux;

	private HashMap<String, ConnectedThread> currentConnections;
	private HashMap<String, BluetoothSocket> socketConnections;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice remoteBluetoothDevice;
    BluetoothSocket socket;
    BluetoothSocket socketServer;
    private boolean isServer = true;
	private String serverName = "Server";
    UUID bluetoothUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";
	
	private BroadcastReceiver discoveryReceiver = null;

    public GodotBluetooth(Godot godot)
	{
		super(godot);
		activity = getActivity();
		discoveryReceiver = null;
	}
	
	@NonNull
	@Override
	public String getPluginName() {
		return "GodotBluetooth";
    }

    @NonNull
    @Override
	public Set<SignalInfo> getPluginSignals()
	{
		Set<SignalInfo> signals = new ArraySet<>();
		
		signals.add(new SignalInfo("status_logged", Integer.class, String.class));
		signals.add(new SignalInfo("discovered_device", String.class, String.class));
		signals.add(new SignalInfo("connection_closed"));
		signals.add(new SignalInfo("connection_failed", String.class));
		signals.add(new SignalInfo("connection_received", String.class, String.class));
		signals.add(new SignalInfo("device_connected", String.class, String.class));
		signals.add(new SignalInfo("device_disconnected", String.class, String.class));
		signals.add(new SignalInfo("data_received", String.class, Object.class));
		
		return signals;
    }
	
	public static Application getApplicationUsingReflection() throws Exception {
		return (Application) Class.forName("android.app.AppGlobals")
				.getMethod("getInitialApplication").invoke(null, (Object[]) null);
	} 

    @UsedByGodot
    public void init(final boolean newBluetoothRequired)
	{
        if (!initialized)
		{
            //myUuid = setUuid(activity.getBaseContext());
            activity.runOnUiThread(new Runnable()
			{
                @Override
                public void run()
				{
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if(bluetoothAdapter == null)
					{
                        Log.e(TAG, "ERROR: bluetooth adapter not found.");
						emitSignal("status_logged", STATUS_ERROR, "no bluetooth adapter found on device");
                        activity.finish();
                    }
                    else if (!bluetoothAdapter.isEnabled())
					{
						Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						activity.startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
						emitSignal("status_logged", STATUS_UPDATE, "requesting bluetooth activation");
						Log.e(TAG, "STATUS: requesting bluetooth activation.");
                    }
                    currentConnections = new HashMap<String, ConnectedThread>();
                    socketConnections = new HashMap<String, BluetoothSocket>();
                    bluetoothRequired = newBluetoothRequired;
                    initialized = true;
					emitSignal("status_logged", STATUS_UPDATE, "bluetooth initialized");
                    /*localHandler = new Handler(Looper.getMainLooper())
					{
                        @Override
                        public void handleMessage(Message msg)
						{
							if(msg.what == MESSAGE_READ)
							{
								String newData = (String) msg.obj;
								emitSignal("message_received", newData);
								//receivedData.append(newData);
								
								int endElement = receivedData.indexOf("}");
								if(endElement > 0)
								{
									String completeData = receivedData.substring(0, endElement);
									int dataSize = completeData.length();
									if(receivedData.charAt(0) == '{')
									{
                                        String finalizedData = receivedData.substring(1, dataSize);
                                        emitSignal("message_received", finalizedData);
									}
                                    receivedData.delete(0, receivedData.length());
								}
							}
                        }
                    };*/
                }
            });
        }
    }
	
	@UsedByGodot
	public void start_discovery()
	{
		if (initialized)
		{
			try
			{
				IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
				discoveryReceiver = new BroadcastReceiver()
				{
					public void onReceive(Context context, Intent intent)
					{
						String action = intent.getAction();
						if (BluetoothDevice.ACTION_FOUND.equals(action))
						{
						   BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						   emitSignal("discovered_device", device.getName(), device.getAddress());
						}
					}
				};
				getApplicationUsingReflection().getApplicationContext().registerReceiver(discoveryReceiver, filter);
			}
			catch(Exception e)
			{
				emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
			}
		}
		else
		{
			emitSignal("status_logged", STATUS_ERROR, "bluetooth not initialized");
		}
	}
	
	@UsedByGodot
	public void stop_discovery()
	{
		try
		{
			getApplicationUsingReflection().getApplicationContext().unregisterReceiver(discoveryReceiver);
		}
		catch (Exception e)
		{
			emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
		}
	}
	
    @UsedByGodot
    public void start_server(final String sName)
    {
        isServer = true;
        if (aThread == null)
        {
			serverName = sName;
            aThread = new AcceptThread();
            aThread.start();
			emitSignal("status_logged", STATUS_UPDATE, "server thread started");
        }
		else
		{
			emitSignal("status_logged", STATUS_ERROR, "server thread already running");
		}
    }


    @UsedByGodot
    public void native_select_paired_device(final boolean overrideConnection)
	{
        if (initialized)
		{
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(connected)
					{
						if (overrideConnection)
						{
							emitSignal("status_updated", STATUS_UPDATE, "overriding open connection");
							close_connection();
							nativePairedDevicesDialogBox();
						}
						else
						{
							emitSignal("status_logged", STATUS_ERROR, "open connection prevented native paired device selection");
							activity.finish();
						}
                    }
                }
            });
        }
        else {
			emitSignal("status_logged", STATUS_ERROR, "bluetooth not initialized");
        }
    }

    private void nativePairedDevicesDialogBox()
	{
        String localDeviceName = bluetoothAdapter.getName();
        String localDeviceAddress = bluetoothAdapter.getAddress();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
		{
            List<String> externalDeviceInfo = new ArrayList<String>();
            for (BluetoothDevice device : pairedDevices)
			{
                String externalDeviceName = device.getName();
                String externalDeviceAddress = device.getAddress();
                externalDeviceInfo.add(externalDeviceName + "\n" + externalDeviceAddress);
            }
            externalDevicesDialogAux = new String[externalDeviceInfo.size()];
            externalDevicesDialogAux = externalDeviceInfo.toArray(new String[externalDeviceInfo.size()]);;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Choose a Paired Device To Connect");
            builder.setItems(externalDevicesDialogAux, new DialogInterface.OnClickListener()
			{
                @Override
                public void onClick(DialogInterface dialog, int which)
				{
					String deviceData = externalDevicesDialogAux[which];
					String[] deviceDataSplit = deviceData.split("\n");
                    connect_device(deviceDataSplit[0], deviceDataSplit[1]);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
		emitSignal("status_updated", "native dialog populated");
    }
	
	@UsedByGodot
	public Dictionary get_paired_devices()
	{
		Dictionary returnedDevices = new Dictionary();
		if (initialized)
		{
			Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0)
			{
				int currentDevice = 0;
				for (BluetoothDevice device : pairedDevices)
				{
					Dictionary deviceDict = new Dictionary();
					deviceDict.put("device_name", device.getName());
					deviceDict.put("device_address", device.getAddress());
					returnedDevices.put("device_"+String.valueOf(currentDevice), deviceDict);
					currentDevice += 1;
				}
			}
		}
		return returnedDevices;
	}

    @UsedByGodot
    public void connect_device(final String deviceName, final String deviceAddress)
	{
		emitSignal("status_logged", STATUS_UPDATE, "attempting connection to device with address "+deviceAddress);
        if (initialized)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
					public void run()
					{
						if(!connected)
						{
							createSocket(deviceAddress);
							connected = true;
						}
						else
						{
							emitSignal("status_logged", STATUS_ERROR, "connection failed due to an already open connection");
							emitSignal("connection_failed", "connection failed due to an already open connection");
							activity.finish();
						}
					}
			});
        }
        else {
            emitSignal("status_logged", STATUS_ERROR, "bluetooth not initialized");
        }
    }

    @UsedByGodot
    public void close_connection()
    {
        emitSignal("connection_closed");
        if (cThreadClient != null)
		{
            try
			{
                if (cThreadClient.mmInStream != null)
				{
                    cThreadClient.mmInStream.close();
                }
                if (cThreadClient.mmOutStream != null)
				{
                    cThreadClient.mmOutStream.close();
                }
                if (cThreadClient.tempSocket != null)
				{
                    cThreadClient.tempSocket.close();
                }
                cThreadClient.interrupt();
            }
            catch (IOException e) {
                emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
            }
        }
        if (isServer)
        {
            try
			{
                isServer = false;
                connected = false;
                if(aThread != null)
				{
                    aThread.interrupt();
                }
                if(cThreadServer != null)
				{
                    if (cThreadServer.mmInStream != null)
					{
                        cThreadServer.mmInStream.close();
                    }
                    if (cThreadServer.mmOutStream != null)
					{
                        cThreadServer.mmOutStream.close();
                    }
                    if (cThreadServer.tempSocket != null)
					{
                        cThreadServer.tempSocket.close();
                    }
                    cThreadServer.interrupt();
                }
            }
            catch (IOException e)
			{
                emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
            }
            try
			{
                for(String item : currentConnections.keySet())
				{
					if(currentConnections.get(item) != null)
					{
						if (currentConnections.get(item).mmInStream != null)
						{
							currentConnections.get(item).mmInStream.close();
						}
						if (currentConnections.get(item).mmOutStream != null)
						{
							currentConnections.get(item).mmOutStream.close();
						}
						if (currentConnections.get(item).tempSocket != null)
						{
							currentConnections.get(item).tempSocket.close();
						}
						currentConnections.get(item).interrupt();
					}
				}

            }
            catch (IOException e) {
				emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
			}
        }
        connected = false;
    }

    private void createSocket (String deviceAddress)
	{
        remoteBluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
        try
        {
            socket = remoteBluetoothDevice.createRfcommSocketToServiceRecord(bluetoothUUID);
            if(!socket.isConnected())
            {
                socket.connect();
            }
            cThreadClient = new ConnectedThread(socket);
            cThreadClient.start();
            emitSignal("device_connected", remoteBluetoothDevice.getName(), remoteBluetoothDevice.getAddress());
            isServer = false;
        }

        catch (IOException e)
		{
            connected = false;
            emitSignal("connection_failed", String.valueOf(e));
			emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
        }
    }
    @UsedByGodot
    private String get_uuid ()
	{
            return String.valueOf(bluetoothUUID);
    }
	
	@UsedByGodot
    public void send_data(final String comData)
	{
        if (initialized)
		{
            activity.runOnUiThread(new Runnable()
			{
                @Override
                    public void run()
					{
                        if(connected)
						{
							if (isServer)
							{
								try
								{
									for (Map.Entry<String, ConnectedThread> device : currentConnections.entrySet())
									{
										device.getValue().sendData(comData);
									}

								}
								catch(ConcurrentModificationException e)
								{
									emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
								}
							}
							else
							{
								cThreadClient.sendData(comData);
							}
                        }
                        else
						{
                            emitSignal("status_logged", STATUS_ERROR, "data failed to send due to lack of connection");
							activity.finish();
                        }
                    }
            });
        }
		else
		{
			emitSignal("status_logged", STATUS_ERROR, "bluetooth not initialized");
		}
    }
	
	@UsedByGodot
	public void send_raw_data(final byte[] comRawData)
	{
        if (initialized)
		{
            activity.runOnUiThread(new Runnable()
			{
                @Override
                    public void run()
					{
                        if(connected)
						{
							if (isServer)
							{
								try
								{
									for (Map.Entry<String, ConnectedThread> device : currentConnections.entrySet())
									{
										device.getValue().sendRawData(comRawData);
									}

								}
								catch(ConcurrentModificationException e)
								{
									emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
								}
							}
							else
							{
								cThreadClient.sendRawData(comRawData);
							}
                        }
                        else
						{
                            emitSignal("status_logged", STATUS_ERROR, "data failed to send due to lack of connection");
							activity.finish();
                        }
                    }
            });
        }
		else
		{
			emitSignal("status_logged", STATUS_ERROR, "bluetooth not initialized");
		}
    }
	
    @UsedByGodot
    public String get_device_name()
    {
        return bluetoothAdapter.getName();
    }
	
    @UsedByGodot
    public String get_device_address()
    {
        return bluetoothAdapter.getAddress();
    }
	
    @UsedByGodot
    public boolean is_server()
    {
        return isServer;
    }

	@UsedByGodot
	public boolean is_initalized()
	{
		return initialized;
	}

    private class AcceptThread extends Thread
    {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread()
		{
            BluetoothServerSocket tmp = null;
            try
			{
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(serverName, bluetoothUUID);
            }
			catch (IOException e)
			{
                emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
            }
            mmServerSocket = tmp;

        }

        public void run()
		{
            socketServer = null;
            while (true)
            {
                try
                {
                    socketServer = mmServerSocket.accept();
                }
				catch (IOException e)
                {
                    emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
                    break;
                }

                if (socketServer != null)
                {
                    emitSignal("connection_received", socketServer.getRemoteDevice().getName(), socketServer.getRemoteDevice().getAddress());
                    connected = true;
                    if (currentConnections.containsKey(socketServer.getRemoteDevice().getAddress()))
                    {
                        currentConnections.get(socketServer.getRemoteDevice().getAddress()).interrupt();
                        currentConnections.remove(socketServer.getRemoteDevice().getAddress());
						socketConnections.remove(socketServer.getRemoteDevice().getAddress());
                    }
                    if (!currentConnections.containsKey(socketServer.getRemoteDevice().getAddress()))
                    {

                        cThreadServer = new ConnectedThread(socketServer);
                        cThreadServer.start();
                        socketConnections.put(socketServer.getRemoteDevice().getAddress(), socketServer);
                        currentConnections.put(socketServer.getRemoteDevice().getAddress(), cThreadServer);
                    }
                }
            }
        }
        public void cancel() {
            try
			{
                mmServerSocket.close();
            }
			catch (IOException e)
			{
                emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
            }
        }
    }

    private class ConnectedThread extends Thread
	{

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket tempSocket;
        public ConnectedThread(BluetoothSocket newSocket)
		{

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            tempSocket = newSocket;
            try
            {
                tmpIn = newSocket.getInputStream();
                tmpOut = newSocket.getOutputStream();
            }
			catch (IOException e) 
			{
				emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
			}

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
		{

            byte[] buffer = new byte[2048];
            int bytes;

            while (true)
			{
                try
                {
                    bytes = mmInStream.read(buffer);
                    String externalData = new String(buffer, 0, bytes);
                    emitSignal("data_received", externalData, new Object[]{buffer});
                }
                catch (IOException f)
				{
					emitSignal("status_logged", STATUS_ERROR, String.valueOf(f));
                    emitSignal("device_disconnected", tempSocket.getRemoteDevice().getName(), tempSocket.getRemoteDevice().getAddress());
                    if (!isServer)
                    {
                        connected = false;
                    }
                    try
                    {
                        if (tempSocket != null)
                        {
                            if (tempSocket.getInputStream() != null)
                            {
                                tempSocket.getInputStream().close();
                            }
                            if (tempSocket.getOutputStream() != null)
                            {
                                tempSocket.getOutputStream().close();
                            }
                            tempSocket.close();
                        }
                    }
                    catch (Exception e)
                    {
                        emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
                    }
                    break;
                }
            }
        }

        public void sendData(String comData)
		{

            byte[] dataBuffer = comData.getBytes();
            try
			{
                mmOutStream.write(dataBuffer);
            } 
            catch (IOException e) 
			{
				emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
			}
        }

        public void sendRawData(byte[] comRawData)
		{

            try
			{
                mmOutStream.write(comRawData);
            } 
            catch (IOException e)
			{
				emitSignal("status_logged", STATUS_ERROR, String.valueOf(e));
			}
        }
    }

    @Override
    public void onMainActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_ENABLE_BT:

                if(resultCode == Activity.RESULT_OK) {
                    //Log.e(TAG,  "Bluetooth Activated!");
                }
                else {
                    if(bluetoothRequired){
                        //Log.e(TAG, "Bluetooth wasn't activated, application closed!");
                        activity.finish();
                    }
                    else{
                        //Log.e(TAG, "Bluetooth wasn't activated!");
                    }
                }

                break;

            default:
                //Log.e(TAG, "ERROR: Unknown situation!");
        }
    }
}
