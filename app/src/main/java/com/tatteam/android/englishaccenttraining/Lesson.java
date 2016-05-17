package com.tatteam.android.englishaccenttraining;

/**
 * Created by Thanh on 16/09/2015.
 */
public class Lesson {
    private int id;
    private String lessonName;
    private String musicName;
    private String transcription;
    private String reducedSpeech;
    private String uri;
    private boolean isPlay = false;

    public Lesson() {
    }

    public Lesson(int id, String lessonName, String musicName, String transcription, String reducedSpeech) {
        this.id = id;
        this.lessonName = lessonName;
        this.musicName = musicName;
        this.transcription = transcription;
        this.reducedSpeech = reducedSpeech;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public void setReducedSpeech(String reducedSpeech) {
        this.reducedSpeech = reducedSpeech;
    }

    public void setUri(String musicName) {
        this.uri = "android.resource://com.tatteam.android.englishaccenttraining/raw/" + musicName;
    }

    public void setIsPlay(boolean isPlay) {
        this.isPlay = isPlay;
    }

    public int getId() {
        return id;
    }

    public String getMusicName() {
        return musicName;
    }

    public String getLessonName() {
        return lessonName;
    }

    public String getTranscription() {
        return transcription;
    }

    public String getReducedSpeech() {
        return reducedSpeech;
    }

    public String getUri() {
        return uri;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public int getImageLesson() {
        switch (getId()) {
            case 1:
                return R.drawable.apartment;
            case 2:
                return R.drawable.breakfast;
            case 3:
                return R.drawable.cellphone;
            case 4:
                return R.drawable.clothing;
            case 5:
                return R.drawable.dentalcare;
            case 6:
                return R.drawable.driving;
            case 7:
                return R.drawable.exercise;
            case 8:
                return R.drawable.family;
            case 9:
                return R.drawable.lunch;
            case 10:
                return R.drawable.jobhunting;
            case 11:
                return R.drawable.movie_rental;
            case 12:
                return R.drawable.success;
            case 13:
                return R.drawable.shopping;
            case 14:
                return R.drawable.restaurant;
            case 15:
                return R.drawable.success;
            case 16:
                return R.drawable.train;
        }
        return 0;

    }
}
