package com.tatteam.android.englishaccenttraining;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import tatteam.com.app_common.AppCommon;
import tatteam.com.app_common.ads.AdsSmallBannerHandler;
import tatteam.com.app_common.util.CloseAppHandler;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AdapterView.OnItemClickListener, ViewPager.OnPageChangeListener, CustomSeekBar.OnSeekbarChangeListener, CloseAppHandler.OnCloseAppListener {

    private static final boolean ADS_ENABLE = true;
    private CloseAppHandler closeAppHandler;
    private LinearLayout layout_MoreApp;
    private ImageView btnMore;
    private ImageButton btnPlayPause, btnNext, btnPrevious;
    private ImageButton btnReplay, btnShuffle;
    private TextView tvLesson, tvCurrentDuration, tvDuration, tvAppName,tvMoreApp;

    private View viewPage1, viewPage2, viewPage3;
    ArrayList<View> listSmallView = new ArrayList<>();

    private ListView lvLesson;

    private CustomSeekBar customSeekbar;
    private Handler seekbarHandler = new Handler();
    private Runnable runnable;

    private int soundPlaying = 0;
    private MediaPlayer player;

    private ArrayList<Lesson> lessonArrayList = new ArrayList<>();
    private ListLessonAdapter adapter;

    private MyPagerAdapter myPagerAdapter;

    private int isRepeat = 0;
    private boolean isShuffle = false;
    private View[] pages;

    private boolean isMediaPlayerPaused = false;

    private final int REPEAT_OFF = 0;
    private final int REPEAT_ONE = 1;
    private final int REPEAT_ALL = 2;

    //
    private AdsSmallBannerHandler adsSmallBannerHandler;
    private FrameLayout adsContainer;
    //incoming call
    private PhoneStateListener phoneStateListener;
    TelephonyManager mgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        player = new MediaPlayer();
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
        btnShuffle = (ImageButton) this.findViewById(R.id.btnShuffle);
        btnMore = (ImageView) this.findViewById(R.id.btnMoreApp);
        btnMore.getBackground().setColorFilter(Color.parseColor("#006064"), PorterDuff.Mode.MULTIPLY);
        layout_MoreApp = (LinearLayout) this.findViewById(R.id.layout_MoreApp);

        tvMoreApp = (TextView) this.findViewById(R.id.tvMoreApp);
        tvAppName = (TextView) this.findViewById(R.id.tvAppName);
        tvLesson = (TextView) this.findViewById(R.id.tvLesson);
        tvCurrentDuration = (TextView) this.findViewById(R.id.tvCurrentDuration);
        tvDuration = (TextView) this.findViewById(R.id.tvDuration);
        Typeface face = Typeface.createFromAsset(getAssets(), "Mathlete_Bulky.otf");
        tvMoreApp.setTypeface(face);
        tvLesson.setTypeface(face);
        tvLesson.setSelected(true);
        tvCurrentDuration.setTypeface(face);
        tvDuration.setTypeface(face);
        tvAppName.setTypeface(face);

        customSeekbar = (CustomSeekBar) this.findViewById(R.id.seekBar_Custom);
        customSeekbar.setup();
        customSeekbar.setSeekbarChangeListener(this);

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
        btnShuffle.setOnClickListener(this);
        layout_MoreApp.setOnClickListener(this);

        pager.setOnPageChangeListener(this);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);

        //close app
        closeAppHandler = new CloseAppHandler(this);
        closeAppHandler.setListener(this);

        //ads
        adsContainer = (FrameLayout) this.findViewById(R.id.ads_container);
        adsSmallBannerHandler = new AdsSmallBannerHandler(this,adsContainer);
        adsSmallBannerHandler.setup();
    }

    private void incomingCall() {

    }



    private void updateSeekBar() {
        if (runnable == null) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    float percent = (float) player.getCurrentPosition() / (float) player.getDuration();
                    if (percent > 0) {
                        customSeekbar.updateIndicator(percent);
                        tvCurrentDuration.setText(changeTime(player.getCurrentPosition()));
                        tvDuration.setText(changeTime(player.getDuration() - player.getCurrentPosition()));

                    }
                    seekbarHandler.postDelayed(runnable, 30);
                }
            };
            seekbarHandler.postDelayed(runnable, 30);
        }
    }

    @Override
    protected void onDestroy() {
        player.stop();
        seekbarHandler.removeCallbacks(runnable);
//        DataSource.getInstance().destroy();
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
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
                        btnPlayPause.setBackgroundResource(R.drawable.play);
                    } else {
                        if (isMediaPlayerPaused) {
                            player.start();
                            isMediaPlayerPaused = false;
                        } else {
                            playSound(soundPlaying);
                        }
                        btnPlayPause.setBackgroundResource(R.drawable.pause);
                    }
                } else {
                    playSound(1);
                    btnPlayPause.setBackgroundResource(R.drawable.pause);
                }
                break;
            case R.id.btnNext:
                if (isShuffle) {
                    playShuffle();
                    playSound(soundPlaying);
                } else {
                    playNextAround();
                }
                break;
            case R.id.btnPrevious:
                if (isShuffle) {
                    playShuffle();
                    playSound(soundPlaying);
                } else {
                    playPrev();
                }
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
            case R.id.btnShuffle:
                if (isShuffle) {
                    isShuffle = false;
                    btnShuffle.setBackgroundResource(R.drawable.shuffer_off);
                } else {
                    isShuffle = true;
                    btnShuffle.setBackgroundResource(R.drawable.shuffer_on);
                }
                break;
            case R.id.layout_MoreApp:
                AppCommon.getInstance().openMoreAppDialog(this);
                break;
        }
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

    private void playSound(int soundPlaying) {
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
        btnPlayPause.setBackgroundResource(R.drawable.pause);
        tvLesson.setText(lessonArrayList.get(soundPlaying).getLessonName());
        tvAppName.setVisibility(View.INVISIBLE);
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
            btnPlayPause.setBackgroundResource(R.drawable.play);
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
        customSeekbar.updateIndicator(0.0f);
    }

    private void LoadData() {
        DataSource.getInstance().init(getApplicationContext());
        DataSource.getInstance().createDatabaseIfNeed();
        lessonArrayList = DataSource.getInstance().getListLesson();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        for (int i = 0; i < lessonArrayList.size(); i++) {
            lessonArrayList.get(i).setIsPlay(false);
        }
        tvLesson.setText(lessonArrayList.get(position).getLessonName());
        playSound(position);
        soundPlaying = position;
        btnPlayPause.setBackgroundResource(R.drawable.pause);
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
        closeAppHandler.handlerKeyBack(this);
    }

    @Override
    public void onRateAppDialogClose() {
        finish();
    }

    @Override
    public void onTryToCloseApp() {
        Toast.makeText(this,"Press once again to exit!",Toast.LENGTH_SHORT).show();
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
            mViewHolder.tvId = (TextView) convertView.findViewById(R.id.tvId);
            mViewHolder.imgPlay = (ImageView) convertView.findViewById(R.id.imgPlay);
            mViewHolder.imgPlay.setTag(lessonArrayList.get(position));


            Typeface listType = Typeface.createFromAsset(getAssets(), "MARI&DAVID.ttf");

            mViewHolder.tvLessonName.setText(lessons.get(position).getLessonName() + "");
            mViewHolder.tvLessonName.setTypeface(listType);

            mViewHolder.tvId.setText(lessons.get(position).getId() + "");
            mViewHolder.tvId.setTypeface(listType);

            if (lessons.get(position).isPlay() == true) {
                mViewHolder.imgPlay.setVisibility(View.VISIBLE);
                mViewHolder.tvId.setVisibility(View.GONE);
            } else {
                mViewHolder.imgPlay.setVisibility(View.GONE);
                mViewHolder.tvId.setVisibility(View.VISIBLE);

            }
            mViewHolder.tvLessonName.setSelected(true);
            return convertView;
        }


        private class MyViewHolder {
            TextView tvLessonName;
            TextView tvId;
            ImageView imgPlay;
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
            Typeface textType = Typeface.createFromAsset(getAssets(), "UTMBillhead1910.ttf");
            if (position == 0) {
                lvLesson = (ListView) layout.findViewById(R.id.listLesson);
                TextView tvLessonTitle = (TextView) layout.findViewById(R.id.tvLessonTitle);
                tvLessonTitle.setTypeface(listType);

                adapter = new ListLessonAdapter(MainActivity.this, lessonArrayList);
                lvLesson.setAdapter(adapter);
                lvLesson.setOnItemClickListener(MainActivity.this);

            } else if (position == 1) {
                TextView tvTrans = (TextView) layout.findViewById(R.id.tvTrans);
                TextView tvTranscription = (TextView) layout.findViewById(R.id.tvTranscription);
                tvTrans.setTypeface(listType);
                tvTranscription.setTypeface(textType);
                tvTranscription.setText(lessonArrayList.get(soundPlaying).getTranscription() + "");
            } else {
                TextView tvSpeech = (TextView) layout.findViewById(R.id.tvSpeech);
                TextView tvReducedSpeech = (TextView) layout.findViewById(R.id.tvReducedSpeech);
                tvSpeech.setTypeface(listType);
                tvReducedSpeech.setTypeface(textType);
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