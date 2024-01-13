/**
*    Abstracts a BLE device
*    Handles one service and several characteristics
*    Can used as a service
*    Works fully asynchron*
*
*   Using:
*   Create a Instance with the BLE Address, the service UUID it will handle and the Application Context
*   add the characteristic it will notify
*   Thread starts on constructor
*
*   Connect Sequencing:
*   - connect->
*        onConnectionStateChange(connected)->
*            discover Services ->
*                onServicesDiscovered->
*                    set Notification->
*                        ->Ready for use*
*
*  Commands:
*        Initialize,
*        Close,
*        Connect,
*        Disconnect,
*        ScanDevices,
*        WriteDescriptor,
*        ReadDescriptor,
*        ReadCharacteristic,
*        WriteCharacteristic,
*  events OnBLEDeviceListener:
*        void OnStatusChanged(BLEDeviceStates state);
*        void onCharacteristicRead(UUID characteristicUUID,byte[] value);
*        void onCharacteristicNotification(UUID characteristicUUID,byte[] value);
*        void onDescriptionRead(UUID characteristicUUID,UUID descriptionUUID,byte[] value);
*        void onScanResult(List<BLEScannedDevice> devices, BLEDeviceStates state);
*        void onError(BLEDeviceErrors error);
*
**/

package Optonohm.products.optonohmapp.mcu.BLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BLEDevice extends Thread implements AutoCloseable{

    private class BLEDeviceCommand
    {
        public BLEDeviceCommand(BLEDeviceCommandStates command)
        {
            Command = command;
        }
        public BLEDeviceCommand(
                BLEDeviceCommandStates command,
                Object param1

        )
        {
            Param1 = param1;
            Command = command;
        }
        public BLEDeviceCommand(
                BLEDeviceCommandStates command,
                Object param1,
                Object param2,
                Object param3,
                Object param4
                )
        {
            Param1 = param1;
            Param2 = param2;
            Param3 = param3;
            Param4 = param4;
            Command = command;
        }
        public BLEDeviceCommand(
                BLEDeviceCommandStates command,
                Object param1,
                Object param2,
                Object param3,
                Object param4,
                Object param5
        )
        {
            Param1 = param1;
            Param2 = param2;
            Param3 = param3;
            Param4 = param4;
            Param5 = param5;
            Command = command;
        }
        public BLEDeviceCommandStates Command;
        public Object Param1;
        public Object Param2;
        public Object Param3;
        public Object Param4;
        public Object Param5;
    }

    /**
     *
     */
    public interface OnBLEDeviceListener
    {
        /**
         *
         * @param state
         */
        void OnStatusChanged(BLEDeviceStates state);

        /**
         *
         * @param characteristicUUID
         * @param value
         */
        void onCharacteristicRead(UUID characteristicUUID,byte[] value);

        /**
         *
         * @param characteristicUUID
         * @param descriptionUUID
         * @param value
         */
        void onDescriptionRead(UUID characteristicUUID,UUID descriptionUUID,byte[] value);

        /**
         *
         * @param characteristicUUID
         * @param value
         */
        void onCharacteristicNotification(UUID characteristicUUID,byte[] value);

        /**
         *
         * @param devices
         * @param state
         */
        void onScanResult(List<BLEScannedDevice> devices, BLEDeviceStates state);

        /**
         *
         * @param error
         */
        void onError(BLEDeviceErrors error);
    }


    /**
     *
     */
    public enum BLEDeviceStates {
        None,
        NotInitalized,
        Disconnected,
        Connected,
    }

    public enum BLEDeviceCommandStates
    {
        Idle,
        Initialize,
        Close,
        SetAddress,
        Connect,
        Connecting,
        Connected,
        Disconnect,
        Disconnecting,
        Disconnected,
        DisconnectedThruClient,
        DiscoverServices,
        DiscoveringServices,
        DiscoverServicesFinish,
        EnableNotificiction,
        EnablingNotification,
        EnableNotificictionFinish,
        Ready,
        ScanDevices,
        ScanDevicesStart,
        ScanningDevices,
        ScanDevicesFinished,
        WriteDescriptor,
        WritingDescriptor,
        WrittenDescriptor,
        ReadDescriptor,
        ReadingDescriptor,
        ReadDescriptorFinished,
        ReadCharacteristic,
        ReadingCharacteristic,
        ReadCharacteristicFinished,
        WriteCharacteristic,
        WritingCharacteristic,
        WriteCharacteristicFinished,
        Error
    }

    public enum BLEDeviceErrors
    {
        Ok,
        CouldNotInitializeBLEManager,
        CouldNotInitializeBLEAdapter,
        CouldNotInitializeBLEScanner,
        CouldNotConnectGatt,
        CouldNotGetDevice,
        NotInitialized,
        AlreadyInitialized,
        NotConnected,
        AlreadyConnected,
        ConnectTimeout,
        AlreadyDisconnected,
        ServiceNotFound,
        ServiceNotValid,
        ServiceDiscoveryTimeout,
        CharacteristicNotFound,
        DescriptionNotFound,
        CouldNotConnectDevice,
        InvalidBLEAddress,
        EmptyBLEAddress,
        CouldNotReadCharacteristic,
        CouldNotReadDescription,
        CouldNotWriteCharacteristic,
        CouldNotWriteDescription,
        CouldNotDiscoverServices,
        CouldNotEnableNotification,
        NotificationInQueue,
        NoGattAvaiable,
        PermissionError,
        NoScannerFound,
        ScanningFailed,
        UnkownCommand
    }

    public enum NotificationType
    {
        Notification,
        Indication
    }

    private class NotificationParameter
    {
        public UUID Uuid;
        public NotificationType Type;
    }

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final int _RESTARTTIMEOUT = 1000;

    private BluetoothManager _BluetoothManager;
    private BluetoothAdapter _BluetoothAdapter;
    private String _BluetoothDeviceAddress;
    private BluetoothLeScanner _Scanner;

    private boolean _Valid;
    private List<BLEScannedDevice> _ScannedDeviceList = new ArrayList<>();
    private Context _Context;
    private BluetoothDevice _BluetoothDevice;
    private BluetoothGatt _BluetoothGatt;

    private BLEDeviceStates _State = BLEDeviceStates.NotInitalized;
    private BLEDeviceStates _LastConnectedState = BLEDeviceStates.None;

    private boolean _StateMachineStop = false;
    private BLEDeviceCommandStates _CommandState = BLEDeviceCommandStates.Idle;
    private BLEDeviceCommandStates _LastCommandState = BLEDeviceCommandStates.Idle;

    private  BLEDeviceCommand _ActualCommand = null;
    private BLEDeviceErrors _Error = BLEDeviceErrors.Ok;
    private boolean _ScanActive = false;
    private boolean _AutoConnectActive = false;

    String _Name = "";
    BLETimeOut _RestartTimeOut = new BLETimeOut();
    BLETimeOut _ScanTimeOut = new BLETimeOut();
    BLETimeOut _ConnectTimeout = new BLETimeOut();
    BLETimeOut _GattTimeout = new BLETimeOut();

    private OnBLEDeviceListener _Listener;
    private List<NotificationParameter> _NotificationCharacteristics = new ArrayList<>();
    private int _NotificationListCounter = 0;
    private UUID _ServiceUIID = null;
    private BluetoothGattService _Service = null;
    private ArrayDeque<BLEDeviceCommand> _CommandQueue = new ArrayDeque<>();
    private boolean _AutoConnect = false;

    private boolean _ConnectedFlag = false;
    private boolean _DisconnectedFlag = false;


    public BLEDevice()
    {
        start();
    }

    public void setOnBLEDeviceListener(OnBLEDeviceListener listener) {
        _Listener = listener;
        _Listener.OnStatusChanged(_State);
    }

    public void setAutoConnect(boolean auto)
    {
        _AutoConnect = auto;
    }
    public void setService(UUID serviceUUID)
    {
        _ServiceUIID = serviceUUID;
    }

    public String getDeviceState()
    {
        return _State.name();
    }

    public void addNotificationCharacteristic(UUID characteristic)
    {
        NotificationParameter param = new NotificationParameter();
        param.Uuid = characteristic;
        param.Type = NotificationType.Notification;

        _NotificationCharacteristics.add(param);
    }

    public void addIndicateNotificationCharacteristic(UUID characteristic)
    {
        NotificationParameter param = new NotificationParameter();
        param.Uuid = characteristic;
        param.Type = NotificationType.Indication;

        _NotificationCharacteristics.add(param);
    }

    /**
     * Initializes and connects (if autoConnect = true) a Ble remote device with one service
     * Fires a OnStatusChanged "connected" on success or a OnError event on fail
     * @param context           Application context
     * @param address           BLE Address as string
     * @param name              The name of the device as string. Used as scan filter
     * @param service           The UUID of the service
     * @param autoConnect       Sets if the should be reconnect automatically
	 */
    public void createDevice(Context context,String address,String name,UUID service,boolean autoConnect) {
        Log.e("BLEDevice", "createDevice req" + address);
        synchronized (this) {

            BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.Initialize, context, address, name, service, autoConnect);
            _CommandQueue.push(cmd);
        }
    }

    /**
     * Closes the device (disconnect and deinitialize)
     * Fires a OnStatusChanged "NotInitalized" on success or a OnError event on fail
     */
    public void closeDevice() {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.Close);
        _CommandQueue.push(cmd);
    }
    /**
     * Sets the BluetoothAddress
     * The device will recreate
     */
    public void setAddress(String address) {
        synchronized (this) {
            BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.SetAddress, address);
            _CommandQueue.push(cmd);
        }
    }
    /**
     * Connects the device
     * Fires a OnStatusChanged "Connected" on success or a OnError event on fail
     */
    public void connectDevice() {
        //Log.e("BLEDevice", "connectDevice req ");
        synchronized (this) {
            BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.Connect);
            _CommandQueue.push(cmd);
        }
    }
    /**
     * Disconnects the device
     * Fires a OnStatusChanged "Disonnected" on success or a OnError  if fail
     */
    public void disconnectDevice() {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.Disconnect);
        _CommandQueue.push(cmd);
    }
    /**
     * Scans for devices
     * The filter is set with the parameter name of createDevice
     * Fires a onScanResult
     */
    public void scanDevices()
    {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.ScanDevices);
        _CommandQueue.push(cmd);
    }
    /**
     * Writes a description of a characteristic of the configured service
     * Fires a OnError on fail
     *
     * @param characteristicUUID    The uuid of the characteristic
     * @param descriptionUUID       The uuid of the description
     * @param value                 The value as byte array
     */
    public void writeDescription(UUID characteristicUUID,UUID descriptionUUID,byte[] value){
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.WriteDescriptor,characteristicUUID,descriptionUUID,value,null);
        _CommandQueue.push(cmd);
    }

    /**
     * Reads a description of a characteristic of the configured service
     * Fires a onDescriptionRead or OnError if fail
     *
     * @param characteristicUUID    The uuid of the characteristic
     * @param descriptionUUID       The uuid of the description
     */
    public void readDescription(UUID characteristicUUID,UUID descriptionUUID) {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.ReadDescriptor,characteristicUUID,descriptionUUID,null,null);
        _CommandQueue.push(cmd);
    }
    /**
     * Writes a characteristic of the configured service
     * Fires a OnError if fail
     *
     * @param characteristicUUID The uuid of the characteristic
     * @param value value as byte array
     */
    public void writeCharacteristic(UUID characteristicUUID,byte[] value){
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.WriteCharacteristic,characteristicUUID,value,null,null);
        _CommandQueue.push(cmd);
    }
    /**
     * Reads a characteristic of the configured service
     *
     * @param characteristicUUID The uuid of the chararcteristic
    */
    public void readCharacteristic(UUID characteristicUUID) {
        BLEDeviceCommand cmd = new BLEDeviceCommand(BLEDeviceCommandStates.ReadCharacteristic,characteristicUUID,null,null,null);
        _CommandQueue.push(cmd);
    }

    //private functions
    private BLEDeviceErrors initializeAdapter() {
        _BluetoothManager = (BluetoothManager) _Context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (_BluetoothManager == null) {
            return BLEDeviceErrors.CouldNotInitializeBLEManager;
        } else {
            _BluetoothAdapter = _BluetoothManager.getAdapter();
            if (_BluetoothAdapter == null) {
                return BLEDeviceErrors.CouldNotInitializeBLEAdapter;
            }
        }

        _Scanner = _BluetoothAdapter.getBluetoothLeScanner();
        if(_Scanner == null) {
            return BLEDeviceErrors.CouldNotInitializeBLEScanner;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors create(Context context,String address,String name, UUID service,boolean autoConnect) {
        Log.e("BLEDevice", "create ");
       BLEDeviceErrors result = BLEDeviceErrors.Ok;
        _Context = context;
        _BluetoothDeviceAddress = address;
        _AutoConnect = autoConnect;
        _ServiceUIID = service;
        _Name = name;
        try {
            result = initializeAdapter();
            if(result != BLEDeviceErrors.Ok) {
                return result;
            }
            try {
                if(_BluetoothDeviceAddress == "")
                {
                    Log.e("BLEDevice", "EmptyBLEAddress ");
                    return BLEDeviceErrors.EmptyBLEAddress;
                }
                _BluetoothDevice = _BluetoothAdapter.getRemoteDevice(_BluetoothDeviceAddress);
            }
            catch(Exception exp)
            {
                Log.e("BLEDevice", "getRemoteDevice " + exp.getMessage());
                return BLEDeviceErrors.InvalidBLEAddress;
            }
            if(_BluetoothDevice == null){
                Log.e("BLEDevice", "_BluetoothDevice == null ");
                return BLEDeviceErrors.CouldNotGetDevice;
            }
            _BluetoothGatt = _BluetoothDevice.connectGatt(_Context, true, _GattCallback);
            if(_BluetoothGatt == null)
            {
                Log.e("create BLEDevice", "_BluetoothGatt == null ");
                return BLEDeviceErrors.CouldNotConnectGatt;
            }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "connect " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors connect() {
        Log.e("BLEDevice", "connect() ");
        long timeout = 0;
        try {
            //occurs on invalid BLE address. does not used for scan
            while(_BluetoothGatt == null)
            {
                Log.e("create BLEDevice", "_BluetoothGatt == null ");
                Thread.sleep(100);
                timeout++;
                if(timeout >  10)
                {
                    return BLEDeviceErrors.CouldNotConnectGatt;
                }
            }

            if(!_BluetoothGatt.connect())
            {
                return BLEDeviceErrors.CouldNotConnectDevice;
            }
        }
        catch(SecurityException | InterruptedException exp) {
            Log.e("BLEDevice", "disconnect " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors disconnect() {
        try {
            _BluetoothGatt.disconnect();
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "disconnect " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors writeDesc(UUID characteristicUUID,UUID descriptionUUID,byte[] value)
    {
        if(_Service == null)
        {
            return BLEDeviceErrors.ServiceNotValid;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            return BLEDeviceErrors.CharacteristicNotFound;
        }
        try {

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptionUUID);
            if(descriptor == null)
            {
                return BLEDeviceErrors.DescriptionNotFound;
            }
            descriptor.setValue(value);
            if(!_BluetoothGatt.writeDescriptor(descriptor))
            {
                return BLEDeviceErrors.CouldNotWriteDescription;
            }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "writeDesc " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors readDesc(UUID characteristicUUID,UUID descriptionUUID) {

        if(_Service == null)
        {
            return BLEDeviceErrors.ServiceNotValid;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            return BLEDeviceErrors.CharacteristicNotFound;
        }
        try{
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptionUUID);
            if(descriptor == null)
            {
                return BLEDeviceErrors.DescriptionNotFound;
            }
             if(!_BluetoothGatt.readDescriptor(descriptor)){
                 return BLEDeviceErrors.CouldNotReadDescription;
             }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "readDesc " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors writeCharacter(UUID characteristicUUID,byte[] value)
    {
        if(_Service == null)
        {
            return BLEDeviceErrors.ServiceNotValid;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            return BLEDeviceErrors.CharacteristicNotFound;
        }
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        characteristic.setValue(value);
        try{
            if(!_BluetoothGatt.writeCharacteristic(characteristic))
            {
                return BLEDeviceErrors.CouldNotWriteCharacteristic;
            }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "writeCharacter " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors readCharacter(UUID characteristicUUID) {
        if(_Service == null)
        {
            return BLEDeviceErrors.ServiceNotValid;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            return BLEDeviceErrors.CharacteristicNotFound;
        }
       try{
           if(!_BluetoothGatt.readCharacteristic(characteristic)){
               return BLEDeviceErrors.CouldNotReadCharacteristic;
           }
       }
       catch(SecurityException exp) {
           Log.e("BLEDevice", "readCharacter " + exp.getMessage());
           return BLEDeviceErrors.PermissionError;
       }
        return BLEDeviceErrors.Ok;
    }
    private BLEDeviceErrors discoverServices()
    {
        try {
           if(!_BluetoothGatt.discoverServices()){
               return BLEDeviceErrors.CouldNotDiscoverServices;
           }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "discoverServices " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors getService()
    {
        try {
            _Service =_BluetoothGatt.getService(_ServiceUIID);
            if(_Service == null)
            {
                return BLEDeviceErrors.ServiceNotFound;
            }
        }
        catch(SecurityException exp) {
            Log.e("BLEDevice", "discoverServices " + exp.getMessage());
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }
    private BLEDeviceErrors setNotificationEvent(UUID characteristicUUID)
    {
        if(_Service == null)
        {
            return BLEDeviceErrors.ServiceNotValid;
        }
        BluetoothGattCharacteristic characteristic = _Service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            return BLEDeviceErrors.CharacteristicNotFound;
        }
        try {
            if(!_BluetoothGatt.setCharacteristicNotification(characteristic, true))
            {
                return BLEDeviceErrors.CouldNotEnableNotification;
            }
        }
             catch(SecurityException exp){
                return BLEDeviceErrors.PermissionError;
            }
            return BLEDeviceErrors.Ok;

    }
    private BLEDeviceErrors enableNotification(NotificationParameter param)
    {
        BLEDeviceErrors result = BLEDeviceErrors.Ok;
        Log.e("BLEDevice", "enableNotification : " + param.Uuid.toString());
        result = setNotificationEvent(param.Uuid);
        if(result != BLEDeviceErrors.Ok)
        {
            return result;
        }
        if(param.Type == NotificationType.Notification) {
            if (writeDesc(param.Uuid, CCCD, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) != BLEDeviceErrors.Ok) {
                return BLEDeviceErrors.CouldNotEnableNotification;
            }
        }else if(param.Type == NotificationType.Indication) {
            if (writeDesc(param.Uuid, CCCD, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE) != BLEDeviceErrors.Ok) {
                return BLEDeviceErrors.CouldNotEnableNotification;
            }
        }
        else
        {
            return BLEDeviceErrors.CouldNotEnableNotification;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors enableNotificationFromQueue()
    {
        BLEDeviceErrors result = BLEDeviceErrors.Ok;
        if(_NotificationListCounter >= _NotificationCharacteristics.size())
        {
            _NotificationListCounter = 0;
            return BLEDeviceErrors.Ok;
        }
        NotificationParameter ch = _NotificationCharacteristics.get(_NotificationListCounter);
        _NotificationListCounter++;
        result = enableNotification(ch);
        if(result == BLEDeviceErrors.Ok)
        {
            result =  BLEDeviceErrors.NotificationInQueue;
        }
       return result;
    }

    private BLEDeviceErrors startScan()
    {
        if (_Scanner == null) {
            return BLEDeviceErrors.NoScannerFound;
        }
        try {

            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setDeviceName(_Name)
                    .build();
            filters.add(filter);
            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .build();
            _Scanner.startScan(filters, scanSettings, _LeScanCallback);
        }
        catch(SecurityException exp){
            return BLEDeviceErrors.PermissionError;
        }
        return BLEDeviceErrors.Ok;
    }

    private BLEDeviceErrors stopScan()
    {
        try {
            _Scanner.stopScan(_LeScanCallback);
        }
         catch(SecurityException exp){
                return BLEDeviceErrors.PermissionError;
            }
            return BLEDeviceErrors.Ok;
    }

    private boolean StateMachine() {
        BLEDeviceErrors result = BLEDeviceErrors.Ok;
        switch (_CommandState) {
            case Idle: {
                if(_CommandQueue.size() > 0)
                {
                    synchronized (this) {
                        _ActualCommand = _CommandQueue.poll();
                        _CommandState = _ActualCommand.Command;
                    }
                }
                else{
                    if(_AutoConnectActive){
                            _CommandState = BLEDeviceCommandStates.Connect;
                        }
                }
                break;
            }
            case Initialize: {
                _Error = create(
                        (Context) _ActualCommand.Param1,
                        (String)_ActualCommand.Param2,
                        (String)_ActualCommand.Param3,
                        (UUID) _ActualCommand.Param4,
                        (boolean) _ActualCommand.Param5);
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Disconnected;
                break;
            }
            case Close:
            {
                return true;
            }
            case Connect: {
                if(_State == BLEDeviceStates.Connected)
                {
                    _Error = BLEDeviceErrors.AlreadyConnected;
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _Error = connect();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Connecting;
                break;
            }
            case Connecting:
            {
                if(_ConnectedFlag)
                {
                    _ConnectedFlag = false;
                    _CommandState = BLEDeviceCommandStates.Connected;
                    break;
                }
                if(_ConnectTimeout.check(1000))
                {
                    if(_AutoConnect)
                    {
                        _AutoConnectActive = true;
                        _CommandState = BLEDeviceCommandStates.Idle;
                        break;
                    }
                    else
                    {
                        _Error = BLEDeviceErrors.ConnectTimeout;
                        _CommandState = BLEDeviceCommandStates.Error;
                    }
                }
                break;
            }
            case Connected: {
                _ConnectedFlag = false;
                _AutoConnectActive = false;
                _CommandState = BLEDeviceCommandStates.DiscoverServices;
                Log.e("BLEDevice", "Connected ");
                break;
            }
            case Disconnect: {
                if(_State == BLEDeviceStates.Disconnected)
                {
                    _Error = BLEDeviceErrors.AlreadyDisconnected;
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _Error = disconnect();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Disconnecting;
                break;
            }
            case Disconnecting:
            {
                break;
            }
            case DisconnectedThruClient: {
                _State = BLEDeviceStates.Disconnected;
                if(_AutoConnect)
                {
                    if(_RestartTimeOut.check(_RESTARTTIMEOUT))
                    {
                        Log.d("BLEDevice", "Restart ");
                        _CommandState = BLEDeviceCommandStates.Connect;
                    }
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Disconnected;
                break;
            }
            case Disconnected: {
               _State = BLEDeviceStates.Disconnected;
                if(_ScanActive)
                {
                    _CommandState = BLEDeviceCommandStates.ScanDevicesStart;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Idle;
                break;
            }
            case DiscoverServices: {
                _Error = discoverServices();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.DiscoveringServices;
                break;
            }
            case DiscoveringServices:
            {
                if(_ConnectTimeout.check(5000))
                {
                    _Error = BLEDeviceErrors.ServiceDiscoveryTimeout;
                    _CommandState = BLEDeviceCommandStates.Error;
                }
                break;
            }
            case DiscoverServicesFinish: {
                _Error = getService();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.EnableNotificiction;
                break;
            }
            case EnableNotificiction: {
                _Error = enableNotificationFromQueue();
                if(_Error == BLEDeviceErrors.Ok)
                {
                    _CommandState = BLEDeviceCommandStates.Ready;
                    break;
                }
                if(_Error == BLEDeviceErrors.CouldNotEnableNotification){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.EnablingNotification;
                break;
            }
            case EnablingNotification:
            {
                break;
            }
            case EnableNotificictionFinish: {
                _CommandState = BLEDeviceCommandStates.Ready;
                break;
            }
            case Ready: {
                _State = BLEDeviceStates.Connected;
                _CommandState = BLEDeviceCommandStates.Idle;
                if(_Listener != null){
                    _Listener.OnStatusChanged(_State);
                }
                break;
            }
            case ReadCharacteristic: {
                if(_State == BLEDeviceStates.Disconnected)
                {
                    _Error = BLEDeviceErrors.NotConnected;
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _Error = readCharacter((UUID)_ActualCommand.Param1);
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.ReadingCharacteristic;
                break;
            }
            case ReadingCharacteristic:
            {
                break;
            }
            case ReadDescriptor: {
                if(_State == BLEDeviceStates.Disconnected)
                {
                    _Error = BLEDeviceErrors.NotConnected;
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _Error = readDesc((UUID)_ActualCommand.Param1,(UUID)_ActualCommand.Param2);
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.ReadingDescriptor;
                break;
            }
            case ReadingDescriptor:
            {
                break;
            }
            case WriteCharacteristic: {
                if(_State == BLEDeviceStates.Disconnected)
                {
                    _Error = BLEDeviceErrors.NotConnected;
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                Log.d("BLEDevice", "writeCharacter : ");
                _Error = writeCharacter((UUID)_ActualCommand.Param1,(byte[])_ActualCommand.Param2);
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.WritingCharacteristic;
                break;
            }
            case WritingCharacteristic:
            {
                break;
            }
            case WriteDescriptor: {
                if(_State == BLEDeviceStates.Disconnected)
                {
                    _Error = BLEDeviceErrors.NotConnected;
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _Error = writeDesc((UUID)_ActualCommand.Param1,(UUID)_ActualCommand.Param2,(byte[])_ActualCommand.Param3);
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.WritingDescriptor;
                break;
            }
            case WritingDescriptor:
            {
                break;
            }
            case ScanDevices:
            {
                _ScanActive = true;
                _LastConnectedState = _State;
                if(_State == BLEDeviceStates.Connected)
                {
                    _CommandState = BLEDeviceCommandStates.Disconnect;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.ScanDevicesStart;
                break;
            }
            case ScanDevicesStart:
            {
                _Error = startScan();
                if(_Error != BLEDeviceErrors.Ok){
                    _CommandState = BLEDeviceCommandStates.Error;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.ScanningDevices;
                break;
            }
            case ScanningDevices:
            {
                if(_ScanTimeOut.check(5000))
                {
                    stopScan();
                    _CommandState = BLEDeviceCommandStates.ScanDevicesFinished;
                    break;
                }
                break;
            }
            case ScanDevicesFinished:
            {
                _ScanActive = false;
                if(_Listener != null){
                    _Listener.onScanResult(_ScannedDeviceList,_State);
                }
                if(_LastConnectedState == BLEDeviceStates.Connected)
                {
                    _CommandState = BLEDeviceCommandStates.Connect;
                    break;
                }
                _CommandState = BLEDeviceCommandStates.Idle;
                break;
            }
            case Error:{
                Log.e("BLEDevice", "Error " + _Error.name());
                if(_Listener != null){
                    _Listener.onError(_Error);
                }
                _Error = BLEDeviceErrors.Ok;
                _CommandState = BLEDeviceCommandStates.Idle;
                break;
            }
            default:{
                _Error = BLEDeviceErrors.UnkownCommand;
                Log.e("BLEDevice", "UnkownCommand " + _CommandState.name());
                _CommandState = BLEDeviceCommandStates.Error;

                break;
            }
        }//switch Command
        return false;
    }

    @Override
    public void close()
    {
        closeDevice();
    }
    @Override
    public void run(){
        while (true) {
            if(StateMachine())
            {
               return;
            }
            if(_LastCommandState != _CommandState){
                Log.e("BLEDevice", "StateChanged " + _CommandState.name());

                _LastCommandState = _CommandState;
            }
        }
    }

    //callBacks
    private final BluetoothGattCallback _GattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
           if(newState == BluetoothProfile.STATE_CONNECTED){
               _ConnectedFlag = true;
           }
           else
           {
              if(_CommandState == BLEDeviceCommandStates.Disconnecting) {
                   _CommandState = BLEDeviceCommandStates.Disconnected;
               }
               else
               {
                   _CommandState = BLEDeviceCommandStates.DisconnectedThruClient;
               }
               _State = BLEDeviceStates.Disconnected;
               if(_Listener != null)
               {
                   _Listener.OnStatusChanged(_State);
               }
           }



        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(_CommandState == BLEDeviceCommandStates.EnablingNotification)
                {
                    _Error = enableNotificationFromQueue();
                    if(_Error == BLEDeviceErrors.Ok){
                        _CommandState = BLEDeviceCommandStates.EnableNotificictionFinish;
                    }
                    else if(_Error == BLEDeviceErrors.NotificationInQueue){
                        Log.e("BLEDevice", "NotificationInQueue : ");
                    }
                    else{
                        _CommandState = BLEDeviceCommandStates.Error;
                    }
                }
                else {
                    _CommandState = BLEDeviceCommandStates.Idle;
                }
            } else {
                Log.e("BLEDevice", "onDescriptorWrite : " + status);
                _CommandState = BLEDeviceCommandStates.Error;
            }
        }
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _CommandState = BLEDeviceCommandStates.Idle;
            } else {
                Log.e("BLEDevice", "onDescriptorRead : " + status);
                _CommandState = BLEDeviceCommandStates.Error;
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _CommandState = BLEDeviceCommandStates.Idle;
            } else {
                Log.e("BLEDevice", "onCharacteristicWrite : " + status);
                _CommandState = BLEDeviceCommandStates.Error;
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         byte[] value,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _CommandState = BLEDeviceCommandStates.ReadCharacteristicFinished;
            } else {
                Log.e("BLEDevice", "onCharacteristicRead : " + status);
                _CommandState = BLEDeviceCommandStates.Error;
            }
            if(_Listener != null)
            {
                _Listener.onCharacteristicRead(characteristic.getUuid(),value);
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic,byte[] value) {
            if(_Listener != null)
            {
                _Listener.onCharacteristicNotification(characteristic.getUuid(),value);
            }

        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if(_Listener != null)
            {
                _Listener.onCharacteristicNotification(characteristic.getUuid(),characteristic.getValue());
            }

        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _CommandState = BLEDeviceCommandStates.DiscoverServicesFinish;
            } else {
                Log.e("BLEDevice", "onServicesDiscovered : " + status);
                _CommandState = BLEDeviceCommandStates.Error;
            }
        }
    };

    private ScanCallback _LeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if(result != null)
                    {
                        String deviceName = "";
                        String deviceAddress = "";

                        BLEScannedDevice dev = new BLEScannedDevice();
                        try
                        {
                            dev.Address = result.getDevice().getAddress();
                            dev.Name = result.getDevice().getName();
                        }
                        catch(SecurityException exp)
                        {
                            Log.i("BLEAdapter", " _LeScanCallback" + exp.getMessage());
                            _Error = BLEDeviceErrors.ScanningFailed;
                            _CommandState = BLEDeviceCommandStates.Error;
                        }

                        if(!_ScannedDeviceList.contains(dev) && dev.Name != null)
                        {
                            Log.i("BLEAdapter", " Scan ." + deviceName);
                            _ScannedDeviceList.add(dev);
                        }

                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);

                }

                @Override
                public void onScanFailed(int errorCode)
                {
                    super.onScanFailed(errorCode);
                    _Error = BLEDeviceErrors.ScanningFailed;
                    _CommandState = BLEDeviceCommandStates.Error;
                }
            };



}
