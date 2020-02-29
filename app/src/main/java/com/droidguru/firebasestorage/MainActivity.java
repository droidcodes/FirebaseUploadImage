package com.droidguru.firebasestorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    TextView tvProgress;
    Button buttonUpload,buttonSelect;
    String picturePath = "";

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.image1);
        buttonUpload = (Button)findViewById(R.id.button_upload);
        buttonSelect = (Button)findViewById(R.id.button_select);
        tvProgress = (TextView)findViewById(R.id.text_progress);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             uploadFromFile();
            }
        });

        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //select file from gallery and upload it to firebase
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,100);
            }
        });

    }


    //upload from local file
    public void uploadFromFile()
    {
        Uri fileUri = Uri.fromFile(new File(picturePath));
        StorageReference imageRef3 = storageReference.child("images/"+fileUri.getLastPathSegment());

        UploadTask uploadTask2 = imageRef3.putFile(fileUri);

        uploadTask2.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error uploading image.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Image uploaded.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //upload with byte[]
    public void uploadFromDataInBytes()
    {
        StorageReference imageRef1 = storageReference.child("images/nature1.jpg");

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte [] data = byteArrayOutputStream.toByteArray();

        //upload image to firebase
        UploadTask uploadTask = imageRef1.putBytes(data);

        //add listeners
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error uploading image.", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Image uploaded successfully.", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double prgCount = (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                tvProgress.setText(prgCount+" %"+" uploaded.");
            }
        });
    }


// upload from input stream
    public void uploadFromStream()
    {
        try
        {
            StorageReference imageRef2 = storageReference.child("images/coffee.jpg");
            InputStream inputStream = new FileInputStream(new File(picturePath));

            UploadTask uploadTask1 = imageRef2.putStream(inputStream);

            uploadTask1.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Error uploading image.", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this, "Image uploaded.", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e)
        {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100)
        {
            if(resultCode==RESULT_OK && data!=null)
            {
                Uri selectedImage = data.getData();
                String [] filePathCols = {MediaStore.Images.Media.DATA};
                if(selectedImage!=null)
                {
                    Cursor cursor = getContentResolver().query(selectedImage,filePathCols,null,null,null);
                    if(cursor!=null)
                    {
                        cursor.moveToFirst();
                        int colIndex = cursor.getColumnIndex(filePathCols[0]);
                        picturePath = cursor.getString(colIndex);
                        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                        cursor.close();
                    }
                }
            }
        }
    }
}
