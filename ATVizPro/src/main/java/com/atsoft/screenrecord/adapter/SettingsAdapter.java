package com.atsoft.screenrecord.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atsoft.screenrecord.R;
import com.atsoft.screenrecord.controllers.settings.SettingManager2;
import com.atsoft.screenrecord.model.SettingsItem;
import com.atsoft.screenrecord.utils.OnSingleClickListener;

import java.util.ArrayList;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private final ArrayList<SettingsItem> mSettingList;

    public interface SettingsListener {

        void onClickItem(String code);
    }

    private SettingsListener listener;

    public SettingsAdapter(Context context, ArrayList<SettingsItem> list) {
        this.mContext = context;
        this.mSettingList = list;
    }

    public void setListener(SettingsListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case 1:
                View view = inflater.inflate(R.layout.layout_item_settings, parent, false);
                return new ViewHolderNormal(view);
            case 2:
                View view2 = inflater.inflate(R.layout.layout_item_settings_up_to_pro, parent, false);
                return new ViewHolderUpToPro(view2);
            case 3:
                View view3 = inflater.inflate(R.layout.layout_item_settings_with_switch_button, parent, false);
                return new ViewHolderSwitch(view3);
        }
        return new ViewHolderNormal(inflater.inflate(R.layout.layout_item_settings, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        if (position == 0) return 2;
        if (position == 4) return 3;
        return 1;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        switch (holder.getItemViewType()) {
            case 1:
                ViewHolderNormal viewHolder1 = (ViewHolderNormal) holder;
                SettingsItem item = mSettingList.get(position);
                if (item.getContent().equals(mContext.getString(R.string.restore_purchase))) {
                    if (SettingManager2.isProApp(mContext)) {
                        viewHolder1.itemView.setAlpha(0.5f);
                    } else viewHolder1.itemView.setAlpha(1f);

                }
                viewHolder1.content_settings.setText(item.getContent());
                viewHolder1.ava_settings.setBackgroundResource(item.getResourceId());
                viewHolder1.itemView.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (listener != null)
                            listener.onClickItem(mSettingList.get(position).getContent());
                    }
                });
                break;

            case 2:
                ViewHolderUpToPro viewHolder2 = (ViewHolderUpToPro) holder;
                if (SettingManager2.isProApp(mContext)) {
                    viewHolder2.itemView.setAlpha(0.5f);
                } else viewHolder2.itemView.setAlpha(1f);
                viewHolder2.itemView.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (listener != null && !SettingManager2.isProApp(mContext))
                            listener.onClickItem(mSettingList.get(position).getContent());
                    }
                });
                break;
            case 3:
                ViewHolderSwitch viewHolder3 = (ViewHolderSwitch) holder;
                SettingsItem itemSwitch = mSettingList.get(position);

                viewHolder3.content_settings.setText(itemSwitch.getContent());
                viewHolder3.ava_settings.setBackgroundResource(itemSwitch.getResourceId());
                viewHolder3.btnSwitch.setChecked(SettingManager2.isEnableFAB(mContext));
                viewHolder3.btnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        SettingManager2.setEnableFAB(mContext, b);

                        System.out.println("thanhlv floating_button  onCheckedChanged " + b);
                        if (listener != null)
                            listener.onClickItem(mSettingList.get(position).getContent());
                    }
                });
                break;
        }
    }


    @Override
    public int getItemCount() {
        return mSettingList.size();
    }

    public static class ViewHolderNormal extends RecyclerView.ViewHolder {
        public TextView content_settings;
        public ImageView ava_settings;

        public ViewHolderNormal(View itemView) {
            super(itemView);
            content_settings = itemView.findViewById(R.id.tv_content_settings);
            ava_settings = itemView.findViewById(R.id.iv_ava_settings);

        }
    }

    public static class ViewHolderUpToPro extends RecyclerView.ViewHolder {

        public ViewHolderUpToPro(View itemView) {
            super(itemView);

        }
    }


    public static class ViewHolderSwitch extends RecyclerView.ViewHolder {
        public TextView content_settings;
        public ImageView ava_settings;
        public Switch btnSwitch;

        public ViewHolderSwitch(View itemView) {
            super(itemView);
            content_settings = itemView.findViewById(R.id.tv_content_settings);
            ava_settings = itemView.findViewById(R.id.iv_ava_settings);
            btnSwitch = itemView.findViewById(R.id.btn_switch);

        }
    }


}
