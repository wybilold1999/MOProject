package com.cyanbirds.momo.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.entity.ReceiveGiftModel;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * @author Cloudsoar(wangyb)
 * @datetime 2016-01-14 18:47 GMT+8
 * @email 395044952@qq.com
 */
public class MyGiftsAdapter extends RecyclerView.Adapter<MyGiftsAdapter.ViewHolder> {
    private Context mContext;
    private List<ReceiveGiftModel> mReceiveGiftModels;

    private OnItemClickListener mOnItemClickListener;
    public MyGiftsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_my_gifts, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReceiveGiftModel receiveGiftModel = mReceiveGiftModels.get(position);
        if(receiveGiftModel == null){
            return;
        }
        holder.portrait.setImageURI(Uri.parse(receiveGiftModel.faceUrl));
        holder.giftUrl.setImageURI(Uri.parse(receiveGiftModel.giftUrl));
        holder.user_name.setText(receiveGiftModel.nickname);
        holder.giftName.setText(receiveGiftModel.giftName);
    }

    public ReceiveGiftModel getItem(int position){
        return mReceiveGiftModels == null ? null : mReceiveGiftModels.get(position);
    }

    @Override
    public int getItemCount() {
        return mReceiveGiftModels == null ? 0 : mReceiveGiftModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        SimpleDraweeView portrait;
        TextView user_name;
        SimpleDraweeView giftUrl;
        TextView giftName;
        public ViewHolder(View itemView) {
            super(itemView);
            portrait = (SimpleDraweeView) itemView.findViewById(R.id.portrait);
            user_name = (TextView) itemView.findViewById(R.id.user_name);
            giftUrl = (SimpleDraweeView) itemView.findViewById(R.id.gift_url);
            giftName = (TextView) itemView.findViewById(R.id.gift_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setReceiveGiftModel(List<ReceiveGiftModel> receiveGiftModels){
        this.mReceiveGiftModels = receiveGiftModels;
        this.notifyDataSetChanged();
    }
}
