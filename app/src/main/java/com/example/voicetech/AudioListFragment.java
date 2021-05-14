package com.example.voicetech;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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


public class AudioListFragment extends Fragment implements AudioListAdapter.onItemListClick {
    private TextView player_header_title;
    private TextView player_filename;

    private ImageButton player_play_btn, player_back_btn, player_next_btn;

    private SeekBar player_seekbar;

    private ConstraintLayout player_sheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private RecyclerView audioList;
    private File[] allFiles;
    private AudioListAdapter audioListAdapter;

    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private File fileToPlay = null;

    private Handler seekbarHandler;
    private Runnable updateSeekbar;
    private int positionFile = 0;

    public AudioListFragment() {
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
        System.out.println(allFiles.length);

        audioListAdapter = new AudioListAdapter(allFiles, this);

        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(audioListAdapter);

//        player_play_btn.setClickable(false);
//        player_play_btn.setEnabled(false);
//        player_back_btn.setClickable(false);
//        player_back_btn.setEnabled(false);
//        player_next_btn.setClickable(false);
//        player_next_btn.setEnabled(false);
//        player_seekbar.setClickable(false);
//        player_seekbar.setEnabled(false);

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

        player_play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pauseAudio();
                } else {
                    if (fileToPlay != null) {
                        resumeAudio();
                    }
                }
            }
        });
        player_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positionFile == 0) {
                    fileToPlay = allFiles[positionFile];
                    if (isPlaying) {
                        stopAudio();
                        playAudio(fileToPlay);
                    } else {
                        playAudio(fileToPlay);
                    }
                } else {
                    positionFile--;
                    System.out.println(positionFile);
                    fileToPlay = allFiles[positionFile];
                    if (isPlaying) {
                        stopAudio();
                        playAudio(fileToPlay);
                    } else {
                        playAudio(fileToPlay);
                    }
                }
            }
        });
        player_next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positionFile == 0) {
                    fileToPlay = allFiles[positionFile];
                    if (isPlaying) {
                        stopAudio();
                        playAudio(fileToPlay);
                    } else {
                        playAudio(fileToPlay);
                    }
                    positionFile++;

                } else {
                    if (positionFile == (allFiles.length - 1)) {
                        positionFile = 0;
                        fileToPlay = allFiles[positionFile];
                        if (isPlaying) {
                            stopAudio();
                            playAudio(fileToPlay);
                        } else {
                            playAudio(fileToPlay);
                        }
                    } else {
                        System.out.println(positionFile);
                        fileToPlay = allFiles[positionFile];
                        if (isPlaying) {
                            stopAudio();
                            playAudio(fileToPlay);
                        } else {
                            playAudio(fileToPlay);
                        }
                        positionFile++;

                    }
                }

            }
        });

        player_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);
                resumeAudio();
            }
        });
    }

    @Override
    public void onClickListener(File file, int position) {
//        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
//        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        positionFile = position;
        System.out.println(positionFile);

        fileToPlay = file;
        if (isPlaying) {
            stopAudio();
            playAudio(fileToPlay);
        } else {
            playAudio(fileToPlay);
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

    private void pauseAudio() {
        mediaPlayer.pause();
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.play, null));
        isPlaying = false;
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void resumeAudio() {
        mediaPlayer.start();
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.pause, null));
        isPlaying = true;

        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    private void stopAudio() {
        //Stop The Audio
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.play, null));
        player_header_title.setText("Stopped");
        isPlaying = false;
        mediaPlayer.stop();
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void playAudio(File fileToPlay) {

        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player_play_btn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.pause, null));
        player_filename.setText(fileToPlay.getName());
        player_header_title.setText("Playing");
        //Play the audio
        isPlaying = true;
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopAudio();
                player_header_title.setText("Finished");
            }
        });

        player_seekbar.setMax(mediaPlayer.getDuration());

        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                player_seekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 100);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isPlaying) {
            stopAudio();
        }
    }

}