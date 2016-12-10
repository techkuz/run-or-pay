package runners.msband.runnerss;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.notifications.MessageFlags;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.HeartRateQuality;
import com.microsoft.band.tiles.BandTile;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RunningActivity extends AppCompatActivity {

    private TextView txtDist;
    private TextView txtCur;
    private TextView gsr;
    private TextView pulse;
    private TextView temp;

    private long dist;

    private static final UUID tileId = UUID.fromString("aa0D508F-70A3-47D4-BBA3-812BADB1F8Aa");

    private int money = 100;

    private long plannedDist = 3000l;

    private Date initialTime;
    private Date curTime;

    private Button removeTile;

    private boolean finished = false;
    private boolean paid = false;
int ii=0;
    private long initialDist = 0;
    private int status = 20;
    private Button btnDelTile;
    private Button btnGetDistance;
    private Button btnConsent;
    private TextView mainTxt;
    private BandClient client = null;
    private int restingHeartRate = 60;
    private int fastRunHeartRate = 120;
    private int curHeartRate = 0;
    private String message = "";
    private long myDist, myDist2;
    private long reachedDistance;
    private double percentage;
    private int counter = 0;
    private int old_counter = 0;
    private float speed;
    private float speed_array[] = {0.0f, 0.0f, 0.0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
    private int length;
    private float slowRunSpeed = 150.0f;
    private float oldCheckRunning = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);
        client = Band.getInstance().getData();
        dist = getIntent().getExtras().getInt("DISTANCE");
        txtDist = (TextView) findViewById(R.id.textdist);
        txtDist.setText(Long.toString(dist)+"m..");
        plannedDist=dist*100;
        initialTime = Calendar.getInstance().getTime();

        txtCur = (TextView) findViewById(R.id.curdist);
        pulse = (TextView) findViewById(R.id.pulse);
        gsr = (TextView) findViewById(R.id.gsr);
        temp = (TextView) findViewById(R.id.temperature);

        new DistSubscriptionTask().execute();

        removeTile = (Button) findViewById(R.id.removeTile);
        removeTile.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                // disableButtons();
                new StopTask().execute();

            }
        });




    }


    public void StopClick(View v) {
        MessageShow("Tyt tipa resultat", "Chto-to umnoe");
    }

    private void MessageShow(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RunningActivity.this);
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


    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {


                if (!finished) {

                    appendToUI(pulse, String.valueOf(event.getHeartRate()));

                    int mestniyStatus = 10;
                    String message2 = "";
                    String title = "";
                    /*message = String.format("Heart Rate = %d beats per minute\n"
                            + "Quality = %s\n", event.getHeartRate(), event.getQuality());*/
                    curHeartRate = event.getHeartRate();

                    if ((event.getQuality()).equals(HeartRateQuality.valueOf("ACQUIRING"))) {
                        message2 = "Wear the band\n";
                        //message += message2;
                        mestniyStatus = -1;
                        title = "Warning";
                    } else if (curHeartRate < restingHeartRate) {
                        message2 = "Too low heart rate\n";
                        //message += message2;
                        mestniyStatus = 0;
                        title = "Warning";
                    } else if (curHeartRate > fastRunHeartRate) {
                        message2 = "Stop running now. \n Too high heart rate.\n";
                        //message += message2;
                        mestniyStatus = 3;
                        title = "Warning";
                    }
                    if (mestniyStatus != status) {
                        status = mestniyStatus;
                        try {
                            client.getNotificationManager().sendMessage(tileId, title, message2, new Date(), MessageFlags.SHOW_DIALOG);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //appendToUI(message);
                    }

                }
            }
        }
    };

    private BandDistanceEventListener MyBandDistListener = new BandDistanceEventListener() {
        @Override
        public void onBandDistanceChanged(final BandDistanceEvent bandDistanceEvent) {
            if (bandDistanceEvent != null) {
                try {
                    dist = bandDistanceEvent.getDistanceToday();




                    if (checkTime() && checkRunning(bandDistanceEvent)) {
                        if (initialDist == 0l) {
                            initialDist = dist;
                            myDist = dist;
                            message += String.format("Initial distance = %d", myDist);

                            txtCur = (TextView) findViewById(R.id.curdist);
                            txtCur.setText(Long.toString(dist - initialDist));

                        } else {
                            myDist2 = dist - initialDist;
                            appendToUI(txtCur, String.valueOf(myDist2));

                            percentage = (100 * myDist2) / plannedDist;

                            if (percentage > 100) {
                                counter = 100;
                            } else if (percentage > 90) {
                                counter = 90;
                            } else if (percentage > 70) {
                                counter = 70;
                            } else if (percentage > 50) {
                                counter = 50;
                            } else if (percentage > 30) {
                                counter = 30;
                            }

                            try {
                                if (counter == 100) {
                                    message += "Completed all";
                                    client.getNotificationManager().sendMessage(tileId, "Excellent!", "You completed challenge", new Date(), MessageFlags.SHOW_DIALOG);
                                    finished = true;
                                } else if (counter != old_counter) {
                                    old_counter = counter;
                                    client.getNotificationManager().sendMessage(tileId, "Cheer up", String.format("You reached %d per cent!", counter), new Date(), MessageFlags.SHOW_DIALOG);
                                    message += String.format("Completed %d", percentage);


                                }
                            } catch (Exception e) {
                                e.printStackTrace();


                                message += String.format("Delta distance = %d", myDist2);
                            }
                        }


                    }
                    //appendToUI(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    };

    protected boolean checkTime() {

        curTime = Calendar.getInstance().getTime();
        long deltaTime = (curTime.getTime() - initialTime.getTime());

        // appendToUI(message+=String.format("%d \n %d", deltaTime, hour.getTime()*(-1)));


        if (((curTime.getTime() - initialTime.getTime()) < 60000) || finished) {
            return true;
        } else {
            try {
                if (!paid) {
                    client.getNotificationManager().sendMessage(tileId, "You failed", "1$ is sent to the orthanage!\n", new Date(), MessageFlags.SHOW_DIALOG);
                    money--;
                    paid = true;
                }


            } catch (BandIOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    protected boolean checkRunning(BandDistanceEvent event) {
        if (!finished) {
            float runningStatus = 0f;
            if (length < 10) {
                speed = event.getSpeed();
                speed_array[length] = speed;
                runningStatus = oldCheckRunning;
                length++;
            }
            if (length == 10) {
                float sum = 0f;
                for (int i = 0; i < length; i++) {
                    sum += speed_array[i];
                }
                runningStatus = sum / length;
                oldCheckRunning = runningStatus;
                length = 0;
                //appendToUI(message += String.format("Average speed: %f !\n", runningStatus));
            }


            if (runningStatus < slowRunSpeed) {
                try {
                    client.getNotificationManager().sendMessage(tileId, "Don't cheat", "Run faster!\n", new Date(), MessageFlags.SHOW_DIALOG);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            } else
                return true;
        }
        return false;


    }


    private class DistSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params){
            try{
                if (getConnectedBandClient()){
                    if(client.getSensorManager().getCurrentHeartRateConsent()== UserConsent.GRANTED){
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);

                    }
                    else{
                        MessageShow("","Something got wrong");
                    }

                    client.getSensorManager().registerDistanceEventListener(MyBandDistListener);
                    client.getSensorManager().registerGsrEventListener(mGsrEventListener);
                    client.getSensorManager().registerSkinTemperatureEventListener(MySkinTempListener);
                    if(!doesTileExist()){
                        addTile();

                    }
                    sendMessage("Lets run!;)");
                } else {
                    MessageShow("", "Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            }
            catch (BandException e){
                String exceptionMessage="";
                switch (e.getErrorType()){
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
                MessageShow("", exceptionMessage);
            }catch (Exception e) {
                MessageShow("", e.getMessage());
            }
            return null;
        }
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                MessageShow("", "Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        MessageShow("", "Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
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
                MessageShow("", e.getMessage());
            }
            return null;
        }
    }


    private boolean doesTileExist() throws BandIOException, InterruptedException, BandException {
        List<BandTile> tiles = client.getTileManager().getTiles().await();
        for (BandTile tile : tiles) {
            if (tile.getTileId().equals(tileId)) {
                return true;
            }
        }
        return false;
    }

    private boolean addTile() throws Exception {
		/* Set the options */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap tileIcon = BitmapFactory.decodeResource(getBaseContext().getResources(), R.raw.big, options);
        Bitmap badgeIcon = BitmapFactory.decodeResource(getBaseContext().getResources(), R.raw.small, options);

        BandTile tile = new BandTile.Builder(tileId, "MessageTile", tileIcon)
                .setTileSmallIcon(badgeIcon).build();
        //appendToUI("Message Tile is adding ...\n");
        if (client.getTileManager().addTile(this, tile).await()) {
            //appendToUI("Message Tile is added.\n");
            return true;
        } else {
            MessageShow("", "Unable to add message tile to the band.\n");
            return false;
        }
    }
    private void sendMessage(String message) throws BandIOException {
        client.getNotificationManager().sendMessage(tileId, "Tile Message", message, new Date(), MessageFlags.SHOW_DIALOG);
        //appendToUI(message + "\n");
    }


    private class StopTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
           //appendToUI("Stopping demo and removing Band Tile\n");
            try {
                if (getConnectedBandClient() && doesTileExist()) {
                //    appendToUI("Removing Tile.\n");
                    removeTile();
                } else {
                 //   appendToUI("Band isn't connected or tile not installed.\n");
                }
            } catch (BandException e) {
                //handleBandException(e);
                return false;
            } catch (Exception e) {
                //appendToUI(e.getMessage());
                return false;
            }

            return true;
        }
    }
    private void removeTile() throws BandIOException, InterruptedException, BandException {
        if (doesTileExist()) {
            client.getTileManager().removeTile(tileId).await();
            MessageShow("Tile is removed", "Have a nice day");
        }
    }

    private void appendToUI(final TextView myTextView, final String string) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myTextView.setText(string);
            }
        });
    }

    private BandGsrEventListener mGsrEventListener = new BandGsrEventListener() {
        @Override
        public void onBandGsrChanged(final BandGsrEvent event) {
            if (event != null) {
                appendToUI(gsr , String.valueOf(event.getResistance()));
            }
        }
    };

    private BandSkinTemperatureEventListener MySkinTempListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent event) {
            if (event != null) {
                appendToUI(temp , String.valueOf(event.getTemperature()));
            }
        }
    };


}

