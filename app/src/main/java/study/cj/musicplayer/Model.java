package study.cj.musicplayer;

import androidx.annotation.NonNull;

public class Model {

    String text_song;
    String text_singer;
    String path;
    public String getText_song() {
        return text_song;
    }
    public void setText_song(String text_song) {
        this.text_song = text_song;
    }
    public String getText_singer() {
        return text_singer;
    }
    public void setText_singer(String text_singer) {
        this.text_singer = text_singer;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    @NonNull
    @Override
    public String toString() {
        return "text_song : "+text_song+"\n"
                +"text_singer : "+text_singer+"\n"
                +"path : "+path+"\n";
    }
}
