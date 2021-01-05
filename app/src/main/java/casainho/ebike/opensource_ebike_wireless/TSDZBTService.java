package casainho.ebike.opensource_ebike_wireless;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import casainho.ebike.opensource_ebike_wireless.data.Global;
import casainho.ebike.opensource_ebike_wireless.data.TSDZ_Configurations;
import casainho.ebike.opensource_ebike_wireless.data.TSDZ_Periodic;

import static casainho.ebike.opensource_ebike_wireless.TSDZConst.PERIODIC_ADV_SIZE;


public class TSDZBTService extends Service {

    private static final String TAG = "TSDZBTService";

    public static String TSDZ_SERVICE = "dac21400-cfdd-462f-bfaf-7f6e4ccbb45f";
    public static String TSDZ_CHARACTERISTICS_PERIODIC = "dac21401-cfdd-462f-bfaf-7f6e4ccbb45f";
    public static String TSDZ_CHARACTERISTICS_CONFIG = "dac21402-cfdd-462f-bfaf-7f6e4ccbb45f";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "dac21402-cfdd-462f-bfaf-7f6e4ccbb45f";

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_TSDZ_SERVICE = UUID.fromString(TSDZ_SERVICE);
    public final static UUID UUID_PERIODIC_CHARACTERISTIC = UUID.fromString(TSDZ_CHARACTERISTICS_PERIODIC);
    public final static UUID UUID_CONFIG_CHARACTERISTIC = UUID.fromString(TSDZ_CHARACTERISTICS_CONFIG);

    public static final String ADDRESS_EXTRA = "ADDRESS";
    public static final String VALUE_EXTRA = "VALUE";

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    public static final String SERVICE_STARTED_BROADCAST = "SERVICE_STARTED";
    public static final String SERVICE_STOPPED_BROADCAST = "SERVICE_STOPPED";
    public static final String CONNECTION_SUCCESS_BROADCAST = "CONNECTION_SUCCESS";
    public static final String CONNECTION_FAILURE_BROADCAST = "CONNECTION_FAILURE";
    public static final String CONNECTION_LOST_BROADCAST = "CONNECTION_LOST";
    public static final String TSDZ_PERIODIC_WRITE_BROADCAST = "TSDZ_PERIODIC_WRITE";
    public static final String TSDZ_CFG_READ_BROADCAST = "TSDZ_CFG_READ";
    public static final String TSDZ_CFG_WRITE_BROADCAST = "TSDZ_CFG_WRITE";
    public static final String TSDZ_MOTOR_READY = "TSDZ_MOTOR_READY";
    public static final String TSDZ_MOTOR_INITIALIZING = "TSDZ_MOTOR_INITIALIZING";
    public static final String TSDZ_MOTOR_OFF = "TSDZ_MOTOR_OFF";

    private static final int MAX_CONNECTION_RETRY = 10;
    private static TSDZBTService mService = null;

    private BluetoothAdapter mBluetoothAdapter;
    private String address;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private ConnectionState mConnectionState = ConnectionState.DISCONNECTED;

    private boolean stopped = false;
    private int connectionRetry = 0;

    private BluetoothGattCharacteristic tsdz_periodic_char = null;
    private BluetoothGattCharacteristic tsdz_config_char = null;

    private int m_motorStatePrevious = -1;

    public static TSDZBTService getBluetoothService() {
        return mService;
    }

    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    public TSDZBTService() {
        Log.d(TAG, "TSDZBTService()");
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        throw new UnsupportedOperationException();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if(intent != null)
        {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_START_FOREGROUND_SERVICE:
                        address = intent.getStringExtra(ADDRESS_EXTRA);
                        if ((address != null) && connect(address))
                            startForegroundService();
                        else {
                            disconnect();
                            stopped = true;
                            Intent bi = new Intent(CONNECTION_FAILURE_BROADCAST);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(bi);
                            stopSelf();
                        }
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        stopped = true;
                        disconnect();
                        stopForegroundService();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /* Used to build and start foreground service. */
    private void startForegroundService()
    {
        Log.d(TAG, "startForegroundService");

        // Create notification default intent.
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String channelId = getString(R.string.app_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(channelId);
            NotificationManager service = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            service.createNotificationChannel(notificationChannel);
        }

        // Create notification builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setContentTitle(getText(R.string.notification_title));
        //builder.setContentText(getText(R.string.notification_message));
        builder.setTicker(getText(R.string.notification_title));
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.ic_bike_notification);
        builder.setPriority(Notification.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);

        // Add Disconnect button intent in notification.
        Intent stopIntent = new Intent(this, TSDZBTService.class);
        stopIntent.setAction(ACTION_STOP_FOREGROUND_SERVICE);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);
        NotificationCompat.Action prevAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Disconnect", pendingStopIntent);
        builder.addAction(prevAction);

        // Build the notification.
        Notification notification = builder.build();

        // Start foreground service.
        startForeground(1, notification);

        Intent bi = new Intent(SERVICE_STARTED_BROADCAST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(bi);
        mService = this;
    }

    private void stopForegroundService()
    {
        Log.d(TAG, "stopForegroundService");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        Intent bi = new Intent(SERVICE_STOPPED_BROADCAST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(bi);
        mService = null;

        // Stop the foreground service.
        stopSelf();
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionRetry = 0;
                Log.i(TAG, "onConnectionStateChange: Connected");
                // Discover services after successful connection.
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = ConnectionState.DISCONNECTED;
                Log.i(TAG, "onConnectionStateChange: Disconnected");
                if (!stopped)
                    if (connectionRetry++ > MAX_CONNECTION_RETRY) {
                        disconnect();
                        stopForegroundService();
                    } else {
                        connect(address);
                        Intent bi = new Intent(CONNECTION_LOST_BROADCAST);
                        LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(bi);
                    }
                else {
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                Log.i(TAG, "Services: " + services.toString());
                for (BluetoothGattService s:services) {
                    UUID serviceUUID = s.getUuid();

                    if (serviceUUID.equals(UUID_TSDZ_SERVICE)) {
                        List<BluetoothGattCharacteristic> lc = s.getCharacteristics();
                        for (BluetoothGattCharacteristic c:lc) {
                            UUID charUUID = c.getUuid();

                            if (charUUID.equals(UUID_PERIODIC_CHARACTERISTIC)) {
                                tsdz_periodic_char = c;
                                Log.d(TAG, "UUID_PERIODIC_CHARACTERISTIC enable notifications");

                                mBluetoothGatt.setCharacteristicNotification(tsdz_periodic_char,true);
                                BluetoothGattDescriptor descriptor = tsdz_periodic_char.getDescriptor(CCCD);
                                if (descriptor != null) {
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    mBluetoothGatt.writeDescriptor(descriptor);
                                }

                            } else if(charUUID.equals(UUID_CONFIG_CHARACTERISTIC)) {
                                tsdz_config_char = c;
                                Log.d(TAG, "UUID_CONFIG_CHARACTERISTIC enable notifications");
                            }
                        }
                    }
                }

                if (tsdz_periodic_char == null || tsdz_config_char == null) {
                    Intent bi = new Intent(CONNECTION_FAILURE_BROADCAST);
                    // TODO bi.putExtra("MESSAGE", "Error Detail");
                    LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(bi);
                    Log.e(TAG, "onServicesDiscovered Characteristic not found!");
                    disconnect();
                    return;
                }

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            //Log.d(TAG, "onDescriptorWrite:" + descriptor.getCharacteristic().getUuid().toString() +
            //        " - " + descriptor.getUuid().toString());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (descriptor.getUuid().equals(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))) {
                    boolean enable = (descriptor.getValue()[0] & 0x01) == 1;

                    if (mConnectionState == ConnectionState.CONNECTING) {
                        mConnectionState = ConnectionState.CONNECTED;
                        Intent bi = new Intent(CONNECTION_SUCCESS_BROADCAST);
                        LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(bi);
                    } else {
                        // DISCONNECTING
                        mBluetoothGatt.disconnect();
                    }

//                    //Log.d(TAG, "onDescriptorWrite: value = " + descriptor.getValue()[0]);
//                    if (descriptor.getCharacteristic().getUuid().equals(UUID_PERIODIC_CHARACTERISTIC))
//                        setCharacteristicNotification(tsdz_periodic_char,enable);
//                    else if (descriptor.getCharacteristic().getUuid().equals(UUID_CONFIG_CHARACTERISTIC))
//                        setCharacteristicNotification(tsdz_config_char,enable);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            //Log.d(TAG, "onCharacteristicRead:" + characteristic.getUuid().toString());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (UUID_CONFIG_CHARACTERISTIC.equals(characteristic.getUuid())) {
                    Intent bi = new Intent(TSDZ_CFG_READ_BROADCAST);
                    bi.putExtra(VALUE_EXTRA, characteristic.getValue());
                    LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(bi);
                } else if (UUID_PERIODIC_CHARACTERISTIC.equals(characteristic.getUuid())) {
                    Intent bi = new Intent(TSDZ_CFG_READ_BROADCAST);
                    bi.putExtra(VALUE_EXTRA, characteristic.getValue());
                    LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(bi);
                }
            } else {
                Log.e(TAG, "Characteristic read Error: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte [] data = characteristic.getValue();
            if (UUID_PERIODIC_CHARACTERISTIC.equals(characteristic.getUuid())) {
                if (data.length == PERIODIC_ADV_SIZE) {
                    TSDZ_Periodic periodic;
                    periodic = Global.getInstance().TSZD2Periodic;
                    periodic.setData(data);

                    int motorState = periodic.motorState;

                    // populate first time
                    if (m_motorStatePrevious == -1)
                        m_motorStatePrevious = motorState;

                    Intent bi = new Intent(TSDZ_PERIODIC_WRITE_BROADCAST);
                    LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(bi);

                    // if we are in connecting state, now we are connected
                    if (mConnectionState == ConnectionState.CONNECTING) {
                        mConnectionState = ConnectionState.CONNECTED;
                        Intent intent = new Intent(CONNECTION_SUCCESS_BROADCAST);
                        LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(intent);
                    }

                    if (mConnectionState == ConnectionState.CONNECTED) {
                        if (motorState != m_motorStatePrevious) {
                            Intent intent;
                            if (motorState == 1) {
                                intent = new Intent(TSDZ_MOTOR_READY);
                                LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(intent);
                            } else if (motorState == 0) {
                                intent = new Intent(TSDZ_MOTOR_OFF);
                                LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(intent);
                            } else {
                                intent = new Intent(TSDZ_MOTOR_INITIALIZING);
                                LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(intent);
                            }
                        }
                    }

                    m_motorStatePrevious = motorState;
                } else {
                    Log.e(TAG, "Wrong Status Advertising Size: " + data.length);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                         int status) {
            //Log.d(TAG, "onCharacteristicWrite:" + characteristic.getUuid().toString());
            if (UUID_CONFIG_CHARACTERISTIC.equals(characteristic.getUuid())) {
                Intent bi = new Intent(TSDZ_CFG_WRITE_BROADCAST);
                bi.putExtra(VALUE_EXTRA, status == BluetoothGatt.GATT_SUCCESS);
                LocalBroadcastManager.getInstance(TSDZBTService.this).sendBroadcast(bi);
            }
        }
    };

    private boolean connect(String address) {
        Log.d(TAG, "connect");
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = ConnectionState.CONNECTING;
                return true;
            } else {
//                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//                mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
//                mBluetoothDeviceAddress = address;
                Log.d(TAG, "Connection failed");
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = ConnectionState.CONNECTING;
        return true;
    }

    private void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (mConnectionState == ConnectionState.CONNECTED) {
            mConnectionState = ConnectionState.DISCONNECTING;
        } else
            mBluetoothGatt.disconnect();
    }

    public ConnectionState getConnectionStatus() {
        return mConnectionState;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     */
    public void readCfg() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || tsdz_config_char == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(tsdz_config_char);
    }

    public void writeCfg(TSDZ_Configurations cfg) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || tsdz_config_char == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        tsdz_config_char.setValue(cfg.toByteArray());
        mBluetoothGatt.writeCharacteristic(tsdz_config_char);
    }

    public void writePeriodic(TSDZ_Periodic periodic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || tsdz_config_char == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        tsdz_periodic_char.setValue(periodic.toByteArray());
        mBluetoothGatt.writeCharacteristic(tsdz_periodic_char);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        boolean ret = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (ret == false) {
            Log.w(TAG, "BluetoothAdapter setCharacteristicNotification fail");
        }

//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(TSDZ_CHARACTERISTICS_PERIODIC));

//        if (enabled)
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        else
//            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

//        mBluetoothGatt.writeDescriptor(descriptor);
    }
}
