package com.github.sangalaa.facerecognitionapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.service.exception.ForbiddenException;
import com.ibm.watson.developer_cloud.service.exception.NotFoundException;
import com.ibm.watson.developer_cloud.service.exception.RequestTooLargeException;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectFacesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.DetectedFaces;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Face;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.FaceAge;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.FaceGender;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageWithFaces;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private CameraView cameraView;
    private ImageView imageView;
    private FloatingActionButton takePictureButton;
    private FloatingActionButton backButton;

    private VisualRecognition visualRecognition;
    private DetectFacesOptions detectFacesOptions;
    private DetectedFaces detectedFaces;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    cameraView.captureImage();
                    break;
                case R.id.back_button:
                    restartCamera();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpCameraView();

        takePictureButton = findViewById(R.id.take_picture);
        if (takePictureButton != null) {
            takePictureButton.setOnClickListener(onClickListener);
        }

        backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(onClickListener);
        }

        imageView = (ImageView) findViewById(R.id.image);
        imageView.setVisibility(View.VISIBLE);

        /*
         * Create credentials.xml resource file to store
         * Visual Recognition apikey and API version
         * <resources>
         *  <string name="visual_recognition_apikey">{your_apikey}</string>
         *  <string name="visual_recognition_version">{vesion   }</string>
         * </resources>
         */
        IamOptions options = new IamOptions.Builder()
                .apiKey(getString(R.string.visual_recognition_apikey))
                .build();

        visualRecognition = new VisualRecognition(getString(R.string.visual_recognition_version), options);
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
                Bitmap result = cameraKitImage.getBitmap();

                displayImage(result);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                result.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
                byte[] resultByteArray = byteArrayOutputStream.toByteArray();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(resultByteArray);

                new DetectFacesTask().execute(byteArrayInputStream);

                if (detectedFaces != null) {
                    Log.d(TAG, "Detected faces");
                    Log.d(TAG, detectedFaces.toString());
                    List<ImageWithFaces> images = detectedFaces.getImages();
                    for (ImageWithFaces image : images) {
                        List<Face> faces = image.getFaces();
                        for (Face face : faces) {
                            // FaceLocation

                            // FaceAge
                            FaceAge faceAge = face.getAge();
                            long minAge = faceAge.getMin();
                            long maxAge = faceAge.getMax();
                            // FaceGender
                            FaceGender faceGender = face.getGender();
                            String gender = faceGender.getGender();

                            Log.d(TAG, gender + " " + minAge);
                        }
                    }
                } else {
                    Log.d(TAG, "0 faces detected");
                }
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {
            }
        });
    }

    private void displayImage(Bitmap bitmap) {
        cameraView.setVisibility(View.INVISIBLE);
        cameraView.stop();

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(bitmap);

        takePictureButton.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.VISIBLE);
    }

    private void restartCamera() {
        imageView.setVisibility(View.INVISIBLE);
        imageView.setImageBitmap(null);

        cameraView.setVisibility(View.VISIBLE);
        cameraView.start();

        takePictureButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.INVISIBLE);
    }

    private class DetectFacesTask extends AsyncTask<InputStream, Void, DetectedFaces> {

        @Override
        protected DetectedFaces doInBackground(InputStream... inputStreams) {
            detectFacesOptions = new DetectFacesOptions.Builder().imagesFile(inputStreams[0]).build();

            DetectedFaces detectFaces = null;

            try {
                detectFaces = visualRecognition.detectFaces(detectFacesOptions).execute();
            } catch (ForbiddenException e) {
                Log.d(TAG, "Invalid API key");
            } catch (RequestTooLargeException e) {
                Log.d(TAG, e.getStatusCode() + ": " + e.getMessage());
            } catch (NotFoundException e) {
                Log.d(TAG, e.getStatusCode() + ": " + e.getMessage());
            }

            return detectFaces;
        }

        @Override
        protected void onPostExecute(DetectedFaces faces) {
            detectedFaces = faces;
        }
    }

}
