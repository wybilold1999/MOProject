package com.cyanbirds.momo.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.ContactInfoActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.PictureModel;
import com.cyanbirds.momo.manager.AppManager;
import com.facebook.drawee.view.SimpleDraweeView;

import java.text.DecimalFormat;
import java.util.List;


/**
 * @author Cloudsoar(wangyb)
 * @datetime 2015-12-26 17:44 GMT+8
 * @email 395044952@qq.com
 */
public class FoundGridAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private boolean mShowFooter = false;

    private List<PictureModel> pictureModels;
    private Context mContext;
    private DecimalFormat mFormat;

    public FoundGridAdapter(List<PictureModel> pics, Context context) {
        this.pictureModels = pics;
        mContext = context;
        mFormat = new DecimalFormat("#.00");
    }

    @Override
    public int getItemViewType(int position) {
        // 最后一个item设置为footerView
        if(!mShowFooter) {
            return TYPE_ITEM;
        }
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        int begin = mShowFooter?1:0;
        if(pictureModels == null) {
            return begin;
        }
        return pictureModels.size() + begin;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder) {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            PictureModel model = pictureModels.get(position);
            if(model == null){
                return;
            }
            viewHolder.portrait.setImageURI(Uri.parse(model.path));
            viewHolder.mUserName.setText(model.nickname);
            if (null == model.distance || model.distance == 0.00) {
                viewHolder.mFromCity.setVisibility(View.VISIBLE);
                viewHolder.mDistanceLayout.setVisibility(View.GONE);
                viewHolder.mFromCity.setText("来自" + model.city);
            } else {
                viewHolder.mDistanceLayout.setVisibility(View.VISIBLE);
                viewHolder.mFromCity.setVisibility(View.GONE);
                viewHolder.mDistance.setText(mFormat.format(model.distance) + " km");
            }
            if (AppManager.getClientUser().isShowVip && model.isVip) {
                viewHolder.mIsVip.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mIsVip.setVisibility(View.GONE);
            }
            viewHolder.mAge.setText(model.age);
            viewHolder.mConstellation.setText(model.constellation);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_found_grid, parent, false);
            ItemViewHolder vh = new ItemViewHolder(v);
            return vh;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.footer, null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new FooterViewHolder(view);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View view) {
            super(view);
        }

    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        SimpleDraweeView portrait;
        TextView mDistance;
        TextView mUserName;
        TextView mAge;
        TextView mConstellation;
        TextView mFromCity;
        ImageView mIsVip;
        RelativeLayout mDistanceLayout;
        RelativeLayout mItemLay;
        public ItemViewHolder(View itemView) {
            super(itemView);
            portrait = (SimpleDraweeView) itemView.findViewById(R.id.portrait);
            mDistance = (TextView) itemView.findViewById(R.id.distance);
            mUserName = (TextView) itemView.findViewById(R.id.tv_user_name);
            mAge = (TextView) itemView.findViewById(R.id.age);
            mConstellation = (TextView) itemView.findViewById(R.id.constellation);
            mFromCity = (TextView) itemView.findViewById(R.id.from_city);
            mIsVip = (ImageView) itemView.findViewById(R.id.is_vip);
            mDistanceLayout = (RelativeLayout) itemView.findViewById(R.id.distance_layout);
            mItemLay = (RelativeLayout) itemView.findViewById(R.id.item_lay);
            mItemLay.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position < 0) {
                return;
            }
            Intent intent = new Intent();
            PictureModel model = pictureModels.get(position);
            intent.setClass(mContext, ContactInfoActivity.class);
            intent.putExtra(ValueKey.USER_ID, String.valueOf(model.usersId));
            mContext.startActivity(intent);
        }
    }

    public void setIsShowFooter(boolean showFooter) {
        this.mShowFooter = showFooter;
    }

    public boolean isShowFooter() {
        return this.mShowFooter;
    }

    public void setPictureModels(List<PictureModel> pictureModels){
        this.pictureModels = pictureModels;
        this.notifyDataSetChanged();
    }
}
