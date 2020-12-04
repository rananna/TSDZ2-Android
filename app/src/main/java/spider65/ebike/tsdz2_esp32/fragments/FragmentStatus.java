package spider65.ebike.tsdz2_esp32.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;

import spider65.ebike.tsdz2_esp32.R;
import spider65.ebike.tsdz2_esp32.TSDZBTService;
import spider65.ebike.tsdz2_esp32.data.TSDZ_Periodic;
import spider65.ebike.tsdz2_esp32.databinding.FragmentStatusBinding;


public class FragmentStatus extends Fragment implements View.OnLongClickListener, MyFragmentListener {

    private static final String TAG = "FragmentStatus";

    //private IntentFilter mIntentFilter = new IntentFilter();

    private final TSDZ_Periodic periodic;

    private FragmentStatusBinding binding;

    private final IntentFilter mIntentFilter = new IntentFilter();

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
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_status, container, false);
        binding.setStatus(periodic);
        binding.setHandler(this);

        binding.fl31.setOnLongClickListener(this::longClickSelectVariable);
        binding.fl32.setOnLongClickListener(this::longClickSelectVariable);
        binding.fl41.setOnLongClickListener(this::longClickSelectVariable);
        binding.fl42.setOnLongClickListener(this::longClickSelectVariable);

        return binding.getRoot();
    }

    boolean longClickSelectVariable(View v) {
        Toast.makeText(getContext(), "I am click", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Data could be changed when fragment was not visible. Refresh the view
        binding.invalidateAll();

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
                        periodic.setData(intent.getByteArrayExtra(TSDZBTService.VALUE_EXTRA));
                        binding.setStatus(periodic);
                    }
                    break;
            }
        }
    };

    // TODO
    // Visualizzazione grafici
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.speedValueTV:
            case R.id.cadenceValueTV:
                break;
        }
        return false;
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
        binding.invalidateAll();
    }
}
