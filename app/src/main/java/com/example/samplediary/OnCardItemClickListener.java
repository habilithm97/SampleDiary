package com.example.samplediary;

import android.view.View;


public interface OnCardItemClickListener {
    public void onItemClick(DiaryAdapter.ViewHolder holder, View view, int position);
}
