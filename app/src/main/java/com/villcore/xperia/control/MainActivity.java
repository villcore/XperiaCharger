package com.villcore.xperia.control;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String SMART_CHARGER_RECEIVER = "com.sonymobile.smartcharger.GE_CHARGE";
    private static final String SMART_CHARGER_RECEIVER_PERMISSION = "com.sonymobile.smartcharger.permission.GE_CHARGE";
    private static final String TAG = MainActivity.class.getName();

    private enum CpuGovernor {

        Powersave("powersave"),
        Performance("performance"),
        Schedutil("schedutil"),
        Ondemand("ondemand"),
        Unknown("");

        private final String name;
        CpuGovernor(String name) {
            this.name = name;
        }

        public static CpuGovernor nameOf(String cpuGovernor) {
            for (CpuGovernor governor : values()) {
                if (governor.name.equals(cpuGovernor)) {
                    return governor;
                }
            }
            return Unknown;
        }
    }

    private enum CpuFreqLevel {

        _lowest("300000", "710400", "844800"),
        _30("883200", "1171200", "1305600"),
        _50("1075200", "1478400", "1747200"),
        _75("1420800", "1958400", "2265600"),
        _100("1804800", "2419200", "2841600"),
        Unknown("", "", "");

        private final String policy0Freq;
        private final String policy4Freq;
        private final String policy7Freq;

        CpuFreqLevel(String policy0Freq, String policy4Freq, String policy7Freq) {
            this.policy0Freq = policy0Freq;
            this.policy4Freq = policy4Freq;
            this.policy7Freq = policy7Freq;
        }

        public static CpuFreqLevel policyOf(String[] policyFreq) {
            if (policyFreq.length != 3) {
                return Unknown;
            }

            String policy0Freq = policyFreq[0];
            String policy4Freq = policyFreq[1];
            String policy7Freq = policyFreq[2];
            return policyOf(policy0Freq, policy4Freq, policy7Freq);
        }

        public static CpuFreqLevel policyOf(String policy0Freq, String policy4Freq, String policy7Freq) {
            for (CpuFreqLevel level : values()) {
                if (level.policy0Freq.equals(policy0Freq)
                        && level.policy4Freq.equals(policy4Freq)
                        && level.policy7Freq.equals(policy7Freq)) {
                    return level;
                }
            }
            return Unknown;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // control
        Button suspendChargeButton = this.findViewById(R.id.suspend_charge);
        suspendChargeButton.setOnClickListener(v -> setSuspendCharge(true));

        Button continueChargeButton = this.findViewById(R.id.continue_charge);
        continueChargeButton.setOnClickListener(v -> setSuspendCharge(false));

        // image_enhancer
        Button enableImageEnhancerButton = this.findViewById(R.id.enable_image_enhancer);
        enableImageEnhancerButton.setOnClickListener(v -> setVideoEnhancer(true));

        Button disableImageEnhancer = this.findViewById(R.id.disable_image_enhancer);
        disableImageEnhancer.setOnClickListener(v -> setVideoEnhancer(false));

        // ext hdr mode
        Button enableExtHdrButton = this.findViewById(R.id.enable_ext_hdr);
        enableExtHdrButton.setOnClickListener(v -> setExtHdr(true));

        Button disableExtHdrButton = this.findViewById(R.id.disable_ext_hdr);
        disableExtHdrButton.setOnClickListener(v -> setExtHdr(false));

        // cpu freq
        Button minCpuFreqButton = this.findViewById(R.id.min_cpu_freq);
        minCpuFreqButton.setOnClickListener(v -> minCpuFreq());

        Button maxCpuFreqButton = this.findViewById(R.id.max_cpu_freq);
        maxCpuFreqButton.setOnClickListener(v -> maxCpuFreq());

        Button _100CpuFreqButton = this.findViewById(R.id._100_cpu_freq);
        _100CpuFreqButton.setOnClickListener(v -> _100percentCpuFreq());

        Button _75CpuFreqButton = this.findViewById(R.id._75_cpu_freq);
        _75CpuFreqButton.setOnClickListener(v -> _75percentCpuFreq());

        Button _50CpuFreqButton = this.findViewById(R.id._50_cpu_freq);
        _50CpuFreqButton.setOnClickListener(v -> _50percentCpuFreq());

        Button _30CpuFreqButton = this.findViewById(R.id._30_cpu_freq);
        _30CpuFreqButton.setOnClickListener(v -> _30percentCpuFreq());

        RadioGroup cpuPerformanceRadioGroup = this.findViewById(R.id.cpu_performance_level_group);
        refreshCpuPerformanceLevel(cpuPerformanceRadioGroup);

        // cpu governor
        Button setPowersaveCpuGovernorButton = this.findViewById(R.id.cpu_governor_powersave);
        setPowersaveCpuGovernorButton.setOnClickListener(v -> setPowersaveCpuGovernor());

        Button setPerformanceCpuGovernorButton = this.findViewById(R.id.cpu_governor_performance);
        setPerformanceCpuGovernorButton.setOnClickListener(v -> setPerformanceCpuGovernor());

        Button setOndemandCpuGovernorButton = this.findViewById(R.id.cpu_governor_ondemand);
        setOndemandCpuGovernorButton.setOnClickListener(v -> setOndemandCpuGovernor());

        Button setschedutilCpuGovernorButton = this.findViewById(R.id.cpu_governor_schedutil);
        setschedutilCpuGovernorButton.setOnClickListener(v -> setSchedutilCpuGovernor());

        RadioGroup cpuGovernorRadioGroup = this.findViewById(R.id.cpu_governor_group);
        refreshCpuCpuGovernor(cpuGovernorRadioGroup);

        // cpu core
        Button disableCpu0Button = this.findViewById(R.id.disable_cpu_0);
        disableCpu0Button.setOnClickListener(v -> changeCpuState(v, 0));
        refreshCpuStateButtonTextAndColor(disableCpu0Button, 0);

        Button disableCpu1Button = this.findViewById(R.id.disable_cpu_1);
        disableCpu1Button.setOnClickListener(v -> changeCpuState(v, 1));
        refreshCpuStateButtonTextAndColor(disableCpu1Button, 1);

        Button disableCpu2Button = this.findViewById(R.id.disable_cpu_2);
        disableCpu2Button.setOnClickListener(v -> changeCpuState(v, 2));
        refreshCpuStateButtonTextAndColor(disableCpu2Button, 2);

        Button disableCpu3Button = this.findViewById(R.id.disable_cpu_3);
        disableCpu3Button.setOnClickListener(v -> changeCpuState(v, 3));
        refreshCpuStateButtonTextAndColor(disableCpu3Button, 3);

        Button disableCpu4Button = this.findViewById(R.id.disable_cpu_4);
        disableCpu4Button.setOnClickListener(v -> changeCpuState(v, 4));
        refreshCpuStateButtonTextAndColor(disableCpu4Button, 4);

        Button disableCpu5Button = this.findViewById(R.id.disable_cpu_5);
        disableCpu5Button.setOnClickListener(v -> changeCpuState(v, 5));
        refreshCpuStateButtonTextAndColor(disableCpu5Button, 5);

        Button disableCpu6Button = this.findViewById(R.id.disable_cpu_6);
        disableCpu6Button.setOnClickListener(v -> changeCpuState(v, 6));
        refreshCpuStateButtonTextAndColor(disableCpu6Button, 6);

        Button disableCpu7Button = this.findViewById(R.id.disable_cpu_7);
        disableCpu7Button.setOnClickListener(v -> changeCpuState(v, 7));
        refreshCpuStateButtonTextAndColor(disableCpu7Button, 7);
    }

    private void setSuspendCharge(boolean suspendState) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su"});
            OutputStream out = process.getOutputStream();
            String cmd = String.format("am broadcast -a %s --ez SUSPEND %s --receiver-permission %s", SMART_CHARGER_RECEIVER, suspendState, SMART_CHARGER_RECEIVER_PERMISSION);
            out.write(cmd.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
            return;
        }

        if (suspendState) {
            Toast.makeText(MainActivity.this, "停止充电", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "继续充电", Toast.LENGTH_SHORT).show();
        }
    }

    private void setVideoEnhancer(boolean enableMode) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su"});
            OutputStream out = process.getOutputStream();
            String cmd = String.format("setprop persist.xperia.swiqi.effect.mode %s", enableMode ? "1" : "0");
            out.write(cmd.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void setExtHdr(boolean enableMode) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su"});
            OutputStream out = process.getOutputStream();
            String cmd = String.format("service call vendor.semc.hardware.extlight.IExtLight/default 3 i32 %s", enableMode ? "1" : "0");
            out.write(cmd.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void minCpuFreq() {
        String[] cpuFreq = new String[] {
                "300000",
                "710400",
                "844800"
        };

        setCpuScalingMinFreq(cpuFreq);
    }

    private void maxCpuFreq() {
        String[] cpuFreq = new String[] {
                "1804800",
                "2419200",
                "2841600"
        };

        setCpuScalingMinFreq(cpuFreq);
        setCpuScalingMaxFreq(cpuFreq);
    }

    private void _100percentCpuFreq() {
        String[] cpuFreq = new String[] {
                CpuFreqLevel._100.policy0Freq,
                CpuFreqLevel._100.policy4Freq,
                CpuFreqLevel._100.policy7Freq,
        };

        setCpuScalingMaxFreq(cpuFreq);
    }

    private void _75percentCpuFreq() {
        String[] cpuFreq = new String[] {
                CpuFreqLevel._75.policy0Freq,
                CpuFreqLevel._75.policy4Freq,
                CpuFreqLevel._75.policy7Freq,
        };

        setCpuScalingMaxFreq(cpuFreq);
    }

    private void _50percentCpuFreq() {
        String[] cpuFreq = new String[] {
                CpuFreqLevel._50.policy0Freq,
                CpuFreqLevel._50.policy4Freq,
                CpuFreqLevel._50.policy7Freq,
        };

        setCpuScalingMaxFreq(cpuFreq);
    }

    private void _30percentCpuFreq() {
        String[] cpuFreq = new String[]{
                CpuFreqLevel._30.policy0Freq,
                CpuFreqLevel._30.policy4Freq,
                CpuFreqLevel._30.policy7Freq,
        };

        setCpuScalingMaxFreq(cpuFreq);
    }

    private void setCpuScalingMinFreq(String[] cpuFreq) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su"});
            OutputStream out = process.getOutputStream();
            String cmd = String.format(
                    "chmod 777 /sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq;" +
                    "echo %s > /sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq;" +
                    "chmod 777 /sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq;" +
                    "echo %s > /sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq;" +
                    "chmod 777 /sys/devices/system/cpu/cpufreq/policy7/scaling_min_freq;" +
                    "echo %s > /sys/devices/system/cpu/cpufreq/policy7/scaling_min_freq;",
                    cpuFreq[0], cpuFreq[1], cpuFreq[2]
            );
            out.write(cmd.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCpuScalingMaxFreq(String[] cpuFreq) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su"});
            OutputStream out = process.getOutputStream();
            String cmd = String.format(
                    "chmod 777 /sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq;" +
                    "echo %s > /sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq;" +
                    "chmod 777 /sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq;" +
                    "echo %s > /sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq;" +
                    "chmod 777 /sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq;" +
                    "echo %s > /sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq;",
                    cpuFreq[0], cpuFreq[1], cpuFreq[2]
            );
            out.write(cmd.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void setPowersaveCpuGovernor() {
        setCpuScalingGovernor(CpuGovernor.Powersave.name);
    }

    private void setPerformanceCpuGovernor() {
        setCpuScalingGovernor(CpuGovernor.Performance.name);
    }

    private void setOndemandCpuGovernor() {
        setCpuScalingGovernor(CpuGovernor.Ondemand.name);
    }

    private void setSchedutilCpuGovernor() {
        setCpuScalingGovernor(CpuGovernor.Schedutil.name);
    }

    private void setCpuScalingGovernor(String governor) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su"});
            OutputStream out = process.getOutputStream();
            String cmd = String.format(
                    "chmod 777 /sys/devices/system/cpu/cpufreq/policy0/scaling_governor;" +
                    "echo %s >  /sys/devices/system/cpu/cpufreq/policy0/scaling_governor;" +
                    "chmod 777 /sys/devices/system/cpu/cpufreq/policy4/scaling_governor;" +
                    "echo %s >  /sys/devices/system/cpu/cpufreq/policy4/scaling_governor;" +
                    "chmod 777 /sys/devices/system/cpu/cpufreq/policy7/scaling_governor;" +
                    "echo %s >  /sys/devices/system/cpu/cpufreq/policy7/scaling_governor;",
                    governor, governor, governor
            );
            out.write(cmd.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshCpuCpuGovernor(RadioGroup radioGroup) {
        // use cpu0 cpu governor as global cpu governor, maybe not be strict
        CpuGovernor selectedCpuGovernor = getCpuGovernor(0);
        switch (selectedCpuGovernor) {
            case Powersave:
                radioGroup.check(R.id.cpu_governor_powersave);
                break;
            case Performance:
                radioGroup.check(R.id.cpu_governor_performance);
                break;
            case Schedutil:
                radioGroup.check(R.id.cpu_governor_schedutil);
                break;
            case Ondemand:
                radioGroup.check(R.id.cpu_governor_ondemand);
                break;
        }
    }

    private CpuGovernor getCpuGovernor(int cpu) {
        try {
            String path = String.format("/sys/devices/system/cpu/cpufreq/policy%s/scaling_governor", cpu);
            FileInputStream fis = new FileInputStream(path);
            byte[] lineBytes = new byte[32];
            int readPos = fis.read(lineBytes);
            fis.close();
            String governor = new String(lineBytes, 0, readPos).trim();
            return CpuGovernor.nameOf(governor);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
        }
        return CpuGovernor.Unknown;
    }

    private void refreshCpuPerformanceLevel(RadioGroup radioGroup) {
        CpuFreqLevel cpuFreqLevel = getCpuFreqLevel();
        switch (cpuFreqLevel) {
            case _30:
                radioGroup.check(R.id._30_cpu_freq);
                break;
            case _50:
                radioGroup.check(R.id._50_cpu_freq);
                break;
            case _75:
                radioGroup.check(R.id._75_cpu_freq);
                break;
            case _100:
                radioGroup.check(R.id._100_cpu_freq);
                break;
        }
    }

    private CpuFreqLevel getCpuFreqLevel() {
        try {
            String[] policyArray = new String[]{"0", "4", "7"};
            String[] freqLevelArray = new String[policyArray.length];
            byte[] lineBytes = new byte[32];
            for (int i = 0; i < policyArray.length; i++) {
                String policy = policyArray[i];
                String path = String.format("/sys/devices/system/cpu/cpu%s/cpufreq/scaling_cur_freq", policy);
                FileInputStream fis = new FileInputStream("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
                int readPos = fis.read(lineBytes);
                fis.close();
                String cpuFreq = new String(lineBytes, 0, readPos).trim();
                freqLevelArray[i] = cpuFreq;
            }
            return CpuFreqLevel.policyOf(freqLevelArray);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
        }
        return CpuFreqLevel.Unknown;
    }

    private void changeCpuState(View view, int cpu) {
        boolean castButtonCorrect = view instanceof Button;
        if (!castButtonCorrect) {
            return;
        }

        Button button = (Button) view;
        boolean cpuOnlineState = getCpuOnline(cpu);
        setCpuOnline(cpu, !cpuOnlineState);
        refreshCpuStateButtonTextAndColor(button, cpu, !cpuOnlineState);
    }

    private void refreshCpuStateButtonTextAndColor(Button button, int cpu) {
        boolean cpuOnlineState = getCpuOnline(cpu);
        refreshCpuStateButtonTextAndColor(button, cpu, cpuOnlineState);
    }

    private void refreshCpuStateButtonTextAndColor(Button button, int cpu, boolean cpuOnlineState) {
        if (cpuOnlineState) {
            button.setTextColor(Color.GREEN);
            String text = getResources().getText(R.string.btn_text_disable_cpu).toString() + cpu;
            button.setText(text);
        } else {
            button.setTextColor(Color.RED);
            String text = getResources().getText(R.string.btn_text_enable_cpu).toString() + cpu;
            button.setText(text);
        }
    }

    private void setCpuOnline(int cpu, boolean online) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su"});
            OutputStream out = process.getOutputStream();
            String cmd = String.format(
                    "chmod 777 /sys/devices/system/cpu/cpu%s/online;" +
                    "echo %s > /sys/devices/system/cpu/cpu%s/online;",
                    cpu, online ? 1 : 0, cpu
            );
            out.write(cmd.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean getCpuOnline(int cpu) {
        try {
            String path = String.format("/sys/devices/system/cpu/cpu%s/online", cpu);
            FileInputStream fis = new FileInputStream(path);
            byte[] lineBytes = new byte[8];
            int readPos = fis.read(lineBytes);
            fis.close();
            String str = new String(lineBytes, 0, readPos).trim();
            return str.equals("1");
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "错误", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}