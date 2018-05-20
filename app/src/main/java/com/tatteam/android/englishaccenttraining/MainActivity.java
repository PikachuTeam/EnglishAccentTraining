package com.tatteam.android.englishaccenttraining;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.tatteam.android.englishaccenttraining.chat.RenameDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import tatteam.com.app_common.ads.AdsBigBannerHandler;
import tatteam.com.app_common.ads.AdsSmallBannerHandler;
import tatteam.com.app_common.util.AppConstant;
import tatteam.com.app_common.util.CommonUtil;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AdapterView.OnItemClickListener, ViewPager.OnPageChangeListener, SeekBar.OnSeekBarChangeListener {

  public static AppConstant.AdsType ADS_TYPE_SMALL;
  public static AppConstant.AdsType ADS_TYPE_BIG;

  private static final int BIG_ADS_SHOWING_INTERVAL = 5;
  private static int BIG_ADS_SHOWING_COUNTER = 1;

  private LinearLayout layout_MediaControl, layout_Record, layoutBtnRecord, layoutBtnPlayRecord;
  private LinearLayout btnYes, btnNo, btnOk, layoutBtnYN, btnBack, btnShare;

  private ImageButton btnPlayPause, btnNext, btnPrevious, btnReplay;
  private TextView tvLesson, tvCurrentDuration, tvDuration, tvDialog, tvDurationRecord;

  private Button btnModeListen;
  private ImageView btnModeRc;
  private TextView btnRecord, btnPlayRecord;
  private View viewPage1, viewPage2, viewPage3;
  ArrayList<View> listSmallView = new ArrayList<>();
  private ListView lvLesson;

  private Handler seekbarHandler = new Handler();
  private Handler recordHandler = new Handler();
  private Runnable runnable, recordRunable;

  private int soundPlaying = 0;
  private MediaPlayer player;
  private MediaPlayer recordPlayer;
  private MediaRecorder recorder;
  private String OUTPUT_FILE;

  private ArrayList<Lesson> lessonArrayList = new ArrayList<>();
  private ListLessonAdapter adapter;

  private MyPagerAdapter myPagerAdapter;
  private LinearLayout layoutViewPage1, layoutViewPage2, layoutViewPage3;
  ViewPager pager;

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

  private final int YOUR_RC_ON = 1;
  private final int YOUR_RC_OFF = 2;
  private final int YOUR_RC_PLAYING = 3;
  private final int START_RC_ON = 4;
  private final int START_RC_OFF = 5;
  private final int STOP_RC = 6;

  //Request permission
  private final int PERMISSION_REQUEST_CODE = 1;
  //
  private AdsSmallBannerHandler adsSmallBannerHandler1;
  private AdsSmallBannerHandler adsSmallBannerHandler2;

  private AdsBigBannerHandler adsBigBannerHandler;
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
          adapter.notifyDataSetChanged();
//                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
//                    player.start();
//                    isMediaPlayerPaused = false;
        } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
          player.pause();
          isMediaPlayerPaused = true;
          adapter.notifyDataSetChanged();
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

    btnRecord = (TextView) this.findViewById(R.id.btnRecord);
    btnPlayRecord = (TextView) this.findViewById(R.id.btnPlayRecord);
    layoutBtnRecord = (LinearLayout) this.findViewById(R.id.layoutRecord);
    layoutBtnPlayRecord = (LinearLayout) this.findViewById(R.id.layoutPlayRecord);
    layout_MediaControl = (LinearLayout) this.findViewById(R.id.layoutControlMedia);
    layout_Record = (LinearLayout) this.findViewById(R.id.layoutRecordBtn);

    tvDurationRecord = (TextView) this.findViewById(R.id.tvDurationRecord);
    btnModeRc = (ImageView) this.findViewById(R.id.btnModeRecord);
    btnModeRc.setEnabled(false);
    btnModeListen = (Button) this.findViewById(R.id.btnModeListen);

    tvLesson = (TextView) this.findViewById(R.id.tvLesson);
    tvCurrentDuration = (TextView) this.findViewById(R.id.tvCurrentDuration);
    tvDuration = (TextView) this.findViewById(R.id.tvDuration);
    Typeface face = Typeface.createFromAsset(getAssets(), "Cocogoose_trial.otf");

    tvLesson.setTypeface(face);
    tvLesson.setSelected(true);
    tvCurrentDuration.setTypeface(face);
    tvDuration.setTypeface(face);

    btnBack = (LinearLayout) this.findViewById(R.id.btnBackpress);
    btnShare = (LinearLayout) this.findViewById(R.id.btnShare);
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
    seekBar.setOnSeekBarChangeListener(this);
    //set adapter indicatorView pager
    pages = new View[3];
    pages[0] = View.inflate(this, R.layout.pager_list_lesson, null);
    pages[1] = View.inflate(this, R.layout.pager_transcription, null);
    pages[2] = View.inflate(this, R.layout.pager_reduced_speech, null);
    myPagerAdapter = new MyPagerAdapter(MainActivity.this, pages);
    pager = (ViewPager) findViewById(R.id.pager);
    pager.setAdapter(myPagerAdapter);

    //init page id (indicatorView pager)
    viewPage1 = (View) this.findViewById(R.id.view_page_1);
    viewPage2 = (View) this.findViewById(R.id.view_page_2);
    viewPage3 = (View) this.findViewById(R.id.view_page_3);

    layoutViewPage1 = (LinearLayout) this.findViewById(R.id.layout_view_page1);
    layoutViewPage2 = (LinearLayout) this.findViewById(R.id.layout_view_page2);
    layoutViewPage3 = (LinearLayout) this.findViewById(R.id.layout_view_page3);

    layoutViewPage1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pager.setCurrentItem(0, true);
      }
    });
    layoutViewPage2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pager.setCurrentItem(1, true);
      }
    });
    layoutViewPage3.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pager.setCurrentItem(2, true);
      }
    });
    //init small indicatorView page
    listSmallView.add(viewPage1);
    listSmallView.add(viewPage2);
    listSmallView.add(viewPage3);

    //event
    btnPlayPause.setOnClickListener(this);
    btnNext.setOnClickListener(this);
    btnPrevious.setOnClickListener(this);
    btnReplay.setOnClickListener(this);
    btnModeRc.setOnClickListener(this);
    btnModeListen.setOnClickListener(this);

    btnBack.setOnClickListener(this);
    btnShare.setOnClickListener(this);

    layoutBtnRecord.setOnClickListener(this);
    btnPlayRecord.setOnClickListener(this);
    pager.setOnPageChangeListener(this);

    player.setOnPreparedListener(this);
    player.setOnCompletionListener(this);

    recordPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        changeRecrodTextUI(YOUR_RC_ON);
        recordPlaying = false;
        isPlayerRecordPaused = false;
        btnRecord.setEnabled(true);
        layoutBtnRecord.setEnabled(true);
        changeRecrodTextUI(START_RC_ON);
        recordHandler.removeCallbacks(recordRunable);
        btnModeListen.setEnabled(true);
        btnModeListen.setBackgroundResource(R.drawable.listen);
      }
    });
    recordPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        recordPlayer.start();
        btnModeListen.setEnabled(false);
        btnModeListen.setBackgroundResource(R.drawable.listen_off);
        updateRecordText();
        recordHandler.postDelayed(recordRunable, 30);
      }
    });

    //ads
    adsSmallBannerHandler1 = new AdsSmallBannerHandler(this, (ViewGroup) pages[1].findViewById(R.id.ads_container), ADS_TYPE_SMALL, AdSize.MEDIUM_RECTANGLE);
    adsSmallBannerHandler1.setup();

    adsSmallBannerHandler2 = new AdsSmallBannerHandler(this, (ViewGroup) pages[2].findViewById(R.id.ads_container), ADS_TYPE_SMALL, AdSize.MEDIUM_RECTANGLE);
    adsSmallBannerHandler2.setup();

    adsBigBannerHandler = new AdsBigBannerHandler(this, ADS_TYPE_BIG);
    adsBigBannerHandler.setup();

    findViewById(R.id.btn_join_chat).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showDialogRename();
      }
    });
  }

  private void showDialogRename() {
    RenameDialog renameDialog = new RenameDialog(this);
    renameDialog.show();
  }

  private void showBigAdsIfNeeded() {
    if (adsBigBannerHandler != null) {
      if (BIG_ADS_SHOWING_COUNTER % BIG_ADS_SHOWING_INTERVAL == 0) {
        try {
          adsBigBannerHandler.show();
        } catch (Exception ex) {
        }
      }
      BIG_ADS_SHOWING_COUNTER++;
    }
  }

  private void updateRecordText() {
    if (recordRunable == null) {
      recordRunable = new Runnable() {
        @Override
        public void run() {
          tvDurationRecord.setText(changeTime(recordPlayer.getCurrentPosition()));
//                    tvDurationRecord.setText(changeTime(recordPlayer.getDuration() - recordPlayer.getCurrentPosition()));
          recordHandler.postDelayed(recordRunable, 30);
        }
      };
      recordHandler.postDelayed(recordRunable, 30);
    }
  }

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
  }

  @Override
  protected void onStop() {
    super.onStop();
    try {
      recorder.stop();
    } catch (RuntimeException e) {

    }
    btnPlayRecord.setEnabled(true);
    layoutBtnPlayRecord.setEnabled(true);
    changeRecrodTextUI(START_RC_ON);
    changeRecrodTextUI(YOUR_RC_ON);
    recordStatus = false;
    btnModeListen.setEnabled(true);
    btnModeListen.setBackgroundResource(R.drawable.listen);
  }

  @Override
  protected void onDestroy() {
    player.stop();
    recordPlayer.stop();
    seekbarHandler.removeCallbacks(runnable);
    recordHandler.removeCallbacks(recordRunable);
//        DataSource.getInstance().destroy();
    if (mgr != null) {
      mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }
    if (adsSmallBannerHandler1 != null) {
      adsSmallBannerHandler1.destroy();
    }
    if (adsSmallBannerHandler2 != null) {
      adsSmallBannerHandler2.destroy();
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
            adapter.notifyDataSetChanged();
          } else {
            if (isMediaPlayerPaused) {
              player.start();
              isMediaPlayerPaused = false;
              adapter.notifyDataSetChanged();
            } else {
              playSound(soundPlaying);
            }
            btnPlayPause.setBackgroundResource(R.drawable.pause_new);
          }
        } else {
          playSound(1);
          isMediaPlayerPaused = false;
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
          btnReplay.setBackgroundResource(R.drawable.replay_1);
        } else if (isRepeat == 1) {
          isRepeat = 2;
          btnReplay.setBackgroundResource(R.drawable.replay_on);
        } else {
          isRepeat = 0;
          btnReplay.setBackgroundResource(R.drawable.replay_off);
        }
        break;
      case R.id.btnShare:
        String androidLink = "https://play.google.com/store/apps/details?id=" + getPackageName();
        String sharedText = getString(R.string.app_name) + ".\nAndroid: " + androidLink;
        CommonUtil.sharePlainText(this, sharedText);
        break;
      case R.id.btnBackpress:
        finish();
        break;
      case R.id.btnModeListen:
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
            adapter.notifyDataSetChanged();
          }
        }

        break;
      case R.id.btnModeRecord:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          if (ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"}, PERMISSION_REQUEST_CODE);
          } else {
            openModeRecord();
          }
        } else {
          openModeRecord();
        }
        break;
      case R.id.layoutRecord:
        if (recordStatus) {
          if (recorder != null) {
            if (checkFileExist()) {
              try {
                recorder.stop();
              } catch (RuntimeException e) {
              }
            }
            btnPlayRecord.setEnabled(true);
            layoutBtnPlayRecord.setEnabled(true);
            changeRecrodTextUI(START_RC_ON);
            changeRecrodTextUI(YOUR_RC_ON);
            recordStatus = false;
            btnModeListen.setEnabled(true);
            btnModeListen.setBackgroundResource(R.drawable.listen);
          }
        } else {
          startRecord();
        }
        break;
      case R.id.btnPlayRecord:
        if (recordPlaying) {
          btnRecord.setEnabled(true);
          layoutBtnRecord.setEnabled(true);
          changeRecrodTextUI(START_RC_ON);
          changeRecrodTextUI(YOUR_RC_ON);
          if (recordPlayer != null) {
            recordPlayer.pause();
            btnModeListen.setEnabled(true);
            btnModeListen.setBackgroundResource(R.drawable.listen);
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
          layoutBtnRecord.setEnabled(false);
          changeRecrodTextUI(START_RC_OFF);
          changeRecrodTextUI(YOUR_RC_PLAYING);

          if (recordPlayer != null) {
            if (isPlayerRecordPaused) {
              if (checkFileExist()) {
                btnPlayRecord.setEnabled(true);
                layoutBtnPlayRecord.setEnabled(true);
                changeRecrodTextUI(YOUR_RC_ON);
                recordPlayer.start();
                btnModeListen.setEnabled(false);
                btnModeListen.setBackgroundResource(R.drawable.listen_off);
                isPlayerRecordPaused = false;
                recordPlaying = true;
              } else {
                btnPlayRecord.setEnabled(false);
                layoutBtnPlayRecord.setEnabled(false);
                changeRecrodTextUI(YOUR_RC_OFF);
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
              isPlayerRecordPaused = false;
            }
          }
        }
        break;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
      case PERMISSION_REQUEST_CODE:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          openModeRecord();
        } else {
          Toast.makeText(MainActivity.this, "Permission Denied,Please allow in App Settings for additional functionality", Toast.LENGTH_SHORT).show();
        }
    }
  }

  private void openModeRecord() {
    if (!isRecord) {
      if (player.isPlaying()) {
        player.pause();
        isMediaPlayerPaused = true;
        btnPlayPause.setBackgroundResource(R.drawable.play_new);
      }
      layout_Record.setVisibility(View.VISIBLE);
      layout_MediaControl.setVisibility(View.GONE);

      OUTPUT_FILE = Environment.getExternalStorageDirectory() + "/" + lessonArrayList.get(soundPlaying).getLessonName() + ".3gpp";
      if (checkFileExist()) {
        btnPlayRecord.setEnabled(true);
        layoutBtnPlayRecord.setEnabled(true);
        changeRecrodTextUI(YOUR_RC_ON);
      } else {
        btnPlayRecord.setEnabled(false);
        layoutBtnPlayRecord.setEnabled(false);
        changeRecrodTextUI(YOUR_RC_OFF);
      }
      isRecord = true;
      adapter.notifyDataSetChanged();
    }
  }


  private void startRecord() {
    tvDurationRecord.setText("-- : --");
    isPlayerRecordPaused = false;
    btnPlayRecord.setEnabled(false);
    layoutBtnPlayRecord.setEnabled(false);

    btnModeListen.setEnabled(false);
    btnModeListen.setBackgroundResource(R.drawable.listen_off);
    changeRecrodTextUI(STOP_RC);
    changeRecrodTextUI(YOUR_RC_OFF);

    if (recorder != null) {
      recorder.release();
    }
    if (checkFileExist()) {
      outputFile.delete();
    }
    recorder = new MediaRecorder();

    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
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

  private void changeRecrodTextUI(int key) {
    switch (key) {
      case YOUR_RC_ON:
        btnPlayRecord.setText(R.string.play_record);
        layoutBtnPlayRecord.setBackgroundResource(R.drawable.border_text);
        btnPlayRecord.setBackgroundColor(Color.TRANSPARENT);
        break;
      case YOUR_RC_OFF:
        btnPlayRecord.setText(R.string.play_record);
        layoutBtnPlayRecord.setBackgroundResource(R.drawable.border_text_grey);
        btnPlayRecord.setBackgroundColor(Color.TRANSPARENT);
        break;
      case YOUR_RC_PLAYING:
        btnPlayRecord.setText(R.string.stop_record);
        layoutBtnPlayRecord.setBackgroundResource(R.drawable.border_text_red);
        break;
      case START_RC_ON:
        btnRecord.setText(R.string.start_record);
        layoutBtnRecord.setBackgroundResource(R.drawable.border_text);
        btnRecord.setBackgroundColor(Color.TRANSPARENT);
        break;
      case START_RC_OFF:
        btnRecord.setText(R.string.start_record);
        layoutBtnRecord.setBackgroundResource(R.drawable.border_text_grey);
        btnRecord.setBackgroundColor(Color.TRANSPARENT);
        break;
      case STOP_RC:
        btnRecord.setText(R.string.stop_recording);
        layoutBtnRecord.setBackgroundResource(R.drawable.border_text_red);
        break;
    }

  }

  private void checkRecordStatusFile() {
    if (recordPlayer.isPlaying()) {
      recordPlayer.pause();
      isPlayerRecordPaused = true;
      btnModeListen.setEnabled(true);
      btnModeListen.setBackgroundResource(R.drawable.listen);
      changeRecrodTextUI(YOUR_RC_ON);

    } else if (isPlayerRecordPaused) {
      if (checkFileExist()) {
        isPlayerRecordPaused = false;
        btnPlayRecord.setEnabled(true);
        layoutBtnPlayRecord.setEnabled(true);
        changeRecrodTextUI(YOUR_RC_ON);
        recordPlaying = false;
      } else {
        isPlayerRecordPaused = false;
        btnPlayRecord.setEnabled(false);
        layoutBtnPlayRecord.setEnabled(false);
        changeRecrodTextUI(YOUR_RC_OFF);
        recordPlaying = false;
      }
    } else {
      if (checkFileExist()) {
        btnPlayRecord.setEnabled(true);
        layoutBtnPlayRecord.setEnabled(true);
        changeRecrodTextUI(YOUR_RC_ON);
      } else {
        btnPlayRecord.setEnabled(false);
        layoutBtnPlayRecord.setEnabled(false);
        changeRecrodTextUI(YOUR_RC_OFF);
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
    tvDurationRecord.setText("-- : --");
    player.reset();
    Lesson lesson = lessonArrayList.get(soundPlaying);

    lesson.setUri(lesson.getMusicName());
    Uri uri = Uri.parse(lesson.getUri());

    lesson.setIsPlay(true);

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

    btnModeRc.setEnabled(true);
    btnModeRc.setBackgroundResource(R.drawable.record_new);
    isMediaPlayerPaused = false;
    btnPlayPause.setBackgroundResource(R.drawable.pause_new);
    tvLesson.setText(lessonArrayList.get(soundPlaying).getLessonName());
    updateSeekBar();
    adapter.notifyDataSetChanged();


    FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    //content type
    Bundle bundle = new Bundle();
    bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, soundPlaying);
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, tvLesson.getText().toString());
    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "mp3");
    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


    //customize
    Bundle bundle2 = new Bundle();
    bundle2.putInt("lesson_id", soundPlaying);
    bundle2.putString("lesson_name", tvLesson.getText().toString());
    bundle2.putString("lesson_type", "mp3");
    mFirebaseAnalytics.logEvent("select_lesson", bundle);
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
      player.pause();
      isMediaPlayerPaused = true;
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
      listSmallView.get(i).setBackgroundResource(R.color.small_view_color);
    }
    listSmallView.get(position).setBackgroundResource(R.color.primary);
  }

  @Override
  public void onPageScrollStateChanged(int state) {

  }

  @Override
  public void onBackPressed() {
    finish();
  }

  //seekbar listener
  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (fromUser) {
      player.seekTo(progress);
      seekBar.setProgress(progress);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {

  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {

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
      mViewHolder.imgDuration = (ImageView) convertView.findViewById(R.id.img_Duration);


      mViewHolder.imgDuration.getBackground().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.MULTIPLY);
      Typeface listType = Typeface.createFromAsset(getAssets(), "UTM_Avo.ttf");
      Typeface textPlaying = Typeface.createFromAsset(getAssets(), "Cocogoose_trial.otf");
      mViewHolder.tvLessonName.setText(lessons.get(position).getLessonName() + "");
      mViewHolder.tvLessonName.setTypeface(listType);
      //set selected help textview marquee forever
      mViewHolder.tvLessonName.setSelected(true);
      mViewHolder.tvPlaying.setTypeface(textPlaying);
      mViewHolder.imgLesson.setImageResource(lessons.get(position).getImageLesson());
      mViewHolder.tvDurationLesson.setText(lessons.get(position).getDuration());
      mViewHolder.tvPlaying.setSelected(true);
      if (lessons.get(position).isPlay()) {
        mViewHolder.tvPlaying.setVisibility(View.VISIBLE);
        mViewHolder.viewPlaying.setVisibility(View.VISIBLE);
      } else {
        mViewHolder.tvPlaying.setVisibility(View.GONE);
        mViewHolder.viewPlaying.setVisibility(View.GONE);
      }

      if (isRecord) {
        mViewHolder.tvPlaying.setText("Recording...");
      } else {
        if (isMediaPlayerPaused) {
          mViewHolder.tvPlaying.setText("Pause...");
        } else {
          mViewHolder.tvPlaying.setText("Playing...");
        }
      }
      return convertView;
    }

    private class MyViewHolder {
      TextView tvLessonName;
      TextView tvDurationLesson;
      TextView tvPlaying;
      ImageView imgLesson;
      ImageView imgDuration;
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
        tvTranscription.setText(lessonArrayList.get(soundPlaying).getTranscription() + "");
      } else {
        TextView tvSpeech = (TextView) layout.findViewById(R.id.tvSpeech);
        TextView tvReducedSpeech = (TextView) layout.findViewById(R.id.tvReducedSpeech);
        tvSpeech.setTypeface(textType);
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