package com.example.samplediary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lib.kingja.switchbutton.SwitchMultiButton;

import static com.example.samplediary.DiaryAdapter.position;

public class ListFragment extends Fragment {
    private static final String TAG = "ListFragment";

    RecyclerView recyclerView;
    DiaryAdapter adapter;

    Context context;

    onTabItemSelectedListener listener;

    static Diary item;

    @Override // 프래그먼트가 액티비티에 붙을 때 호출됨 -> 액티비티를 위해 설정해야하는 정보들은 이 곳에서 처리함
    public void onAttach(@NonNull Context context) { // context 객체나 리스너 객체를 참조하여 변수에 할당
        super.onAttach(context);

        this.context = context;

        // 액티비티에 필요한 인터페이스가 구현이 됐는지 확인
        if (context instanceof onTabItemSelectedListener) {
            listener = (onTabItemSelectedListener) context;
        } else { // throw new 예외~ 구문은 강제로 예외를 발생 시킬 수 있음
            throw new RuntimeException(context.toString() + "onTabSelected() 메서드를 구현해주세요. ");
        }
    }

    @Override
    public void onDetach() { // 프래그먼트가 액티비티에서 내려올 때  호출됨
        super.onDetach();

        if (context != null) {
            context = null;
            listener = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_list, container, false);
        // rootView는 최상위 레이아웃, xml 인플레이션을 통해 참조한 객체임 -> 인플레이션 과정이 끝나고 나면 프래그먼트가 하나의 뷰처럼 동작할 수 있게됨
        initUi(rootView);

        loadDiaryListData();

        return rootView;
    }

    // 인플레이션 후에 xml 레이아웃 안에 들어 있는 위젯이나 레이아웃을 찾아 변수에 할당하는 코드를 넣음
    private void initUi(ViewGroup rootView) {

        Button writeBtn = rootView.findViewById(R.id.writeBtn);
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onTabSelected(1); // 작성하기 버튼을 누르면 두 번째 프래그먼트(작성화면)를 띄워줌
                }
            }
        });

        SwitchMultiButton switchBtn = rootView.findViewById(R.id.switchBtn);
        switchBtn.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                adapter.switchLayout(position);
                adapter.notifyDataSetChanged();
            }
        });

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.scrollToPosition(position);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.smoothScrollToPosition(position);
            }
        }, 500);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DiaryAdapter();
        /*
        adapter.addItem(new Diary(0, "서울특별시 신짱구", "0", ", ", ", ", "오늘도 열심히 공부", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Diary(0, "서울특별시 신짱구", "0", ", ", ", ", "점심에 동네 떡볶이 맛집 갔다옴", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Diary(0, "서울특별시 신짱구", "0", ", ", ", ", "오늘도 열심히 운동", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Diary(0, "서울특별시 신짱구", "0", ", ", ", ", "안녕하세요", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Diary(0, "서울특별시 신짱구", "0", ", ", ", ", "하빌리즘 입니다", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Diary(0, "서울특별시 신짱구", "0", ", ", ", ", "오늘도 열심히 공부", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Diary(0, "서울특별시 신짱구", "0", ", ", ", ", "점심에 동네 떡볶이 맛집 갔다옴", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Diary(0, "서울특별시 신짱구", "0", ", ", ", ", "오늘도 열심히 운동", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Diary(0, "서울특별시 신짱구", "0", ", ", ", ", "안녕하세요", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Diary(0, "서울특별시 신짱구", "0", ", ", ", ", "하빌리즘 입니다", "0", "cube.jpg", "2월 17일"));
         */

        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnCardItemClickListener() {
            @Override
            public void onItemClick(DiaryAdapter.ViewHolder holder, View view, int position) {
                item = adapter.getItem(position);
                Log.d(TAG, "아이템 선택됨 : " + item.get_id());
                Toast.makeText(getContext(), "날씨 확인 : "  + item.weather, Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.showWriteFragment(item);
                }
            }
        });
    }

    public int loadDiaryListData() { // 저장된 데이터를 불러와서 리스트로 보여줌
        AppConstants.println("리스트로 보여줄 데이터를 불러옴. ");

        // 작성 일자를 기준으로 테이블의 필드들을 조회하는 sql 변수 할당
        String sql = "select _id, WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE, CREATE_DATE, MODIFY_DATE from " + DiaryDatabase.TABLE_DIARY + " order by CREATE_DATE desc";

        int recordCount = -1;

        DiaryDatabase database = DiaryDatabase.getInstance(context);
        // 데이터 베이스가 있을 경우 Cursor를 사용해 데이터 조회하기
        if (database != null) {
            Cursor outCursor = database.rawQuery(sql);

            recordCount = outCursor.getCount();
            AppConstants.println("레코드 개수 : " + recordCount + "\n");

            ArrayList<Diary> items = new ArrayList<Diary>();

            // 레코드 개수만큼 조회하기
            for (int i = 0; i < recordCount; i++) {
                outCursor.moveToNext();

                int _id = outCursor.getInt(0);
                String weather = outCursor.getString(1);
                String address = outCursor.getString(2);
                String locationX = outCursor.getString(3);
                String locationY = outCursor.getString(4);
                String contents = outCursor.getString(5);
                String mood = outCursor.getString(6);
                String picture = outCursor.getString(7);
                String dateStr = outCursor.getString(8);
                String createDateStr = null;

                if (dateStr != null && dateStr.length() > 10) {
                    try {
                        Date inDate = AppConstants.dateFormat4.parse(dateStr);
                        createDateStr = AppConstants.dateFormat3.format(inDate);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    createDateStr = "";
                }
                AppConstants.println("#" + i + " -> " + _id + ", " + weather + ", " + address + ", " + locationX + ", " + locationY + ", " + contents + ", " +
                        mood + ", " + picture + ", " + createDateStr);
                items.add(new Diary(_id, weather, address, locationX, locationY, contents, mood, picture, createDateStr));
            }
            outCursor.close();

            // 어댑터로 리스트에 설정
            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }
        return recordCount;
    }
}