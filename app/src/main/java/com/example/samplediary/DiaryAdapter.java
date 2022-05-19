package com.example.samplediary;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> implements OnCardItemClickListener {

    static ArrayList<Diary> items = new ArrayList<>();
    //ArrayList<Diary> items;
    //ListFragment listFragment;

    int layoutType = 0;

    OnCardItemClickListener listener;

    static int position;

    Context context;

    /*
    public DiaryAdapter(ArrayList<Diary> items, ListFragment listFragment) {
        this.items = items;
        this.listFragment = listFragment;
    } */

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = (LayoutInflater.from(viewGroup.getContext())).inflate(R.layout.card_item, viewGroup, false);

        context = viewGroup.getContext();

        return new ViewHolder(itemView, this, layoutType); // ViewHolder 객체를 생성하면서 View 객체와 listener, layout Type을 전달하고 그 ViewHolder 객체를 return
    }

    @Override // 생성된 ViewHolder에 데이터를 Binding 함, ViewHolder가 재사용될 때 View 객체는 기존 것 그대로 사용하고 데이터만 바꿔줌
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Diary item = items.get(position);

        holder.setItem(item);
        holder.setLayoutType(layoutType);
    }

    /*
    public void filterList(ArrayList<Diary> filteredList) {
        // 전달 받은 필터링 된 아이템 어레이 리스트로 변경함
       items = filteredList;
        notifyDataSetChanged();
    } */

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(ArrayList<Diary> items) {
        this.items = items;
    }

    public Diary getItem(int position) {
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

    // ViewHolder 클래스
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        LinearLayout layout1, layout2;

        ImageView moodImg, moodImg2, pictureImg, pictureImg2, weatherImg, weatherImg2;
        TextView contentsTv, contentsTv2, locationTv, locationTv2, dateTv, dateTv2;

        // ViewHolder 생성자로 전달되는 View 객체를 참조함(아이템들은 View로 만들어지고, View는 ViewHolder에 담아둠)
        public ViewHolder(@NonNull View itemView, final OnCardItemClickListener listener, int layoutType) {
            super(itemView); // 이 View 객체를 부모 클래스의 변수에 담아둠

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

            // 각 아이템 클릭 시 동작
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

            // 각 아이템을 길게 클릭 시 동작
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    position = getAdapterPosition();

                    // 대화상자 생성
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("삭제하기");
                    builder.setMessage("선택한 일기를 정말로 삭제하시겠습니까 ?");
                    builder.setIcon(R.drawable.delete);
                    builder.setPositiveButton("삭제하기", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // deleteDiary();
                                    //Toast.makeText(context, "삭제되었습니다. ", Toast.LENGTH_SHORT).show();
                                }
                            });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    builder.show();
                    return true;
                }
            });
        }

        public void setItem(Diary item) {
            // 기분 설정
            String mood = item.getMood();
            int moodIndex = Integer.parseInt(mood);
            setMoodImg(moodIndex);

            // 날씨 설정
            String weather = item.getWeather();
            int weatherIndex = Integer.parseInt(weather);
            setWeatherImg(weatherIndex);

            // 이미지 설정
            String picturePath = item.getPicture();
            if (picturePath != null && !picturePath.equals("")) { // 이미지가 있으면
                // 이미지 이모티콘 보이게
                pictureImg.setVisibility(View.VISIBLE);
                pictureImg2.setVisibility(View.VISIBLE);
                pictureImg2.setImageURI(Uri.parse("file://" + picturePath)); // 이미지 가져와서 설정
            } else { // 이미지가 없으면
                // 이미지 이모티콘 안보이게
                pictureImg.setVisibility(View.GONE);
                pictureImg2.setVisibility(View.GONE);
                pictureImg2.setImageResource(R.drawable.noimagefound);
            }

            contentsTv.setText(item.getContents());
            contentsTv2.setText(item.getContents());

            locationTv.setText(item.getAddress());
            locationTv2.setText(item.getAddress());

            dateTv.setText(item.getCreateDateStr());
            dateTv2.setText(item.getCreateDateStr());
        }

        public void setMoodImg(int moodIndex) {
            switch (moodIndex) {
                case 0:
                    moodImg.setImageResource(R.drawable.smile1_48);
                    moodImg2.setImageResource(R.drawable.smile1_48);
                    break;
                case 1:
                    moodImg.setImageResource(R.drawable.smile2_48);
                    moodImg2.setImageResource(R.drawable.smile2_48);
                    break;
                case 2:
                    moodImg.setImageResource(R.drawable.smile3_48);
                    moodImg2.setImageResource(R.drawable.smile3_48);
                    break;
                case 3:
                    moodImg.setImageResource(R.drawable.smile4_48);
                    moodImg2.setImageResource(R.drawable.smile4_48);
                    break;
                case 4:
                    moodImg.setImageResource(R.drawable.smile5_48);
                    moodImg2.setImageResource(R.drawable.smile5_48);
                    break;
                default:
                    moodImg.setImageResource(R.drawable.smile3_48);
                    moodImg2.setImageResource(R.drawable.smile3_48);
                    break;
            }
        }

        public void setWeatherImg(int weatherIndex) {
            switch (weatherIndex) {
                case 0:
                    weatherImg.setImageResource(R.drawable.weather_sun);
                    weatherImg2.setImageResource(R.drawable.weather_sun);
                    break;
                case 1:
                    weatherImg.setImageResource(R.drawable.weather_mini_cloud);
                    weatherImg2.setImageResource(R.drawable.weather_mini_cloud);
                    break;
                case 2:
                    weatherImg.setImageResource(R.drawable.weather_sun_cloud);
                    weatherImg2.setImageResource(R.drawable.weather_sun_cloud);
                    break;
                case 3:
                    weatherImg.setImageResource(R.drawable.weather_cloud);
                    weatherImg2.setImageResource(R.drawable.weather_cloud);
                    break;
                case 4:
                    weatherImg.setImageResource(R.drawable.weather_rain);
                    weatherImg2.setImageResource(R.drawable.weather_rain);
                    break;
                case 5:
                    weatherImg.setImageResource(R.drawable.weather_snow_rain);
                    weatherImg2.setImageResource(R.drawable.weather_snow_rain);
                    break;
                case 6:
                    weatherImg.setImageResource(R.drawable.weather_snow);
                    weatherImg2.setImageResource(R.drawable.weather_snow);
                    break;
                default:
                    weatherImg.setImageResource(R.drawable.weather_sun);
                    weatherImg2.setImageResource(R.drawable.weather_sun);
                    break;
            }
        }

        public void setLayoutType(int layoutType) { // 아이템을 내용 중심으로 보여줄지 이미지 중심으로 보여줄지 선택할 수 있음
            if (layoutType == 0) { // 내용 중심
                layout1.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.GONE);
            } else if (layoutType == 1) { // 이미지 중심
                layout1.setVisibility(View.GONE);
                layout2.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return true;
        }
    }
}
