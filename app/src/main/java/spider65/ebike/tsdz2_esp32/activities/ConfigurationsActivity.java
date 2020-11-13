package spider65.ebike.tsdz2_esp32.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import spider65.ebike.tsdz2_esp32.R;
import spider65.ebike.tsdz2_esp32.TSDZBTService;
import spider65.ebike.tsdz2_esp32.data.TSDZ_Configurations;
import spider65.ebike.tsdz2_esp32.databinding.ActivityConfigurationsBinding;

public class ConfigurationsActivity extends AppCompatActivity {

    private static final String TAG = "ConfigurationsActivity";
    private final TSDZ_Configurations cfg = new TSDZ_Configurations();
    private final IntentFilter mIntentFilter = new IntentFilter();
    private ActivityConfigurationsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_configurations);
        binding.setHandler(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mIntentFilter.addAction(TSDZBTService.TSDZ_CFG_READ_BROADCAST);
        mIntentFilter.addAction(TSDZBTService.TSDZ_CFG_WRITE_BROADCAST);
        TSDZBTService service = TSDZBTService.getBluetoothService();
        if (service != null && service.getConnectionStatus() == TSDZBTService.ConnectionState.CONNECTED)
            service.readCfg();
        else {
            showDialog(getString(R.string.error), getString(R.string.connection_error));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    public void onOkCancelClick(View view) {
        switch (view.getId()) {
            case R.id.okButton:
                saveCfg();
                break;
            case R.id.cancelButton:
                finish();
                break;
        }
    }

//    public void onClickInductance(View view) {
//        switch (view.getId()) {
//            case R.id.inductance36BT:
//                binding.inductanceET.setText("80");
//                break;
//            case R.id.inductance48BT:
//                binding.inductanceET.setText("142");
//                break;
//        }
//    }
//    //  invalidate all to hide/show the checkbox dependant fields
//    public void onCheckedChanged(View view, boolean checked) {
//        switch (view.getId()) {
//            case R.id.assistCB:
//                binding.assistWPRET.setEnabled(checked);
//                break;
//            case R.id.streetPowerCB:
//                binding.streetPowerET.setEnabled(checked);
//                break;
//            case R.id.torqueFixCB:
//                binding.torqueADCOffsetET.setEnabled(checked);
//                break;
//        }
//    }

    private void saveCfg() {
        Integer val;
        Float valFloat;
        boolean checked;
        if ((val = checkRange(binding.wheelmaxspeedET, 1, 99)) == null) {
            showDialog(getString(R.string.max_wheel_speed), getString(R.string.range_error, 0, 99));
            return;
        }
        cfg.ui8_wheel_max_speed = val;

        if ((val = checkRange(binding.wheelPerimeterET, 750, 3000)) == null) {
            showDialog(getString(R.string.max_wheel_speed), getString(R.string.range_error, 750, 3000));
            return;
        }
        cfg.ui16_wheel_perimeter = val;

        if ((valFloat = checkRange(binding.batterySocBatteryTotalWhET, 0.0f, 999.0f)) == null) {
            showDialog(getString(R.string.battery_soc_battery_total_wh), getString(R.string.range_error, 0.0f, 999.0f));
            return;
        }
        cfg.ui32_wh_x10_100_percent = (long) (valFloat * 10);

        if ((valFloat = checkRange(binding.batterySocResetVoltageET, 16.0f, 63.0f)) == null) {
            showDialog(getString(R.string.battery_soc_reset_voltage), getString(R.string.range_error, 16.0f, 63.0f));
            return;
        }
        cfg.ui16_battery_voltage_reset_wh_counter_x10 = (int) (valFloat * 10);

        if ((valFloat = checkRange(binding.batterySocUsedWhET, 0.0f, 9990.0f)) == null) {
            showDialog(getString(R.string.battery_soc_used_wh), getString(R.string.range_error, 0.0f, 9990.0f));
            return;
        }
        cfg.ui32_wh_x10_100_percent = (long) (valFloat * 10);


        TSDZBTService service = TSDZBTService.getBluetoothService();
        if (service != null && service.getConnectionStatus() == TSDZBTService.ConnectionState.CONNECTED)
            service.writeCfg(cfg);
        else {
            showDialog(getString(R.string.error), getString(R.string.connection_error));
        }
    }

    Integer checkRange(EditText et, int min, int max) {
        int val = Integer.parseInt(et.getText().toString());
        if (val < min || val > max) {
            et.setError(getString(R.string.range_error, min, max));
            return null;
        }
        return val;
    }

    Float checkRange(EditText et, float min, float max) {
        float val = Float.parseFloat(et.getText().toString());
        if (val < min || val > max) {
            et.setError(getString(R.string.range_error, min, max));
            return null;
        }
        return val;
    }

    private void showDialog (String title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (title != null)
            builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent.getAction());
        if (intent.getAction() == null)
            return;
        switch (intent.getAction()) {
            case TSDZBTService.TSDZ_CFG_READ_BROADCAST:
                if (cfg.setData(intent.getByteArrayExtra(TSDZBTService.VALUE_EXTRA))) {
                    binding.setCfg(cfg);
                }
                break;
            case TSDZBTService.TSDZ_CFG_WRITE_BROADCAST:
                if (intent.getBooleanExtra(TSDZBTService.VALUE_EXTRA,false))
                    finish();
                else
                    showDialog(getString(R.string.error), getString(R.string.write_cfg_error));
                break;
         }
        }
    };
}
