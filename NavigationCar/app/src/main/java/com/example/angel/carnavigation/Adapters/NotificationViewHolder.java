package com.example.angel.carnavigation.Adapters;

import android.view.View;
import android.widget.TextView;

import com.example.angel.carnavigation.R;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.cardView)
    CardView cardView;

    @BindView(R.id.textView_titulo)
    TextView titulo;

    @BindView(R.id.textView_descripcion)
    TextView descripcion;

    @BindView(R.id.textView_fecha)
    TextView fecha;

    public NotificationViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
