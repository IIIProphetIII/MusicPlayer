package study.cj.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

public class PlayerService extends Service {


    public static MediaPlayer mp = new MediaPlayer();
    public final IBinder binder = new MyBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder{
        PlayerService getService(){
            return PlayerService.this;
        }
    }

    public PlayerService(String initSongPath){
        try {
            // 设置初始播放歌曲
            mp.setDataSource(initSongPath);
            mp.prepare();
        } catch (IOException e) {
            Log.d("hint","can't get to the song");
            e.printStackTrace();
        }
    }

    public PlayerService(){
        // 空构造函数
    }

    // 播放和暂停
    public void playOrPause(){
        if (mp.isPlaying()){
            mp.pause();
        }else {
            mp.start();
        }
    }

    // 停止播放
    public void stop(){
        if (mp!=null){
            mp.stop();
            try {
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mp.seekTo(0);
        }
    }

    // 下一首
    public void nextMusic(String nextPath){
        if (mp != null){
            mp.stop();
            mp.reset();
            try {
                mp.setDataSource(nextPath);
                mp.prepare();
                mp.seekTo(0);
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 上一首
    public void preMusic(String prePath) {
        if(mp != null) {
            mp.stop();
            mp.reset();
            try {
                mp.setDataSource(prePath);
                mp.prepare();
                mp.seekTo(0);
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
