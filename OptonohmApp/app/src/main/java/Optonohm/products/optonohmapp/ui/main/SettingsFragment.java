package Optonohm.products.optonohmapp.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.optonohmapp.R;
import com.example.optonohmapp.databinding.FragmentDataBinding;
import Optonohm.products.optonohmapp.mcu.mcu.OptonohmConfiguration;

public class SettingsFragment extends PreferenceFragmentCompat {

    private PageViewModel _VModel;
    private FragmentDataBinding binding;
    View _RootView;
    EditTextPreference _BeatPulseTimePref;
    EditTextPreference _BeatBrightnessPref;
    ColorPreferenceCompat _BeatColorPref;
    EditTextPreference _StartBeatPulseTimePref;
    EditTextPreference _StartBeatBrightnessPref;
    ColorPreferenceCompat _StartBeatColorPref;
    EditTextPreference _BluetoothAddressPref;
    Preference _SynchronizePref;


    String _RootKey;
    SynchronizeDialog _Dialog = new SynchronizeDialog();
    OptonohmConfiguration _Config = new OptonohmConfiguration();


    void UploadConfiguration(){
        _VModel.readConfiguration();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        _RootKey = rootKey;
        setPreferencesFromResource(R.xml.root_preferences, _RootKey);

        _BluetoothAddressPref =  (EditTextPreference) findPreference("bluetooth_Address");
        if(_BluetoothAddressPref != null)
        {
            _BluetoothAddressPref.setEnabled(false);
        }

        _BeatPulseTimePref = (EditTextPreference) findPreference("beat_pulse_time");
        if (_BeatPulseTimePref != null) {
            _BeatPulseTimePref.setOnBindEditTextListener(( EditText editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_VARIATION_NORMAL));
            _BeatPulseTimePref.setOnPreferenceChangeListener(_OnBeatPulseTimePref);
        }

        _BeatBrightnessPref = (EditTextPreference) findPreference("beat_brightness");
        if (_BeatBrightnessPref != null) {
            _BeatBrightnessPref.setOnBindEditTextListener(( EditText editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_FLAG_DECIMAL) );
            _BeatBrightnessPref.setOnPreferenceChangeListener(_OnBeatBrightnessPref);
        }

        _BeatColorPref = (ColorPreferenceCompat) findPreference("beat_color");
        if (_BeatColorPref != null) {
            _BeatColorPref.setOnPreferenceChangeListener(_OnBeatColorPref);
        }

        _StartBeatPulseTimePref = (EditTextPreference) findPreference("start_beat_brightness");
        if (_StartBeatPulseTimePref != null) {
            _StartBeatPulseTimePref.setOnBindEditTextListener(( EditText editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_VARIATION_NORMAL));
            _StartBeatPulseTimePref.setOnPreferenceChangeListener(_OnStartBeatPulseTimePref);
        }

        _StartBeatBrightnessPref = (EditTextPreference) findPreference("start_beat_pulse_time");
        if (_StartBeatBrightnessPref != null) {
            _StartBeatBrightnessPref.setOnBindEditTextListener(( EditText editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_FLAG_DECIMAL) );
            _StartBeatBrightnessPref.setOnPreferenceChangeListener(_OnStartBeatBrightnessPref);
        }

        _StartBeatColorPref = (ColorPreferenceCompat) findPreference("start_beat_color");
        if (_StartBeatColorPref != null) {
            _StartBeatColorPref.setOnPreferenceChangeListener(_OnStartBeatColorPref);
        }


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);
        _RootView = view;
    }


    Preference.OnPreferenceChangeListener _OnBeatPulseTimePref = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {



           return true;
        }
    };

    Preference.OnPreferenceChangeListener _OnBeatBrightnessPref = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {



            return true;
        }
    };
    Preference.OnPreferenceChangeListener _OnBeatColorPref = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {



            return true;
        }
    };

    Preference.OnPreferenceChangeListener _OnStartBeatPulseTimePref = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {



            return true;
        }
    };

    Preference.OnPreferenceChangeListener _OnStartBeatBrightnessPref = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {



            return true;
        }
    };


    Preference.OnPreferenceChangeListener _OnStartBeatColorPref = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {



            return true;
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("SettingsFragment", "onDestroy");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.w("SettingsFragment", "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w("SettingsFragment", "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w("SettingsFragment", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w("SettingsFragment", "onResume");
    }


}