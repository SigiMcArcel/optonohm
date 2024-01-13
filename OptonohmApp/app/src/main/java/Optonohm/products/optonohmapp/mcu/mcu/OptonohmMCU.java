package Optonohm.products.optonohmapp.mcu.mcu;

import android.content.Context;

import Optonohm.products.optonohmapp.mcu.BLE.BLEDevice;
import Optonohm.products.optonohmapp.mcu.BLE.BLEScannedDevice;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstraction of the cipedTronic device
 * - Configuring, starts and controlling the BLE Service
 * - calculates the recieved data to readable values
 */
public class OptonohmMCU extends  BLEDevice {


    private static volatile OptonohmMCU _Instance = null;

    public static OptonohmMCU getInstance() {
        if (_Instance == null) {
            _Instance = new OptonohmMCU();
        }
        return _Instance;
    }

    public interface OnOptonohmDeviceListener {
        void onStatusChanged(String state);
        void onDeviceError(String error);
        void onGetConfiguration(OptonohmConfiguration configuration);
        void onScanResult(List<BLEScannedDevice> devices, String state);
    }

    //Bluetooth
    public static final UUID CIPED_SERVICE_UUID = UUID.fromString("b1fb1816-f607-42a1-827d-f84ae6bdf20a");
    public static final UUID CIPED_CONTROL_POINT_CHARACTER_UUID = UUID.fromString("b1fb0002-f607-42a1-827d-f84ae6bdf20a");

    private List<OnOptonohmDeviceListener> _Listeners = new ArrayList<>();
    private OptonohmControlPoint _OptonohmControlPoint = new OptonohmControlPoint(this, CIPED_CONTROL_POINT_CHARACTER_UUID);





    private OptonohmMCU() {
        super();
        addIndicateNotificationCharacteristic(CIPED_CONTROL_POINT_CHARACTER_UUID);
        setOnBLEDeviceListener(_OnBLEDeviceListener);
        _OptonohmControlPoint.setOnOptonohmControlPointListener(_OnOptonohmControlPointListener);
    }

    public void setOnOptonohmDeviceListener(OnOptonohmDeviceListener listener) {
        _Listeners.add(listener);
        listener.onStatusChanged(getDeviceState());
    }

    OptonohmControlPoint.OnOptonohmControlPointListener _OnOptonohmControlPointListener = new OptonohmControlPoint.OnOptonohmControlPointListener() {
        @Override
        public void onResponse(OptonohmControlPoint.OptonohmControlPointOpCodes opCode, OptonohmControlPoint.OptonohmControlPointResultCodes result, ByteBuffer parameter) {

        }

        @Override
        public void onError(OptonohmControlPoint.OptonohmControlPointOpCodes opCode, OptonohmControlPoint.OptonohmControlPointResultCodes result) {

        }

        @Override
        public void onGetConfig(OptonohmConfiguration conf, OptonohmControlPoint.OptonohmControlPointResultCodes result) {
            for (OnOptonohmDeviceListener listener:_Listeners) {
                listener.onGetConfiguration(conf);
            }
        }

        @Override
        public void onEmpty(){

        }

    };


    public void setConfiguration(OptonohmConfiguration configuration) {
        _OptonohmControlPoint.setConfiguration(configuration);
    }

    public void getConfiguration()
    {
        _OptonohmControlPoint.getConfiguration();
    }
    public void Start()
    {
        _OptonohmControlPoint.Start();
    }
    public void Stop()
    {
        _OptonohmControlPoint.Stop();
    }

    public void createDevice(Context context, String address) {

        createDevice(context, address, "OPTN", CIPED_SERVICE_UUID, true);
    }

    BLEDevice.OnBLEDeviceListener _OnBLEDeviceListener = new OnBLEDeviceListener() {
        @Override
        public void OnStatusChanged(BLEDeviceStates state) {
            for (OnOptonohmDeviceListener listener:_Listeners) {
                listener.onStatusChanged(state.name());
            }
        }

        @Override
        public void onCharacteristicRead(UUID characteristicUUID, byte[] value) {

        }

        @Override
        public void onDescriptionRead(UUID characteristicUUID, UUID descriptionUUID, byte[] value) {

        }

        @Override
        public void onCharacteristicNotification(UUID characteristicUUID, byte[] value) {


            if (characteristicUUID.compareTo(CIPED_CONTROL_POINT_CHARACTER_UUID) == 0) {
                _OptonohmControlPoint.processResults(value);

            } else
                return;
        }

        @Override
        public void onScanResult(List<BLEScannedDevice> devices, BLEDeviceStates state) {

            for (OnOptonohmDeviceListener listener:_Listeners
                 ) {
                listener.onScanResult(devices,"");
            }

        }

        @Override
        public void onError(BLEDeviceErrors error) {

            for (OnOptonohmDeviceListener listener:_Listeners
            ) {
                listener.onDeviceError(error.name());
            }
        }
    };

@Override
    public void finalize()
    {
        close();
    }


}
