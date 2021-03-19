package com.nfs.ascent.signalstrengthtest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import com.master.permissionhelper.PermissionHelper;
import com.zues.netstat.health.DialogExecutorImpl;
import com.zues.netstat.health.DialogExecutorImpl2;
import com.zues.netstat.health.HealthManager;
import com.zues.netstat.utils.IPUtils;
import com.zues.netstat.ping.PingManager;
import com.zues.netstat.dm.PingResult;
import com.zues.netstat.dm.PingStats;
import com.zues.netstat.service.NetworkStrengthService;
import com.zues.netstat.dm.SignalStats;
import com.zues.netstat.utils.NetworkUtils;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    private LinearLayout viewStrengthStatus;
    private TextView txtStrengthStatus;
    private TextView resultText;
    private EditText editIpAddress;
    private ScrollView scrollView;
    private Button pingButton;
    private Button healthButton;
    View optionsBtn;
    private ProgressDialog progressDialog = null;


    private static final String TAG = MainActivity.class.getSimpleName();
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);

        viewStrengthStatus = findViewById(R.id.view_strength_status);
        txtStrengthStatus = findViewById(R.id.txt_strength_status);
        resultText = findViewById(R.id.resultText);
        editIpAddress = findViewById(R.id.editIpAddress);
        scrollView = findViewById(R.id.scrollView1);
        pingButton = findViewById(R.id.pingButton);
        healthButton = findViewById(R.id.healthCheck);

        InetAddress ipAddress = IPUtils.getLocalIPv4Address();
        if (ipAddress != null) {
            editIpAddress.setText(ipAddress.getHostAddress());
        }

        pingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doPing();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        healthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doHealthCheck();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        optionsBtn = findViewById(R.id.options);
        optionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsMenu();
            }
        });

        checkForNecessaryPermissions();
//        checkConnection();
    }

    private void appendResultsText(SpannableString text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultText.append(text);
                resultText.append("\n");
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    private void setEnabled(final View view, final boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (view != null) {
                    view.setEnabled(enabled);
                }
            }
        });
    }

    private void doPing() {
        String ipAddress = editIpAddress.getText().toString();

        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText(new SpannableString("Invalid Ip Address"));
            return;
        }
        setEnabled(pingButton, false);

        // region  ---------------------------------------------- Approach one ----------------------------------------------
        /*
        // Perform a single synchronous ping
        PingResult pingResult = null;
        try {
            pingResult = Ping.onAddress(ipAddress).setTimeOutMillis(1000).doPing();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            appendResultsText(new SpannableString(e.getMessage()));
            setEnabled(pingButton, true);
            return;
        }


        appendResultsText(new SpannableString("Pinging Address: " + pingResult.getAddress().getHostAddress()));
        appendResultsText(new SpannableString("HostName: " + pingResult.getAddress().getHostName()));
        appendResultsText(new SpannableString(String.format("%.2f ms", pingResult.getTimeTaken())));
        */
        // endregion  ------------------------------------------  Approach one ----------------------------------------------

        // Perform an asynchronous ping
        PingManager.onAddress(ipAddress).setTimeOutMillis(1000).setTimes(4).doPing(new PingManager.PingListener() {
            @Override
            public void onResult(PingResult pingResult) {
                if (pingResult.isReachable) {
                    appendResultsText(new SpannableString(String.format("%.2f ms", pingResult.getTimeTaken())));
                } else {
                    appendResultsText(new SpannableString(getString(R.string.timeout)));
                }
                Log.d(TAG, " [ PING RESULT -> onResule ] : " + pingResult.toString());
            }

            @Override
            public void onFinished(PingStats pingStats) {
                appendResultsText(new SpannableString(String.format("Pings: %d, Packets lost: %d",
                        pingStats.getNoPings(), pingStats.getPacketsLost())));
                appendResultsText(new SpannableString(String.format("Min/Avg/Max Time: %.2f/%.2f/%.2f ms",
                        pingStats.getMinTimeTaken(), pingStats.getAverageTimeTaken(), pingStats.getMaxTimeTaken())));
                setEnabled(pingButton, true);
                Log.d(TAG, " [ PING STATS -> onFinished ] : " + pingStats.toString());
            }

            @Override
            public void onError(Exception e) {
                // TODO: STUB METHOD
                appendResultsText(new SpannableString(e.getLocalizedMessage()));
                setEnabled(pingButton, true);
            }
        });

    }


    private void doHealthCheck() {
        String ipAddress = editIpAddress.getText().toString();

        if (TextUtils.isEmpty(ipAddress)) {
            appendResultsText(new SpannableString("Invalid URL"));
            return;
        }
        setEnabled(healthButton, false);


        HealthManager healthManager = HealthManager
                .onUrl(ipAddress)
//                .setProgressExecutor(new DialogExecutorImpl2(progressDialog))
                .checkHealth(stats -> {
                    appendResultsText(new SpannableString(stats.toString()));
                    setEnabled(healthButton, true);
                });
    }

    private void showOptionsMenu() {
        PopupMenu popup = new PopupMenu(this, optionsBtn);
        popup.getMenuInflater().inflate(R.menu.main_menu_options, popup.getMenu());
        if (NetworkStrengthService.mServiceIsStarted) {
            popup.getMenu().add("stop");
        } else {
            popup.getMenu().add("start");
        }
        popup.setOnMenuItemClickListener(item -> {
            NetworkStrengthService.mServiceIsStarted = !NetworkStrengthService.mServiceIsStarted;
            int command = NetworkStrengthService.mServiceIsStarted ? NetworkStrengthService.COMMAND_START : NetworkStrengthService.COMMAND_STOP;

            if (command == NetworkStrengthService.COMMAND_START) {
                resultText.setText("");
            }
            updateServiceStatus(command);
            return true;
        });
        popup.show();
    }

    private void updateServiceStatus(@IntRange(from = NetworkStrengthService.COMMAND_START, to = NetworkStrengthService.COMMAND_STOP) int command) {
        viewStrengthStatus.setVisibility(command == NetworkStrengthService.COMMAND_START ? View.VISIBLE : View.GONE);
        Intent intent = new Intent(MainActivity.this, NetworkStrengthService.class);
//        NetworkStrengthCheckingService.mServiceIsStarted = command == NetworkStrengthCheckingService.COMMAND_START;
        intent.putExtra(NetworkStrengthService.COMMAND_KEY, command);
        startService(intent);
    }

    private void checkForNecessaryPermissions() {

        permissionHelper = new PermissionHelper(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        }, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onPermissionDeniedBySystem() {
                Toast.makeText(MainActivity.this, "Permission denied. Clear app data to run app.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkConnection() {
        TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperatorName = tMgr.getNetworkOperatorName();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        int networkType = tMgr.getNetworkType();
        SignalStrength signalStrength = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            signalStrength = tMgr.getSignalStrength();
        }
        int simState = tMgr.getSimState();
        Log.d("MainActivity::", " [ networkType= " + networkType + " ] " + ", [networkOperatorName= " + networkOperatorName + " ] , [simState= " + simState + " ] , [signalStrength= " + signalStrength + " ] , ");

//        String mPhoneNumber = tMgr.getLine1Number();
    }

    @Override
    protected void onPause() {

//        Log.d(TAG, NetworkStrengthService.mServiceIsStarted + " onPause > command " + NetworkStrengthService.COMMAND_STOP);

//        updateServiceStatus(NetworkStrengthCheckingService.COMMAND_STOP);

        Intent intent = new Intent(this, NetworkStrengthService.class);
        stopService(intent);
        unregisterReceiver(mSignalReceiver);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        int command = NetworkStrengthService.mServiceIsStarted ? NetworkStrengthService.COMMAND_START : NetworkStrengthService.COMMAND_STOP;
//        Log.d(TAG, NetworkStrengthService.mServiceIsStarted + " onResume > command " + command);
        if (NetworkStrengthService.mServiceIsStarted) {
            updateServiceStatus(NetworkStrengthService.COMMAND_START);
        }

        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(NetworkStrengthService.ACTION_SIGNAL_STATS);
        registerReceiver(mSignalReceiver, intentFilter1);
//
//        Intent intent = new Intent(this, NetworkStrengthCheckingService.class);
//        intent.putExtra(NetworkStrengthCheckingService.KEY_INTERVAL, 1);
//        startService(intent);
    }

    BroadcastReceiver mSignalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.hasExtra(NetworkStrengthService.KEY_SIGNAL_STATS)) {
                    SignalStats stats = (SignalStats) intent.getExtras().get(NetworkStrengthService.KEY_SIGNAL_STATS);
                    Log.d(TAG, " [ Signal Strength Details ] : " + stats.toString());

                    appendResultsText(getSpannedText(stats));
//                    appendResultsText(stats.toString());

                    updateUI(stats);
                }
            }
        }
    };

    @NotNull
    private SpannableString getSpannedText(SignalStats stats) {

        String text = stats.getcTypeIdentifier() + " " + stats.getcSubTypeIdentifier();
        SpannableString spannableString = new SpannableString(text);
        int color = Color.RED;
        color = getResources().getColor(R.color.state_poor, null);
        if (stats.getcSubType() == NetworkUtils.CONNECTION_NO_CONNECTION) {
            color = Color.RED;
        } else if (stats.getcSubType() == NetworkUtils.CONNECTION_POOR) {
            color = getResources().getColor(R.color.state_poor, null);
        } else if (stats.getcSubType() == NetworkUtils.CONNECTION_MODERATE) {
            color = getResources().getColor(R.color.state_moderate, null);
        } else if (stats.getcSubType() == NetworkUtils.CONNECTION_GOOD) {
            color = getResources().getColor(R.color.state_good, null);
        } else if (stats.getcSubType() == NetworkUtils.CONNECTION_EXCELLENT) {
            color = getResources().getColor(R.color.state_excellent, null);
        } else { // CONNECTION_UNKNOWN
            color = Color.BLACK;
        }
        spannableString.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private void updateUI(@NotNull SignalStats stats) {

        txtStrengthStatus.setText(stats.getcTypeIdentifier());
        if (stats.getcSubType() == NetworkUtils.CONNECTION_NO_CONNECTION) {
            viewStrengthStatus.setBackgroundColor(getResources().getColor(R.color.state_no_internet, null));
        } else if (stats.getcSubType() == NetworkUtils.CONNECTION_POOR) {
            viewStrengthStatus.setBackgroundColor(getResources().getColor(R.color.state_poor, null));
        } else if (stats.getcSubType() == NetworkUtils.CONNECTION_MODERATE) {
            viewStrengthStatus.setBackgroundColor(getResources().getColor(R.color.state_moderate, null));
        } else if (stats.getcSubType() == NetworkUtils.CONNECTION_GOOD) {
            viewStrengthStatus.setBackgroundColor(getResources().getColor(R.color.state_good, null));
        } else if (stats.getcSubType() == NetworkUtils.CONNECTION_EXCELLENT) {
            viewStrengthStatus.setBackgroundColor(getResources().getColor(R.color.state_excellent, null));
        }
    }

}
