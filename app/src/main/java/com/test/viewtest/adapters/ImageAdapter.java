package com.test.viewtest.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.test.viewtest.R;

import java.util.List;

/**
 * Created by wuyr on 3/8/18 10:40 AM.
 */

public class ImageAdapter extends BaseAdapter<Integer, ImageAdapter.ViewHolder> {

    public ImageAdapter(Context context, List<Integer> data, int layoutId) {
        super(context, data, layoutId);
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(mLayoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(mContext).load(mData.get(position)).into(holder.imageView);
        holder.imageView.setOnClickListener(v -> Toast.makeText(mContext, holder.getAdapterPosition() + " clicked", Toast.LENGTH_SHORT).show());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
