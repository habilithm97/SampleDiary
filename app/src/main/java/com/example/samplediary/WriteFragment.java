package com.example.samplediary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samplediary.R;
import com.github.channguyen.rsv.RangeSliderView;

public class WriteFragment extends Fragment {

    private static final String TAG = "WriteFragment";

    Context context;
    onTabItemSelectedListener listener;
    OnRequestListener requestListener;

    TextView dateTv, locationTv, weatherTv;
    ImageView weatherIcon, pictureInput;

    boolean isPhotoCaptured;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;

    int selectedPhotoMenu;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;

        if(context instanceof onTabItemSelectedListener) {
            listener = (onTabItemSelectedListener) context;
        }

        if(context instanceof OnRequestListener) {
            requestListener = (OnRequestListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(context != null) {
            context = null;
            listener = null;
            requestListener = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_write, container, false);

        initUi(rootView);

        if(requestListener != null)
        {
            requestListener.onRequest("getCurrentLocation"); // 현재 위치 요청하기!!!
            println("현재 위치 요청함");
            //Toast.makeText(getContext(), "현재 위치 요청함", Toast.LENGTH_SHORT).show();
        }
        return rootView;
    }
    private void initUi(ViewGroup rootView) { // 인플레이션 후에 xml 레이아웃 안에 들어 있는 위젯이나 레이아웃을 찾아
        // 변수에 할당하는 코드들을 넣기 위해 만들어 둔 것임

        weatherIcon = rootView.findViewById(R.id.weatherIcon);
        dateTv = rootView.findViewById(R.id.dateTv);
        locationTv = rootView.findViewById(R.id.locationTv);
        weatherTv = rootView.findViewById(R.id.weatherTv);
        EditText contentEdt = rootView.findViewById(R.id.contentsEdt);

        pictureInput = rootView.findViewById(R.id.pictureInput);
        pictureInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPhotoCaptured || isPhotoFileSaved) { // 이미 사진이 있거나 사진 파일이 저장된 경우
                    showDialog(AppConstants.CONTENT_PHOTO_EX);
                } else { // 사진이 없는 경우
                    showDialog(AppConstants.CONTENT_PHOTO);
                }
            }
        });

        Button saveBtn = rootView.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onTabSelected(0); // 리스트 프래그먼트로 화면 전환
                }
            }
        });

        Button deleteBtn = rootView.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onTabSelected(0); // 리스트 프래그먼트로 화면 전환
                }
            }
        });

        Button cancelBtn = rootView.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onTabSelected(0); // 리스트 프래그먼트로 화면 전환
                }
            }
        });

        RangeSliderView sliderView = rootView.findViewById(R.id.sliderView);
        sliderView.setOnSlideListener(new RangeSliderView.OnSlideListener() {
            @Override
            public void onSlide(int index) { // 값이 바뀔 때마다 호출
            }
        });
        sliderView.setInitialIndex(2); // 다섯 개의 기분 중 가운데 기분이 디폴트 값임
    }

    public void setDateString(String dateString) {
        dateTv.setText(dateString);
    }

    public void setWeather(String data) {

        if (data != null) {
            if (data.equals("맑음")) {
                weatherIcon.setImageResource(R.drawable.weather_sun);
                weatherTv.setText("맑음");
            } else if (data.equals("구름 조금")) {
                weatherIcon.setImageResource(R.drawable.weather_mini_cloud);
                weatherTv.setText("구름 조금");
            } else if (data.equals("구름 많음")) {
                weatherIcon.setImageResource(R.drawable.weather_sun_cloud);
                weatherTv.setText("구름 많음");
            } else if (data.equals("흐림")) {
                weatherIcon.setImageResource(R.drawable.weather_cloud);
                weatherTv.setText("흐림");
            } else if (data.equals("비")) {
                weatherIcon.setImageResource(R.drawable.weather_rain);
                weatherTv.setText("비");
            } else if (data.equals("눈/비")) {
                weatherIcon.setImageResource(R.drawable.weather_snow_rain);
                weatherTv.setText("눈/비");
            } else if (data.equals("눈")) {
                weatherIcon.setImageResource(R.drawable.weather_snow);
                weatherTv.setText("눈");
            } else {
                Log.d(TAG, "알 수 없는 날씨 : " + data);
            }
        }
    }

    public void setAddress(String data) {
        locationTv.setText(data);
    }

    public void showDialog(int id) {
        AlertDialog.Builder builder = null;

        switch (id) {
            case AppConstants.CONTENT_PHOTO: // 사진이 없는 경우
                builder = new AlertDialog.Builder(context);
                builder.setTitle("사진 메뉴 선택");
                // setSingleChoiceItems(배열, 라디오 버튼의 체크 위치, 선택 시 발생하는 이벤트)
                builder.setSingleChoiceItems(R.array.array_photo, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichBtn) {
                        selectedPhotoMenu = whichBtn;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedPhotoMenu == 0) {
                           //showPhotoCaptureActivity();
                        } else if (selectedPhotoMenu == 1) {
                            //showPhotoSelectionActivity();
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                break;


            case AppConstants.CONTENT_PHOTO_EX: // 이미 사진이 있거나 사진 파일이 저장된 경우
               builder = new AlertDialog.Builder(context);
                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichBtn) {
                        selectedPhotoMenu = whichBtn;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedPhotoMenu == 0) {
                            //showPhtoCaptureActivity();
                        } else if (selectedPhotoMenu == 1) {
                            //showPhotoSelectionActivity();
                        } else if (selectedPhotoMenu == 2) {
                            isPhotoCanceled = true;
                            isPhotoCaptured = false;

                            pictureInput.setImageResource(R.drawable.cube);
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                break;

            default:
                break;
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void println(String data) {
        Log.d(TAG, data);
    }
}