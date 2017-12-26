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
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.ContactInfoActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.Contact;
import com.dl7.tag.TagLayout;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;


/**
 * @author Cloudsoar(wangyb)
 * @datetime 2015-12-26 18:34 GMT+8
 * @email 395044952@qq.com
 */
public class FoundNewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private List<Contact> mContacts;
    private Context mContext;
    private boolean mShowFooter = false;

    public FoundNewAdapter(List<Contact> clientUsers, Context mContext) {
        this.mContacts = clientUsers;
        this.mContext = mContext;
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_found_new, parent, false);
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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder){
            Contact clientUser = mContacts.get(position);
            if(clientUser == null){
                return;
            }
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.userName.setText(clientUser.user_name);
            itemViewHolder.age.setText(String.valueOf(clientUser.birthday));
            if ("男".equals(clientUser.sex)) {
                itemViewHolder.mSexImg.setImageResource(R.mipmap.list_male);
            } else {
                itemViewHolder.mSexImg.setImageResource(R.mipmap.list_female);
            }
            itemViewHolder.signature.setText(clientUser.signature);
            if (!TextUtils.isEmpty(clientUser.face_url)) {
                itemViewHolder.portrait.setImageURI(Uri.parse(clientUser.face_url));
            } else {
                itemViewHolder.portrait.setImageURI(Uri.parse("res:///" + R.mipmap.default_head));
            }
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

    public Contact getItem(int position){
        if (mContacts == null || mContacts.size() < 1) {
            return null;
        }
        return mContacts == null ? null : mContacts.get(position);
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View view) {
            super(view);
        }

    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        SimpleDraweeView portrait;
        TextView userName;
        TextView age;
        TextView signature;
        ImageView mSexImg;
        TagLayout tag_layout;
        public ItemViewHolder(View itemView) {
            super(itemView);
            portrait = (SimpleDraweeView) itemView.findViewById(R.id.portrait);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            age = (TextView) itemView.findViewById(R.id.age);
            signature = (TextView) itemView.findViewById(R.id.signature);
            mSexImg = (ImageView) itemView.findViewById(R.id.sex_img);
            tag_layout = (TagLayout) itemView.findViewById(R.id.tag_layout);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Intent intent = new Intent();
            if (mContacts.size() > position) {
                Contact contact = mContacts.get(position);
                if (contact != null) {
                    intent.setClass(mContext, ContactInfoActivity.class);
                    intent.putExtra(ValueKey.CONTACT, contact);
                    intent.putExtra(ValueKey.FROM_ACTIVITY, "FoundNewAdapter");
                    mContext.startActivity(intent);
                }
            }
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
