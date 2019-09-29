package com.akashpaul.justpackit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private  static final int MAIN_ACTIVITY =11;
    public static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int READ_PERMISSION_REQUEST_CODE = 2;
    private static final int WRITE_PERMISSION_REQUEST_CODE = 3;
    Button btn_measure;

    private TextView mTextView;
//    Button btn_locate;
//    Button btn_gallery;
//    Button btn_exit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndAskForNextPermission();
        File dir_image = new File(Environment.getExternalStorageDirectory() + File.separator + "Ruler");
        dir_image.mkdirs();

        btn_measure = (Button)findViewById(R.id.btn_measure);

        mTextView = (TextView) findViewById(R.id.btn_text);


//        btn_locate = (Button)findViewById(R.id.btn_locate);
//        btn_gallery = (Button)findViewById(R.id.btn_gallery);
//        btn_exit = (Button)findViewById(R.id.btn_exit);

        btn_measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MeasureActivity.class);
//                startActivity(intent);
                MainActivity.this.startActivityForResult(intent,MAIN_ACTIVITY);
            }

        });

//        btn_locate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), LocateActivity.class);
//                startActivity(intent);
//            }
//        });
//        btn_gallery.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                File[] imageFiles;
//                imageFiles = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ruler").listFiles();
//                if(imageFiles.length<1){
//                    Toast.makeText(getApplicationContext(),"저장된 사진이 없습니다.", Toast.LENGTH_SHORT).show();
//                }
//                else{
//                    Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
//                    startActivity(intent);
//                }
//            }
//        });
//        btn_exit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
    }

    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater mainInflater = getMenuInflater();
        mainInflater.inflate(R.menu.main_option, menu);
        return true;
    }

//    public boolean onOptionsItemSelected(MenuItem item){
//        switch (item.getItemId()){
//            case R.id.a:
//                Toast.makeText(getApplicationContext(), "노원종/김성태", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.b:
//                Toast.makeText(getApplicationContext(), "Ruler v1.0", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.c:
//                Toast.makeText(getApplicationContext(), "메뉴얼로 이동합니다.", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getApplicationContext(), ManualActivity.class);
//                startActivity(intent);
//                return true;
//        }
//        return false;
//    }

    private void requestCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    private void requestMemoryPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }
    private void checkAndAskForNextPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_REQUEST_CODE);
            } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestCameraPermission();
        requestMemoryPermission();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Results", "Inside Main onActivityResult, " + " Request Code:" +requestCode + " Result Code:" + resultCode+" Data:" +data);
        if (requestCode == MAIN_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                try{
                    Bundle b = data.getExtras();
                    Double stage_value = b.getDouble("stage_value");
                    Toast.makeText(getApplicationContext(),"Volume "+stage_value, Toast.LENGTH_LONG).show();
                    Log.d("MainActivity ",stage_value.toString());
                    mTextView.setText("\n Your parcel's volume is "+stage_value.toString()+" \n We recommend this box for shipping ");
//                    switch (image_type) {
//
//                    }
//            mPreview.setImageType(imageType);
//            Log.d("stage","Got bundle "+b);
                    if (b.getBoolean("EXIT")== true) {

                    }
                    else{


                    }
                } catch(Exception ex){

                    Log.d("stage","CA - No bundle ");

                }

            }
        }
    }

    /*public void onGalleryClicked(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);

        photoPickerIntent.setDataAndType(data, "image/*");

        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }*/
}
