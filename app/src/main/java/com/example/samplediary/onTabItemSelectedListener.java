package com.example.samplediary;

// 액티비티와 연계하기 위한 인터페이스
public interface onTabItemSelectedListener {
    public void onTabSelected(int position);
    public void showWriteFragment(Diary item);
}
