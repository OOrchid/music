package com.example.music;

import android.app.IntentService;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.Context;


import android.app.NotificationManager;
import android.app.PendingIntent;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyDownloadService extends IntentService {
    private String fileName;

    public MyDownloadService(String name) {
        super(name);

    }

    public MyDownloadService() {
        super("");
    }
    public static void sendScanMediaAction(Context ctx,File file) {
        Intent mediaScanIntent = new Intent(
                "android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        ctx.sendBroadcast(mediaScanIntent);
    }

    protected void onHandleIntent(Intent intent) {

        final String url = intent.getStringExtra("path");
        String str = url.substring(0,url.indexOf("/download"));
        fileName=str.substring(str.lastIndexOf("/")+1);
        final long startTime = System.currentTimeMillis();
        Log.i("DOWNLOAD","startTime="+startTime);
        Handler handler=new Handler(Looper.getMainLooper());
        handler.post(new Runnable(){
            public void run(){
                Toast.makeText(getApplicationContext(), "开始下载，请耐心等待，提示成功后刷新", Toast.LENGTH_LONG).show();
            }
        });
        Request request = new Request.Builder().url(url).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                e.printStackTrace();
                Log.i("DOWNLOAD","download failed");
                Handler handler=new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    public void run(){
                        Toast.makeText(getApplicationContext(), "下载失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Sink sink = null;
                BufferedSink bufferedSink = null;
                if(response.body().contentLength()<100000 )
                {
                    Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getApplicationContext(), "音乐不存在!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else
                try {
                    //ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    //File directory = cw.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsoluteFile();
                    File dest = new File("/storage/emulated/0/Download",   fileName+".mp3");
                    sink = Okio.sink(dest);
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.writeAll(response.body().source());

                    bufferedSink.close();
                    Log.i("DOWNLOAD","download success");
                    Log.i("DOWNLOAD","totalTime="+ (System.currentTimeMillis() - startTime));
                     sendScanMediaAction(MyDownloadService.this,dest);
                   Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getApplicationContext(), "下载成功!", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("DOWNLOAD","download failed");
                    Handler handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getApplicationContext(), "下载失败!", Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    if(bufferedSink != null){
                        bufferedSink.close();
                    }

                }
            }
        });

    }



}