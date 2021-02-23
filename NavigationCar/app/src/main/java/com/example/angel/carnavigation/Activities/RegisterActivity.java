package com.example.angel.carnavigation.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.angel.carnavigation.GlobalVars.GlobalVars;
import com.example.angel.carnavigation.LocaleManager.LocaleHelper;
import com.example.angel.carnavigation.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    @BindView(R.id.regEditTextEmail)
    EditText regTextEmail;

    @BindView(R.id.regEditTextUsername)
    EditText regTextUser;

    @BindView(R.id.regEditTextPass)
    EditText regTextPass;

    @BindView(R.id.regEditTextRepass)
    EditText regTextRepass;

    private GlobalVars gVars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        gVars = GlobalVars.getInstance();
    }

    @OnClick(R.id.regBtnRegister)
    public void onPressRegister(View view) {
        register();
    }

    private void register() {
        String email = regTextEmail.getText().toString();
        String password = regTextPass.getText().toString();
        String name = regTextUser.getText().toString();
        String repassword = regTextRepass.getText().toString();

        if (validateRegister(email,password,name,repassword)){
            registerWithFirebase(email,password,name);
        }
    }

    private boolean validateRegister(String email,String password,String name,String repassword){

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Introduce un email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Introduce una contraseña", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length()<6) {
            Toast.makeText(this, "La contraseña tiene que tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Introduce un nombre", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(repassword)) {
            Toast.makeText(this, "Repite la contraseña", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(repassword)) {
            Toast.makeText(getApplicationContext(), "Las contraseñas tienen que coincidir", Toast.LENGTH_SHORT).show();
            Log.d("CONTRASENAS: "+password,repassword);
            return false;
        }
        return true;

    }

    private void registerWithFirebase(String email,String password,final String name){
        gVars.getmAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = gVars.getmAuth().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                            }
                                        }
                                    });
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "El correo utilizado ya existe",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(this, LocationActivity.class);
            startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
