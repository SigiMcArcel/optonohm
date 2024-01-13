package Optonohm.products.optonohmapp.mcu.mcu;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.UUID;

import Optonohm.products.optonohmapp.mcu.BLE.BLEDevice;

public class OptonohmControlPoint {
    public enum  OptonohmControlPointOpCodes
    {
        None,
        SetConfig,
        GetConfig,
        Start,
        Stop,
        Error
    };

    public enum  OptonohmControlPointResultCodes
    {
        Reserved,
        Success,
        NotSupported,
        InvalidParameter,
        OperationFailed
    };

    public interface OnOptonohmControlPointListener {
        void onResponse(OptonohmControlPointOpCodes opCode,OptonohmControlPointResultCodes result, ByteBuffer parameter);
        void onError(OptonohmControlPointOpCodes opCode,OptonohmControlPointResultCodes result);
        void onGetConfig(OptonohmConfiguration conf,OptonohmControlPointResultCodes result);
        void onEmpty();
    }

    BLEDevice _Device = null;
    UUID _UUID = null;
    LinkedList<OptonohmControlPointOpCodes> PendingRequests = new LinkedList<>() ;

    private OnOptonohmControlPointListener _Listener = null;

    public OptonohmControlPoint(BLEDevice device, UUID uuid)
    {
        _Device = device;
        _UUID = uuid;
    }

   void setOnOptonohmControlPointListener(OnOptonohmControlPointListener listener)
   {
       _Listener = listener;
   }

    public ByteBuffer ConfigToBuffer(OptonohmConfiguration conf)
    {
        ByteBuffer buffer = ByteBuffer.allocate(17);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putChar((char)conf.Version);
        buffer.putChar((char)conf.BMP);
        buffer.put(conf.BeatDivisor);
        buffer.put(conf.BeatMultiplicator);
        buffer.put(conf.BeatPulseTimeMs);
        buffer.put(conf.BeatColor.Red);
        buffer.put(conf.BeatColor.Green);
        buffer.put(conf.BeatColor.Blue);
        buffer.put(conf.BeatBrightness);
        buffer.put(conf.StartBeatPulseTimeMs);
        buffer.put(conf.StartBeatColor.Red);
        buffer.put(conf.StartBeatColor.Green);
        buffer.put(conf.StartBeatColor.Blue);
        buffer.put(conf.StartBeatBrightness);
        buffer.put(conf.State);
        return buffer;
    }

    public OptonohmConfiguration BufferToConfig(ByteBuffer buffer)
    {
        OptonohmConfiguration conf = new OptonohmConfiguration();

        conf.Version = (int)buffer.getChar(2);
        conf.BMP = (int)buffer.getChar(4);
        conf.BeatDivisor = buffer.get(6);
        conf.BeatMultiplicator = buffer.get(7);
        conf.BeatPulseTimeMs = buffer.get(8);
        conf.BeatColor.Red = buffer.get(9);
        conf.BeatColor.Green = buffer.get(10);
        conf.BeatColor.Blue = buffer.get(11);
        conf.BeatBrightness = buffer.get(12);
        conf.StartBeatPulseTimeMs = buffer.get(13);
        conf.StartBeatColor.Red  = buffer.get(14);
        conf.StartBeatColor.Green  = buffer.get(15);
        conf.StartBeatColor.Blue = buffer.get(16);
        conf.StartBeatBrightness = buffer.get(17);
        conf.State = buffer.get(18);


        return conf;
    }

    public void sendRequest(OptonohmControlPointOpCodes opCode,ByteBuffer data )
    {
        ByteBuffer buffer = ByteBuffer.allocate(data.capacity() + 2);
        buffer.put((byte)opCode.ordinal());
        buffer.put((byte)0);
        buffer.put(data);
        PendingRequests.add(opCode);
        _Device.writeCharacteristic(_UUID,buffer.array());
    }
    public void Start()
    {
        ByteBuffer buffer = ConfigToBuffer(new OptonohmConfiguration());
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        sendRequest(OptonohmControlPointOpCodes.Start,buffer);
    }

    public void Stop()
    {
        ByteBuffer buffer = ConfigToBuffer(new OptonohmConfiguration());
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        sendRequest(OptonohmControlPointOpCodes.Stop,buffer);
    }

    public void setConfiguration(OptonohmConfiguration conf)
    {
        ByteBuffer buffer = ConfigToBuffer(conf);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        sendRequest(OptonohmControlPointOpCodes.SetConfig,buffer);
    }

    public void getConfiguration()
    {
        ByteBuffer buffer = ConfigToBuffer(new OptonohmConfiguration());
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        sendRequest(OptonohmControlPointOpCodes.GetConfig,buffer);
    }



    public void processResults(byte[] data)
    {
        Log.d("OnOptonohmTronicControlPointListener","processResults ");
        ByteBuffer buffer = ByteBuffer.allocate(19);//
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        /*if(PendingRequests.isEmpty())
        {
            _Listener.onError(OptonohmControlPointOpCodes.None,OptonohmControlPointResultCodes.OperationFailed);
            return;
        }*/

        if (data.length != 19) {
            if(_Listener != null) {
                _Listener.onError(OptonohmControlPointOpCodes.None,OptonohmControlPointResultCodes.OperationFailed);
                return;
            }
        }
        buffer.put(data);
        OptonohmControlPoint.OptonohmControlPointResultCodes OpResultCode = OptonohmControlPoint.OptonohmControlPointResultCodes.values()[data[1]];
        OptonohmControlPoint.OptonohmControlPointOpCodes OpRequestCode = OptonohmControlPoint.OptonohmControlPointOpCodes.values()[data[0]];


        if(OpResultCode != OptonohmControlPoint.OptonohmControlPointResultCodes.Success)
        {

            if(_Listener != null) {
                _Listener.onError(OpRequestCode, OpResultCode);
            }
            Log.e("OnCipedTronicControlPointListener",OpRequestCode.name());
            return;
        }
        if(PendingRequests.contains(OpRequestCode)) {
            PendingRequests.remove(OpRequestCode);
            Log.d("OnCipedTronicControlPointListener","PendingRequests.remove " + OpRequestCode.name());
        }


        switch(OpRequestCode)
        {
            case Start:
            {
                break;
            }
            case Stop:
            {
                break;
            }
            case SetConfig:
            {
                break;
            }

            case GetConfig:
            {
                OptonohmConfiguration conf = BufferToConfig(buffer);
                if(_Listener != null) {
                    _Listener.onGetConfig(conf,OpResultCode);
                }

                break;
            }
            default:
            {
                if(_Listener != null) {
                    _Listener.onError(PendingRequests.poll(), OpResultCode);
                }
                Log.e("OnCipedTronicControlPointListener", OptonohmControlPoint.OptonohmControlPointResultCodes.OperationFailed.name());
                break;
            }
        }
        if(PendingRequests.isEmpty())
        {
            if(_Listener != null) {
                _Listener.onEmpty();
            }
        }
    }
}
