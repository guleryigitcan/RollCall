//Okan Ari 200001696
//Yiğitcan Güler 200001709
package com.example.rollcall;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1beta1.WriteResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth myAuth ;
    String user_id;
    FirebaseUser user;
    private CollectionReference usersRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_camera, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        db= FirebaseFirestore.getInstance();


        user = myAuth.getInstance().getCurrentUser();
        String userId =user.getUid();
        usersRef =db.collection("users").document(userId).collection("course");
        Bundle extras = getIntent().getExtras();
        String Name=extras.getString("name");
        String Surname=extras.getString("surname");
        String Mail=extras.getString("mail");
        String Stdonumber=extras.getString("stdonumber");
        String Pass1=extras.getString("pass1");
        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", Name);
        userData.put("lastName", Surname);
        userData.put("mail", Mail);
        userData.put("studentNumber", Stdonumber);
        userData.put("password", Pass1);
        try {
            user_id=user.getUid();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if(IsDataValid(Mail,Pass1,Name,Surname,Stdonumber)) {
            db.collection("users").document(user_id)
                    .set(userData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "DocumentSnapshot successfully written!");

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error writing document", e);
                        }
                    });

        }

    }

    private boolean IsDataValid(String mail, String pass1, String name, String surname, String stdonumber) {
        if(TextUtils.isEmpty(mail)){

            return false;
        }
        else if(TextUtils.isEmpty(name)){

            return false;
        }
        else if( TextUtils.isEmpty(surname)){

            return false;
        }
        else if(TextUtils.isEmpty(stdonumber) ){

            return false;
        }
        return true;

    }


}