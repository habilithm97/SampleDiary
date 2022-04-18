package com.example.samplediary;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import lib.kingja.switchbutton.SwitchMultiButton;

public class ListFragment extends Fragment {
    private static final String TAG = "ListFragment";

    RecyclerView recyclerView;
    CardAdapter adapter;

    Context context;

    onTabItemSelectedListener listener;

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CardAdapter();

        adapter.addItem(new Card(0, "서울시 강남구 홍길동", "0", ", ", ", ", "오늘도 열심히 공부", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Card(0, "서울시 강남구 홍길동", "0", ", ", ", ", "점심에 동네 떡볶이 맛집 갔다옴", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Card(0, "서울시 강남구 홍길동", "0", ", ", ", ", "오늘도 열심히 운동", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Card(0, "서울시 강남구 홍길동", "0", ", ", ", ", "안녕하세요", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Card(0, "서울시 강남구 홍길동", "0", ", ", ", ", "하빌리즘 입니다", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Card(0, "서울시 강남구 홍길동", "0", ", ", ", ", "오늘도 열심히 공부", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Card(0, "서울시 강남구 홍길동", "0", ", ", ", ", "점심에 동네 떡볶이 맛집 갔다옴", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Card(0, "서울시 강남구 홍길동", "0", ", ", ", ", "오늘도 열심히 운동", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Card(0, "서울시 강남구 홍길동", "0", ", ", ", ", "안녕하세요", "0", "cube.jpg", "2월 17일"));
        adapter.addItem(new Card(0, "서울시 강남구 홍길동", "0", ", ", ", ", "하빌리즘 입니다", "0", "cube.jpg", "2월 17일"));

        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnCardItemClickListener() {
            @Override
            public void onItemClick(CardAdapter.ViewHolder holder, View view, int position) {
                Card item = adapter.getItem(position);
                Toast.makeText(getContext(), "아이템 선택 : " + item.getContents(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int loadDiaryListData()_ {
        AppConstants.println("저장된 데이터를 로드함. ");
        String sql = "select -id, WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE, CREATE_DATE, MODIFY_DATE from "
                + DiaryDatabase.TABLE_DIARY + " order by CREATE_DATE desc";

        int recordCount = -1;

        DiaryDatabase database = DiaryDatabase.getInstance(context);
        if(database != null) {
            Cursor outCursor = database.ra
        }
    }
}