package com.examples.atvizpro.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.atvizpro.R;

import java.util.ArrayList;

public class BasicAdapter extends RecyclerView.Adapter<BasicAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> list;

    public interface BasicAdapterListener {
        void onClickBasicItem(String text);
    }

    private BasicAdapterListener listener;

    public BasicAdapter(Context context, ArrayList<String> list, BasicAdapterListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_string, parent, false);
        return new ViewHolder(view);
    }

    int posSelected = 0;
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String item = list.get(position);

        holder.name.setText(item);
        if (position != posSelected) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        } else
            holder.itemView.setBackgroundResource(R.drawable.shape_round_bg_video_item);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                listener.onClickBasicItem(list.get(position));
                posSelected = position;
                notifyDataSetChanged();
            }
        });


    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_text);

        }
    }
}
