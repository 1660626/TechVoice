package com.example.voicetech;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RecordFragment extends Fragment implements View.OnClickListener {

    private static RecordFragment instance;

    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLE_RATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private NavController navController;
//    private String recordPermission = Manifest.permission.RECORD_AUDIO;

    private ImageButton record_btn, record_list_btn, record_play_btn, record_pause_btn, record_stop_btn;
    private Chronometer record_timer;

    private boolean isRecording = false;

    //    private int PERMISSION_CODE = 21;
    private long pause_timer = 0;

    private MediaRecorder mediaRecorder;
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    public TextView record_filename;
    private String recordFile;
//    private String recordFileFinal;


    public RecordFragment() {

    }
    public static RecordFragment getInstance() {
        return instance;
    }

    public void myMethod() {
        // do something...
        System.out.println(record_timer.getText());

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

        // find id
        navController = Navigation.findNavController(view);
        record_list_btn = view.findViewById(R.id.record_list_btn);
        record_btn = view.findViewById(R.id.record_btn);
        record_play_btn = view.findViewById(R.id.record_play_btn);
        record_pause_btn = view.findViewById(R.id.record_pause_btn);
        record_stop_btn = view.findViewById(R.id.record_stop_btn);
        record_timer = view.findViewById(R.id.record_timer);
        record_filename = view.findViewById(R.id.record_filename);

        //set visible
        record_play_btn.setVisibility(View.INVISIBLE);
        record_pause_btn.setVisibility(View.INVISIBLE);
        record_stop_btn.setVisibility(View.INVISIBLE);

        //set Event
        record_list_btn.setOnClickListener(this);
        record_btn.setOnClickListener(this);
        record_play_btn.setOnClickListener(this);
        record_pause_btn.setOnClickListener(this);
        record_stop_btn.setOnClickListener(this);
        instance = this;
    }

//    public void onPause () {
//        super.onPause();
//        if ( isRecording ) {
//            record_pause_btn.setVisibility(View.INVISIBLE);
//            record_play_btn.setVisibility(View.VISIBLE);
//            stopRecording(false);
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_list_btn:

//                if(isRecording){
//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
//                    alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            navController.navigate(R.id.action_recordFragment_to_audioListFragment);
//                            isRecording = false;
//                        }
//                    });
//                    alertDialog.setNegativeButton("CANCEL", null);
//                    alertDialog.setTitle("Audio Still recording");
//                    alertDialog.setMessage("Are you sure, you want to stop the recording?");
//                    alertDialog.create().show();
//                } else {
                navController.navigate(R.id.action_recordFragment_to_audioListFragment);
//                }
                break;

            case R.id.record_btn:
                record_stop_btn.setVisibility(View.VISIBLE);
                record_pause_btn.setVisibility(View.VISIBLE);
                record_list_btn.setVisibility(View.INVISIBLE);
                record_btn.setVisibility(View.INVISIBLE);
                startRecording(false);

                break;
            case R.id.record_pause_btn:

                stopRecording(false);
                break;
            case R.id.record_play_btn:
                record_play_btn.setVisibility(View.INVISIBLE);
                record_pause_btn.setVisibility(View.VISIBLE);
                startRecording(true);
                break;
            case R.id.record_stop_btn:
                record_play_btn.setVisibility(View.INVISIBLE);
                record_stop_btn.setVisibility(View.INVISIBLE);
                record_pause_btn.setVisibility(View.INVISIBLE);
                record_list_btn.setVisibility(View.VISIBLE);
                record_btn.setVisibility(View.VISIBLE);
                stopRecording(true);
                break;
        }

    }

    private void startRecording(final boolean b) {


        if (b == false) {   //Start record
            record_timer.setBase(SystemClock.elapsedRealtime());
            record_timer.start();

            record_filename.setText("Recording.....");

        }
        if (b == true) {  //continues record
            record_filename.setText("Recording.....");

            record_timer.setBase(SystemClock.elapsedRealtime() - pause_timer);
            record_timer.start();
        }
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
        recorder.startRecording();
        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile(b);
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }
    public void getinfor() {

    }
    public void stopRecording(boolean b) {


        if (recorder != null) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            pause_timer = 0;
            recordingThread = null;
        }

        if (b == true) { //end record
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
            Date now = new Date();
            recordFile = "Recording_" + formatter.format(now);
            record_filename.setText(recordFile);
            copyWaveFile(getTempFilename(), getFilename());
            deleteTempFile();
            record_timer.setBase(SystemClock.elapsedRealtime());
            record_timer.stop();
        }
        if (b == false) { // pause record
            record_pause_btn.setVisibility(View.INVISIBLE);
            record_play_btn.setVisibility(View.VISIBLE);
            record_filename.setText("Pause recording.....");

            record_timer.stop();
            pause_timer = SystemClock.elapsedRealtime() - record_timer.getBase();
        }

    }

//    private void pauseRecording() {
//        record_timer.stop();
//        pause_timer = SystemClock.elapsedRealtime() - record_timer.getBase();
//        mediaRecorder.pause();
//    }
//
//    private void playRecording() {
//
//        record_timer.setBase(SystemClock.elapsedRealtime() - pause_timer);
//        record_timer.start();
//        mediaRecorder.resume();
//    }

    private String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath + "/recording_temp.raw");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (file.getAbsolutePath());
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath + "/" + record_filename.getText() + ".wav");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (file.getAbsolutePath());
    }

    private void writeAudioDataToFile(boolean b) {
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();

        System.out.println(filename);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename, b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;

        if (os != null) {
            while (isRecording) {
                read = recorder.read(data, 0, bufferSize);

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 44;
        long longSampleRate = RECORDER_SAMPLE_RATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLE_RATE * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename, true);
            totalAudioLen = in.getChannel().size() + out.getChannel().size();
            totalDataLen = totalAudioLen + 44;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate, outFilename);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate, String outFileName) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        RandomAccessFile rFile = new RandomAccessFile(outFileName, "rw");
        rFile.seek(0);
        rFile.write(header, 0, 44);
        rFile.close();
    }
}