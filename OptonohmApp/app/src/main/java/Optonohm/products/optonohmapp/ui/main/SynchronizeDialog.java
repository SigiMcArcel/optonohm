package Optonohm.products.optonohmapp.ui.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.example.optonohmapp.databinding.FragmentSynchronizeDialogBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.optonohmapp.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SynchronizeDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SynchronizeDialog extends DialogFragment{

    public enum SynchronizeDialogResult
    {
        Upload,
        Download,
        Cancel
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sync");
        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(_Listener != null)
                {
                    _Listener.onFinishDialog(SynchronizeDialogResult.Upload);
                }
            }
        });
        builder.setNeutralButton("Download", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(_Listener != null)
                {
                    _Listener.onFinishDialog(SynchronizeDialogResult.Download);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(_Listener != null)
                {
                    _Listener.onFinishDialog(SynchronizeDialogResult.Cancel);
                }
            }
        });
        return builder.create();
    };



    public interface SynchronizeDialogListener {
        void onFinishDialog(SynchronizeDialogResult result);
    }
    private FragmentSynchronizeDialogBinding binding;
    private SynchronizeDialogListener _Listener;


    public SynchronizeDialog() {

    }

    public static SynchronizeDialog newInstance(SynchronizeDialogListener listener) {
        SynchronizeDialog fragment = new SynchronizeDialog();
        Bundle args = new Bundle();
        return fragment;
    }

    public void setOnSynchronizeDialogListener(SynchronizeDialogListener listener)
    {
        _Listener = listener;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_synchronize_dialog, null,false);

        setCancelable(true);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_synchronize_dialog, container, false);
    }
}