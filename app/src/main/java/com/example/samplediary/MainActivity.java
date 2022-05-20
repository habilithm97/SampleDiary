package com.example.samplediary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// 파스텔톤 색상 링크 :  https://coolors.co/d3f8e2-e4c1f9-f694c1-edd5b2-a9def9

/*
*기상청 날씨를 가져오려면 기상청에서 제공하는 날짜 포맷이 필요함
 -주소 안에 포함된 gridx와 gridy 파라미터는 날씨 정보를 확인하고 싶은 지역을 나타내는데,
 -지역을 나타내는 값이 경위도 좌표가 아닌 격자의 번호로 표시되어 있기 때문에
 -경위도 좌표를 격자 번호로 변환하는 과정이 필요함

*XML Pull Parser 인터페이스
 -XML : 사람과 기계가 모두 읽을 수 있는 형식으로 데이터를 기술할 수 있도록 데이터를 문서화하는 규칙을 정의한 마크업 언어
  -> 인터넷을 통한 데이터 교환 시, 단순함/범용성/사용성을 제공하는 것이 XML을 사용하는 가장 큰 목적임
 -Pull Parser : 특정 위치까지 파싱되어 내용을 처리한 후 계속 파싱할 것인지 멈출 것인지를 개발자가 제어할 수 있는 특징이 있음
*/

public class MainActivity extends AppCompatActivity implements onTabItemSelectedListener, OnRequestListener, AutoPermissionsListener, MyApplication.OnResponseListener {

    private static final String TAG = "MainActivity";

    ListFragment listFragment;
    WriteFragment writeFragment;
    GraphFragment graphFragment;

    BottomNavigationView bottomNavigationView;
    
    Location currentLocation;
    String currentWeather;
    String currentAddress;
    GPSListener gpsListener; // 위치 정보를 수신함

    int locationCount = 0; // 위치 정보를 확인한 횟수(위치를 한 번 확인한 후에는 위치 요청을 취소할 수 있도록)

    public static DiaryDatabase diaryDatabase = null;

    SimpleDateFormat todayDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listFragment = new ListFragment();
        writeFragment = new WriteFragment();
        graphFragment = new GraphFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, listFragment).commit();

        bottomNavigationView = findViewById(R.id.bottomNavi);
        // 하단 탭에 들어 있는 각각의 버튼을 눌렀을 때
        // onNavigationItemSelected()가 자동으로 호출되므로 그 안에서 메뉴 아이템의 id 값으로 버튼을 구분한 후 그에 맞는 기능을 구현함
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.tab1:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, listFragment).commit();

                                if(WriteFragment.item != null) { // (작성화면의 아이템이 있을 경우)수정 모드일 경우에만
                                    writeFragmentClear(); // 리스트 탭을 누르게 되면 작성화면이 초기화됨
                                }
                                return true;

                            case R.id.tab2:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, writeFragment).commit();
                                return true;

                            case R.id.tab3:
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, graphFragment).commit();
                                return true;
                        }
                        return false;
                    }
                });

        setPicturePath(); // 이미지 경로 접근 및 폴더 없으면 폴더 생성

        // 앱이 처음 시작될 때 AutoPermissions 승인 요청을 처리하기 위해
        AutoPermissions.Companion.loadAllPermissions(this, 101);

        openDatabase(); // 데이터 베이스 열기
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 데이터베이스가 있으면 닫고 초기화
        if(diaryDatabase != null) {
            diaryDatabase.close();
            diaryDatabase = null;
        }
    }

    // 데이터베이스 오픈(데이터베이스가 없으면 생성)
    public void openDatabase() {
        // 데이터베이스가 있으면 닫은 후 초기화하고
        if(diaryDatabase != null) {
            diaryDatabase.close();
            diaryDatabase = null;
        }

        diaryDatabase = DiaryDatabase.getInstance(this); // 데이터베이스 접근

        boolean isOpen = diaryDatabase.open();
        if(isOpen) {
            Log.d(TAG, "데이터베이스가 오픈됨. ");
        } else {
            Log.d(TAG, "데이터베이스가 오픈되지 않음. ");
        }
    }

    public void writeFragmentClear() { // 작성 화면 초기화
        WriteFragment.contentEdt.setText(null);
        WriteFragment.isPhotoCaptured = false;
        WriteFragment.isPhotoFileSaved = false;
        WriteFragment.moodSlider.setInitialIndex(2); // 다섯 개의 기분 중 가운데 기분이 디폴트 값임
        WriteFragment.item = null;
    }

    public void setPicturePath() {
        String folderPath = getFilesDir().getAbsolutePath(); // 내부 저장소 file 경로 접근 방법
        AppConstants.FOLDER_PHOTO = folderPath + File.separator + "photo";

        File photoFolder = new File(AppConstants.FOLDER_PHOTO);
        // 이미지 폴더가 존재하지 않으면 생성
        if(!photoFolder.exists()) {
            photoFolder.mkdir();
        }
    }

    public void onRequest(String command) { // 두 번째 프래그먼트에서 호출됨
        if(command != null) {
            if(command.equals("getCurrentLocation")) {
                getCurrentLocation(); // 현재 위치 확인이 시작됨
            }
        }
    }

    public void getCurrentLocation() { // 현재 위치 요청
        Date currentDate = new Date(); // 현재 날짜를 가져와서

        //String currentDateString = AppConstants.dateFormat3.format(currentDate); // 형식에 맞는 현재 날짜를 변수에 할당
        if (todayDateFormat == null) {
            todayDateFormat = new SimpleDateFormat(getResources().getString(R.string.today_date_format));
        }
        String currentDateString = todayDateFormat.format(currentDate); // 형식에 맞는 현재 날짜를 변수에 할당
        AppConstants.println("currentDateString : " + currentDateString);

        if (writeFragment != null) {
            writeFragment.setDateString(currentDateString); // 두 번째 프래그먼트 상단에 현재 날짜를 표시함
        }

        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try {
            currentLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 위치 관리자의 위치 제공자로 최근 위치 정보를 확인해서 현재 위치 변수에 할당함
            if (currentLocation != null) {
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();
                String message = "최근 위치 : 위도 : " + latitude + "\n경도:" + longitude;
                println(message);

                // 현재 위치가 확인되면 호출됨
                getCurrentWeather(); // 현재 위치를 이용해서 날씨 확인
                getCurrentAddress(); // 현재 위치를 이용해서 주소 확인
             }

            gpsListener = new GPSListener(); // 위치 리스너 객체 생성, 요청된 위치를 수신하기 위함
            // 최소 시간으로는 10초, 최소 거리는 0으로 하여 10초마다 위치 정보를 전달받게됨
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener); // 현재 위치 갱신
            println("현재 위치가 갱신되었습니다.");

        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    public void stopLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            manager.removeUpdates(gpsListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    class GPSListener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) { // 위치가 확인되었을 때 자동으로 호출됨
            currentLocation = location;
            locationCount++;

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            String message = "최근 위치 : 위도 : " + latitude + "\n경도:" + longitude;
            println(message);

            // 현재 위치가 확인되면 호출됨
            getCurrentWeather(); // 현재 위치를 이용해서 날씨 확인
            getCurrentAddress(); // 현재 위치를 이용해서 주소 확인
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
        }
    }

    public void onTabSelected(int position) {
        if(position == 0) {
            bottomNavigationView.setSelectedItemId(R.id.tab1);
        } else if(position == 1) {
            // 첫 번째 프래그먼트 상단의 작성하기 버튼을 누르면 동작
            writeFragment = new WriteFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, writeFragment).commit();

            bottomNavigationView.setSelectedItemId(R.id.tab2);
        } else if(position == 2) {
            bottomNavigationView.setSelectedItemId(R.id.tab3);
        }
    }

    @Override
    public void showWriteFragment(Diary item) { // 기존의 저장된 아이템을 불러와서 두 번째 프래그먼트에 보여줄 때 호출됨
        writeFragment = new WriteFragment();
        writeFragment.setItem(item);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, writeFragment).commit();
    }

    /*
    public void showWriteFragment2() {
        WriteFragment writeFragment = new WriteFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, writeFragment).commit();
    } */

    public void getCurrentWeather() { // 현재 날씨 가져오기
        // GridUtill 객체의 getGrid()로 격자 번호 확인
        Map<String, Double> gridMap = GridUtil.getGrid(currentLocation.getLatitude(), currentLocation.getLongitude());

        double gridX = gridMap.get("x");
        double gridY = gridMap.get("y");
        println("x좌표 : " + gridX + ", " + "y좌표 : " + gridY);

        sendWeatherRequest(gridX, gridY);
    }

    public void sendWeatherRequest(double gridX, double gridY) { // 기상청 날씨 서버로 요청 전송
        String url = "http://www.kma.go.kr/wid/queryDFS.jsp"; // 기상청 API
        // Math.round : 소수점 이하를 반올림 (Math.ceil : 올림, Math.floor : 버림)
        url += "?gridx=" + Math.round(gridX);
        url += "&gridy=" + Math.round(gridY);

        Map<String,String> params = new HashMap<String,String>();

        MyApplication.send(AppConstants.REQ_WEATHER_BY_GRID, Request.Method.GET, url, params, this); // SingleTon Pattern
    }

    public void processResponse(int requestCode, int responseCode, String response) { // 기상청 날씨 서버로부터 응답을 받으면 호출됨
        if (responseCode == 200) {
            if (requestCode == AppConstants.REQ_WEATHER_BY_GRID) {

                // XML 응답 데이터를 자바 객체로 생성
                XmlParserCreator parserCreator = new XmlParserCreator() {
                    @Override
                    public XmlPullParser createParser() {
                        try {
                            return XmlPullParserFactory.newInstance().newPullParser();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                GsonXml gsonXml = new GsonXmlBuilder().setXmlParserCreator(parserCreator).setSameNameLists(true).create();
                // 자바 객체는 WeatherResult라는 객체로 정의
                WeatherResult weatherResult = gsonXml.fromXml(response, WeatherResult.class);

                try {
                    // 작성화면의 좌측 상단에 있는 날씨 아이콘에 현재 날씨 설정
                    WeatherItem item = weatherResult.body.datas.get(0);
                    currentWeather = item.wfKor;
                    if (writeFragment != null) {
                        if(writeFragment.item == null) { // 수정모드가 아닌 새 글 작성모드일 경우에만
                            writeFragment.setWeather(item.wfKor); // 기상청의 현재 날씨 문자열을 받아 아이콘을 설정함
                        }
                    }
                    // 위치를 한번 확인한 후에는 위치 요청 서비스를 중지함
                    if (locationCount > 0) {
                        stopLocationService();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else {
                println("알 수 없는 요청 코드 : " + requestCode);
            }
        } else {
            println("응답 코드 실패 : " + responseCode);
        }
    }

    //GeoCoder 클래스(주소 -> 좌표) : 주소를 지리학적 좌표로 변환할 수 있음 -> 이러한 과정을 지오 코딩(반대 과정은 역 지오 코딩이라고함)
    public void getCurrentAddress() { // 현재 위치를 이용해 주소를 확인
        // Geocoder 클래스를 이용해 현재 위치를 주소로 변환하는 것을 확인할 수 있음(역 지오 코딩)
        Geocoder geocoder = new Geocoder(this, Locale.getDefault()); // Locale : 명확한 지리적/언어적 지역으로서 숫자와 날짜 같은 정보를 지역에 어울리는 표현법으로 조정함
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(addresses != null && addresses.size() > 0) {
            currentAddress = null;

            Address address = addresses.get(0);

            if(address.getLocality() != null) {
                currentAddress = address.getLocality(); // 현재 주소(시/도)
            }

            if (address.getSubLocality() != null) {
                if (currentAddress != null) { // 현재 주소가 null이 아니면
                    currentAddress +=  " " + address.getSubLocality(); // 현재 주소(시/도)에 (군/구) 추가
                } else { // 현재 주소가 null이면
                    currentAddress = address.getSubLocality(); // 현재 주소(군/구) -> 지금 이거로 뜸;
                }
            }

            String country = address.getCountryName(); // 국가
            String adminArea = address.getAdminArea(); // 시/도
            println("주소 : " + country + " " + adminArea + " " + currentAddress);

            if(writeFragment != null) {
                writeFragment.setAddress(adminArea + " " + currentAddress);
            }
        }
    }

    private void println(String data) {
        Log.d(TAG, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int i, String[] permissions) {
        //Toast.makeText(this, "권한이 거부됨 : " + permissions.length, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGranted(int i, String[] permissions) {
        //Toast.makeText(this, "권한이 승인됨 : " + permissions.length, Toast.LENGTH_SHORT).show();
    }

}