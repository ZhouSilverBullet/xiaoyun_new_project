package com.sdxxtop.robotproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sdxxtop.robotproject.R;

/**
 * Created by Administrator on 2018/9/16.
 */

public class GridAdapter extends BaseAdapter {

    private int[] imgBgList = {R.drawable.orange_bg, R.drawable.blue_bg, R.drawable.purple_bg, R.drawable.green_bg};
    private int[] imgSrcList = {R.drawable.response, R.drawable.follow, R.drawable.access, R.drawable.interview};
    private String[] titleList = {"问答", "带路", "来访", "面试"};

    @Override
    public int getCount() {
        return imgBgList.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_item, parent, false);
            holder.ivBackBg = convertView.findViewById(R.id.iv_back_bg);
            holder.ivSrc = convertView.findViewById(R.id.iv_src);
            holder.tvTitle = convertView.findViewById(R.id.tv_title);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.ivBackBg.setBackgroundResource(imgBgList[position]);
        holder.ivSrc.setImageResource(imgSrcList[position]);
        holder.tvTitle.setText(titleList[position]);
        holder.ivBackBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            }
        });

        convertView.clearAnimation();
        if (position == 2) {
            TranslateAnimation animation = new TranslateAnimation(-100f, 0f, 100f, 0f);
            animation.setDuration(1500);
            animation.setInterpolator(new OvershootInterpolator());
            convertView.startAnimation(animation);
        } else if (position == 0) {
            TranslateAnimation animation = new TranslateAnimation(-100f, 0f, -100f, 0f);
            animation.setDuration(1500);
            animation.setInterpolator(new OvershootInterpolator());
            convertView.startAnimation(animation);
        } else if (position == 1) {
            TranslateAnimation animation = new TranslateAnimation(100f, 0f, -100f, 0f);
            animation.setDuration(1500);
            animation.setInterpolator(new OvershootInterpolator());
            convertView.startAnimation(animation);
        } else {
            TranslateAnimation animation = new TranslateAnimation(100f, 0f, 100f, 0f);
            animation.setDuration(1500);
            animation.setInterpolator(new OvershootInterpolator());
            convertView.startAnimation(animation);
        }
        return convertView;
    }

    class Holder {
        ImageView ivBackBg;
        ImageView ivSrc;
        TextView tvTitle;
    }

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }
}
