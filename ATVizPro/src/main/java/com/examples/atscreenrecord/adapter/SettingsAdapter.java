package com.examples.atscreenrecord.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.atscreenrecord.R;
import com.examples.atscreenrecord.controllers.settings.SettingManager2;
import com.examples.atscreenrecord.model.FAQItem;
import com.examples.atscreenrecord.model.SettingsItem;

import java.util.ArrayList;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {
    private final Context mContext;
    private ArrayList<SettingsItem> mFAQs;

    public interface SettingsListener {

        void onClickItem(String code);
    }

    private SettingsListener listener;

    public SettingsAdapter(Context context, ArrayList<SettingsItem> list) {
        this.mContext = context;
        this.mFAQs = list;
    }

    public void setListener(SettingsListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.layout_item_settings, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        SettingsItem item = mFAQs.get(position);
        if (item.getContent().equals(mContext.getString(R.string.upgrade_to_pro))) {
            if (SettingManager2.getRemoveAds(mContext)) {
                holder.itemView.setAlpha(0.5f);
            } else holder.itemView.setAlpha(1f);
        }
        holder.content_settings.setText(item.getContent());
        holder.ava_settings.setBackgroundResource(item.getResourceId());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getContent().equals(mContext.getString(R.string.upgrade_to_pro))) {
                    if (SettingManager2.getRemoveAds(mContext)) return;
                }
                if (listener != null) listener.onClickItem(mFAQs.get(position).getContent());
            }
        });
    }


    @Override
    public int getItemCount() {
        return mFAQs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView content_settings;
        public ImageView ava_settings;

        public ViewHolder(View itemView) {
            super(itemView);
            content_settings = itemView.findViewById(R.id.tv_content_settings);
            ava_settings = itemView.findViewById(R.id.iv_ava_settings);

        }
    }


}
