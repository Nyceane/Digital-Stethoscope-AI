package com.manjurulhoque.voicerecorder;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecordPlayActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mMediaPlayer;

    @BindView(R.id.record_name)
    TextView record_name;
    @BindView(R.id.btn_Play)
    ImageView btn_Play;
    @BindView(R.id.seekBar)
    SeekBar mSeekBar;
    @BindView(R.id.timeDuration)
    TextView timeTotalDuration;
    @BindView(R.id.timeCurrent)
    TextView timeCurrent;

    Thread updateSeekBar;
    int currentPosition = 0;
    Timer timer;

    // variables
    String time;
    String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record);
        ButterKnife.bind(this);

        time = getIntent().getStringExtra("time");
        name = getIntent().getStringExtra("record_name");
        record_name.setText(name);

        File record_file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Voice Recorder/" + name + ".m4a");

        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mMediaPlayer.getDuration();
                while (currentPosition < totalDuration) {
                    try {
                        sleep(500);
                        currentPosition = mMediaPlayer.getCurrentPosition();
                        mSeekBar.setProgress(currentPosition);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        playRecord(record_file);
        timeTotalDuration.setText(getDuration(record_file));

        btn_Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMediaPlayer.getDuration() == mSeekBar.getMax()){
                    mSeekBar.setProgress(0);
                    updateDuration();
                }
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    btn_Play.setImageResource(R.drawable.play);
                } else {
                    mMediaPlayer.start();
                    updateDuration();
                    btn_Play.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        mMediaPlayer.setOnCompletionListener(this);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    private void playRecord(File record) {
        Uri u = Uri.parse(record.getPath());
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), u);
        mMediaPlayer.start();
        mSeekBar.setMax(mMediaPlayer.getDuration());
        updateSeekBar.start();
        updateDuration();
        btn_Play.setImageResource(R.drawable.ic_pause);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        btn_Play.setImageResource(R.drawable.play);
    }

    private void updateDuration() {
        if (mMediaPlayer != null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("RecordPlayActivityRun", "HERE");
                            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                                timeCurrent.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        timeCurrent.setText(getCurrentDuration(mMediaPlayer.getCurrentPosition()));
                                    }
                                });
                            } else {
                                timer.cancel();
                                timer.purge();
                            }
                        }
                    });
                }
            }, 0, 1000);
        }
    }

    private String getCurrentDuration(long duration) {
        return Utils.formatMilliSecond(duration);
    }

    private String getDuration(File file) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Utils.formatMilliSecond(Long.parseLong(durationStr));
    }
}
