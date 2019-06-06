package com.example.cookandroid.watchme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.InputStream;
import java.util.List;


public class FaceActivity extends AppCompatActivity {
    ImageView ImageView_face, imageLC, imageRC;
    Button button;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);

        ImageView_face = (ImageView)findViewById(R.id.ImageView_face);

        button = (Button)findViewById(R.id.Button_ML);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    // 선택한 이미지에서 비트맵 생성
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    final Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();
                    // 이미지 표시
                    ImageView_face.setImageBitmap(img);
                        mContext = this;

                        final RelativeLayout RelativeLayout_face = findViewById(R.id.RelativeLayout_face);
                        RelativeLayout_face.removeViewInLayout(imageLC);
                        RelativeLayout_face.removeViewInLayout(imageRC);

                        FirebaseVisionFaceDetectorOptions options =
                                new FirebaseVisionFaceDetectorOptions.Builder()
                                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                                        .build();

                        final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(img);

                        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                                .getVisionFaceDetector(options);

                        Task<List<FirebaseVisionFace>> result =
                                detector.detectInImage(image)
                                        .addOnSuccessListener(
                                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                                    @Override
                                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                                        // Task completed successfully
                                                        Log.d("FACES", faces.toString());

                                                        if(faces.toString().equals("[]")){
                                                            Toast.makeText(getApplicationContext(),"얼굴을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                                                        }

                                                        Point p = new Point();
                                                        Display display = getWindowManager().getDefaultDisplay();
                                                        display.getSize(p);

                                                        for (FirebaseVisionFace face : faces) {
                                                            FirebaseVisionFaceLandmark leftCheek = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK);
                                                            float lcx = leftCheek.getPosition().getX();
                                                            float lcy = leftCheek.getPosition().getY();

                                                            FirebaseVisionFaceLandmark rightCheek = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_CHEEK);
                                                            float rcx =rightCheek.getPosition().getX();
                                                            float rcy =rightCheek.getPosition().getY();

                                                            imageLC = new ImageView(mContext);
                                                            imageLC.setImageResource(R.drawable.left_whiskers);
                                                            imageLC.setX(p.x * lcx / img.getWidth() - 100);
                                                            imageLC.setY(p.y * lcy / img.getHeight() - 100);

                                                            imageLC.setLayoutParams(new RelativeLayout.LayoutParams(150,150));
                                                            RelativeLayout_face.addView(imageLC);

                                                            imageRC = new ImageView(mContext);
                                                            imageRC.setImageResource(R.drawable.right_whiskers);
                                                            imageRC.setX(p.x * rcx / img.getWidth() - 100);
                                                            imageRC.setY(p.y * rcy / img.getHeight() - 100);

                                                            imageRC.setLayoutParams(new RelativeLayout.LayoutParams(150,150));
                                                            RelativeLayout_face.addView(imageRC);
                                                        }
                                                    }
                                                })
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Task failed with an exception
                                                        // ...

                                                    }
                                                });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
