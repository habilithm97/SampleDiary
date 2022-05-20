package com.example.samplediary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.samplediary.Diary;
import com.example.samplediary.DiaryAdapter;
import com.example.samplediary.onTabItemSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lib.kingja.switchbutton.SwitchMultiButton;
import static com.example.samplediary.DiaryAdapter.position;

public class ListFragment extends Fragment {
    private static final String TAG = "ListFragment";

    // 검색 필터 파트
    //ArrayList<Diary> items, filteredList; // 어댑터에서 가져온 아이템 어레이 리스트와 필터링된 아이템 어레이 리스트
    EditText searchEdt;

    DiaryAdapter adapter;

    Context context;

    onTabItemSelectedListener listener;

    static Diary item;

    @Override // 프래그먼트가 액티비티에 붙을 때 호출됨 -> 액티비티를 위해 설정해야하는 정보들은 이 곳에서 처리함
    public void onAttach(@NonNull Context context) { // context 객체나 listener 객체를 참조하여 변수에 할당
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
    public void onDetach() { // 프래그먼트가 액티비티에서 내려올 때 호출됨
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
        /*
        // 검색 필터 파트
        items = new ArrayList<>(); // 아이템 어레이 리스트
        filteredList = new ArrayList<>(); // 필터링된 아이템 어레이 리스트
         */

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        // 첫 번째 프래그먼트로 이동 시 포커스를 맨 위로 이동함
        recyclerView.scrollToPosition(position);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.smoothScrollToPosition(position);
            }
        }, 500);

        //adapter = new DiaryAdapter(items, this); // 이 부분에서 어댑터 클래스와 연결
        adapter = new DiaryAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 각 아이템들을 클릭하면 두 번째 프래그먼트인 작성화면으로 화면이 전환되어 아이템에 저장된 데이터가 표시됨
        adapter.setOnItemClickListener(new OnCardItemClickListener() {
            @Override
            public void onItemClick(DiaryAdapter.ViewHolder holder, View view, int position) {
                item = adapter.getItem(position);
                Log.d(TAG, "아이템 선택됨 : " + item.get_id());

                if (listener != null) {
                    listener.showWriteFragment(item);
                }
            }
        });

        /*
        searchEdt = (EditText)rootView.findViewById(R.id.searchEdt);
        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { // 입력하기 전에 처리
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // 입력과 동시에 처리
            }

            @Override
            public void afterTextChanged(Editable s) { // 입력 후에 처리
                String str = searchEdt.getText().toString();
                searchFilter(str);
            }
        }); */
    }

    public int loadDiaryListData() { // 저장된 데이터를 불러와서 리스트에 보여줌
        AppConstants.println("리스트로 보여줄 데이터를 불러옴. ");

        // 작성 일자(CREATE_DATE)를 기준으로 테이블의 필드들을 조회하는 sql 변수 할당
        String sql = "select _id, WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE, CREATE_DATE, MODIFY_DATE from " + DiaryDatabase.TABLE_DIARY + " order by CREATE_DATE desc";

        int recordCount = -1;

        DiaryDatabase database = DiaryDatabase.getInstance(context); // 데이터베이스 접근
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

    /*
    public void searchFilter(String str) { // 검색창에 입력한 문자열이 파라미터로 전달됨
        filteredList.clear(); // 필터링된 아이템 어레이 리스트를 비우고

        for(int i = 0; i < items.size(); i++) {
            if(items.get(i).getContents().toLowerCase().contains(str.toLowerCase())) { // 입력한 문자열과 일치하는 아이템이면
                filteredList.add(items.get(i)); // 필터링된 아이템 어레이 리스트에 추가하기
            }
        }
        adapter.filterList(filteredList); // 어댑터의 filterList 메서드를 호출해서 필터링된 아이템 어레이 리스트를 파라미터로 전달함
    } */
}