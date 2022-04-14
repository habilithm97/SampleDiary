package com.example.samplediary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.File;

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

    File file;

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
                if(isPhotoCaptured || isPhotoFileSaved) { // 이미 사진이 있거나 사진이 저장된 경우
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
            case AppConstants.CONTENT_PHOTO_EX: // 이미 사진이 있거나 사진 파일이 저장된 경우 -> +삭제하기 까지
                // 사진

                builder = new AlertDialog.Builder(context);
                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo_ex, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichBtn) {
                        selectedPhotoMenu = whichBtn;
                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedPhotoMenu == 0) {
                            showPhotoCaptureActivity(); // 사진 찍기
                        } else if (selectedPhotoMenu == 1) {
                            showPhotoSelectionActivity(); // 앨범에서 선택하기
                        } else if (selectedPhotoMenu == 2) { // 삭제하기
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
                            showPhotoCaptureActivity(); // 사진 찍기
                        } else if (selectedPhotoMenu == 1) {
                            showPhotoSelectionActivity(); // 앨범에서 선택하기
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

    public void showPhotoCaptureActivity() { // 사진 찍기
        // 파일이 없으면 생성(카메라 앱에서 촬영한 사진을 저장할 file)
        if(file == null) {
            file = createFile();
        }
        /*
        *FileProvider
          -ContentProvider의 서브 클래스로서 file:// 형태의 uri 대신 content:// 형태의 uri를 사용함
           (앱 간의 파일 공유를 위해 content:// 형태로 uri를 보내고 이 uri에 대해 임시 액세스 권한을 부여해야함 -> FileProvider 클래스로 권한 부여)
        */

        // 카메라 앱에서 공유하여 사용할 수 있는 파일 정보를 Uri 객체로 생성
        Uri fileUri = FileProvider.getUriForFile(context, "com.example.samplediary.fileprovider", file);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // 미리 Intent 객체에 정의된 카메라 앱 실행 액션 정보(시스템에게 요청할거임)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // Intent에 어떤 파일을 저장할 것인지 지정 및 파일의 Uri 정보 설정
        if(intent.resolveActivity(context.getPackageManager()) != null) { // 실행 가능한지 확인(카메라 앱 유무 확인)
            startActivityForResult(intent, AppConstants.REQ_PHOTO_CAPTURE); // 카메라 앱 화면 실행
        }
    }

    private File createFile() { // 파일 생성
        String filename = "capture.jpg"; // sd 카드 파일 이름
        File storageDir = Environment.getExternalStorageDirectory();
        File outFile = new File(storageDir, filename);

        return outFile;
    }

    public void showPhotoSelectionActivity() { // 앨범에서 선택하기
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, AppConstants.REQ_PHOTO_SELECTION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(intent != null) {
            switch (requestCode) {
                case AppConstants.REQ_PHOTO_CAPTURE: // 사진 찍기 메뉴를 선택했을 경우
                    Log.d(TAG, "사진 찍기 메뉴의 onActivityResult() ");
                    Log.d(TAG, "resultCode : " + resultCode);

                    Bitmap resultPhotoBitmap = decodeSampleBitmapFromResource(file, pictureInput.getWidth(), pictureInput.getHeight());
                    pictureInput.setImageBitmap(resultPhotoBitmap);

                    // 사진이 넣었기 때문에 사진 유무 상태를 변경
                    isPhotoCaptured = true;
                    isPhotoFileSaved = true;

                    break;

                case AppConstants.REQ_PHOTO_SELECTION: // 앨범에서 선택하기 메뉴를 선택했을 경우
                    Log.d(TAG, "앨범에서 선택하기 메뉴의 onActivityResult() ");

                    Uri selectionImage = intent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = context.getContentResolver().query(selectionImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    resultPhotoBitmap = decodeSampleBitmapFromResource(new File(filePath), pictureInput.getWidth(), pictureInput.getHeight());
                    pictureInput.setImageBitmap(resultPhotoBitmap);
                    isPhotoCaptured = true;

                    break;
            }

        }
    }

    // 대용량 Bitmap을 Exception 없이 효과적으로 로딩하기
    public static Bitmap decodeSampleBitmapFromResource(File res, int reqWidth, int reqHeight) {
        /* BitmapFactory 클래스는 Bitmap을 만들 수 있는 Decoding 메서드들을 제공하는데, 이미지 데이터 소스에 따라 가장 적합한 Decoding 방법을 선택함
        *Decoding 메서드
         -decodeByteArray() : byte[] 배열을 해석해서 Bitmap을 생성함
         -decodeFile() : 파일 경로의 파일을 Bitmap으로 생성함
         -decodeResource() : res 폴더에 저장된 이미지를 Bitmap으로 생성함
         -decodeStream() : InputStream으로 Bitmap을 생성함
         */
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // true일 경우, 이미지의 크기만 구해서 옵션에 설정함
        BitmapFactory.decodeFile(res.getAbsolutePath(), options);

        /*
        *int inSampleSize
         -이미지 파일을 Decoding 할 때 원본 이미지 크기대로 Decoding 할지, 축소해서 Decoding 할지를 지정함
         -값은 1 또는 2의 거듭 제곱 값이 들어갈 수 있음(그 외의 값일 경우 가장 가까운 2의 거듭 제곱 값으로 내림되어 실행됨
         -값이 1일 경우(또는 1보다 작을 경우) : 원본 이미지 크기로
         -값이 2일 경우(또는 2보다 클 경우) : 가로/세로 픽셀을 해당 값 만큼 나눈 크기로
          ex) 값을 2로 지정했을 경우 이미지의 가로/세로 값이 각각 2로 나누어진 크기로 설정되어 실제 이미지 크기는 원본 이미지의 1/4가 됨됨
        */
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false; // 로드하기 위해서 true에서 false로 설정함

        return BitmapFactory.decodeFile(res.getAbsolutePath(), options);
    }

    // 로드하려는 이미지가 실제 필요한 사이즈보다 큰지 체크하고
    // 실제 필요한 사이즈로 이미지를 조절하기
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight; // 높이
        final int width = options.outWidth; // 너비
        int inSampleSize = 1; // 원본 이미지

        if(height > reqHeight || width > reqWidth) { // 필요한 사이즈보다 크면
            final int halfHeight = height;
            final int halfWidth = width;

            while((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2; // 이미지 축소
            }
        }
        return inSampleSize;
    }

    public void println(String data) {
        Log.d(TAG, data);
    }
}