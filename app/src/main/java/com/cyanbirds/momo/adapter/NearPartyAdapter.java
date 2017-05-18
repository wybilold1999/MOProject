package com.cyanbirds.momo.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.PartyDetailActivity;
import com.cyanbirds.momo.activity.ViewPagerPhotoViewActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.cyanbirds.momo.entity.LoveParty;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @ClassName:NearPartyAdapter
 * @Description:相亲活动
 * @author wangyb
 */
public class NearPartyAdapter extends ArrayAdapter<String> implements
		OnItemClickListener {

	private Context mContext;
	private LoveParty mLoveParty;
	public NearPartyAdapter(Context context, LoveParty loveParty, List<String> objects,
							GridView grid) {
		super(context, 0, objects);
		mContext = context;
		mLoveParty = loveParty;
		grid.setOnItemClickListener(this);
	}

	@Override
	public int getCount() {
		return super.getCount();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.item_near_party, null);
			holder = new ViewHolder();
			holder.mPartyImg = (SimpleDraweeView) convertView
					.findViewById(R.id.image_party);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String imgUrl = getItem(position);
		holder.mPartyImg.setImageURI(Uri.parse(imgUrl));

		return convertView;
	}

	private class ViewHolder {
		SimpleDraweeView mPartyImg;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		/*Intent intent = new Intent(mContext, ViewPagerPhotoViewActivity.class);
		intent.putStringArrayListExtra(ValueKey.IMAGE_URL,
				(ArrayList<String>) imgUrls);
		intent.putExtra(ValueKey.POSITION, position);
		mContext.startActivity(intent);*/
		Intent intent = new Intent(mContext, PartyDetailActivity.class);
		intent.putExtra(ValueKey.DATA, mLoveParty);
		mContext.startActivity(intent);
	}
}
