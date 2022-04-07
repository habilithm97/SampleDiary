package com.example.samplediary;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.samplediary.R;

import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> implements OnCardItemClickListener {

    ArrayList<Card> items = new ArrayList<Card>();

    int layoutType = 0;

    OnCardItemClickListener listener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = (LayoutInflater.from(viewGroup.getContext())).inflate(R.layout.card_item, viewGroup, false);

        return new ViewHolder(itemView, this, layoutType); // 뷰홀더 객체를 생성하면서 뷰 객체와 리스너, 레이아웃 타입을 전달하고 그 뷰홀더 객체 리턴함
    }

    @Override // 생성된 뷰홀더에 데이터를 바인딩해줌, 뷰홀더가 재사용될 때 뷰 객체는 기존 것 그대로 사용하고 데이터만 바꿔줌
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Card item = items.get(position);

        holder.setItem(item);
        holder.setLayoutType(layoutType);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Card item) {
        items.add(item);
    }

    public void setItems(ArrayList<Card> items) {
        this.items = items;
    }

    public Card getItem(int position) {
        return items.get(position);
    }

    public void setOnItemClickListener(OnCardItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if(listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }

    public void switchLayout(int position) {
        layoutType = position;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layout1, layout2;

        ImageView moodImg, moodImg2, pictureImg, pictureImg2, weatherImg, weatherImg2;

        TextView contentsTv, contentsTv2, locationTv, locationTv2, dateTv, dateTv2;

        // 뷰홀더 생성자로 전달되는 뷰 객체를 참조함(아이템들은 뷰로 만들어지고, 뷰는 뷰홀더에 담아둠)
        public ViewHolder(@NonNull View itemView, final OnCardItemClickListener listener, int layoutType) {
            super(itemView); // 이 뷰 객체를 부모 클래스의 변수에 담아둠

            layout1 = itemView.findViewById(R.id.layout1);
            layout2 = itemView.findViewById(R.id.layout2);

            moodImg = itemView.findViewById(R.id.moodImg);
            moodImg2 = itemView.findViewById(R.id.moodImg2);

            pictureImg = itemView.findViewById(R.id.pictureImg);
            pictureImg2 = itemView.findViewById(R.id.pictureImg2);

            weatherImg = itemView.findViewById(R.id.weatherImg);
            weatherImg2 = itemView.findViewById(R.id.weatherImg2);

            contentsTv = itemView.findViewById(R.id.contentsTv);
            contentsTv2 = itemView.findViewById(R.id.contentsTv2);

            locationTv = itemView.findViewById(R.id.locationTv);
            locationTv2 = itemView.findViewById(R.id.locationTv2);

            dateTv = itemView.findViewById(R.id.dateTv);
            dateTv2 = itemView.findViewById(R.id.dateTv2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (listener != null) {
                        listener.onItemClick(ViewHolder.this, view, position);
                    }
                }
            });
            setLayoutType(layoutType);
        }

        public void setItem(Card item) {
            // 기분 설정
            String mood = item.getMood();
            int moodIndex = Integer.parseInt(mood);
            setMoodImg(moodIndex);

            // 사진 설정
            String picturePath = item.getPicture();
            if (picturePath != null && !picturePath.equals("")) { // 사진이 있으면
                // 사진 보이게
                pictureImg.setVisibility(View.VISIBLE);
                pictureImg2.setVisibility(View.VISIBLE);
                pictureImg2.setImageURI(Uri.parse("file://" + picturePath));
            } else { // 사진이 없으면
                pictureImg.setVisibility(View.GONE);
                pictureImg2.setVisibility(View.GONE);
                pictureImg2.setImageResource(R.drawable.noimagefound);
            }

            // 날씨 설정
            String weather = item.getWeather();
            int weatherIndex = Integer.parseInt(weather);
            setWeatherImg(weatherIndex);

            contentsTv.setText(item.getContents());
            contentsTv2.setText(item.getContents());

            locationTv.setText(item.getAddress());
            locationTv2.setText(item.getAddress());

            dateTv.setText(item.getCreateDate());
            dateTv2.setText(item.getCreateDate());
        }

        public void setMoodImg(int moodIndex) {
            switch (moodIndex) {
                case 0:
                    moodImg.setImageResource(R.drawable.smile);
                    moodImg2.setImageResource(R.drawable.smile);

                case 1:
                    moodImg.setImageResource(R.drawable.smile);
                    moodImg2.setImageResource(R.drawable.smile);

                case 2:
                    moodImg.setImageResource(R.drawable.smile);
                    moodImg2.setImageResource(R.drawable.smile);

                case 3:
                    moodImg.setImageResource(R.drawable.smile);
                    moodImg2.setImageResource(R.drawable.smile);

                case 4:
                    moodImg.setImageResource(R.drawable.smile);
                    moodImg2.setImageResource(R.drawable.smile);
                default:
                    moodImg.setImageResource(R.drawable.smile);
                    moodImg2.setImageResource(R.drawable.smile);
            }
        }

        public void setWeatherImg(int weatherIndex) {
            switch (weatherIndex) {
                case 0:
                    weatherImg.setImageResource(R.drawable.weather);
                    weatherImg2.setImageResource(R.drawable.weather);

                case 1:
                    weatherImg.setImageResource(R.drawable.weather);
                    weatherImg2.setImageResource(R.drawable.weather);

                case 2:
                    weatherImg.setImageResource(R.drawable.weather);
                    weatherImg2.setImageResource(R.drawable.weather);

                case 3:
                    weatherImg.setImageResource(R.drawable.weather);
                    weatherImg2.setImageResource(R.drawable.weather);

                case 4:
                    weatherImg.setImageResource(R.drawable.weather);
                    weatherImg2.setImageResource(R.drawable.weather);

                case 5:
                    weatherImg.setImageResource(R.drawable.weather);
                    weatherImg2.setImageResource(R.drawable.weather);

                case 9:
                    weatherImg.setImageResource(R.drawable.weather);
                    weatherImg2.setImageResource(R.drawable.weather);
                default:
                    weatherImg.setImageResource(R.drawable.weather);
                    weatherImg2.setImageResource(R.drawable.weather);
            }
        }

        public void setLayoutType(int layoutType) { // 아이템을 내용 중심으로 or 사진 중심으로 다른 레이아웃
            if (layoutType == 0) { // 내용 중심
                layout1.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.GONE);
            } else if (layoutType == 1) { // 사진 중심
                layout1.setVisibility(View.GONE);
                layout2.setVisibility(View.VISIBLE);
            }
        }
    }
}
