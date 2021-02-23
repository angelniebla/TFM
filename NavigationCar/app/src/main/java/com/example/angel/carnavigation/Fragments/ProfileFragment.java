package com.example.angel.carnavigation.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.angel.carnavigation.Activities.LocationActivity;
import com.example.angel.carnavigation.GlobalVars.GlobalVars;
import com.example.angel.carnavigation.R;
import com.squareup.picasso.Picasso;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class ProfileFragment extends Fragment{

    @BindView(R.id.profileTextName)
    TextView profileName;

    @BindView(R.id.profileTextEmail)
    TextView profileEmail;

    @BindView(R.id.profileImage)
    ImageView profileImage;

    @BindView(R.id.switchAll)
    Switch swAll;

    @BindView(R.id.switchAccident)
    Switch swAccident;

    @BindView(R.id.switchStatus)
    Switch swStatus;

    @BindView(R.id.switchSpeed)
    Switch swSpeed;

    @BindView(R.id.switchHelp)
    Switch swHelp;

    @BindView(R.id.switchEvents)
    Switch swEvents;

    @BindView(R.id.switchRoadLine)
    Switch swRoadLine;

    private GlobalVars gVars = new GlobalVars().getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, rootView);
        gVars = GlobalVars.getInstance();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        profileName.setText(gVars.getmAuth().getCurrentUser().getDisplayName());
        profileEmail.setText(gVars.getmAuth().getCurrentUser().getEmail());
        if(gVars.getmAuth().getCurrentUser().getPhotoUrl() != null){
            Picasso.get().load(gVars.getmAuth().getCurrentUser().getPhotoUrl()).into(profileImage);
        }
    }

    @OnClick(R.id.profileButtonSingOut)
    public void onPressSignOut(View view) {
        ((LocationActivity)getActivity()).signOut();
    }

    @OnClick(R.id.profileButtonLanguage)
    public void onPressChangeLanguage(View view) {
        ((LocationActivity)getActivity()).changeLanguage();
    }

    @OnCheckedChanged({R.id.switchAll, R.id.switchAccident, R.id.switchStatus, R.id.switchSpeed, R.id.switchHelp, R.id.switchEvents, R.id.switchRoadLine})
    public void onRadioButtonCheckChanged(CompoundButton button, boolean checked) {
        switch (button.getId()) {
            case R.id.switchAll:
                swAccident.setChecked(checked);
                swStatus.setChecked(checked);
                swSpeed.setChecked(checked);
                swHelp.setChecked(checked);
                swEvents.setChecked(checked);
                swRoadLine.setChecked(checked);
                gVars.setAlertAll(checked);
                break;
            case R.id.switchAccident:
                gVars.setAlertAccident(checked);
                break;
            case R.id.switchStatus:
                gVars.setAlertStatus(checked);
                break;
            case R.id.switchSpeed:
                gVars.setAlertSpeed(checked);
                break;
            case R.id.switchHelp:
                gVars.setAlertHelp(checked);
                break;
            case R.id.switchEvents:
                gVars.setAlertEvent(checked);
                break;
            case R.id.switchRoadLine:
                gVars.setRoadLine(checked);
                break;
        }
    }

    public void restartChecked(){
        swAll.setChecked(gVars.getAlertAll());
        swAccident.setChecked(gVars.getAlertAccident());
        swStatus.setChecked(gVars.getAlertStatus());
        swSpeed.setChecked(gVars.getAlertSpeed());
        swHelp.setChecked(gVars.getAlertHelp());
        swEvents.setChecked(gVars.getAlertEvent());
        swRoadLine.setChecked(gVars.getRoadLine());
    }

    @Override
    public void onResume() {
        super.onResume();
        restartChecked();
    }



}
