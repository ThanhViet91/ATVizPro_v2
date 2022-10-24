package com.examples.atscreenrecord_test.adapter;

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

import com.examples.atscreenrecord_test.R;
import com.examples.atscreenrecord_test.model.FAQItem;
import com.examples.atscreenrecord_test.utils.OnSingleClickListener;

import java.util.ArrayList;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.ViewHolder>{
    private final Context mContext;
    private ArrayList<FAQItem> mFAQs;

    public FAQAdapter(Context context, ArrayList<FAQItem> list) {
        this.mContext = context;
        this.mFAQs = list;
    }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.layout_item_faq, parent, false);
            return new ViewHolder(view);
        }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FAQItem item = mFAQs.get(position);
        holder.tv_question.setText(item.getQuestion());
        if (item.getShown()) {
            holder.rl_head_faq.setBackgroundResource(R.drawable.shape_round_white_30);
            holder.itemView.setBackgroundResource(R.drawable.shape_round_white_30);
            holder.ic_down.setVisibility(View.GONE);
            holder.ic_up.setVisibility(View.VISIBLE);
            holder.tv_answer.setVisibility(View.VISIBLE);
            holder.tv_answer.setText(item.getAnswer());
        } else {
            holder.rl_head_faq.setBackgroundResource(R.drawable.shape_round_transparent);
            holder.itemView.setBackgroundResource(R.drawable.shape_round_white_60);
            holder.ic_down.setVisibility(View.VISIBLE);
            holder.ic_up.setVisibility(View.GONE);
            holder.tv_answer.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return mFAQs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_question, tv_answer;
        public ImageView ic_up, ic_down;
        public RelativeLayout rl_head_faq;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_question = itemView.findViewById(R.id.tv_question);
            tv_answer = itemView.findViewById(R.id.tv_answer);
            ic_up = itemView.findViewById(R.id.ic_up);
            ic_down = itemView.findViewById(R.id.ic_down);
            rl_head_faq = itemView.findViewById(R.id.rl_head_faq);

            rl_head_faq.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if (mFAQs.get(getLayoutPosition()).getShown()) {
                        hideAnswer(getLayoutPosition());
                    } else
                    showAnswer(getLayoutPosition());
                }
            });

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void hideAnswer(int i) {
        mFAQs.get(i).setShown(false);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showAnswer(int i) {
        for (FAQItem item: mFAQs) {
            item.setShown(false);
        }
        mFAQs.get(i).setShown(true);
        notifyDataSetChanged();
    }

}
