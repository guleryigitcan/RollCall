//Okan Ari 200001696
//Yiğitcan Güler 200001709
package com.example.rollcall.ui.Camera;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.rollcall.Course;
import com.example.rollcall.CourseAdapter;
import com.example.rollcall.CourseAttendance;
import com.example.rollcall.R;
import com.example.rollcall.ui.CourseAttendanceAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.net.MediaType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.http.Url;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class CameraFragment extends Fragment {

    private CameraViewModel dashboardViewModel;
    Button button;
    private static final int Image_Capture_Code = 1;
    private Context globalContext=null;
    private Bitmap photoSelect;
    private Uri selectedImage;
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();;
    StorageReference storageReference;
    Button saveButton,attend;
    String user_id;
    String user_mail;
    ListView list;
    View view;

    LinearLayoutManager manager;
    ArrayList<Course> courses = new ArrayList<Course>();
    HashMap<String, String> data;
    private CircleImageView imagCapture;
    FirebaseUser user;
    private CollectionReference usersRef;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String res ;
    private StorageReference mStorageReference;


    public void volleyPost(String mail){
        String postUrl = "http://10.0.2.2:5001/face_rec";
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        JSONObject postData = new JSONObject();
        try {
            postData.put("mail", mail);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Response", response.getString("result"));
                    if(response.getString("result").equals("true")){
                    Toast.makeText(globalContext,"Derse katılım sağladınız",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                try {
                    Log.d("Response", error.getMessage());
                    Toast.makeText(globalContext,"Bilinmeyen bir hata oluştu",Toast.LENGTH_SHORT).show();
                }catch (Exception e) {
                    e.printStackTrace();

                }

            }
        });


        requestQueue.add(jsonObjectRequest);

    }



    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(CameraViewModel.class);
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        globalContext=this.getActivity();
        user = FirebaseAuth.getInstance().getCurrentUser();
        user_id=mAuth.getCurrentUser().getUid();
        storageReference= FirebaseStorage.getInstance().getReference();
        imagCapture = (CircleImageView) root.findViewById(R.id.circleImage);

        user_mail= user.getEmail();

        attend =(Button) root.findViewById(R.id.attend);
        attend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volleyPost(user_mail);
            }
        });

        button = (Button) root.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectImage(getActivity());

            }
        });
        list =(ListView)root.findViewById(R.id.listView2);

        String userId=user.getUid();
        usersRef =db.collection("users").document(userId).collection("course");
        usersRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String data ="";
                        for(QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                            Course course = documentSnapshot.toObject(Course.class);
                            for(String tag: course.getAttendance()){
                                data+="\n-"+tag;
                            }
                            courses.add(new Course(course.getLectureName(),course.getLectureCode(),course.getLecturerName(), Collections.singletonList(data)));
                        }
                        CourseAttendanceAdapter courseAttendanceAdapter = new CourseAttendanceAdapter(inflater,courses);
                        list.setAdapter(courseAttendanceAdapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                volleyPost(user_mail);
            }
        });

        getImage();
        return root;
    }
    public void getImage(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        String path=user.getEmail();
        mStorageReference=FirebaseStorage.getInstance().getReference().child("images/"+path);

        try {
            final File localFile=File.createTempFile(path,"jpg");
            mStorageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(globalContext,"Picture retrieved",Toast.LENGTH_SHORT).show();
                    Bitmap bitmap= BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    imagCapture.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(globalContext,"Error Occurred",Toast.LENGTH_SHORT).show();
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    private void selectImage(Context context){
        final CharSequence[] options={"Take photo","Choose from gallery","Cancel"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose your profile picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private Uri getImageUri(Context applicationContext, Bitmap photo) {
               ByteArrayOutputStream bytes = new ByteArrayOutputStream();
               photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(globalContext.getContentResolver(), photo, "Title", null);
                return Uri.parse(path);
    }

    private void uploadImage()
    {
        if (selectedImage != null) {


            final ProgressDialog progressDialog
                    = new ProgressDialog(globalContext);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            StorageReference ref = storageReference.child("images/" ).child(user.getEmail());

            ref.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    progressDialog.dismiss();
                                    Toast.makeText(globalContext,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {


                            progressDialog.dismiss();
                            Toast.makeText(globalContext,"Failed"+e.getMessage(),Toast.LENGTH_SHORT).show();
                            Toast.makeText(globalContext, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int)progress + "%");
                                }
                            });
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                                imagCapture.setImageBitmap(bitmap);
                                Uri tempUri = getImageUri(globalContext.getApplicationContext(), bitmap);
                                selectedImage=tempUri;
                                 uploadImage();

                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null ) {
                         selectedImage =  data.getData();

                        try {
                            photoSelect=MediaStore.Images.Media.getBitmap(globalContext.getContentResolver(),selectedImage);
                            imagCapture.setImageBitmap(photoSelect);
                            uploadImage();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                    break;
            }
        }

    }



}