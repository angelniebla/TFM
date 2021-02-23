/*
Bluetooth communications task

Extends AsyncTask to support threaded interaction with a sever over a Bluetooth socket.

Copyright 2018  Gunnar Bowman, Emily Boyes, Trip Calihan, Simon D. Levy, Shepherd Sims

MIT License
 */

package com.example.angel.carnavigation.Communicators;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.angel.carnavigation.Activities.LocationActivity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothCommunicator extends AsyncTask<Void, Void, Void> {

    public boolean mConnected = false;
    //private ProgressDialog mProgressDialog;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket mBluetoothSocket = null;
    //private AppCompatActivity mCurrentActivity = null;
    private String mAddress = null;
    public LocationActivity mCurrentActivity;


    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothCommunicator(LocationActivity activity, String address) {
        mCurrentActivity = activity;
        mAddress =  address;

    }

    @Override
    protected void onPreExecute()     {
        super.onPreExecute();
        //mProgressDialog = ProgressDialog.show(mCurrentActivity, "Connecting...", "Please wait!!!");  //show a progress dialog
    }

    @Override
    protected Void doInBackground(Void... devices) { //while the progress dialog is shown, the connection is done in background

        try {
            if (mBluetoothSocket == null || !mConnected) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mAddress);//connects to the device's address and checks if it's available
                mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                mBluetoothSocket.connect();//start connection
                mConnected = true;
                mCurrentActivity.bluetoothConnection = true;
            }
        }
        catch (IOException e) {
            mConnected = false;//if the try failed, you can check the exception here
            mCurrentActivity.bluetoothConnection = false;
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void result) { //after the doInBackground, it checks if everything went fine

        super.onPostExecute(result);

        if (!mConnected){
            message("Connection Failed");
            //mCurrentActivity.bluetoothConnection = false;
        }
        else {
            message("Connected.");
            //mCurrentActivity.bluetoothConnection = true;
            mCurrentActivity.manageConnection();
        }
        //mProgressDialog.dismiss();
    }

    public void write(String s) {

        try {
            mBluetoothSocket.getOutputStream().write(s.getBytes());
        }
        catch (IOException e) {
        }
    }

    public String read() {

        byte[] buffer = new byte[2560];
        int bytes;

        try {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams

            tmpIn = mBluetoothSocket.getInputStream();
            tmpOut = mBluetoothSocket.getOutputStream();

            DataInputStream mmInStream = new DataInputStream(tmpIn);
            DataOutputStream mmOutStream = new DataOutputStream(tmpOut);

            bytes = mmInStream.read(buffer);
            String readMessage = new String(buffer, 0, bytes);
            return readMessage;

        } catch (IOException e) {
            e.printStackTrace();
        }

        String i = "-1";

        try {
            //i = mBluetoothSocket.getInputStream().read();
            InputStream stream = mBluetoothSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            i = reader.readLine();

        }
        catch (IOException e) {
        }

        return i;
    }

    public int available() {

        int n = 0;

        try {
            n = mBluetoothSocket.getInputStream().available();
        }
        catch (IOException e) {
            return n;
        }

        return n;
    }

    public void disconnect() {
        if (mBluetoothSocket!=null) //If the btSocket is busy
        {
            try  {
                mBluetoothSocket.close(); //close connection
            }
            catch (IOException e) {
                message("Error");
            }
        }

        message("Disconnected");

        mCurrentActivity.bluetoothConnection = false;

        //mCurrentActivity.finish();
    }


    private void message(String s) {
        Toast.makeText(mCurrentActivity.getApplicationContext(),s, Toast.LENGTH_LONG).show();
    }

}
