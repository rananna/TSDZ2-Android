package spider65.ebike.tsdz2_esp32.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;

import spider65.ebike.tsdz2_esp32.R;
import spider65.ebike.tsdz2_esp32.TSDZBTService;
import spider65.ebike.tsdz2_esp32.data.TSDZ_Periodic;
import spider65.ebike.tsdz2_esp32.data.Variable;

import static spider65.ebike.tsdz2_esp32.data.Variable.DataType.fromInteger;

public class FragmentStatus extends Fragment implements View.OnLongClickListener, MyFragmentListener {

    private static final String TAG = "FragmentStatus";

    //private IntentFilter mIntentFilter = new IntentFilter();

    private final TSDZ_Periodic periodic;

    private final IntentFilter mIntentFilter = new IntentFilter();

    private View onVariableLongClickView;

    View mRootView;
    TextView mBatterySOCTV,
            mAssistLevelValueTV;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentStatus.
     */
    public static FragmentStatus newInstance(TSDZ_Periodic status) {
        return new FragmentStatus(status);
    }

    private FragmentStatus(TSDZ_Periodic status) {
        this.periodic = status;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mIntentFilter.addAction(TSDZBTService.TSDZ_PERIODIC_WRITE_BROADCAST);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        mRootView = (ConstraintLayout) inflater.inflate(R.layout.fragment_status, container, false);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBatterySOCTV = (TextView) getView().findViewById(R.id.batterySOCTV);
        mBatterySOCTV.setText(String.valueOf(periodic.batterySOC / 10));

        ((TextView) getView().findViewById(R.id.assistLevelTV)).setText(R.string.assist_level);
        mAssistLevelValueTV = (TextView) getView().findViewById(R.id.assistLevelValueTV);
        mAssistLevelValueTV.setText(String.valueOf(periodic.assistLevel));

        getView().findViewById(R.id.assistLevelPlusBT).setOnClickListener(this::onPlusDownClick);
        getView().findViewById(R.id.assistLevelMinusBT).setOnClickListener(this::onPlusDownClick);

        getView().findViewById(R.id.fl1).setOnLongClickListener(this::longClickSelectVariable);
        getView().findViewById(R.id.fl31).setOnLongClickListener(this::longClickSelectVariable);
        getView().findViewById(R.id.fl32).setOnLongClickListener(this::longClickSelectVariable);
        getView().findViewById(R.id.fl41).setOnLongClickListener(this::longClickSelectVariable);
        getView().findViewById(R.id.fl42).setOnLongClickListener(this::longClickSelectVariable);

        // TODO update var number from preferences
        periodic.variablesConfig.put(getView().findViewById(R.id.fl1).getId(),
                new Variable(getView().findViewById(R.id.fl1TV).getId(),
                        getView().findViewById(R.id.fl1ValueTV).getId(),
                        Variable.DataType.speed)
        );

        periodic.variablesConfig.put(getView().findViewById(R.id.fl31).getId(),
                new Variable(getView().findViewById(R.id.fl31TV).getId(),
                        getView().findViewById(R.id.fl31ValueTV).getId(),
                        Variable.DataType.humanPower)
        );

        periodic.variablesConfig.put(getView().findViewById(R.id.fl32).getId(),
                new Variable(getView().findViewById(R.id.fl32TV).getId(),
                        getView().findViewById(R.id.fl32ValueTV).getId(),
                        Variable.DataType.motorCurrent)
        );

        periodic.variablesConfig.put(getView().findViewById(R.id.fl41).getId(),
                new Variable(getView().findViewById(R.id.fl41TV).getId(),
                        getView().findViewById(R.id.fl41ValueTV).getId(),
                        Variable.DataType.batteryCurrent)
        );

        periodic.variablesConfig.put(getView().findViewById(R.id.fl42).getId(),
                new Variable(getView().findViewById(R.id.fl42TV).getId(),
                        getView().findViewById(R.id.fl42ValueTV).getId(),
                        Variable.DataType.pedalCadence)
        );

        updateVariableViews();
    }

    boolean longClickSelectVariable(View v) {
        Toast.makeText(getContext(), "I am click", Toast.LENGTH_SHORT).show();

        onVariableLongClickView = v;

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Choose variable");

        // add a list
        builder.setItems(this.getContext().getResources().getStringArray(R.array.variables), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int frameLayoutID = onVariableLongClickView.getId();
                int variableID = which;

                Variable variable = periodic.variablesConfig.get(frameLayoutID);
                variable.dataType = Variable.DataType.fromInteger(variableID);
                periodic.variablesConfig.put(frameLayoutID, variable);

                updateVariableViews();
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Data could be changed when fragment was not visible. Refresh the view
//        binding.invalidateAll();

        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(mMessageReceiver, mIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(mMessageReceiver);
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive " + intent.getAction());
            if (intent.getAction() == null)
                return;
            switch (intent.getAction()) {
                case TSDZBTService.TSDZ_PERIODIC_WRITE_BROADCAST:
                    if (periodic.setData(intent.getByteArrayExtra(TSDZBTService.VALUE_EXTRA))) {

                        mBatterySOCTV.setText(String.valueOf((int) periodic.batterySOC));
                        mAssistLevelValueTV.setText(String.valueOf(periodic.assistLevel));

                        updateVariableViews();
                    }
                    break;
            }
        }
    };

    void updateVariableViews() {

        TextView tv;

        for (int frameLayoutID : periodic.variablesConfig.keySet()) {
            Variable variable = periodic.variablesConfig.get(frameLayoutID);

            tv = (TextView) getView().findViewById(variable.labelTV);
            tv.setText(variable.dataType.getName());
            tv = (TextView) getView().findViewById(variable.valueTV);

            switch (variable.dataType) {
                case batteryVoltage:
                    tv.setText(String.valueOf(periodic.batteryVoltage));
                    break;

                case batteryCurrent:
                    tv.setText(String.valueOf(periodic.batteryCurrent));
                    break;

                case batterySOC:
                    tv.setText(String.valueOf(periodic.batterySOC));
                    break;

                case batteryUsedEnergy:
                    tv.setText(String.valueOf(periodic.wattsHour));
                    break;

                case batteryADCCurrent:
                    tv.setText(String.valueOf(periodic.ADCBatteryCurrent));
                    break;

                case motorCurrent:
                    tv.setText(String.valueOf(periodic.motorCurrent));
                    break;

                case motorTemperature:
                    tv.setText(String.valueOf(periodic.motorTemperature));
                    break;

                case motorSpeed:
                    tv.setText(String.valueOf(periodic.motorSpeedERPS));
                    break;

                case speed:
                    tv.setText(String.valueOf(periodic.wheelSpeed));
                    break;

                case hallSensors:
                    tv.setText(String.valueOf(periodic.motorHallSensors));
                    break;

                case pedalSide:
                    tv.setText(String.valueOf(periodic.PASPedalRight));
                    break;

                case throttle:
                    tv.setText(String.valueOf(periodic.throttle));
                    break;

                case throttleADC:
                    tv.setText(String.valueOf(periodic.ADCThrottle));
                    break;

                case torqueSensorADC:
                    tv.setText(String.valueOf(periodic.ADCPedalTorqueSensor));
                    break;

                case pedalWeight:
                    tv.setText(String.valueOf(periodic.pedalWeight));
                    break;

                case pedalWeightWithOffset:
                    tv.setText(String.valueOf(periodic.pedalWeightWithOffset));
                    break;

                case pedalCadence:
                    tv.setText(String.valueOf(periodic.pedalCadence));
                    break;

                case dutyCyle:
                    tv.setText(String.valueOf(periodic.dutyCycle));
                    break;

                case focAngle:
                    tv.setText(String.valueOf(periodic.FOCAngle));
                    break;

                case humanPower:
                    tv.setText(String.valueOf(periodic.humanPedalPower));
                    break;

                case odometer:
                    tv.setText(String.valueOf(periodic.odometer));
                    break;
            }
        }
    }

    public void onPlusDownClick(View view) {

        switch (view.getId()) {
            case R.id.assistLevelPlusBT:
                if (periodic.assistLevel < 6)
                    periodic.assistLevelTarget = periodic.assistLevel + 1;
                break;
            case R.id.assistLevelMinusBT:
                if (periodic.assistLevel > 0)
                    periodic.assistLevelTarget = periodic.assistLevel - 1;
                break;
        }

        if ((view.getId() == R.id.assistLevelPlusBT ||
                view.getId() == R.id.assistLevelMinusBT) &&
                (periodic.assistLevelTarget != periodic.assistLevel)) {

            TSDZBTService service = TSDZBTService.getBluetoothService();
            if (service != null && service.getConnectionStatus() == TSDZBTService.ConnectionState.CONNECTED) {
                service.writePeriodic(periodic);
            }
        }
    }

    @Override
    public void refreshView() {
//        binding.invalidateAll();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }
}
