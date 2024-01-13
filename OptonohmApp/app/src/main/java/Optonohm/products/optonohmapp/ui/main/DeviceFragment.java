package Optonohm.products.optonohmapp.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import Optonohm.products.optonohmapp.mcu.BLE.BLEScannedDevice;
import com.example.optonohmapp.databinding.FragmentDeviceBinding;

import com.google.android.material.snackbar.Snackbar;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeviceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class DeviceFragment extends Fragment {
    private FragmentDeviceBinding binding;
    private PageViewModel _VModel;

    public DeviceFragment() {
        // Required empty public constructor
    }


    public static DeviceFragment newInstance() {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("DeviceFragment", "onCreate");

    }

    ArrayAdapter<BLEScannedDevice> _ArrayAdapter;
    List<BLEScannedDevice> _Devices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w("DeviceFragment", "onCreateView");
        binding = FragmentDeviceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        _VModel = new ViewModelProvider(this).get(PageViewModel.class);
        _VModel.getScanResults().observe(getViewLifecycleOwner(), new Observer<List<BLEScannedDevice>>() {
            @Override
            public void onChanged(@Nullable List<BLEScannedDevice> devices) {
                final ListView listv = binding.ListViewDevices;
                _Devices = devices;
                _ArrayAdapter = new ArrayAdapter<BLEScannedDevice>(getContext(),android.R.layout.simple_list_item_1,devices);
                listv.setAdapter(_ArrayAdapter);
                binding.buttonSearchDevice.setEnabled(true);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.w("DeviceFragment", "onViewCreated");

        Context cont = this.getContext();
        binding.buttonSearchDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.buttonSearchDevice.setEnabled(false);
                _VModel.scanDevices();
            }
        });

        final ListView listv = binding.ListViewDevices;


        listv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView ls = (TextView) view;
                Context hostActivity = getActivity();
                AlertDialog.Builder adb = new AlertDialog.Builder(hostActivity);
                adb.setTitle("Add Device");
                adb.setMessage("Add Device");
                final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(hostActivity);

                        SharedPreferences.Editor edt = prefs.edit();
                        BLEScannedDevice dev = (BLEScannedDevice) parent.getAdapter().getItem(position);
                        edt.putString("bluetooth_Address", dev.getAddress());
                        edt.apply();
                        edt.commit();

                        _VModel.createDevice(dev);
                        Snackbar.make(view, "bluetooth Address saved", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        _Devices.remove(positionToRemove);
                        _ArrayAdapter.notifyDataSetChanged();
                    }
                });

                adb.show();
            }
        });


    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("DeviceFragment", "onDestroy");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.w("DeviceFragment", "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w("DeviceFragment", "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w("DeviceFragment", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w("DeviceFragment", "onResume");
    }
}