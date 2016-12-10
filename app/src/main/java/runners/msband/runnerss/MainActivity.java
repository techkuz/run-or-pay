package runners.msband.runnerss;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.lang.ref.WeakReference;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private BandClient client = null;
    private Button btnSet;
    private Button check;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        editText = (EditText)findViewById(R.id.distance);
        btnSet = (Button) findViewById(R.id.setbutton);
        btnSet.setBackgroundColor(0xFF9E9E9E);
        final WeakReference<Activity> reference = new WeakReference<Activity>(this);

        check = (Button) findViewById(R.id.check);
        check.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                new HeartRateConsentTask().execute(reference);
                ConnectClick();
            }
        });

    }

    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {
            try {
                if (getConnectedBandClient()) {

                    if (params[0].get() != null) {
                        client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean consentGiven) {
                            }
                        });
                    }
                } else {
                    MessageShow("", "Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                MessageShow("",exceptionMessage);

            } catch (Exception e) {
                //appendToUI(e.getMessage());
            }
            return null;
        }
    }


    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                MessageShow("","Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
            Band.getInstance().setData(client);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        //appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }

    public void SetChallengeClick(View v)
    {
        int dist;
        if (client != null && client.isConnected()) {
            try {
                dist = Integer.parseInt(editText.getText().toString());
            } catch (NumberFormatException e) {
                MessageShow("Wrong distance format!", "Enter correct distance, please");
                btnSet.setBackgroundColor(0xFF9E9E9E);
                return;
            }
        }
        else {
            MessageShow("Connect MS Band", "MS Band is not connected");
            btnSet.setBackgroundColor(0xFF9E9E9E);
            return;
        }
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra("DISTANCE", dist);
        startActivity(intent);
    }

    public void ConnectClick() {
        btnSet.setBackgroundColor(0xFF00BCD4);
        Toast.makeText(MainActivity.this, "Connecting...",
                Toast.LENGTH_LONG).show();
    }

    private void MessageShow(String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    public class AutoBoot extends BroadcastReceiver {
        @Override


        public void onReceive(Context context, Intent intent) {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
