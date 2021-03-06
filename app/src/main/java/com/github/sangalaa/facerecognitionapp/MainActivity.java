package com.github.sangalaa.facerecognitionapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.FaceLocation;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageWithFaces;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private CameraView cameraView;

    private ImageView imageView;

    private FloatingActionButton takePictureButton;

    private FloatingActionButton backButton;

    private Bitmap bitmap;


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
        cameraView = findViewById(R.id.camera_view);
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

                ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                boolean isWifiConnected = networkInfo.isConnected();
                networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                boolean isMobileConnected = networkInfo.isConnected();

                if (isWifiConnected || isMobileConnected) {
                    new DetectFacesTask().execute(result);
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_network_connection, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {
            }
        });
    }

    /**
     * Replaces a CameraView with an ImageView, then display a bitmap in the ImageView.
     * @param bitmap is the bitmap which will be displayed in the ImageView.
     */
    private void displayImage(Bitmap bitmap) {
        cameraView.setVisibility(View.INVISIBLE);
        cameraView.stop();

        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(bitmap);

        takePictureButton.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.VISIBLE);
    }

    /**
     * Starts the camera and replaces an ImageView with a CameraView.
     */
    private void restartCamera() {
        imageView.setVisibility(View.INVISIBLE);
        imageView.setImageBitmap(null);

        cameraView.setVisibility(View.VISIBLE);
        cameraView.start();

        takePictureButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.INVISIBLE);
    }

    private class DetectFacesTask extends AsyncTask<Bitmap, Void, DetectedFaces> {

        /** A bitmap, which will be used to create a request */
        private Bitmap bitmap;

        @Override
        protected DetectedFaces doInBackground(Bitmap... bitmaps) {
            bitmap = bitmaps[0];
            ByteArrayInputStream byteArrayInputStream = createInputStreamFromBitmap(bitmaps[0]);

            // Creates a DetectFacesOption object containing
            // the parameter values for the detectFaces method.
            detectFacesOptions = new DetectFacesOptions.Builder().
                    imagesFile(byteArrayInputStream).build();

            DetectedFaces detectFaces = null;

            try {
                // Synchronous execution of the detectFaces() method
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
        protected void onPostExecute(DetectedFaces detectfaces) {
            detectedFaces = detectfaces;

            // If there is at least one face in the picture.
            if (detectedFaces != null) {
                Log.d(TAG, detectedFaces.toString());
                // The images List will contain only one image
                List<ImageWithFaces> images = detectedFaces.getImages();

                // Extracts relevant date from the
                List<FaceData> faceDataList = extractFaceDataFromImages(images);

                if (faceDataList.size() > 0) {
                    Bitmap drawBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(drawBitmap);

                    // Creates a Paint object, which will be used to draw rectangles.
                    Paint rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    rectPaint.setColor(getResources().getColor(R.color.colorAccent));

                    canvas.drawBitmap(bitmap, 0, 0, rectPaint);

                    for (FaceData faceData : faceDataList) {
                        rectPaint.setStyle(Paint.Style.STROKE);
                        rectPaint.setStrokeWidth(8);

                        float height = (float) faceData.getHeight();
                        float width = (float) faceData.getWidth();
                        float left = (float) faceData.getLeft();
                        float top = (float) faceData.getTop();
                        float right = left + width;
                        float bottom = top + height;
                        String gender = faceData.getGender();
                        long minAge = faceData.getMinAge();

                        // Draw a rectangle around a face.
                        canvas.drawRect(left, top, right, bottom, rectPaint);

                        // Draw a smaller rectangle for displaying person's gender and minimum age.
                        rectPaint.setStyle(Paint.Style.FILL);
                        canvas.drawRect(left-4, top-70, right+4, top, rectPaint);

                        // Creates a Paint object, which will be used to draw gender and minimum age.
                        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        textPaint.setColor(Color.BLACK);
                        textPaint.setTextSize(60F);
                        // Used trial and error method to get the right position for the text.
                        canvas.drawText(gender, left+4, top-11, textPaint);
                        canvas.drawText(String.valueOf(minAge), right-72, top-11, textPaint);
                        imageView.setImageBitmap(drawBitmap);
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.zero_faces_detected, Toast.LENGTH_LONG).show();
                }
            }
        }

        /**
         * Returns a ByteArrayInputStream created from a bitmap,
         * which will be used in the Visual Recognition request.
         * @param bitmap from which the ByteArrayInputStream will be created.
         * @return the ByteArrayInputStream created from the bitmap.
         */
        private ByteArrayInputStream createInputStreamFromBitmap(Bitmap bitmap) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
            byte[] resultByteArray = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(resultByteArray);

            return byteArrayInputStream;
        }

        private List<FaceData> extractFaceDataFromImages(List<ImageWithFaces> images) {
            List<FaceData> faceDataList = new ArrayList<>();
            for (ImageWithFaces image : images) {
                List<Face> faces = image.getFaces();
                for (Face face : faces) {
                    // FaceLocation
                    FaceLocation faceLocation = face.getFaceLocation();
                    double height = faceLocation.getHeight();
                    double width = faceLocation.getWidth();
                    double left = faceLocation.getLeft();
                    double top = faceLocation.getTop();

                    // FaceAge
                    FaceAge faceAge = face.getAge();
                    long minAge = faceAge.getMin();
                    long maxAge = faceAge.getMax();

                    // FaceGender
                    FaceGender faceGender = face.getGender();
                    String gender = faceGender.getGender();

                    faceDataList.add(new FaceData(height, width, left, top, gender, minAge, maxAge));
                    Log.d(TAG, faceDataList.get(faceDataList.size()-1).toString());
                }
            }
            return faceDataList;
        }
    }

}
