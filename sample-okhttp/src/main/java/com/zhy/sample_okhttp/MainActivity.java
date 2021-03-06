package com.zhy.sample_okhttp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.FileCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity
{


    private String mBaseUrl = "http://192.168.1.102:8080/okHttpServer/";

    private static final String TAG = "MainActivity";

    private TextView mTv;
    private ImageView mImageView;
    private ProgressBar mProgressBar;


    public class MyStringCallback extends StringCallback
    {
        @Override
        public void onBefore(Request request)
        {
            super.onBefore(request);
            setTitle("loading...");
        }

        @Override
        public void onAfter()
        {
            super.onAfter();
            setTitle("Sample-okHttp");
        }

        @Override
        public void onError(Call call, Exception e)
        {
            e.printStackTrace();
            mTv.setText("onError:" + e.getMessage());
        }

        @Override
        public void onResponse(String response)
        {
            Log.e("TAG","onResponse：complete");
            mTv.setText("onResponse:" + response);
        }

        @Override
        public void inProgress(float progress)
        {
            Log.e(TAG, "inProgress:" + progress);
            mProgressBar.setProgress((int) (100 * progress));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTv = (TextView) findViewById(R.id.id_textview);
        mImageView = (ImageView) findViewById(R.id.id_imageview);
        mProgressBar = (ProgressBar) findViewById(R.id.id_progress);
        mProgressBar.setMax(100);
    }

    public void getHtml(View view)
    {
        String url = "http://sec.mobile.tiancity.com/server/mobilesecurity/version.xml";
//        url="http://www.391k.com/api/xapi.ashx/info.json?key=bd_hyrzjjfb4modhj&size=10&page=1";
        OkHttpUtils
                .get()
                .url(url)
//                .addHeader("Accept-Encoding","")
                .build()
                .execute(new MyStringCallback());

    }

    public void postString(View view)
    {
        String url = mBaseUrl + "user!postString";
        OkHttpUtils
                .postString()
                .url(url)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .content(new Gson().toJson(new User("zhy", "123")))
                .build()
                .execute(new MyStringCallback());

    }

    public void postFile(View view)
    {
        File file = new File(Environment.getExternalStorageDirectory(), "messenger_01.png");
        if (!file.exists())
        {
            Toast.makeText(MainActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = mBaseUrl + "user!postFile";
        OkHttpUtils
                .postFile()
                .url(url)
                .file(file)
                .build()
                .execute(new MyStringCallback());

    }

    public void getUser(View view)
    {
        String url = mBaseUrl + "user!getUser";
        OkHttpUtils
                .post()//
                .url(url)//
                .addParams("username", "hyman")//
                .addParams("password", "123")//
                .build()//
                .execute(new UserCallback()
                {
                    @Override
                    public void onError(Call call, Exception e)
                    {
                        mTv.setText("onError:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(User response)
                    {
                        mTv.setText("onResponse:" + response.username);
                    }
                });
    }


    public void getUsers(View view)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "zhy");
        String url = mBaseUrl + "user!getUsers";
        OkHttpUtils//
                .post()//
                .url(url)//
//                .params(params)//
                .build()//
                .execute(new ListUserCallback()//
                {
                    @Override
                    public void onError(Call call, Exception e)
                    {
                        mTv.setText("onError:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(List<User> response)
                    {
                        mTv.setText("onResponse:" + response);
                    }
                });
    }


    public void getHttpsHtml(View view)
    {
        String url = "https://kyfw.12306.cn/otn/";

        OkHttpUtils
                .get()//
                .url(url)//
                .build()//
                .execute(new MyStringCallback());

    }

    public void getImage(View view)
    {
        mTv.setText("");
        String url = "http://images.csdn.net/20150817/1.jpg";
        OkHttpUtils
                .get()//
                .url(url)//
                .tag(this)//
                .build()//
                .connTimeOut(20000)
                .readTimeOut(20000)
                .writeTimeOut(20000)
                .execute(new BitmapCallback()
                {
                    @Override
                    public void onError(Call call, Exception e)
                    {
                        mTv.setText("onError:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(Bitmap bitmap)
                    {
                        Log.e("TAG","onResponse：complete");
                        mImageView.setImageBitmap(bitmap);
                    }
                });
    }


    public void uploadFile(View view)
    {

        File file = new File(Environment.getExternalStorageDirectory(), "messenger_01.png");
        if (!file.exists())
        {
            Toast.makeText(MainActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("username", "张鸿洋");
        params.put("password", "123");

        Map<String, String> headers = new HashMap<>();
        headers.put("APP-Key", "APP-Secret222");
        headers.put("APP-Secret", "APP-Secret111");

        String url = mBaseUrl + "user!uploadFile";

        OkHttpUtils.post()//
                .addFile("mFile", "messenger_01.png", file)//
                .url(url)//
                .params(params)//
                .headers(headers)//
                .build()//
                .execute(new MyStringCallback());
    }


    public void multiFileUpload(View view)
    {
        File file = new File(Environment.getExternalStorageDirectory(), "messenger_01.png");
        File file2 = new File(Environment.getExternalStorageDirectory(), "test1#.txt");
        if (!file.exists())
        {
            Toast.makeText(MainActivity.this, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("username", "张鸿洋");
        params.put("password", "123");

        String url = mBaseUrl + "user!uploadFile";
        OkHttpUtils.post()//
                .addFile("mFile", "messenger_01.png", file)//
                .addFile("mFile", "test1.txt", file2)//
                .url(url)
                .params(params)//
                .build()//
                .execute(new MyStringCallback());
    }


    public void downloadFile(View view)
    {
        String url = "http://dldir1.qq.com/qqfile/QQforMac/QQ_V4.1.1.dmg";
        OkHttpUtils//
                .get()//
                .url(url)//
                .build()//
                .execute(new FileCallback(Environment.getExternalStorageDirectory().getAbsolutePath(), "gson-2.2.1.jar")//
                {

                    @Override
                    public void onBefore(Request request)
                    {
                        super.onBefore(request);
                    }

                    @Override
                    public void inProgress(float progress)
                    {
                        Log.e(TAG, "inProgress :" + progress);
                        mProgressBar.setProgress((int) (100 * progress));
                    }

                    @Override
                    public void onError(Call call, Exception e)
                    {
                        Log.e(TAG, "onError :" + e.getMessage());
                    }

                    @Override
                    public void onResponse(File file)
                    {
                        Log.e(TAG, "onResponse :" + file.getAbsolutePath());
                    }
                });
    }


    public void otherRequestDemo(View view)
    {
        //also can use delete ,head , patch
        /*
        OkHttpUtils
                .put()//
                .url("http://11111.com")
                .requestBody
                        ("may be something")//
                .build()//
                .execute(new MyStringCallback());



        OkHttpUtils
                .head()//
                .url(url)
                .addParams("name", "zhy")
                .build()
                .execute();

       */


    }

    public void clearSession(View view)
    {
        OkHttpUtils.getInstance().getCookieStore().removeAll();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
    }
}
