package com.example.voicetech;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;



public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick, View.OnClickListener {

    private MediaPlayerService player;
    boolean serviceBound = false;

    private static AudioListFragment instance = null;

    private TextView player_header_title;
    private TextView player_filename;

    private ImageButton player_play_btn, player_back_btn, player_next_btn;

    private SeekBar player_seekbar;

    private ConstraintLayout player_sheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private RecyclerView audioList;
    private File[] allFiles;
    private AudioListAdapter audioListAdapter;

    //    private MediaPlayer mediaPlayer = null;
    public File fileToPlay = null;

    private Handler seekbarHandler;
    private Runnable updateSeekbar;
    private int positionFile = 0;

    public AudioListFragment() {
    }

    public static AudioListFragment getInstance() {
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_list, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        player_sheet = view.findViewById(R.id.player_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(player_sheet);
        audioList = view.findViewById(R.id.audio_list_view);


        player_play_btn = view.findViewById(R.id.player_play_btn);
        player_back_btn = view.findViewById(R.id.player_back_btn);
        player_next_btn = view.findViewById(R.id.player_next_btn);
        player_seekbar = view.findViewById(R.id.player_seekbar);

        //set Event
        player_play_btn.setOnClickListener(this);
        player_back_btn.setOnClickListener(this);
        player_next_btn.setOnClickListener(this);

        player_header_title = view.findViewById(R.id.player_header_title);
        player_filename = view.findViewById(R.id.player_filename);

        String path = Environment.getExternalStorageDirectory().getPath();
        File directory = new File(path);
        allFiles = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.toLowerCase().endsWith(".wav")) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        if (allFiles != null) {
            audioListAdapter = new AudioListAdapter(allFiles, this);

            audioList.setHasFixedSize(true);
            audioList.setLayoutManager(new LinearLayoutManager(getContext()));
            audioList.setAdapter(audioListAdapter);
        }

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //We cant do anything here for this app
            }
        });


        player_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(player != null) {
                    player.pauseMedia();
                    player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.play, null));
                    seekbarHandler.removeCallbacks(updateSeekbar);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(player != null) {
                    int progress = seekBar.getProgress();
                    player.resumeMedia();
                    player.mediaPlayer.seekTo(progress);

                    updateRunnable();
                    seekbarHandler.postDelayed(updateSeekbar, 0);
                }
            }
        });
        instance = this;
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            getActivity().unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    @Override
    public void onClickListener(File file, int position) {
        fileToPlay = file;

        if (!serviceBound) {
            positionFile = position;
            System.out.println("serviceBound:" + serviceBound);
            Intent playerIntent = new Intent(getActivity(), MediaPlayerService.class);
            playerIntent.putExtra("media", fileToPlay.getAbsolutePath());
            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent, serviceConnection, getActivity().BIND_AUTO_CREATE);
        } else {
            player.resetMedia();
        }
    }

    @Override
    public void onItemLongClick(View view, File file, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Confirm");
        builder.setMessage("Are you sure delete file: " + file.getName() + " ? ");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                System.out.println(position);
                audioListAdapter.notifyItemRemoved(position);
                audioListAdapter.notifyItemRangeChanged(position, allFiles.length);
                allFiles = changeArray(position);
                audioListAdapter = new AudioListAdapter(allFiles, AudioListFragment.this);

                audioList.setHasFixedSize(true);
                audioList.setLayoutManager(new LinearLayoutManager(getContext()));
                audioList.setAdapter(audioListAdapter);


                String path = Environment.getExternalStorageDirectory().getPath();

                File directory = new File(path);
                File filedelete = new File(directory, file.getName());
                Toast.makeText(getContext(), "Bạn đã xoá thành công file" + file.getName(), Toast.LENGTH_SHORT).show();
                boolean deleted = filedelete.delete();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();


    }

    private File[] changeArray(int positionDelete) {
        File[] newFiles = new File[allFiles.length - 1];
        for (int i = 0, k = 0; i < allFiles.length; i++) {

            if (i == positionDelete) {
                continue;
            }

            // else copy the element
            newFiles[k++] = allFiles[i];
        }
        return newFiles;
    }


    public void pauseAudioUI() {
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.play, null));
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    public void resumeAudioUI() {
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.pause, null));
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    public void stopAudioUI() {
        //Stop The Audio
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.play, null));
        player_header_title.setText("Stopped");
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    public void setupUIAudio() {

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.pause, null));
        player_filename.setText(fileToPlay.getName());
        player_header_title.setText("Playing");

        if (player != null) {
            player_seekbar.setMax(player.mediaPlayer.getDuration());
        }

        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    public void setTitlePlayer() {
        player_header_title.setText("Finished");
    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                try {
                    if (player != null) {
                        player_seekbar.setProgress(player.mediaPlayer.getCurrentPosition());
                    }
                    seekbarHandler.postDelayed(this, 100);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    player.stopSelf();
                }
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( player != null ) {
            seekbarHandler.removeCallbacks(updateSeekbar);
        }
    }

    public void setPauseAudio() {
        if ( player != null ) {
            player.pauseMedia();
        }
    }

    @Override
    public void onClick(View v) {
        if (player != null) {
            boolean isPlaying = player.mediaPlayer.isPlaying();
            switch (v.getId()) {
                case R.id.player_play_btn:
                    if (isPlaying) {
                        player.pauseMedia();

                    } else {
                        if (fileToPlay != null) {
                            player.resumeMedia();
                        }
                    }
                    break;
                case R.id.player_back_btn:
                    if (positionFile != 0) {
                        positionFile--;
                        System.out.println(positionFile);
                    } else {
                        positionFile = allFiles.length - 1;
                    }
                    fileToPlay = allFiles[positionFile];
                    player.resetMedia();
                    break;
                case R.id.player_next_btn:
                    if (positionFile == 0) {
                        positionFile++;
                        fileToPlay = allFiles[positionFile];
                    } else {
                        if (positionFile == (allFiles.length - 1)) {
                            positionFile = 0;
                            fileToPlay = allFiles[positionFile];
                        } else {
                            positionFile++;
                            System.out.println(positionFile);
                            fileToPlay = allFiles[positionFile];

                        }
                    }
                    player.resetMedia();
                    break;
            }
        }
    }
}