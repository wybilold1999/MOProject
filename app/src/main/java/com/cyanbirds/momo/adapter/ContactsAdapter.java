package com.cyanbirds.momo.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.ContactInfoActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.Contact;
import com.cyanbirds.momo.utils.PinYinUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.lb.recyclerview_fast_scroller.RecyclerViewFastScroller.BubbleTextGetter;

import java.util.List;

/**
 * @author zxj
 * @ClassName:ContactsAdapter
 * @Description:通讯录Adapter
 * @Date:2015年5月17日下午3:53:21
 */
public class ContactsAdapter extends
        RecyclerView.Adapter<ContactsAdapter.ViewHolder> implements
        BubbleTextGetter {

    private Context mContext;
    private List<Contact> mContacts;

    public ContactsAdapter(Context context, List<Contact> contacts) {
        this.mContacts = contacts;
        this.mContext = context;
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        return mContacts == null ? 0 : mContacts.size();
    }

    @Override
    public long getItemId(int position) {
        return mContacts.get(position).hashCode();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            Contact contact = mContacts.get(position);
            if (!TextUtils.isEmpty(contact.rename)) {
                holder.mNickName.setText(contact.rename);
            } else if (!TextUtils.isEmpty(contact.nickname)) {
                holder.mNickName.setText(contact.nickname);
            } else if (!TextUtils.isEmpty(contact.user_name)) {
                holder.mNickName.setText(contact.user_name);
            } else {
                holder.mNickName.setText(contact.userId);
            }

            holder.mPortrait.setImageURI(Uri.parse(contact.face_url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_contacts, parent, false));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            OnClickListener {

        SimpleDraweeView mPortrait;
        TextView mNickName;
        TextView un_read_number;
        LinearLayout contacts_lay;
        TextView contacts_count;

        public ViewHolder(View itemView) {
            super(itemView);
            mPortrait = (SimpleDraweeView) itemView.findViewById(R.id.portrait);
            mNickName = (TextView) itemView.findViewById(R.id.nickname);
            un_read_number = (TextView) itemView
                    .findViewById(R.id.un_read_number);
            contacts_lay = (LinearLayout) itemView
                    .findViewById(R.id.contacts_lay);
            contacts_count = (TextView) itemView
                    .findViewById(R.id.contacts_count);
            contacts_lay.setOnClickListener(this);
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
                    mContext.startActivity(intent);
                }
            }
        }
    }

    @Override
    public String getTextToShowInBubble(int position) {
        Contact c = mContacts.get(position);
        String l = "";
        if (!TextUtils.isEmpty(c.rename)
                && !TextUtils.isEmpty(c.conRemarkPYShort)) {
            l = c.conRemarkPYShort;
        } else if (!TextUtils.isEmpty(c.nickname)
                && !TextUtils.isEmpty(c.pyInitial)) {
            l = c.pyInitial;
        } else if (!TextUtils.isEmpty(c.user_name)) {
            l = PinYinUtil.getPinYin(c.user_name);
        } else {
            l = c.userId;
        }
        return Character.toString(l.charAt(0));
    }

}
