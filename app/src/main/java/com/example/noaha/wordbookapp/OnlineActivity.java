package com.example.noaha.wordbookapp;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import static com.example.noaha.wordbookapp.R.id.text;

public class OnlineActivity extends AppCompatActivity {

    private TextView txt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        String word= (String) bundle.getSerializable("word");
         txt = (TextView) findViewById(R.id.textOnline);

        try {
            SearchOnline(word);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }



    }


    public void SearchOnline(String word) throws UnsupportedEncodingException {

        String url = "http://fanyi.youdao.com/openapi.do";
        String keyfrom = "keyfrom=haobaoshui";
        String key = "key=1650542691";
        String type = "type=data&doctype=json";
        String version = "version=1.1";
        String tag = "testag";
        String search = URLEncoder.encode(word, "UTF-8");

        String youdaoUrl = url+"?"+keyfrom+"&"+key+"&"+type+"&"+version+"&q="+search;
        Log.d(tag,youdaoUrl);
        final String translate =  new String(youdaoUrl.getBytes("iso-8859-1"),"utf-8");



                try
                {
                    AnalyzingOfJson(translate);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

    }

    private void AnalyzingOfJson(String url) throws Exception
    {
        String message = null;
        String tag = "youdao";

        Log.d(tag,"url="+url);
        // 第一步，创建HttpGet对象
        HttpGet httpGet = new HttpGet(url);
        // 第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象
        HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200)
        {
            // 第三步，使用getEntity方法活得返回结果
            String result = EntityUtils.toString(httpResponse.getEntity());
            System.out.println("result:" + result);
            JSONArray jsonArray = new JSONArray("[" + result + "]");

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null)
                {
                    String errorCode = jsonObject.getString("errorCode");
                    if (errorCode.equals("20"))
                    {
                        Toast.makeText(getApplicationContext(), "要翻译的文本过长", Toast.LENGTH_SHORT);
                    }
                    else if (errorCode.equals("30 "))
                    {
                        Toast.makeText(getApplicationContext(), "无法进行有效的翻译", Toast.LENGTH_SHORT);
                    }
                    else if (errorCode.equals("40"))
                    {
                        Toast.makeText(getApplicationContext(), "不支持的语言类型", Toast.LENGTH_SHORT);
                    }
                    else if (errorCode.equals("50"))
                    {
                        Toast.makeText(getApplicationContext(), "无效的key", Toast.LENGTH_SHORT);
                    }
                    else
                    {
                        // 要翻译的内容
                        String query = jsonObject.getString("query");
                        message = query;
                        Log.d(tag,"query="+query);
                        // 翻译内容
                        String translation = jsonObject.getString("translation");
                        message += "\t" + translation;
                        Log.d(tag,"translation="+translation);
                        // 有道词典-基本词典
                        if (jsonObject.has("basic"))
                        {
                            JSONObject basic = jsonObject.getJSONObject("basic");
                            if (basic.has("phonetic"))
                            {
                                String phonetic = basic.getString("phonetic");
                                message += "\n\t" + phonetic;
                            }
                            if (basic.has("phonetic"))
                            {
                                String explains = basic.getString("explains");
                                message += "\n\t" + explains;
                            }
                        }
                        // 有道词典-网络释义
                        if (jsonObject.has("web"))
                        {
                            String web = jsonObject.getString("web");
                            JSONArray webString = new JSONArray("[" + web + "]");
                            message += "\n网络释义：";
                            JSONArray webArray = webString.getJSONArray(0);
                            int count = 0;
                            while(!webArray.isNull(count)){

                                if (webArray.getJSONObject(count).has("key"))
                                {
                                    String key = webArray.getJSONObject(count).getString("key");
                                    message += "\n\t<"+(count+1)+">" + key;
                                }
                                if (webArray.getJSONObject(count).has("value"))
                                {
                                    String value = webArray.getJSONObject(count).getString("value");
                                    message += "\n\t   " + value;
                                }
                                count++;
                            }
                        }
                    }
                }
            }
            txt.setText(message);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "提取异常", Toast.LENGTH_SHORT);
        }
    }

}
