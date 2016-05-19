package example.user.com.facebookdemo_app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {

    SessionManager session;
    MediaRecorder mRecorder;
    Button _Send, _Start, _Stop, _Play;
    boolean recording = false;
    ImageView _ImgView;
    MediaPlayer musicplayer;
    TextView _CountTimer;
    private String outputFile = null;
    private static final int REQUEST_PERMISSIONS = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        if(hasMicrophone()){
            Toast.makeText(MainActivity.this, "Detect your microphone", Toast.LENGTH_LONG).show();
        }else
        {
            showAlertDialog(MainActivity.this, "Microphone Status",
                    "Not Detected your microphone", false);
        }

        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();

        String name = user.get(SessionManager.KEY_NAME);
        String email = user.get(SessionManager.KEY_EMAIL);
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
        _Start = (Button) findViewById(R.id.start);
        _Stop = (Button) findViewById(R.id.stop);
        _Send = (Button) findViewById(R.id.sendbtn);
        _Play = (Button) findViewById(R.id.play);
        _CountTimer = (TextView) findViewById(R.id.timer_view);
        _ImgView = (ImageView) findViewById(R.id.imgView);
        /**
         * premission for marshmallow in paste  http://paste.ofcode.org/MSMgvAC5vyJUZCNq7DTxQs
         */

        _CountTimer.setText("00:30");
        final CounterClass timer = new CounterClass(30000, 1000);
        _Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicplayer != null && musicplayer.isPlaying()) {
                    musicplayer.stop();
                }
                try {
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                    mRecorder.setOutputFile(outputFile);
                    try {
                        mRecorder.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mRecorder.start();
                    timer.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                _ImgView.setVisibility(View.VISIBLE);
                _Start.setVisibility(View.INVISIBLE);
                _Play.setVisibility(View.INVISIBLE);
                _Stop.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Start Recording", Toast.LENGTH_SHORT).show();
            }
        });
        _Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _ImgView.setVisibility(View.INVISIBLE);
                _Start.setVisibility(View.VISIBLE);
                _Stop.setVisibility(View.INVISIBLE);
                _Play.setVisibility(View.VISIBLE);
                try {
                    mRecorder.stop();
                    timer.cancel();
                    _CountTimer.setText("Stop Recording");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Stop Recording", Toast.LENGTH_SHORT).show();
            }
        });
        _Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicplayer = new MediaPlayer();
                try {
                    musicplayer.setDataSource(outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    musicplayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                musicplayer.start();
                if (_Start.isPressed()) {
                    musicplayer.stop();
                }
                Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onFinish() {
            _CountTimer.setText("Completed");
            _Play.setVisibility(View.VISIBLE);
            _Start.setVisibility(View.VISIBLE);
            _Stop.setVisibility(View.INVISIBLE);
            _ImgView.setVisibility(View.INVISIBLE);
            mRecorder.stop();
            mRecorder.release();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            _CountTimer.setText((millisUntilFinished / 1000) + "");

        }
    }
    protected boolean hasMicrophone() {
        PackageManager pmanager = this.getPackageManager();
        return pmanager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE);
    }

    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);
        // Setting Dialog Message
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                System.exit(0);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }
}
