package com.tonkar.volleyballreferee.ui.rotation;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.rotation.RotationQrSupport;

public class RotationQrScanActivity extends AppCompatActivity {
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    finish();
                    return;
                }
                boolean ok = RotationQrSupport.applyFromQr(this, result.getContents());
                String msg = getString(ok ? R.string.rotation_applied : R.string.rotation_invalid);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                setResult(ok ? RESULT_OK : RESULT_CANCELED);
                finish();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScanOptions options = new ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt(getString(R.string.scan_rotation_prompt))
                .setBeepEnabled(true)
                .setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }
}
