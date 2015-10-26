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
    public Lesson(){}

    public Lesson(int id,String lessonName,String musicName,String transcription,String reducedSpeech){
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
        this.uri ="android.resource://com.tatteam.android.englishaccenttraining/raw/"+musicName;
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
}
