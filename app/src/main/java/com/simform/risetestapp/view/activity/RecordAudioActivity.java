package com.simform.risetestapp.view.activity;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.simform.risetestapp.R;
import com.simform.risetestapp.util.FileUtils;
import com.simform.risetestapp.util.ViewUtils;
import com.simform.risetestapp.util.recorder.WavAudioRecord;

import java.io.File;
import java.io.IOException;

public class RecordAudioActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_AUDIO = 20;

    private static final int PERMISSIONS_REQUEST_WRITE = 30;

    private static final String TAG = RecordAudioActivity.class.getSimpleName();

    private String mRecordFilePath = FileUtils.createTempFile("wav.wav");

    private WavAudioRecord mWavRecorder;

    private boolean mRecording = false;

    private Runnable mCounterRunnable;

    private Handler mHandler = new Handler();

    private long mStart;

    private boolean mSuccess = false;

    private ImageView recordImageViewPlay;
    private boolean mIsPlaying = false;

    @Override
    protected void onCreate(Bundle bundle) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        super.onCreate(bundle);

        setContentView(R.layout.activity_record_audio);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_AUDIO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE);
        } else {
            setupRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
            case PERMISSIONS_REQUEST_WRITE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }

    private void handleRecordButton() {
        ImageView recordImageView = ViewUtils.find(this, R.id.record_button);

        if (!mRecording) {
            Log.d(TAG, "Recording audio to file: " + mRecordFilePath);

            mWavRecorder.startRecording();

            mRecording = true;
            startCounter();

            recordImageView.setImageResource(R.drawable.ra_button_recording);
        } else {
            recordImageView.setImageResource(R.drawable.ra_button_stopped);
            recordImageView.setVisibility(View.INVISIBLE);
            recordImageViewPlay.setVisibility(View.VISIBLE);

            stopCounter();
            mRecording = false;

            mWavRecorder.stopRecording();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        File file = new File(mRecordFilePath);
        if (!mSuccess && file.exists() && !file.delete()) {
            Log.d(TAG, "Recording file wasn't deleted");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            if (mRecording) {
                mWavRecorder.stopRecording();
            }

            mSuccess = true;

            /*Intent data = new Intent();
            data.putExtra(Constants.Intents.RETURN_FILENAME, mRecordFilePath);
            setResult(RESULT_OK, data);*/
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecording() {
        ImageView recordImageView = ViewUtils.find(this, R.id.record_button);
        recordImageView.setOnClickListener(v -> handleRecordButton());

        mWavRecorder = new WavAudioRecord(mRecordFilePath);

        recordImageViewPlay = ViewUtils.find(this, R.id.record_play);
        recordImageViewPlay.setOnClickListener(v -> {
            if(mIsPlaying){
                recordImageView.setImageResource(R.drawable.ic_play);
            }else {
                recordImageView.setImageResource(R.drawable.ic_pause_small);
                playPauseAudio(mIsPlaying);
            }
        });
    }

    private void playPauseAudio(boolean mIsPlaying){
        Uri myUri = Uri.parse(mRecordFilePath); // initialize Uri here
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    private void startCounter() {
        final TextView timerScView = ViewUtils.find(this, R.id.record_timer_sc);
        final TextView timerMsView = ViewUtils.find(this, R.id.record_timer_ms);

        mStart = System.currentTimeMillis();
        mCounterRunnable = () ->
        {
            if (!mRecording) {
                return;
            }

            long duration = System.currentTimeMillis() - mStart;

            int ms = (int) (duration % 1000) / 100;
            int sc = (int) (duration / 1000) % 60;
            int mn = (int) ((duration / (1000 * 60)) % 60);

            timerScView.setText(String.format("%d:%02d", mn, sc));
            timerMsView.setText(String.format(".%01d", ms));

            mHandler.postDelayed(mCounterRunnable, 50);
        };

        mHandler.postAtFrontOfQueue(mCounterRunnable);
    }

    private void stopCounter() {
        mHandler.removeCallbacks(mCounterRunnable);
    }

}