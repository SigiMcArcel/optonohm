package Optonohm.products.optonohmapp.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.example.optonohmapp.R;
import com.example.optonohmapp.databinding.FragmentDataBinding;
import com.google.android.material.snackbar.Snackbar;

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import Optonohm.products.optonohmapp.mcu.mcu.OptonohmConfiguration;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataFragment extends Fragment {
    private FragmentDataBinding binding;

    private NumberPicker numberPickerBMP;
    private NumberPicker numberPickerMul;
    private NumberPicker numberPickerDiv;
    private PageViewModel _VModel;
    private boolean _Started = false;


    private OptonohmConfiguration _OptonohmConfiguration = new OptonohmConfiguration();

    public DataFragment() {
        // Required empty public constructor
    }

    public static DataFragment newInstance() {
        DataFragment fragment = new DataFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    private void setConnect(boolean connected)
    {
        if(connected )
        {
            binding.buttonStartStop.setEnabled(true);
            binding.buttonSync.setEnabled(true);
        }
        else
        {
            binding.buttonStartStop.setEnabled(false);
            binding.buttonSync.setEnabled(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("DataFragment", "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String[] bpms = new String [30];
        String[] div = new String [2];
        Log.w("DataFragment", "onCreateView");

        Context hostActivity = getActivity();

        binding = FragmentDataBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        numberPickerBMP =  binding.NumberPickerBPM;
        numberPickerMul = binding.NumberPickerMul;
        numberPickerDiv = binding.NumberPickerDiv;
        for(int i = 0;i < 30;i++)
        {
            bpms[i] = Integer.toString((i + 1) * 10);
        }
        numberPickerBMP.setMaxValue(29);
        numberPickerBMP.setMinValue(0);
        numberPickerBMP.setDisplayedValues(bpms);

        numberPickerMul.setMaxValue(8);
        numberPickerMul.setMinValue(1);
        div[0] = "4";
        div[1] = "8";
        numberPickerDiv.setDisplayedValues(div);
        numberPickerDiv.setMaxValue(2);
        numberPickerDiv.setMinValue(1);

        numberPickerBMP.setOnScrollListener(_OnScrollListener);
        numberPickerMul.setOnScrollListener(_OnScrollListener);
        numberPickerDiv.setOnScrollListener(_OnScrollListener);
        setConnect(false);

        binding.buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!_Started) {
                    _Started = true;
                    binding.buttonStartStop.setText("Stop");
                    _VModel.Start();
                }
                else
                {
                    _Started = false;
                    binding.buttonStartStop.setText("Start");
                    _VModel.Stop();
                }
            }
        });

        binding.buttonSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _VModel.writeConfiguration();
            }
        });
        _VModel = new ViewModelProvider(getActivity()).get(PageViewModel.class);

        _VModel.getState().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String state) {
                if(state.equals("Connected"))
                {

                    setConnect(true);
                }
               else{
                    setConnect(false);
                }
            }
        });
        _VModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Snackbar.make(root, "Error " + s, Snackbar.LENGTH_LONG)
                        .setAction("Action " + s, null).show();
            }
        });

        _VModel.OptonohmConfiguration().observe(getViewLifecycleOwner(), new Observer<OptonohmConfiguration>() {
            @Override
            public void onChanged(OptonohmConfiguration s) {
                numberPickerBMP.setValue((s.BMP/10) - 1);
                if(s.BeatDivisor == 4)
                {
                    numberPickerDiv.setValue(1);
                }
                else
                {
                    numberPickerDiv.setValue(2);
                }
                numberPickerMul.setValue(s.BeatMultiplicator);

                if(s.State == 1)
                {
                    _Started = true;
                    binding.buttonStartStop.setText("Stop");
                }
                else
                {
                    _Started = false;
                    binding.buttonStartStop.setText("Start");
                }
            }
        });

        // Inflate the layout for this fragment
        return root;
    }

    NumberPicker.OnScrollListener _OnScrollListener = new NumberPicker.OnScrollListener() {
        @Override
        public void onScrollStateChange(NumberPicker view, int scrollState) {

            if(scrollState == 0)
            {
                Log.d("picker onScrollStateChange id ", view.getId() + " " + numberPickerBMP.getId());
                if(view.getId() == numberPickerBMP.getId())
                {
                    Log.d("picker numberPickerBMP id ", view.getId() + " " + numberPickerBMP.getId());

                    _VModel.setBmp(view.getValue() * 10);
                }else if(view.getId() == numberPickerMul.getId())
                {
                    Log.d("picker numberPickerMul id ", view.getId() + " " + numberPickerMul.getId());
                    _VModel.setMul(view.getValue());
                }
                else if(view.getId() == numberPickerDiv.getId())
                {
                    Log.d("picker numberPickerMul id ", view.getId() + " " + numberPickerDiv.getId());
                    if(view.getValue() == 1)
                    {
                        _VModel.setDiv(4);
                    }
                    else {
                        _VModel.setDiv(8);
                    }
                }
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("DataFragment", "onDestroy");
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w("DataFragment", "onStop");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w("DataFragment", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w("DataFragment", "onResume");
    }
}