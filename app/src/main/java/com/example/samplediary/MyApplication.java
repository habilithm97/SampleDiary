package com.example.samplediary;

import android.app.Application;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

// Volley 라이브러리를 이용해 인터넷으로 날씨 데이터를 요청할 때는 RequestQueue 객체를 사용해야되는데
// RequestQueue 객체를 좀 더 단순화하려면 Application 클래스를 상속하는 별도의 클래스를 만들고 그 안에서 요청을 처리할 수 있도록 함

/*
*Application 클래스
 -앱 내부의 기본 클래스
 -첫 번째 액티비티가 표시되기 전에 전역 상태를 초기화할 때 사용됨

 -어느 컴포넌트에서나 공유할 수 있는 전역 클래스로서, Application을 상속받은 클래스는 공동으로 관리해야 하는 데이터를 작성하기에 적합함
 -다른 서브 클래스보다 먼저 인스턴스화됨

 -액티비티 간 어떠한 클래스든 공유할 수 있고, Application 객체의 멤버는 프로세스 어디에서나 참조가 가능함
 -앱의 컴포넌트들을 공동으로 사용할 수 있기 때문에 공통되는 내용을 작성해주면 어디서는 context 객체를 이용한 접근이 가능함
 -공통으로 전역 변수를 사용하고 싶을 때 Application 클래스를 상속받아 사용 가능함

 -어떤 컴포넌트 간이든 어떤 클래스를 공유함은 물론이고 Application 객체의 멤버는 프로세스 어디에서나 참조 가능함
  -> 공통으로 전역 변수를 사용하고 싶을 때 Application 클래스를 상속 받아 사용할 수 있음


 1. 클래스를 만들고 Application 클래스를 상속한 다음에 매니페스트의 anodroid:name 속성에 등록해서 사용함
 2. 어떤 값을 컴포넌트 사이에서 공유해서 사용할 수 있게 해줌
 3. Application 클래스를 상속 받은 클래스는 첫 번째 액티비티봐 먼저 인스턴스화됨
 */

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    public static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        if(requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack() {
                @Override
                protected HttpURLConnection createConnection(URL url) throws IOException {
                    HttpURLConnection connection = super.createConnection(url);
                    connection.setInstanceFollowRedirects(false);
                    return connection;
                }
            });
        }
    }

    @Override
    public void onTerminate() { // Application 객체와 모든 컴포넌트가 종료될 때 호출되지만 항상 발생되지는 않고 종료 처리할 때만 사용됨
        super.onTerminate();
    }

    public static interface OnResponseListener { // 리스너
        public void processResponse(int reqeustCode, int responseCode, String response); // send() 메서드를 호출한 쪽에서 응답 결과를 처리할 수 있도록
    }

    // 요청 객체를 만들어 요청을 처리함
    public static void send(final int requestCode, final int requestMethod, final String url, final Map<String,String> params, final OnResponseListener listener) {

        // 문자열을 주고 받기 위한 요청 객체
        StringRequest request = new StringRequest(requestMethod, url,
                new Response.Listener<String>() { // 응답을 문자열로 받아서 여기에 넣음
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, requestCode + "으로부터의 응답 : " + response);

                        if (listener != null) {
                            listener.processResponse(requestCode, 200, response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, requestCode + "으로부터의 에러 : " + error.getMessage());

                        if (listener != null) {
                            listener.processResponse(requestCode, 400, error.getMessage());
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        request.setShouldCache(false); // 이전 응답 결과를 사용하지 않겠다면 캐시를 사용하지 않도록 false
        request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        if (MyApplication.requestQueue != null) {
            MyApplication.requestQueue.add(request);
            Log.d(TAG, "요청 전송됨 : " + requestCode);
            Log.d(TAG, " 요청한 URL : " + url);
        } else {
            Log.d(TAG, "요청 큐가 없습니다.");
        }
    }
}
