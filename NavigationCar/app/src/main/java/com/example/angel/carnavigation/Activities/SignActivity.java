package com.example.angel.carnavigation.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.angel.carnavigation.GlobalVars.GlobalVars;
import com.example.angel.carnavigation.Listeners.ListenerRequest;
import com.example.angel.carnavigation.LocaleManager.LocaleHelper;
import com.example.angel.carnavigation.R;
import com.example.angel.carnavigation.SharedPreference.PreferenceManager;
import com.example.angel.carnavigation.Volley.ControllerVolley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SignActivity extends AppCompatActivity implements ListenerRequest{

    private static final String TAG = "LocationActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    private GlobalVars gVars;

    @BindView(R.id.sign_in_button)
    SignInButton btnSignIn;

    @BindView(R.id.logEditTextEmail)
    EditText regTextEmail;

    @BindView(R.id.logEditTextPassword)
    EditText regTextPass;

    @BindView(R.id.logProgressBarSign)
    ProgressBar progressBarSign;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sign_in);
//        ButterKnife.bind(this);
        gVars = GlobalVars.getInstance();
        new PreferenceManager().initPreference(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        gVars.setSignInClient(GoogleSignIn.getClient(this, gso));

        gVars.setmAuth(FirebaseAuth.getInstance());

        FirebaseUser currentUser = gVars.getmAuth().getCurrentUser();
        updateUI(currentUser);
    }


    @OnClick(R.id.sign_in_button)
    public void onPressSignIn(View view) {
        signInGoogle();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Resultado que nos devuelve GoogleSignInApi.getSignInIntent(...)
        if (requestCode == ERROR_DIALOG_REQUEST) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //  Exito al iniciar sesion con Google, autenticacion con Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                //  Fallo al iniciar sesion con Google
                Log.w(TAG, "Google sign in failed", e);
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        gVars.getmAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //  Exito al iniciar sesion con Firebase, guardamos las credenciales.
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = gVars.getmAuth().getCurrentUser();
                            updateUI(user);
                            saveCredentials(user.getUid());
                        } else {
                            //  Fallo al iniciar sesion con Firebase.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        } }
                });
    }

    private void firebaseAuth(String email, String password) {
        Log.d(TAG, "firebaseAuth:" + email);
        gVars.getmAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = gVars.getmAuth().getCurrentUser();
                            updateUI(user);
                            saveCredentials(user.getUid());
                            //gVars.setUser(new User(user.getEmail()));
                            //saveCredentials(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignActivity.this, " El correo o la contraseña de su cuenta es incorrecto.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signInGoogle() {
        Intent signInIntent = gVars.getSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, ERROR_DIALOG_REQUEST);
    }

    @OnClick(R.id.logBtnSignIn)
    public void signIn() {
        String email = regTextEmail.getText().toString();
        String password = regTextPass.getText().toString();
        if(validateLogIn(email,password)){
            progressBarSign.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            firebaseAuth(email, password);
        }

    }

    @OnClick(R.id.logBtnRegister)
    public void register() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private boolean validateLogIn(String email,String password){
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Introduce un email o contraseña validos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }



    private void updateUI(FirebaseUser user) {
        //progressBarSign.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if (user != null) {
            //findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            gVars.setShowAlert(true);
            Intent intent = new Intent(SignActivity.this, LocationActivity.class);
            startActivity(intent);
        } else {
            //findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            setContentView(R.layout.activity_sign_in);
            ButterKnife.bind(this);

        }
    }

    @Override
    public void onBackPressed() {
    }

    private void saveCredentials(String userId){
        PreferenceManager.getInstance().setUserId(userId);
        String tokenId = FirebaseInstanceId.getInstance().getToken();
        PreferenceManager.getInstance().setTokenId(tokenId);
        ControllerVolley.sendCredentials(this);
        ControllerVolley.sendConfiguration(this);
    }

    @Override
    public void requestCompleted() {

    }

    @Override
    public void requestError(int statusCode) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        new PreferenceManager().initPreference(base);
        super.attachBaseContext(LocaleHelper.onAttach(base, "es"));
    }
}