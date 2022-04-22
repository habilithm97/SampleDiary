package com.example.samplediary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WriteFragment extends Fragment {
    private static final String TAG = "WriteFragment";

    Context context;
    onTabItemSelectedListener listener;
    OnRequestListener requestListener;

    TextView dateTv, locationTv, weatherTv;
    ImageView weatherIcon, pictureInput;
    EditText contentEdt;

    boolean isPhotoCaptured;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;

    int selectedPhotoMenu;

    File file;

    int mMode = AppConstants.MODE_INSERT; // 일기를 새로 만드는지(default), 아니면 기존 일기를 수정하는지를 구분하는 구분자 값임
    int _id = -1;
    int weatherIndex = 0;
    RangeSliderView moodSlider;
    int moodIndex = 2;

    Diary item; // 기존 일기가 있으면 작성화면으로 전환되면서 item 변수 값이 설정되고 화면에는 item 변수가 들어 있는 데이터를 보여줌

    Bitmap resultPhotoBitmap;

    SimpleDateFormat todayDateFormat;

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

        if(requestListener != null) {
            requestListener.onRequest("getCurrentLocation"); // 현재 위치 요청하기!!!
        }
        return rootView;
    }

    private void initUi(ViewGroup rootView) { // 인플레이션 후에 xml 레이아웃 안에 들어 있는 위젯이나 레이아웃을 찾아
        // 변수에 할당하는 코드들을 넣기 위해 만들어 둔 것임

        weatherIcon = rootView.findViewById(R.id.weatherIcon);
        dateTv = rootView.findViewById(R.id.dateTv);
        locationTv = rootView.findViewById(R.id.locationTv);
        weatherTv = rootView.findViewById(R.id.weatherTv);
        contentEdt = rootView.findViewById(R.id.contentsEdt);

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
                if(mMode == AppConstants.MODE_INSERT) { // 저장
                    saveDiary();
                    contentEdt.setText(null);
                    // 사진이 삭제했기 때문에 사진 유무 상태를 변경
                    isPhotoCaptured = false;
                    isPhotoFileSaved = false;

                } else if(mMode == AppConstants.MODE_MODIFY) { // 수정
                    modifyDiary();
                }
                if(listener != null) {
                    listener.onTabSelected(0); // 리스트 프래그먼트로 화면 전환
                }
            }
        });

        Button deleteBtn = rootView.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDiary();

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

        moodSlider = rootView.findViewById(R.id.sliderView);
        final RangeSliderView.OnSlideListener listener = new RangeSliderView.OnSlideListener() {
            @Override
            public void onSlide(int index) { // 값이 바뀔 때마다 호출됨
                AppConstants.println("기분이 변경됨 : " + index);
                moodIndex = index;
            }
        };
        moodSlider.setOnSlideListener(listener);
        moodSlider.setInitialIndex(2); // 다섯 개의 기분 중 가운데 기분이 디폴트 값임
    }

    private void saveDiary() {
        String address = locationTv.getText().toString();
        String contents = contentEdt.getText().toString();
        String picturePath = savePicture();

        String sql = "insert into " + DiaryDatabase.TABLE_DIARY + "(WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE) values(" +
                "'"+ weatherIndex + "', " +
                "'"+ address + "', " +
                "'"+ "" + "', " +
                "'"+ "" + "', " +
                "'"+ contents + "', " +
                "'"+ moodIndex + "', " +
                "'"+ picturePath + "')";

        Log.d(TAG, "sql : " + sql);
        DiaryDatabase database = DiaryDatabase.getInstance(context);
        database.execSQL(sql); // 데이터 베이스의 쿼리를 실행시켜 데이터를 삽입함
    }

    private void modifyDiary() {
        if(item != null) {
            String address = locationTv.getText().toString();
            String contents = contentEdt.getText().toString();
            String picturePath = savePicture();

            String sql = "update " + DiaryDatabase.TABLE_DIARY +
                    " set " +
                    "   WEATHER = '" + weatherIndex + "'" +
                    "   ,ADDRESS = '" + address + "'" +
                    "   ,LOCATION_X = '" + "" + "'" +
                    "   ,LOCATION_Y = '" + "" + "'" +
                    "   ,CONTENTS = '" + contents + "'" +
                    "   ,MOOD = '" + moodIndex + "'" +
                    "   ,PICTURE = '" + picturePath + "'" +
                    " where " +
                    "   _id = " + item._id;

            Log.d(TAG, "sql : " + sql);
            DiaryDatabase database = DiaryDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }

    private void deleteDiary() {
        AppConstants.println("삭제 메서드가 호출됨. ");

        if(item != null) {
            String sql = "delete from " + DiaryDatabase.TABLE_DIARY + " where " + "  _id = " + item._id;

            Log.d(TAG, "sql : " + sql);
            DiaryDatabase database = DiaryDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }

    private String savePicture() { // 사진을 데이터 베이스에 저장함
        if (resultPhotoBitmap == null) {
            AppConstants.println("저장할 사진이 없음. ");
            return "";
        }
        // 이미지는 폴더를 만들어 저장하고, 이미지 경로만 데이터 베이스에 저장함
        File photoFolder = new File(AppConstants.FOLDER_PHOTO); // 이미지를 저장할 폴더
        if(!photoFolder.isDirectory()) { // 사진 폴더가 없으면 생성
            Log.d(TAG, "사진 폴더 생성 : " + photoFolder);
            photoFolder.mkdirs();
        }

        String photoFilename = createFilename(); // 현재 날짜를 이미지 파일 이름으로함
        String picturePath = photoFolder + File.separator + photoFilename; // 이미지 경로

        try {
            FileOutputStream outstream = new FileOutputStream(picturePath); // 이미지 경로로 파일 생성
            resultPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream); // 이미지를 압축(100이면 그대로)
            outstream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return picturePath;
    }

    private File createFile() { // 파일 생성
        // String filename = "capture.jpg"; // sd 카드 파일 이름
        String filename = createFilename();
        //File storageDir = Environment.getExternalStorageDirectory();
        //File outFile = new File(storageDir, filename);
        File outFile = new File(context.getFilesDir(), filename);
        Log.d("메인 ", "파일 경로 : " + outFile.getAbsolutePath());

        return outFile;
    }

    private String createFilename() { // 파일 이름은 현재 날짜를 기준으로함
        Date curDate = new Date();
        String curDateStr = String.valueOf(curDate.getTime());

        return curDateStr;
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

    public void setWeatherIndex(int index) {
        if (index == 0) {
            weatherIcon.setImageResource(R.drawable.weather_sun);
            weatherIndex = 0;
        } else if (index == 1) {
            weatherIcon.setImageResource(R.drawable.weather_mini_cloud);
            weatherIndex = 1;
        } else if (index == 2) {
            weatherIcon.setImageResource(R.drawable.weather_sun_cloud);
            weatherIndex = 2;
        } else if (index == 3) {
            weatherIcon.setImageResource(R.drawable.weather_cloud);
            weatherIndex = 3;
        } else if (index == 4) {
            weatherIcon.setImageResource(R.drawable.weather_rain);
            weatherIndex = 4;
        } else if (index == 5) {
            weatherIcon.setImageResource(R.drawable.weather_snow_rain);
            weatherIndex = 5;
        } else if (index == 6) {
            weatherIcon.setImageResource(R.drawable.weather_snow);
            weatherIndex = 6;
        } else {
            Log.d(TAG, "알 수 없는 인덱스 : " + index);
        }
    }

    public void setAddress(String data) {
        locationTv.setText(data);
    }

    public void setContents(String data) {
        contentEdt.setText(data);
    }

    public void setPicture(String picturePath, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        resultPhotoBitmap = BitmapFactory.decodeFile(picturePath, options);

        pictureInput.setImageBitmap(resultPhotoBitmap);
    }

    public void setMood(String mood) {
        try {
            moodIndex = Integer.parseInt(mood);
            moodSlider.setInitialIndex(moodIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setItem(Diary item) {
        this.item = item;
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

                            pictureInput.setImageResource(R.drawable.imagetoset);

                            // 사진이 삭제했기 때문에 사진 유무 상태를 변경
                            isPhotoCaptured = false;
                            isPhotoFileSaved = false;
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
        /*
        // 파일이 없으면 생성(카메라 앱에서 촬영한 사진을 저장할 file)
        if(file == null) {
            file = createFile();
        } */
        try {
            file = createFile();
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
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

                    resultPhotoBitmap = decodeSampleBitmapFromResource(file, pictureInput.getWidth(), pictureInput.getHeight());
                    pictureInput.setImageBitmap(resultPhotoBitmap);

                    // 사진이 넣었기 때문에 사진 유무 상태를 변경
                    isPhotoCaptured = true;
                    isPhotoFileSaved = true;

                    break;

                    /*
                    *MediaStore
                     -안드로이드 시스템에서 제공하는 미디어 데이터 데이터 베이스
                     -시스템은 파일시스템에 저장되어 있는 미디어 파일들을 이 데이터 베이스에 추가하여 여러 앱에서 사용할 수 있도록함
                     -시스템이 제공하는 provider를 사용해서 미디어 파일을 query 할 수 있음
                     */
                case AppConstants.REQ_PHOTO_SELECTION: // 앨범에서 선택하기 메뉴를 선택했을 경우
                    Log.d(TAG, "앨범에서 선택하기 메뉴의 onActivityResult() ");

                    Uri selectionImage = intent.getData(); // 가져올 데이터의 주소
                    String[] filePathColumn = {MediaStore.Images.Media.DATA}; // 가져올 컬럼 이름 목록

                    /*
                    *getContentResolver.query(Uri, projection, selection, selectionArgs, sortOrder)
                     -Uri : table_name이라는 이름의 provider 테이블에 매핑됨, content://scheme 방식의 원하는 데이터를 가져오기 위해 정해진 주소
                     -projection : 검색된 각 행에 포함되어야 하는 열의 배열, 가져올 컬럼 이름 목록으로 null이면 모든 컬럼임
                     -selection : 행을 선택하는 기준을 지정함, where 절에 해당하는 내용
                     -selectionArgs : selection에서 ?로 표시한 곳에 들어갈 데이터
                     -sortOrder : 리턴된 Cursor 내에 행이 나타나는 순서를 지정함, 정렬을 위한 구문
                     */
                    Cursor cursor = context.getContentResolver().query(selectionImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]); // cursor를 사용해 컬럼 인덱스 가져오기
                    String filePath = cursor.getString(columnIndex); // cursor를 사용해 가져온 컬럼 인텍스를 문자열로 변환하기
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