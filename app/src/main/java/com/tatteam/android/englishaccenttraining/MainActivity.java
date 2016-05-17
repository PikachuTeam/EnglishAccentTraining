package com.tatteam.android.englishaccenttraining;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import tatteam.com.app_common.ads.AdsBigBannerHandler;
import tatteam.com.app_common.ads.AdsSmallBannerHandler;
import tatteam.com.app_common.util.AppConstant;
import tatteam.com.app_common.util.CloseAppHandler;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AdapterView.OnItemClickListener, ViewPager.OnPageChangeListener, CustomSeekBar.OnSeekbarChangeListener, CloseAppHandler.OnCloseAppListener {

    private static final boolean ADS_ENABLE = false;
    private static final int BIG_ADS_SHOWING_INTERVAL = 5;
    private static int BIG_ADS_SHOWING_COUNTER = 1;

    private CloseAppHandler closeAppHandler;
    private LinearLayout layout_MediaControl, layout_Record, layoutBtnRecord, layoutBtnPlayRecord;
    private LinearLayout btnYes, btnNo, btnOk, layoutBtnYN;

    private ImageButton btnPlayPause, btnNext, btnPrevious, btnReplay;
    private TextView tvLesson, tvCurrentDuration, tvDuration, tvDialog, tvDurationRecord;

    private Button btnRecord, btnPlayRecord, btnModeRc, btnModeListen;
    private View viewPage1, viewPage2, viewPage3;
    ArrayList<View> listSmallView = new ArrayList<>();

    private ListView lvLesson;

    //    private CustomSeekBar customSeekbar;
    private Handler seekbarHandler = new Handler();
    private Runnable runnable;

    private int soundPlaying = 0;
    private MediaPlayer player;
    private MediaPlayer recordPlayer;
    private MediaRecorder recorder;
    private String OUTPUT_FILE;

    private ArrayList<Lesson> lessonArrayList = new ArrayList<>();
    private ListLessonAdapter adapter;

    private MyPagerAdapter myPagerAdapter;

    private int isRepeat = 0;
    private boolean isShuffle = false;
    private View[] pages;

    private boolean isMediaPlayerPaused = false;
    private boolean isPlayerRecordPaused = false;
    private boolean recordStatus = false;
    private boolean recordPlaying = false;
    private boolean isRecord = false;

    private final int REPEAT_OFF = 0;
    private final int REPEAT_ONE = 1;
    private final int REPEAT_ALL = 2;

    //
    private AdsSmallBannerHandler adsSmallBannerHandler;
    private AdsBigBannerHandler adsBigBannerHandler;
    private FrameLayout adsContainer;
    //incoming call
    private PhoneStateListener phoneStateListener;
    TelephonyManager mgr;

    File outputFile;
    Dialog customDialog;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        OUTPUT_FILE = Environment.getExternalStorageDirectory() + "/" + lessonArrayList.get(soundPlaying).getLessonName() + ".3gpp";
        player = new MediaPlayer();
        recordPlayer = new MediaPlayer();
        LoadData();
        initViews();
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    player.pause();
                    isMediaPlayerPaused = true;
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    player.start();
                    isMediaPlayerPaused = false;
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    player.pause();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    private void initViews() {
        btnNext = (ImageButton) this.findViewById(R.id.btnNext);
        btnPlayPause = (ImageButton) this.findViewById(R.id.btnPlayPause);
        btnPrevious = (ImageButton) this.findViewById(R.id.btnPrevious);
        btnReplay = (ImageButton) this.findViewById(R.id.btnReplay);

        btnRecord = (Button) this.findViewById(R.id.btnRecord);
        btnPlayRecord = (Button) this.findViewById(R.id.btnPlayRecord);
        layoutBtnRecord = (LinearLayout) this.findViewById(R.id.layoutRecord);
        layoutBtnPlayRecord = (LinearLayout) this.findViewById(R.id.layoutPlayRecord);
        layout_MediaControl = (LinearLayout) this.findViewById(R.id.layoutControlMedia);
        layout_Record = (LinearLayout) this.findViewById(R.id.layoutRecordBtn);

        tvDurationRecord = (TextView) this.findViewById(R.id.tvDurationRecord);
        btnModeRc = (Button) this.findViewById(R.id.btnModeRecord);
        btnModeListen = (Button) this.findViewById(R.id.btnModeListen);

        tvLesson = (TextView) this.findViewById(R.id.tvLesson);
        tvCurrentDuration = (TextView) this.findViewById(R.id.tvCurrentDuration);
        tvDuration = (TextView) this.findViewById(R.id.tvDuration);
        Typeface face = Typeface.createFromAsset(getAssets(), "Cocogoose_trial.otf");

        tvLesson.setTypeface(face);
        tvLesson.setSelected(true);
        tvCurrentDuration.setTypeface(face);
        tvDuration.setTypeface(face);

        //Dialog
        customDialog = new Dialog(this, R.style.dialogstyle);
        customDialog.setContentView(R.layout.custom_dialog);
        customDialog.setCanceledOnTouchOutside(false);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tvDialog = (TextView) customDialog.findViewById(R.id.tvDialog);
        btnYes = (LinearLayout) customDialog.findViewById(R.id.btnYes);
        btnNo = (LinearLayout) customDialog.findViewById(R.id.btnNo);
        btnOk = (LinearLayout) customDialog.findViewById(R.id.btnOK);
        layoutBtnYN = (LinearLayout) customDialog.findViewById(R.id.layout_btnYN);

//        customSeekbar.setup();
//        customSeekbar.setSeekbarChangeListener(this);
        seekBar = (SeekBar) this.findViewById(R.id.seekBar);

        //init page id (indicatorView pager)
        viewPage1 = (View) this.findViewById(R.id.view_page_1);
        viewPage2 = (View) this.findViewById(R.id.view_page_2);
        viewPage3 = (View) this.findViewById(R.id.view_page_3);

        //init small indicatorView page
        listSmallView.add(viewPage1);
        listSmallView.add(viewPage2);
        listSmallView.add(viewPage3);

        //set adapter indicatorView pager
        pages = new View[3];
        pages[0] = View.inflate(this, R.layout.pager_list_lesson, null);
        pages[1] = View.inflate(this, R.layout.pager_transcription, null);
        pages[2] = View.inflate(this, R.layout.pager_reduced_speech, null);
        myPagerAdapter = new MyPagerAdapter(MainActivity.this, pages);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(myPagerAdapter);

        //event
        btnPlayPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnReplay.setOnClickListener(this);
        btnModeRc.setOnClickListener(this);
        btnModeListen.setOnClickListener(this);

        btnRecord.setOnClickListener(this);
        btnPlayRecord.setOnClickListener(this);
        pager.setOnPageChangeListener(this);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);

        recordPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnPlayRecord.setBackgroundResource(R.drawable.your_rc);
                recordPlaying = false;
                isPlayerRecordPaused = false;
                btnRecord.setEnabled(true);
            }
        });
        recordPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                recordPlayer.start();
            }
        });
        //close app
        closeAppHandler = new CloseAppHandler(this);
        closeAppHandler.setListener(this);

        //ads
        if (ADS_ENABLE) {
            adsContainer = (FrameLayout) this.findViewById(R.id.ads_container);
            adsSmallBannerHandler = new AdsSmallBannerHandler(this, adsContainer, AppConstant.AdsType.SMALL_BANNER_LANGUAGE_LEARNING);
            adsSmallBannerHandler.setup();

            adsBigBannerHandler = new AdsBigBannerHandler(this, AppConstant.AdsType.BIG_BANNER_LANGUAGE_LEARNING);
            adsBigBannerHandler.setup();
        }
    }

    private void showBigAdsIfNeeded() {
        if (ADS_ENABLE && adsBigBannerHandler != null) {
            if (BIG_ADS_SHOWING_COUNTER % BIG_ADS_SHOWING_INTERVAL == 0) {
                try {
                    adsBigBannerHandler.show();
                } catch (Exception ex) {
                }
            }
            BIG_ADS_SHOWING_COUNTER++;
        }
    }

//    Runnable run = new Runnable() {
//        @Override
//        public void run() {
//            updateSeekBar();
//        }
//    };

    private void updateSeekBar() {
        if (runnable == null) {
            runnable = new Runnable() {
                @Override
                public void run() {
//                    float percent = (float) player.getCurrentPosition() / (float) player.getDuration();
//                    if (percent > 0) {
//                        customSeekbar.updateIndicator(percent);
                    seekBar.setProgress(player.getCurrentPosition());
                    seekBar.setMax(player.getDuration());
                    tvCurrentDuration.setText(changeTime(player.getCurrentPosition()));
                    tvDuration.setText(changeTime(player.getDuration() - player.getCurrentPosition()));

//                    }
                    seekbarHandler.postDelayed(runnable, 30);

                }
            };
            seekbarHandler.postDelayed(runnable, 30);

        }
//        seekBar.setProgress(player.getCurrentPosition());
//        seekBar.setMax(player.getDuration());
//        tvCurrentDuration.setText(changeTime(player.getCurrentPosition()));
//        tvDuration.setText(changeTime(player.getDuration() - player.getCurrentPosition()));
//        seekbarHandler.postDelayed(run,30);
    }

    @Override
    protected void onDestroy() {
        player.stop();
        seekbarHandler.removeCallbacks(runnable);
//        DataSource.getInstance().destroy();
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        if (adsSmallBannerHandler != null) {
            adsSmallBannerHandler.destroy();
        }
        if (adsBigBannerHandler != null) {
            adsBigBannerHandler.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlayPause:
                if (player != null) {
                    if (player.isPlaying()) {
                        player.pause();
                        isMediaPlayerPaused = true;
                        btnPlayPause.setBackgroundResource(R.drawable.play_new);
                    } else {
                        if (isMediaPlayerPaused) {
                            player.start();
                            isMediaPlayerPaused = false;
                        } else {
                            playSound(soundPlaying);
                        }
                        btnPlayPause.setBackgroundResource(R.drawable.pause_new);
                    }
                } else {
                    playSound(1);
                    btnPlayPause.setBackgroundResource(R.drawable.pause_new);
                }
                break;
            case R.id.btnNext:
                if (isShuffle) {
                    playShuffle();
                    playSound(soundPlaying);
                } else {
                    playNextAround();
                }
                showBigAdsIfNeeded();
                break;
            case R.id.btnPrevious:
                if (isShuffle) {
                    playShuffle();
                    playSound(soundPlaying);
                } else {
                    playPrev();
                }
                showBigAdsIfNeeded();
                break;
            case R.id.btnReplay:
                if (isRepeat == 0) {
                    isRepeat = 1;
                    btnReplay.setBackgroundResource(R.drawable.replay_one);
                } else if (isRepeat == 1) {
                    isRepeat = 2;
                    btnReplay.setBackgroundResource(R.drawable.replay_all);
                } else {
                    isRepeat = 0;
                    btnReplay.setBackgroundResource(R.drawable.replay_off);
                }
                break;
//            case R.id.btnShuffle:
//                if (isShuffle) {
//                    isShuffle = false;
//                    btnShuffle.setBackgroundResource(R.drawable.shuffer_off);
//                } else {
//                    isShuffle = true;
//                    btnShuffle.setBackgroundResource(R.drawable.shuffer_on);
//                }
//                break;
//            case R.id.layout_MoreApp:
//                AppCommon.getInstance().openMoreAppDialog(this);
//                break;
            case R.id.btnModeListen:
//                if (recordPlayer.isPlaying()) {
//                    tvDialog.setText("Would you like to stop the record ?");
//                    btnOk.setVisibility(View.GONE);
//                    btnYes.setVisibility(View.VISIBLE);
//                    btnNo.setVisibility(View.VISIBLE);
//                    customDialog.show();
//                    btnYes.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            btnRecord.setEnabled(true);
//                            btnPlayRecord.setBackgroundResource(R.drawable.your_rc);
//                            if (recordPlayer != null) {
//                                recordPlayer.pause();
//                            }
//                            recordPlaying = false;
//                            isPlayerRecordPaused = true;
//                            customDialog.dismiss();
//                            progressItemListviewClick(position);
//                        }
//                    });
//                    btnNo.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            customDialog.dismiss();
//                        }
//                    });
//                } else if (recordStatus) {
//                    tvDialog.setText("Would you like to stop the record ?");
//                    btnOk.setVisibility(View.GONE);
//                    btnYes.setVisibility(View.VISIBLE);
//                    btnNo.setVisibility(View.VISIBLE);
//                    customDialog.show();
//                    btnYes.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (recorder != null) {
//                                btnRecord.setBackgroundResource(R.drawable.start_rc);
//                                recorder.stop();
//                                btnPlayRecord.setEnabled(true);
//                                btnPlayRecord.setBackgroundResource(R.drawable.your_rc);
//                                recordStatus = false;
//                                customDialog.dismiss();
//                                progressItemListviewClick(position);
//                            }
//                        }
//                    });
//                    btnNo.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            customDialog.dismiss();
//                        }
//                    });
                if (isRecord) {
                    if (recordStatus) {
                        tvDialog.setText("You must stop recording !!!");
                        layoutBtnYN.setVisibility(View.GONE);
                        btnOk.setVisibility(View.VISIBLE);
                        customDialog.show();
                        btnOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                            }
                        });
                    } else if (recordPlayer.isPlaying()) {
                        tvDialog.setText("You must stop recording !!!");
                        layoutBtnYN.setVisibility(View.GONE);
                        btnOk.setVisibility(View.VISIBLE);
                        customDialog.show();
                        btnOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                            }
                        });
                    } else {
                        layout_Record.setVisibility(View.GONE);
                        layout_MediaControl.setVisibility(View.VISIBLE);

                        isRecord = false;
                    }
                }
                break;
            case R.id.btnModeRecord:
//                else {
                if (!isRecord) {
                    if (player.isPlaying()) {
                        tvDialog.setText("Would you like to pause the playing track ?");
                        layoutBtnYN.setVisibility(View.VISIBLE);
                        btnOk.setVisibility(View.GONE);
                        customDialog.show();
                        btnYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                player.pause();
                                btnPlayPause.setBackgroundResource(R.drawable.play_new);
                                customDialog.dismiss();
                            }
                        });
                        btnNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                            }
                        });
                    }
                    layout_Record.setVisibility(View.VISIBLE);
                    layout_MediaControl.setVisibility(View.GONE);

                    OUTPUT_FILE = Environment.getExternalStorageDirectory() + "/" + lessonArrayList.get(soundPlaying).getLessonName() + ".3gpp";
//                    outputFile = new File(OUTPUT_FILE);
                    if (checkFileExist()) {
                        btnPlayRecord.setEnabled(true);
                        btnPlayRecord.setBackgroundResource(R.drawable.your_rc);
                    } else {
                        btnPlayRecord.setEnabled(false);
                        btnPlayRecord.setBackgroundResource(R.drawable.your_rc_off);

                    }
                    isRecord = true;
                }
                break;
            case R.id.btnRecord:
                btnRecord.setBackgroundResource(R.drawable.stop_rc);
                if (recordStatus) {
                    btnRecord.setBackgroundResource(R.drawable.start_rc);
                    if (recorder != null) {
                        recorder.stop();
                        btnPlayRecord.setEnabled(true);
                        btnPlayRecord.setBackgroundResource(R.drawable.your_rc);
                        recordStatus = false;
                    }
                } else {
//                    if (player.isPlaying()) {
//                        player.pause();
//                        isMediaPlayerPaused = true;
//                        btnPlayPause.setBackgroundResource(R.drawable.play_new);
//                    }
                    if (player.isPlaying()) {
                        tvDialog.setText(R.string.pause_media);
                        layoutBtnYN.setVisibility(View.VISIBLE);
                        btnOk.setVisibility(View.GONE);
                        customDialog.show();
                        btnYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                player.pause();
                                isMediaPlayerPaused = true;
                                btnPlayPause.setBackgroundResource(R.drawable.play_new);
                                customDialog.dismiss();
                                startRecord();
                            }
                        });
                        btnNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                                startRecord();
                            }
                        });
                    } else {
                        startRecord();
                    }

                }
                break;
            case R.id.btnPlayRecord:
                if (recordPlaying) {
                    btnRecord.setEnabled(true);
                    btnPlayRecord.setBackgroundResource(R.drawable.your_rc);
                    if (recordPlayer != null) {
                        recordPlayer.pause();
                    }
                    recordPlaying = false;
                    isPlayerRecordPaused = true;
                } else {
                    if (player.isPlaying()) {
                        player.pause();
                        isMediaPlayerPaused = true;
                        btnPlayPause.setBackgroundResource(R.drawable.play_new);
                    }
                    btnRecord.setEnabled(false);
                    btnPlayRecord.setBackgroundResource(R.drawable.rc_playing);
                    if (recordPlayer != null) {
                        if (isPlayerRecordPaused) {
//                            outputFile = new File(OUTPUT_FILE);
                            if (checkFileExist()) {
                                btnPlayRecord.setEnabled(true);
                                btnPlayRecord.setBackgroundResource(R.drawable.your_rc);
                                recordPlayer.start();
                                isPlayerRecordPaused = false;
                                recordPlaying = true;
                            } else {
                                btnPlayRecord.setEnabled(false);
                                btnPlayRecord.setBackgroundResource(R.drawable.your_rc_off);
                            }
                        } else {
                            recordPlaying = true;
                            recordPlayer.reset();
                            try {
                                recordPlayer.setDataSource(OUTPUT_FILE);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            recordPlayer.prepareAsync();
                        }
                    }
                }
                break;
        }
    }

    private void startRecord() {
        btnPlayRecord.setEnabled(false);
        btnPlayRecord.setBackgroundResource(R.drawable.your_rc_off);
        isPlayerRecordPaused = false;
        btnRecord.setBackgroundResource(R.drawable.stop_rc);
        if (recorder != null) {
            recorder.release();
        }
//                    OUTPUT_FILE = Environment.getExternalStorageDirectory() + "/" + lessonArrayList.get(soundPlaying).getLessonName() + ".3gpp";
//        outputFile = new File(OUTPUT_FILE);
        if (checkFileExist()) {
            outputFile.delete();
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(OUTPUT_FILE);
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
        recordStatus = true;
    }

    private String changeTime(int duration) {
        String newTime;
        int totalSec = duration / 1000;
        int minute = totalSec / 60;
        int sec = totalSec % 60;
        if (minute < 10) {
            if (sec < 10) {
                newTime = "0" + minute + ":0" + sec;
                return newTime;
            } else {
                newTime = "0" + minute + ":" + sec;
                return newTime;
            }
        } else {
            if (sec < 10) {
                newTime = minute + ":0" + sec;
                return newTime;
            } else {
                newTime = minute + ":" + sec;
                return newTime;
            }
        }


    }

    //    private void setDurationRecord(){
//        try {
//            recordPlayer.setDataSource(OUTPUT_FILE);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        tvDurationRecord.setText(changeTime(recordPlayer.getDuration())+"");
//    }
    private void checkRecordStatusFile() {
        if (recordPlayer.isPlaying()) {
            recordPlayer.pause();
            isPlayerRecordPaused = true;
            btnPlayRecord.setBackgroundResource(R.drawable.your_rc);
        } else if (isPlayerRecordPaused) {
            if (checkFileExist()) {
                isPlayerRecordPaused = false;
                btnPlayRecord.setEnabled(true);
                btnPlayRecord.setBackgroundResource(R.drawable.your_rc);
                recordPlaying = false;
            } else {
                isPlayerRecordPaused = false;
                btnPlayRecord.setEnabled(false);
                btnPlayRecord.setBackgroundResource(R.drawable.your_rc_off);
                recordPlaying = false;
            }
        } else {
            if (checkFileExist()) {
                btnPlayRecord.setEnabled(true);
                btnPlayRecord.setBackgroundResource(R.drawable.your_rc);
            } else {
                btnPlayRecord.setEnabled(false);
                btnPlayRecord.setBackgroundResource(R.drawable.your_rc_off);
            }
        }
    }

    private boolean checkFileExist() {
        outputFile = new File(OUTPUT_FILE);
        if (outputFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void playSound(int soundPlaying) {
        OUTPUT_FILE = Environment.getExternalStorageDirectory() + "/" + lessonArrayList.get(soundPlaying).getLessonName() + ".3gpp";
        checkRecordStatusFile();
//        if(recordPlayer.isPlaying()){
//            recordPlayer.pause();
//            isPlayerRecordPaused = true;
//            btnPlayRecord.setBackgroundResource(R.drawable.play_new);
//        }else if(isPlayerRecordPaused){
//            outputFile = new File(OUTPUT_FILE);
//            if (outputFile.exists()){
//                isPlayerRecordPaused = false;
//                btnPlayRecord.setEnabled(true);
//            }else {
//                isPlayerRecordPaused = false;
//                btnPlayRecord.setEnabled(false);
//            }
//        }
        player.reset();
        Lesson lesson = lessonArrayList.get(soundPlaying);

        lesson.setUri(lesson.getMusicName());
        Uri uri = Uri.parse(lesson.getUri());

        lesson.setIsPlay(true);

        adapter.notifyDataSetChanged();
        updatePage23();

        try {
            player.setDataSource(getApplicationContext(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            player.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
        btnPlayPause.setBackgroundResource(R.drawable.pause_new);
        tvLesson.setText(lessonArrayList.get(soundPlaying).getLessonName());
//        tvAppName.setVisibility(View.INVISIBLE);
        updateSeekBar();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
    }

    private void playNextAround() {
        soundPlaying++;
        if (soundPlaying >= lessonArrayList.size()) {
            lessonArrayList.get(lessonArrayList.size() - 1).setIsPlay(false);
            soundPlaying = 0;
        } else {
            lessonArrayList.get(soundPlaying - 1).setIsPlay(false);
        }
        playSound(soundPlaying);
        tvLesson.setText(lessonArrayList.get(soundPlaying).getLessonName());
    }

    private void playNext() {
        soundPlaying++;
        if (soundPlaying >= lessonArrayList.size()) {
            soundPlaying--;
            player.stop();
        } else {
            lessonArrayList.get(soundPlaying - 1).setIsPlay(false);
            playSound(soundPlaying);
        }
        tvLesson.setText(lessonArrayList.get(soundPlaying).getLessonName());

    }

    private void playPrev() {
        soundPlaying--;
        if (soundPlaying < 0) {
            lessonArrayList.get(0).setIsPlay(false);
            soundPlaying = lessonArrayList.size() - 1;
        } else {
            lessonArrayList.get(soundPlaying + 1).setIsPlay(false);
        }
        playSound(soundPlaying);
        tvLesson.setText(lessonArrayList.get(soundPlaying).getLessonName());
    }

    private void playShuffle() {
        for (int i = 0; i < lessonArrayList.size(); i++) {
            lessonArrayList.get(i).setIsPlay(false);
        }
        int sound = soundPlaying;
        Random random = new Random();

        for (int j = 0; j < lessonArrayList.size(); j++) {
            soundPlaying = random.nextInt(lessonArrayList.size());
            if (sound != soundPlaying) {
                break;
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isRepeat == REPEAT_OFF) {
            player.stop();
            btnPlayPause.setBackgroundResource(R.drawable.play_new);
        } else if (isRepeat == REPEAT_ONE) {
            playSound(soundPlaying);
        } else {
            if (isShuffle) {
                playShuffle();
                playSound(soundPlaying);
            } else {
                playNextAround();
            }
        }
//        customSeekbar.updateIndicator(0.0f);
    }

    private void LoadData() {
        lessonArrayList = DataSource.getListLesson();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        showBigAdsIfNeeded();
        if (!isRecord) {
            progressItemListviewClick(position);
        }
    }

    private void progressItemListviewClick(int position) {
        for (int i = 0; i < lessonArrayList.size(); i++) {
            lessonArrayList.get(i).setIsPlay(false);
        }
        tvLesson.setText(lessonArrayList.get(position).getLessonName());
        playSound(position);
        soundPlaying = position;
        btnPlayPause.setBackgroundResource(R.drawable.pause_new);
        lessonArrayList.get(position).setIsPlay(true);
        adapter.notifyDataSetChanged();
        updatePage23();
    }

    private void updatePage23() {
        ((TextView) pages[1].findViewById(R.id.tvTranscription)).setText(lessonArrayList.get(soundPlaying).getTranscription());
        ((TextView) pages[2].findViewById(R.id.tvReducedSpeech)).setText(Html.fromHtml(lessonArrayList.get(soundPlaying).getReducedSpeech()));
    }

    //set page change listener
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < listSmallView.size(); i++) {
            listSmallView.get(i).setVisibility(View.INVISIBLE);
        }
        listSmallView.get(position).setVisibility(View.VISIBLE);
//        if(position == 2){
//            layout_MediaControl.setVisibility(View.GONE);
//            layout_Record.setVisibility(View.VISIBLE);
//        }else
//        {
//            layout_MediaControl.setVisibility(View.VISIBLE);
//            layout_Record.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSeekbarChangeListener(int smallViewWidth, int parentWidth) {
        float percent = (float) smallViewWidth / parentWidth;
        if (player != null && player.isPlaying()) {
            player.seekTo((int) (player.getDuration() * percent));
        }
    }

    @Override
    public void onBackPressed() {
        closeAppHandler.setKeyBackPress(this);
    }

    @Override
    public void onRateAppDialogClose() {
        finish();
    }

    @Override
    public void onTryToCloseApp() {
        Toast.makeText(this, "Press once again to exit!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReallyWantToCloseApp() {
        finish();
    }

    //adapter listview
    private class ListLessonAdapter extends BaseAdapter {
        Context mContext;
        ArrayList<Lesson> lessons;
        LayoutInflater inflater;


        public ListLessonAdapter(Context context, ArrayList<Lesson> objects) {
            this.mContext = context;
            this.lessons = objects;
            inflater = LayoutInflater.from(this.mContext);
        }

        @Override
        public int getCount() {
            return lessons.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyViewHolder mViewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_lesson_row_item, null);
                mViewHolder = new MyViewHolder();
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (MyViewHolder) convertView.getTag();
            }

            mViewHolder.tvLessonName = (TextView) convertView.findViewById(R.id.tvLessonName);
            mViewHolder.tvDurationLesson = (TextView) convertView.findViewById(R.id.tv_Duration_Lesson);
            mViewHolder.tvPlaying = (TextView) convertView.findViewById(R.id.tv_Playing);
            mViewHolder.viewPlaying = (View) convertView.findViewById(R.id.view_Playing);
            mViewHolder.imgLesson = (ImageView) convertView.findViewById(R.id.imgLesson);

            Typeface listType = Typeface.createFromAsset(getAssets(), "UTM_Avo.ttf");
            Typeface textPlaying = Typeface.createFromAsset(getAssets(), "Cocogoose_trial.otf");
            mViewHolder.tvLessonName.setText(lessons.get(position).getLessonName() + "");
            mViewHolder.tvLessonName.setTypeface(listType);
            mViewHolder.tvPlaying.setTypeface(textPlaying);
            mViewHolder.imgLesson.setImageResource(lessons.get(position).getImageLesson());
            if (lessons.get(position).isPlay()) {
                mViewHolder.tvPlaying.setVisibility(View.VISIBLE);
                mViewHolder.viewPlaying.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.tvPlaying.setVisibility(View.INVISIBLE);
                mViewHolder.viewPlaying.setVisibility(View.GONE);
            }

            mViewHolder.tvLessonName.setSelected(true);

            return convertView;
        }

        private class MyViewHolder {
            TextView tvLessonName;
            TextView tvDurationLesson;
            TextView tvPlaying;
            ImageView imgLesson;
            View viewPlaying;
        }
    }

    //adapter indicatorView pager
    private class MyPagerAdapter extends PagerAdapter {
        Context context;
        View[] pages;

        public MyPagerAdapter(Context context, View[] pages) {
            this.context = context;
            this.pages = pages;
        }

        public Object instantiateItem(ViewGroup collection, int position) {
            View layout = pages[position];
            Typeface listType = Typeface.createFromAsset(getAssets(), "MARI&DAVID.ttf");
            Typeface textType = Typeface.createFromAsset(getAssets(), "UTM_Avo.ttf");
            if (position == 0) {
                lvLesson = (ListView) layout.findViewById(R.id.listLesson);
                adapter = new ListLessonAdapter(MainActivity.this, lessonArrayList);
                lvLesson.setAdapter(adapter);
                lvLesson.setOnItemClickListener(MainActivity.this);

            } else if (position == 1) {
                TextView tvTrans = (TextView) layout.findViewById(R.id.tvTrans);
                TextView tvTranscription = (TextView) layout.findViewById(R.id.tvTranscription);
                tvTrans.setTypeface(textType);
//                tvTranscription.setTypeface(textType);
                tvTranscription.setText(lessonArrayList.get(soundPlaying).getTranscription() + "");
            } else {
                TextView tvSpeech = (TextView) layout.findViewById(R.id.tvSpeech);
                TextView tvReducedSpeech = (TextView) layout.findViewById(R.id.tvReducedSpeech);
                tvSpeech.setTypeface(textType);
//                tvReducedSpeech.setTypeface(textType);
                tvReducedSpeech.setText(Html.fromHtml(lessonArrayList.get(soundPlaying).getReducedSpeech() + ""));
            }
            collection.addView(layout);
            return layout;

        }

        @Override
        public int getCount() {
            return pages.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        //update page immediately
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}