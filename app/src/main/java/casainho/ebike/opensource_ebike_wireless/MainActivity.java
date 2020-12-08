package casainho.ebike.opensource_ebike_wireless;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import casainho.ebike.opensource_ebike_wireless.activities.AboutActivity;
import casainho.ebike.opensource_ebike_wireless.activities.BluetoothSetupActivity;
import casainho.ebike.opensource_ebike_wireless.activities.ConfigurationsActivity;
import casainho.ebike.opensource_ebike_wireless.data.TSDZ_Debug;
import casainho.ebike.opensource_ebike_wireless.data.TSDZ_Periodic;

import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import static java.util.Arrays.copyOfRange;
import static casainho.ebike.opensource_ebike_wireless.TSDZConst.DEBUG_ADV_SIZE;
import static casainho.ebike.opensource_ebike_wireless.TSDZConst.PERIODIC_ADV_SIZE;
import static casainho.ebike.opensource_ebike_wireless.activities.BluetoothSetupActivity.KEY_DEVICE_MAC;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = "MainActivity";
    private static final String KEY_SCREEN_ON = "SCREEN_ON";
    private static final String KEY_FULL_SCREEN_ON = "FULL_SCREEN_ON";

    private TextView mTitle;
    private boolean serviceRunning;
    private  FloatingActionButton fabButton;
    private MainPagerAdapter mainPagerAdapter;

    private static final int REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int APP_PERMISSION_REQUEST = 1;

    IntentFilter mIntentFilter = new IntentFilter();

    private ViewPager viewPager;
    private final byte[] lastStatusData = new byte[PERIODIC_ADV_SIZE];
    private final byte[] lastDebugData = new byte[DEBUG_ADV_SIZE];

    private final TSDZ_Periodic status = new TSDZ_Periodic();
    private final TSDZ_Debug debug = new TSDZ_Debug();

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        boolean screenOn = MyApp.getPreferences().getBoolean(KEY_SCREEN_ON, false);
        if (screenOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        boolean fullScreenOn = MyApp.getPreferences().getBoolean(KEY_FULL_SCREEN_ON, false);
        if (fullScreenOn)
            setFullscreen(true);
        else
            setFullscreen(false);

        mainPagerAdapter = new MainPagerAdapter(this, getSupportFragmentManager(), status, debug);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(mainPagerAdapter);

//        viewPager.setOnTouchListener(this);
//
//        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
//
//            @Override
//            public void onPageSelected(int position) {
//                switch (position) {
//                    case 0:
//                        mTitle.setText(R.string.tsdz2_wireless);
//                        break;
//                    case 1:
//                        mTitle.setText(R.string.debug);
//                        break;
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {}
//        });
//
//        gestureDetector = new GestureDetector(this,new OnSwipeListener(){
//            @Override
//            public boolean onSwipe(Direction direction) {
//                if (direction==Direction.up){
//                    // Log.d(TAG, "onSwipe: up");
//                    Intent myIntent = new Intent(MainActivity.this, ChartActivity.class);
//                    MainActivity.this.startActivity(myIntent);
//                    return false;
//                }
//                if (direction==Direction.down){
//                    Log.d(TAG, "onSwipe: down");
//                    return false;
//                }
//                return false;
//            }
//        });

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);
        mTitle = toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(R.string.ebike_wireless);

        // find the Bluetooth connection logo
        View view = toolbar.getChildAt(0);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkDevice()) {
                    Intent intent = new Intent(MainActivity.this, TSDZBTService.class);
                    if (serviceRunning) {
                        intent.setAction(TSDZBTService.ACTION_STOP_FOREGROUND_SERVICE);
                    } else {
                        intent.setAction(TSDZBTService.ACTION_START_FOREGROUND_SERVICE);
                        intent.putExtra(TSDZBTService.ADDRESS_EXTRA, MyApp.getPreferences().getString(KEY_DEVICE_MAC, null));
                    }

                    if (Build.VERSION.SDK_INT >= 26)
                        startForegroundService(intent);
                    else
                        startService(intent);
                }
            }
        });

        checkPermissions();

        mIntentFilter.addAction(TSDZBTService.SERVICE_STARTED_BROADCAST);
        mIntentFilter.addAction(TSDZBTService.SERVICE_STOPPED_BROADCAST);
        mIntentFilter.addAction(TSDZBTService.CONNECTION_SUCCESS_BROADCAST);
        mIntentFilter.addAction(TSDZBTService.CONNECTION_FAILURE_BROADCAST);
        mIntentFilter.addAction(TSDZBTService.CONNECTION_LOST_BROADCAST);

        checkBT();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIStatus();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.screenONCB);
        item.setChecked(MyApp.getPreferences().getBoolean(KEY_SCREEN_ON, false));

        item = menu.findItem(R.id.fullScreenONCB);
        item.setChecked(MyApp.getPreferences().getBoolean(KEY_FULL_SCREEN_ON, false));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        TSDZBTService service = TSDZBTService.getBluetoothService();
        boolean connected;

        if (service != null && service.getConnectionStatus() == TSDZBTService.ConnectionState.CONNECTED) {
            menu.findItem(R.id.config).setEnabled(true);
            connected = true;
        } else {
            menu.findItem(R.id.config).setEnabled(false);
            connected = false;
        }

        if (checkDevice()) {
            // Bluetooth device is configured
            menu.findItem(R.id.connectBluetooth).setEnabled(true);
            if (connected) {
                menu.findItem(R.id.connectBluetooth).setTitle(R.string.disconnect_bluetooth);
            } else {
                menu.findItem(R.id.connectBluetooth).setTitle(R.string.connect_bluetooth);
            }
        } else {
            menu.findItem(R.id.connectBluetooth).setEnabled(false);
            menu.findItem(R.id.connectBluetooth).setTitle(R.string.connect_bluetooth);
        }

        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int id = item.getItemId();
        switch (id) {
            case R.id.connectBluetooth:
                if (!checkDevice()) {
                    Toast.makeText(this, "Please select the bluetooth device to connect", Toast.LENGTH_LONG).show();
                    return true;
                }

                intent = new Intent(MainActivity.this, TSDZBTService.class);
                if (serviceRunning) {
                    intent.setAction(TSDZBTService.ACTION_STOP_FOREGROUND_SERVICE);
                } else{
                    intent.setAction(TSDZBTService.ACTION_START_FOREGROUND_SERVICE);
                    intent.putExtra(TSDZBTService.ADDRESS_EXTRA, MyApp.getPreferences().getString(KEY_DEVICE_MAC, null));
                }

                if (Build.VERSION.SDK_INT >= 26)
                    startForegroundService(intent);
                else
                    startService(intent);

                invalidateOptionsMenu();
                return true;

            case R.id.config:
                intent = new Intent(this, ConfigurationsActivity.class);
                startActivity(intent);
                return true;

            case R.id.btSetup:
                intent = new Intent(this, BluetoothSetupActivity.class);
                startActivity(intent);
                return true;

            case R.id.screenONCB:
                boolean isChecked = !item.isChecked();
                item.setChecked(isChecked);
                SharedPreferences.Editor editor = MyApp.getPreferences().edit();
                editor.putBoolean(KEY_SCREEN_ON, isChecked);
                editor.apply();
                if (isChecked)
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                else
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                return true;

            case R.id.fullScreenONCB:
                isChecked = !item.isChecked();
                item.setChecked(isChecked);
                editor = MyApp.getPreferences().edit();
                editor.putBoolean(KEY_FULL_SCREEN_ON, isChecked);
                editor.apply();
                if (isChecked)
                    setFullscreen(true);
                else
                    setFullscreen(false);

                return true;

            case R.id.about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        TSDZBTService service = TSDZBTService.getBluetoothService();
        if (service == null || service.getConnectionStatus() != TSDZBTService.ConnectionState.CONNECTED)
            return;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        TSDZBTService service = TSDZBTService.getBluetoothService();
        return service != null && service.getConnectionStatus() == TSDZBTService.ConnectionState.CONNECTED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode != RESULT_OK) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth activation failed");
                builder.setMessage("Since bluetooth is not active, this app will not be able to run.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener((DialogInterface) -> finish());
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == APP_PERMISSION_REQUEST) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission request failed");
                builder.setMessage("Application will end.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener((DialogInterface) -> finish());
                builder.show();
            }
        }
    }

    private boolean checkDevice() {
        String mac = MyApp.getPreferences().getString(KEY_DEVICE_MAC, null);
        if (mac != null) {
            final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            final BluetoothAdapter btAdapter = btManager.getAdapter();
            BluetoothDevice selectedDevice = btAdapter.getRemoteDevice(mac);
            return selectedDevice.getBondState() == BluetoothDevice.BOND_BONDED;
        }
        return false;
    }

    private void refreshView() {
    }

    private void updateUIStatus() {
        if (TSDZBTService.getBluetoothService() != null) {
            serviceRunning = true;
            if (TSDZBTService.getBluetoothService().getConnectionStatus() == TSDZBTService.ConnectionState.CONNECTED)
                mTitle.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.bt_connected, 0, 0, 0);
            else
                mTitle.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.bt_connecting, 0, 0, 0);
        } else {
            mTitle.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.bt_disconnected, 0, 0, 0);
            serviceRunning = false;
        }
    }

    private void checkBT() {
        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    private void checkPermissions() {
        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    APP_PERMISSION_REQUEST);
        }
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(TAG, "onReceive " + intent.getAction());
            if (intent.getAction() == null)
                return;
            byte [] data;
            switch (intent.getAction()) {
                case TSDZBTService.SERVICE_STARTED_BROADCAST:
                    Log.d(TAG, "SERVICE_STARTED_BROADCAST");
                    TSDZBTService service = TSDZBTService.getBluetoothService();
                    if (service != null && service.getConnectionStatus() != TSDZBTService.ConnectionState.CONNECTED)
                        mTitle.setCompoundDrawablesWithIntrinsicBounds(
                                R.mipmap.bt_connecting, 0, 0, 0);
                    serviceRunning = true;
					invalidateOptionsMenu();
                    break;
                case TSDZBTService.SERVICE_STOPPED_BROADCAST:
                    Log.d(TAG, "SERVICE_STOPPED_BROADCAST");
                    mTitle.setCompoundDrawablesWithIntrinsicBounds(
                    R.mipmap.bt_disconnected, 0, 0, 0);
                    serviceRunning = false;
					invalidateOptionsMenu();
                    break;
                case TSDZBTService.CONNECTION_SUCCESS_BROADCAST:
                    Log.d(TAG, "CONNECTION_SUCCESS_BROADCAST");
                    mTitle.setCompoundDrawablesWithIntrinsicBounds(
					R.mipmap.bt_connected, 0, 0, 0);
					invalidateOptionsMenu();
					break;
                case TSDZBTService.CONNECTION_FAILURE_BROADCAST:
                    Log.d(TAG, "CONNECTION_FAILURE_BROADCAST");
                    Toast.makeText(getApplicationContext(), "Connection failure to EBike wireless board", Toast.LENGTH_LONG).show();
                    mTitle.setCompoundDrawablesWithIntrinsicBounds(
					R.mipmap.bt_connecting, 0, 0, 0);
					invalidateOptionsMenu();
					break;
                case TSDZBTService.CONNECTION_LOST_BROADCAST:
                    Log.d(TAG, "CONNECTION_LOST_BROADCAST");
                    Toast.makeText(getApplicationContext(), "Connection lost to EBike wireless board", Toast.LENGTH_LONG).show();
                    mTitle.setCompoundDrawablesWithIntrinsicBounds(
					R.mipmap.bt_connecting, 0, 0, 0);
					invalidateOptionsMenu();
					break;
            }
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return false;
    }

    private void setFullscreen(boolean fullscreen)
    {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen)
        {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        else
        {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

}
