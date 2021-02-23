package com.example.angel.carnavigation.Listeners;

import android.content.Context;

public interface ListenerRequest {
    void requestCompleted();
    void requestError(int statusCode);
    Context getContext();
}
