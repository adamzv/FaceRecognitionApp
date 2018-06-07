package com.github.sangalaa.facerecognitionapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraProperties;
import com.wonderkiln.camerakit.CameraView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private CameraView cameraView;
    private ImageView imageView;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    cameraView.captureImage();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpCameraView();
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.take_picture);
        if (button != null) {
            button.setOnClickListener(onClickListener);
        }

        imageView = (ImageView) findViewById(R.id.image);

        cameraView.setMethod(CameraKit.Constants.METHOD_STANDARD);
        cameraView.setZoom(0);

    }


    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    public void setUpCameraView() {
        cameraView = (CameraView) findViewById(R.id.camera_view);
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Log.d(TAG, "onImage");
                Toast.makeText(MainActivity.this, "onImage", Toast.LENGTH_LONG).show();
                Bitmap result = cameraKitImage.getBitmap();

                cameraView.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(result);
                cameraView.stop();
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

}
