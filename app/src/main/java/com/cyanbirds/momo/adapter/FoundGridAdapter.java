package com.cyanbirds.momo.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.ContactInfoActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.Contact;
import com.cyanbirds.momo.entity.PictureModel;
import com.cyanbirds.momo.manager.AppManager;
import com.cyanbirds.momo.utils.PreferencesUtils;
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

    private List<Contact> mContacts;
    private Context mContext;
    private DecimalFormat mFormat;
    private String mCurCity;//当前城市

    public FoundGridAdapter(List<Contact> pics, Context context) {
        this.mContacts = pics;
        mContext = context;
        mFormat = new DecimalFormat("#.00");
        mCurCity = PreferencesUtils.getCity(mContext);
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
        if(mContacts == null) {
            return begin;
        }
        return mContacts.size() + begin;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder) {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            Contact model = mContacts.get(position);
            if(model == null){
                return;
            }
            viewHolder.portrait.setImageURI(Uri.parse(model.face_url));
            viewHolder.mUserName.setText(model.user_name);
            viewHolder.mAge.setText(model.birthday);
            viewHolder.mConstellation.setText(model.constellation);
            viewHolder.state_marry.setText("情感状态：" + model.state_marry);
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
        TextView mUserName;
        TextView mAge;
        TextView mConstellation;
        TextView state_marry;
        RelativeLayout mItemLay;
        public ItemViewHolder(View itemView) {
            super(itemView);
            portrait = (SimpleDraweeView) itemView.findViewById(R.id.portrait);
            mUserName = (TextView) itemView.findViewById(R.id.tv_user_name);
            state_marry = (TextView) itemView.findViewById(R.id.state_marry);
            mAge = (TextView) itemView.findViewById(R.id.age);
            mConstellation = (TextView) itemView.findViewById(R.id.constellation);
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
            Contact contact = mContacts.get(position);
            intent.putExtra(ValueKey.CONTACT, contact);
            intent.putExtra(ValueKey.FROM_ACTIVITY, this.getClass().getSimpleName());
            intent.setClass(mContext, ContactInfoActivity.class);
            mContext.startActivity(intent);
        }
    }

    public void setIsShowFooter(boolean showFooter) {
        this.mShowFooter = showFooter;
    }

    public boolean isShowFooter() {
        return this.mShowFooter;
    }

    public void setClientUsers(List<Contact> users){
        this.mContacts = users;
        this.notifyDataSetChanged();
    }
}
