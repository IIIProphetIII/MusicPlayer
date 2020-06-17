package study.cj.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public int musicIndex = 0;
    public ArrayList<Model> musics;
    PlayerService playerService;
    private SeekBar seekBar;
    private SeekBar volBar;
    AudioManager am;
    private TextView musicStatus, musicTime;
    private Button btnPlayOrPause, btnStop, btnQuit;
    private LinearLayout imgContainer;
    private ListView musicList;
    private SimpleDateFormat time = new SimpleDateFormat("m:ss");
    public Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取音乐
        musics = new ScanMusic().query(this);
        // 歌曲名称列表
//        ArrayList<String> musicNames = new ArrayList<>();
//        for (int i = 0; i < musics.size(); i++) {
//            musicNames.add(musics.get(i).getText_song());
//        }
        // 服务绑定
        playerService = new PlayerService(musics.get(musicIndex).getPath());
        bindServiceConnection();

        am = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        seekBar = (SeekBar)this.findViewById(R.id.MusicSeekBar);
        seekBar.setProgress(playerService.mp.getCurrentPosition());
        seekBar.setMax(playerService.mp.getDuration());
        volBar = (SeekBar)this.findViewById(R.id.VolumeSeekBar);
        volBar.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
//        volBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        musicStatus = (TextView)this.findViewById(R.id.MusicStatus);
        musicTime = (TextView)this.findViewById(R.id.MusicTime);
        btnPlayOrPause = (Button)this.findViewById(R.id.BtnPlayorPause);
        imgContainer = (LinearLayout)this.findViewById(R.id.ImageContainer);
        loadingCover(musics.get(musicIndex).getPath());
//        musicList = (ListView)this.findViewById(R.id.MusicList);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,musicNames);
//        musicList.setAdapter(adapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 将回退键事件设置为启动主界面的一个活动
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerService = ((PlayerService.MyBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerService = null;
        }
    };

    private void bindServiceConnection() {
        Intent intent = new Intent(MainActivity.this, PlayerService.class);
        startService(intent);
        bindService(intent, conn, this.BIND_AUTO_CREATE);
    }

//    public void changeImg(String musicPath){
//        //  封面
//        try {
//            System.out.println(musicPath);
//            Mp3File song = new Mp3File(musicPath);
//            if (song.hasId3v2Tag()){
//                imgContainer.removeAllViews();
//                ID3v2 id3v2tag = song.getId3v2Tag();
//                byte[] imageData = id3v2tag.getAlbumImage();
//                Bitmap bm = ImgUtil.getPicFromBytes(imageData, new BitmapFactory.Options());
//                ImageView iv = new ImageView(this);
//                iv.setImageBitmap(bm);
//                imgContainer.addView(iv);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (UnsupportedTagException e) {
//            e.printStackTrace();
//        } catch (InvalidDataException e) {
//            e.printStackTrace();
//        }
//    }

    // 加载专辑封面
    public void loadingCover(String mediaUri) {
        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(mediaUri);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
        if (picture != null){
            imgContainer.removeAllViews();
            Bitmap bitmap= BitmapFactory.decodeByteArray(picture,0,picture.length);
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(bitmap);
            imgContainer.addView(iv);
        }else {
            imgContainer.removeAllViews();
            ImageView iv = new ImageView(this);
            iv.setImageDrawable(getResources().getDrawable(R.drawable.default_cover));
            imgContainer.addView(iv);
        }
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // 修改ui
            if (playerService.mp.isPlaying()){
                // musicStatus.setText(getResources().getString(R.string.playing));
                musicStatus.setText(musics.get(musicIndex).toString());
                btnPlayOrPause.setText(getResources().getString(R.string.pause).toUpperCase());
            } else {
//                musicStatus.setText(getResources().getString(R.string.pause));
                btnPlayOrPause.setText(getResources().getString(R.string.play).toUpperCase());
            }
            musicTime.setText(time.format(playerService.mp.getCurrentPosition())+"/"+time.format(playerService.mp.getDuration()));
            // 播放进度条
            seekBar.setProgress(playerService.mp.getCurrentPosition());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser){
                        playerService.mp.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            // 音量条
            volBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser){
                        float vol=1 - (float)(Math.log(100-volBar.getProgress())/Math.log(100));
                        playerService.mp.setVolume(vol, vol);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            handler.postDelayed(runnable, 100);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
//        if(playerService.mp.isPlaying()) {
//            musicStatus.setText(getResources().getString(R.string.playing));
//        } else {
//            musicStatus.setText(getResources().getString(R.string.pause));
//        }
        // 调整播放进度
        seekBar.setProgress(playerService.mp.getCurrentPosition());
        seekBar.setMax(playerService.mp.getDuration());
        handler.post(runnable);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.BtnPlayorPause:
                playerService.playOrPause();
                break;
            case R.id.BtnStop:
                musicStatus.setText("");
                playerService.stop();
                seekBar.setProgress(0);
                break;
            case R.id.btnPre:
                // 默认循环播放
                musicIndex = (musicIndex + musics.size() - 1)%musics.size();
                playerService.preMusic(musics.get(musicIndex).getPath());
                loadingCover(musics.get(musicIndex).getPath());
                break;
            case R.id.btnNext:
                // 默认循环播放
                musicIndex = (musicIndex + 1)%musics.size();
                playerService.nextMusic(musics.get(musicIndex).getPath());
                loadingCover(musics.get(musicIndex).getPath());
                break;
            case R.id.BtnQuit:
                handler.removeCallbacks(runnable);
                unbindService(conn);
                try {
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        for (int i = 0; i <musics.size(); i++) {
            menu.add(1, i, Menu.FIRST, musics.get(i).getText_song());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);

        int id = item.getItemId();
        musicIndex = id;
        playerService.nextMusic(musics.get(musicIndex).getPath());
        loadingCover(musics.get(musicIndex).getPath());
        return true;
    }
}