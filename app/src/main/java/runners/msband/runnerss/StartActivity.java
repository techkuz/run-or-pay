package runners.msband.runnerss;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class StartActivity extends AppCompatActivity {

    private TextView timertxt;
    private static final String FORMAT = "%02d:%02d:%02d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        timertxt = (TextView) findViewById(R.id.timerview);
        Timer timer = Timer.getInstance();
        timer.setData(new CountDownTimer(24*60*60000, 1000) {

            public void onTick(long millisUntilFinished) {

                timertxt.setText("" + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                timertxt.setText("00:00:00");
            }
        });
        timer.getData().start();
    }

    public void StartClick(View view)
    {
        AlertDialog.Builder ad = new AlertDialog.Builder(StartActivity.this);
        ad.setTitle("Let's start!");
        ad.setMessage("Are you sure?");
        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(StartActivity.this, "Starting...",
                        Toast.LENGTH_LONG).show();
                StartRunning();
            }
        });
        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(StartActivity.this, "See you later",
                        Toast.LENGTH_LONG).show();
            }
        });
        ad.setCancelable(true);
        ad.show();
    }

    public void StartRunning() {
        Intent intent = new Intent(this, RunningActivity.class);
        int dist = getIntent().getExtras().getInt("DISTANCE");
        intent.putExtra("DISTANCE", dist);
        startActivity(intent);
    }
}
