package com.manjurulhoque.voicerecorder.fragment;


import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.manjurulhoque.voicerecorder.R;
import com.manjurulhoque.voicerecorder.VisualizerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VoiceDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String TAG = VoiceDialogFragment.class.getName();

    private MediaRecorder mMediaRecorder;
    private Handler handler;
    File tempFile;
    private boolean recording = false;
    private long mStartTime = 0;
    @BindView(R.id.imageViewPlay)
    ImageView imageViewPlay;
    @BindView(R.id.linearLayoutStop)
    LinearLayout linearLayoutStop;
    @BindView(R.id.imageViewStop)
    ImageView imageViewStop;
    @BindView(R.id.imageViewDelete)
    ImageView imageViewDelete;
    @BindView(R.id.visualizerView)
    VisualizerView visualizerView;
    @BindView(R.id.mTimerTextView)
    TextView mTimerTextView;
    @BindView(R.id.textViewDelete)
    TextView textViewDelete;

    private int[] amplitudes = new int[100];
    private int i = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_voice, container, false);
        ButterKnife.bind(this, view);

        imageViewPlay.setOnClickListener(this);
        imageViewStop.setOnClickListener(this);

        handler = new Handler();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mMediaRecorder != null) {
            handler.removeCallbacks(updateVisualizer);
            visualizerView.clear();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow()
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imageViewPlay) {
            if (!recording) {
                recording = true;
                imageViewPlay.setImageResource(R.drawable.ic_pause);
                if (mMediaRecorder == null)
                    mMediaRecorder = new MediaRecorder();
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
                    mMediaRecorder.setAudioEncodingBitRate(48000);
                } else {
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mMediaRecorder.setAudioEncodingBitRate(64000);
                }
                mMediaRecorder.setAudioSamplingRate(16000);

                try {
                    tempFile = getOutputFile();
                    tempFile.getParentFile().mkdirs();
                    //tempFile = File.createTempFile("VoiceRecorder " + Calendar.getInstance().getTime(), ".3gp", getActivity().getExternalFilesDir(null));

                    mMediaRecorder.setOutputFile(tempFile.getAbsolutePath());
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                    mStartTime = SystemClock.elapsedRealtime();
                    handler.postDelayed(mTickExecutor, 100);
                    handler.post(updateVisualizer);
                } catch (IllegalStateException | IOException e) {
                    Log.e(TAG, e.toString());
                }
            } else {
                imageViewPlay.setImageResource(R.drawable.play);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder = null;
                mStartTime = 0;
                handler.removeCallbacks(mTickExecutor);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    mMediaRecorder.resume();
//                }
                recording = false;
            }
        } else if (view.getId() == R.id.imageViewStop) {
//            File newFile = new File(getActivity().getExternalFilesDir(null).getAbsolutePath() +
//                    File.separator + tempFile.getName() + ".3gp");
            if (tempFile != null) {
                tempFile.renameTo(tempFile);
                Toast.makeText(getContext(), "Recording saved", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    }

    private File getOutputFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Voice Recorder/RECORDING_"
                + dateFormat.format(new Date())
                + ".m4a");
    }

    private void tick() {
        long time = (mStartTime < 0) ? 0 : (SystemClock.elapsedRealtime() - mStartTime);
        int minutes = (int) (time / 60000);
        int seconds = (int) (time / 1000) % 60;
        int milliseconds = (int) (time / 100) % 10;
        mTimerTextView.setText(minutes + ":" + (seconds < 10 ? "0" + seconds : seconds) + ":" + (milliseconds < 10 ? "0" + milliseconds : milliseconds));
        if (mMediaRecorder != null) {
            amplitudes[i] = mMediaRecorder.getMaxAmplitude();
            if (i >= amplitudes.length - 1) {
                i = 0;
            } else {
                ++i;
            }
        }
    }

    private Runnable mTickExecutor = new Runnable() {
        @Override
        public void run() {
            tick();
            handler.postDelayed(mTickExecutor, 100);
        }
    };

    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (recording) {
                int x = mMediaRecorder.getMaxAmplitude();
                visualizerView.addAmplitude(x);
                visualizerView.invalidate();
                handler.postDelayed(this, 50);
            }
        }
    };
}
