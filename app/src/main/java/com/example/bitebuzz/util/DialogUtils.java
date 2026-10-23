package com.example.bitebuzz.util;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.example.bitebuzz.R;
import com.example.bitebuzz.databinding.DialogAboutBinding;
import com.example.bitebuzz.databinding.DialogInputBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Reusable dialogs so screens share the same friendly style.
 */
public final class DialogUtils {

    private DialogUtils() {
    }

    public interface OnTextSubmitted {
        void onTextSubmitted(String text);
    }

    public static void showInputDialog(Activity activity,
                                       @StringRes int titleRes,
                                       @StringRes int hintRes,
                                       String initialValue,
                                       @StringRes int emptyErrorRes,
                                       OnTextSubmitted callback) {
        DialogInputBinding binding = DialogInputBinding.inflate(LayoutInflater.from(activity));
        TextInputLayout inputLayout = binding.getRoot();
        inputLayout.setHint(activity.getString(hintRes));
        binding.dialogInputEditText.setText(initialValue == null ? "" : initialValue);
        if (!TextUtils.isEmpty(initialValue)) {
            binding.dialogInputEditText.setSelection(initialValue.length());
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(titleRes)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String value = binding.dialogInputEditText.getText() == null
                            ? ""
                            : binding.dialogInputEditText.getText().toString().trim();
                    if (value.isEmpty()) {
                        inputLayout.setError(activity.getString(emptyErrorRes));
                        return;
                    }
                    inputLayout.setError(null);
                    dialog.dismiss();
                    callback.onTextSubmitted(value);
                }));

        dialog.show();
        binding.dialogInputEditText.requestFocus();
    }

    public static void showAboutDialog(Activity activity) {
        DialogAboutBinding binding = DialogAboutBinding.inflate(LayoutInflater.from(activity));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.close, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showInfoDialog(Activity activity,
                                      @StringRes int titleRes,
                                      @StringRes int messageRes) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setPositiveButton(R.string.close, null)
                .show();
    }

    public static void showMessageDialog(Activity activity, CharSequence title, CharSequence message) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.close, null)
                .show();
    }
}
