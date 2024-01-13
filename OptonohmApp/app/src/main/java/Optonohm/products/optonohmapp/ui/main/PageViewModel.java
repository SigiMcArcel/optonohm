package Optonohm.products.optonohmapp.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;
import androidx.preference.PreferenceManager;
import androidx.preference.Preference;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;

import Optonohm.products.optonohmapp.mcu.BLE.BLEScannedDevice;
import Optonohm.products.optonohmapp.mcu.mcu.OptonohmConfiguration;
import Optonohm.products.optonohmapp.mcu.mcu.OptonohmMCU;

import java.util.List;

public class PageViewModel extends AndroidViewModel {


    private OptonohmMCU _OptonohmMCU = (OptonohmMCU) OptonohmMCU.getInstance();
    private MutableLiveData<List<BLEScannedDevice>> _Scanresults = new MutableLiveData<>();
    private MutableLiveData<String> _ErrorString = new MutableLiveData<>();
    private MutableLiveData<String> _StateString = new MutableLiveData<>();

    private MutableLiveData<OptonohmConfiguration> _OptonohmConfiguration = new MutableLiveData<>();
    OptonohmConfiguration _Configuration = new OptonohmConfiguration();
    private Context _AppContext;

    MutableLiveData<OptonohmConfiguration> OptonohmConfiguration() {
        return _OptonohmConfiguration;
    }


    public OptonohmConfiguration getConfFromSettings()
    {
        OptonohmConfiguration conf = new OptonohmConfiguration();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_AppContext);
        conf.StartBeatPulseTimeMs = (byte)Integer.parseInt(prefs.getString("start_beat_pulse_time","50"));
        conf.StartBeatBrightness = (byte)Integer.parseInt(prefs.getString("start_beat_brightness","100"));
        conf.BeatPulseTimeMs = (byte)Integer.parseInt(prefs.getString("beat_pulse_time","50"));
        conf.BeatBrightness = (byte)Integer.parseInt(prefs.getString("beat_brightness","100"));
        int startColor = prefs.getInt("start_beat_color",0xff0000);
        conf.StartBeatColor.setColor(startColor);
        int color = prefs.getInt("beat_color",0xff0000);
        conf.BeatColor.setColor(color);
        conf.BMP = Integer.parseInt(prefs.getString("device_bpm","100"));
        conf.BeatMultiplicator = (byte)Integer.parseInt(prefs.getString("device_mul","100"));
        conf.BeatDivisor = (byte)Integer.parseInt(prefs.getString("device_div","100"));
        return conf;
    }

    public void setConfigurationInSettings(OptonohmConfiguration configuration)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_AppContext);
        SharedPreferences.Editor edt = prefs.edit();
        edt.putString("beat_pulse_time", Integer.toString(configuration.BeatPulseTimeMs));
        edt.apply();
        edt.commit();

        edt.putString("beat_brightness", Integer.toString(configuration.BeatBrightness));
        edt.apply();
        edt.commit();

        edt.putInt("beat_color", configuration.BeatColor.getColorAlpha());
        edt.apply();
        edt.commit();

        edt.putString("start_beat_pulse_time", Integer.toString(configuration.BeatPulseTimeMs));
        edt.apply();
        edt.commit();

        edt.putString("start_beat_brightness", Integer.toString(configuration.StartBeatBrightness));
        edt.apply();
        edt.commit();

        edt.putInt("start_beat_color", configuration.StartBeatColor.getColorAlpha());
        edt.apply();
        edt.commit();

        edt.putString("device_bpm", Integer.toString(configuration.BMP));
        edt.apply();
        edt.commit();

        edt.putString("device_mul", Integer.toString(configuration.BeatMultiplicator));
        edt.apply();
        edt.commit();

        edt.putString("device_div", Integer.toString(configuration.BeatDivisor));
        edt.apply();
        edt.commit();

        edt.putString("device_version", Integer.toString(configuration.Version));
        edt.apply();
        edt.commit();

    }

    public void setBmp(int bpm)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_AppContext);
        SharedPreferences.Editor edt = prefs.edit();
        edt.putString("device_bpm", Integer.toString(bpm));
        edt.apply();
        edt.commit();
    }

    public void setMul(int mul)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_AppContext);
        SharedPreferences.Editor edt = prefs.edit();
        edt.putString("device_mul", Integer.toString(mul));
        edt.apply();
        edt.commit();
    }

    public void setDiv(int div)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_AppContext);
        SharedPreferences.Editor edt = prefs.edit();
        edt.putString("device_div", Integer.toString(div));
        edt.apply();
        edt.commit();
    }


    public PageViewModel(@NonNull Application application)
    {
        super(application);
        _AppContext = application.getApplicationContext();
        _OptonohmMCU.setOnOptonohmDeviceListener(_OnOptonohmDeviceListener);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_AppContext);
        pref.registerOnSharedPreferenceChangeListener(_OnSharedPreferenceChangeListener);
    }

    public OptonohmMCU.OnOptonohmDeviceListener _OnOptonohmDeviceListener = new OptonohmMCU.OnOptonohmDeviceListener() {
        @Override
        public void onStatusChanged(String state) {
            if(state == "Connected")
            {
                _OptonohmMCU.getConfiguration();
            }
            _StateString.postValue(state);
        }

        @Override
        public void onDeviceError(String error) {
            _ErrorString.postValue(error);
        }

        @Override
        public void onScanResult(List<BLEScannedDevice> devices, String state) {
            _Scanresults.postValue(devices);
        }

        @Override
        public void onGetConfiguration(OptonohmConfiguration configuration){
            setConfigurationInSettings(configuration);
            _OptonohmConfiguration.postValue(configuration);
        }
    };

    SharedPreferences.OnSharedPreferenceChangeListener _OnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            _Configuration =  getConfFromSettings();
        }
    };

    public void createDevice(BLEScannedDevice device)
    {
        _OptonohmMCU.createDevice(_AppContext,device.Address);
        _OptonohmMCU.connectDevice();
    }

    //device controls
    public void readConfiguration()
    {
        _OptonohmMCU.getConfiguration();
    }
    public void writeConfiguration()
    {
        _OptonohmMCU.setConfiguration(_Configuration);
    }
    public void Start(){_OptonohmMCU.Start();};
    public void Stop(){_OptonohmMCU.Stop();};
    public void disconnectDevice()
    {
        _OptonohmMCU.close();
    }
    public void scanDevices()
    {
        _OptonohmMCU.scanDevices();
    }

    public LiveData<List<BLEScannedDevice>> getScanResults() {
        return _Scanresults;
    }
    public LiveData<String> getError() {
        return _ErrorString;
    }
    public LiveData<String> getState() {
        return _StateString;
    }



}