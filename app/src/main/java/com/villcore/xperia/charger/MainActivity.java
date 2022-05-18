package com.villcore.xperia.charger;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String SMART_CHARGER_RECEIVER = "com.sonymobile.smartcharger.GE_CHARGE";
    private static final String SMART_CHARGER_RECEIVER_PERMISSION = "com.sonymobile.smartcharger.permission.GE_CHARGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button suspendChargeButton = this.findViewById(R.id.suspend_charge);
        suspendChargeButton.setOnClickListener(v -> setSuspendCharge(true));

        Button continueChargeButton = this.findViewById(R.id.continue_charge);
        continueChargeButton.setOnClickListener(v -> setSuspendCharge(false));
    }

    private void setSuspendCharge(boolean suspendState) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su"});
            OutputStream out = process.getOutputStream();
            String cmd = String.format("am broadcast -a %s --ez SUSPEND %s --receiver-permission %s", SMART_CHARGER_RECEIVER, suspendState, SMART_CHARGER_RECEIVER_PERMISSION);
            out.write(cmd.getBytes());
            out.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
            return;
        }

//        FIXME: 如果用以下代码逻辑，如果以root身份发送intent呢？
//        Intent intent = new Intent(SMART_CHARGER_RECEIVER);
//        intent.putExtra("SUSPEND", suspendState);
//        MainActivity.this.sendBroadcast(intent, SMART_CHARGER_RECEIVER_PERMISSION);

        if (suspendState) {
            Toast.makeText(MainActivity.this, "停止充电", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "继续充电", Toast.LENGTH_SHORT).show();
        }
    }
}