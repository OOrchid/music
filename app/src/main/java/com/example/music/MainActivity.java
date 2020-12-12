package com.example.music;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music.R;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView mListView;
    private List<Song> list;
    private MyAdapter adapter;
    private EditText edit;
    private MediaPlayer mediaPlayer=new MediaPlayer();  ;
    private SeekBar seekBar;
    private int currPosition;
    private Song currSong;
    private TextView nameText;
    private TextView totalTime;
    private TextView currTime;
    private ImageButton next;
    private ImageButton previous;
    private ImageButton stop;
    private ImageButton play;
    private int currState=IDLE;//当前播放器的状态
    Handler handler=new Handler();
    //    定义当前播放器的状态
     private static final int IDLE=0;   //空闲：没有播放音乐
     private static final int PAUSE=1;  //暂停：播放音乐时暂停
     private static final int START=2;  //正在播放音乐
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button sendRequest = findViewById(R.id.send_request);
       Button refresh=findViewById(R.id.refresh);
        edit = findViewById(R.id.code);
       seekBar=findViewById(R.id.seekBar);
        nameText=findViewById(R.id.nameDisplay);
        totalTime=findViewById(R.id.totalTime);
        currTime=findViewById(R.id.currTime);
        next=findViewById(R.id.next);
        play=findViewById(R.id.play);
        stop=findViewById(R.id.stop);
        previous=findViewById(R.id.previous);
        sendRequest.setOnClickListener(this);
        refresh.setOnClickListener(this);
        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        this.edit = findViewById(R.id.code);
        initView();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

           Song song=list.get(position);
           nameText.setText(song.song);
                Log.i("SONG",song.toString());

                currPosition = position;
                currSong=song;
                initSeekBar();
                play();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(currState==START){
                    if(fromUser){ //如果是人为改变进度，则改变相应地显示时长
                        currTime.setText(toTime(progress));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //开始拖动进度条，将音乐播放器停止
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //结束拖动进度条，按照新的进度继续播放音乐
                if(currState==START){
                    mediaPlayer.seekTo(seekBar.getProgress());
                    mediaPlayer.start();
                }
            }
        });
    }
    private void initSeekBar(){
                 int duration=currSong.duration;
                 seekBar.setMax(duration);
                 seekBar.setProgress(0);
                 currTime.setText("00:00");
                 if(duration>0){
                         totalTime.setText(toTime(duration));
                     }
             }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_request:
                download(edit.getText().toString());
                break;
            case R.id.refresh:

              list = MusicUtils.getMusicData(this);
              adapter= new MyAdapter(this, list);
               mListView.setAdapter(adapter);
                break;
            case R.id.play:
                if(currSong==null)
                { Toast.makeText(MainActivity.this,"未选中歌曲",Toast.LENGTH_LONG).show();
                    break;}
               mediaPlayer.start();
                break;
            case R.id.stop:
                mediaPlayer.pause();
                break;
            case R.id.next:
                currPosition++;
                if(currPosition>=adapter.getCount())currPosition=0;
                currSong= (Song) adapter.getItem(currPosition);
                nameText.setText(currSong.song);
                initSeekBar();
                play();
                break;
            case R.id.previous:
                currPosition--;
                if(currPosition<=-1)currPosition=adapter.getCount()-1;
                currSong= (Song) adapter.getItem(currPosition);
                nameText.setText(currSong.song);
                initSeekBar();
                play();
                break;

        }
    }
public void play(){
    mediaPlayer.reset();
    if(currSong==null)
    { Toast.makeText(MainActivity.this,"未选中歌曲",Toast.LENGTH_LONG).show();
       return;}
    try {

        Log.i("curSONG",currSong.toString());

        mediaPlayer.setDataSource(currSong.path);
        mediaPlayer.prepare();
        mediaPlayer.start();
        currState=START;
        //监听播放时回调函数
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mediaPlayer.getCurrentPosition()<seekBar.getMax()){

                    seekBar.setProgress(mediaPlayer.getCurrentPosition());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            currTime.setText(toTime(mediaPlayer.getCurrentPosition()));
                        }
                    });
                }
                else{

                    try {

                        currPosition++;
                        if(currPosition>=adapter.getCount())currPosition=0;
                        currSong= (Song) adapter.getItem(currPosition);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                initSeekBar();
                                nameText.setText(currSong.song);
                            }
                        });
                         mediaPlayer.reset();
                        mediaPlayer.setDataSource(currSong.path);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        },0,500);


    } catch (IOException e) {
        e.printStackTrace();
    }
}
    public void download(String str) {
        str = str.replaceAll(" ", "-");
        str = str.toLowerCase();
        Intent intent = new Intent(this, MyDownloadService.class);
        //携带额外数据
        intent.putExtra("path",
                "https://freemusicarchive.org/track/" + str + "/download");
        //发送数据给service
        startService(intent);


    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.list);
        list = new ArrayList<>();
        //把扫描到的音乐赋值给list
        list = MusicUtils.getMusicData(this);
        adapter = new MyAdapter(this, list);
        mListView.setAdapter(adapter);
    }

    private String toTime(int duration){
             Date date=new Date();
                 SimpleDateFormat sdf=new SimpleDateFormat("mm:ss", Locale.getDefault());
                 sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                 date.setTime(duration);
                 return sdf.format(date);
             }
    class MyAdapter extends BaseAdapter {
        private Context context;
        private List<Song> list;

        public MyAdapter(MainActivity mainActivity, List<Song> list) {
            this.context = mainActivity;
            this.list = list;

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                holder = new ViewHolder();
                //引入布局
                view = View.inflate(context, R.layout.item_music_listview, null);
                //实例化对象
                holder.song = (TextView) view.findViewById(R.id.item_mymusic_song);
                holder.singer = (TextView) view.findViewById(R.id.item_mymusic_singer);
                holder.duration = (TextView) view.findViewById(R.id.item_mymusic_duration);
                holder.position = (TextView) view.findViewById(R.id.item_mymusic_postion);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            //给控件赋值
            holder.song.setText(list.get(i).song.toString());
            holder.singer.setText(list.get(i).singer.toString());
            //时间需要转换一下
            int duration = list.get(i).duration;
            String time = MusicUtils.formatTime(duration);
            holder.duration.setText(time);
            holder.position.setText(i + 1 + "");

            return view;
        }

        class ViewHolder {
            TextView song;//歌曲名
            TextView singer;//歌手
            TextView duration;//时长
            TextView position;//序号

        }
    }
}