package com.cyanbirds.momo.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.entity.BetweenLovers;
import com.cyanbirds.momo.entity.ClientUser;
import com.cyanbirds.momo.entity.Gift;
import com.cyanbirds.momo.manager.AppManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;


/**
 * @author Cloudsoar(wangyb)
 * @datetime 2015-12-26 18:34 GMT+8
 * @email 395044952@qq.com
 * 礼物适配器
 */
public class GiftMarketAdapter extends
		RecyclerView.Adapter<GiftMarketAdapter.ViewHolder> {

	private Context mContext;
	private List<Gift> mGifts;
	private OnItemClickListener mOnItemClickListener;

	public GiftMarketAdapter(Context context, List<Gift> gifts) {
		this.mGifts = gifts;
		mContext = context;
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_gift_market, parent, false);
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Gift gift =  mGifts.get(position);
		holder.mGiftName.setText(gift.name);
		holder.mImgUrl.setImageURI(Uri.parse(gift.dynamic_image_url));
		if (AppManager.getClientUser().isShowVip) {
			holder.mVipLay.setVisibility(View.VISIBLE);
			if (gift.vip_amount == 0) {
				holder.mVipAmount.setText("免费");
			} else {
				holder.mVipAmount.setText(gift.vip_amount + "金币");
			}
		} else {
			holder.mVipLay.setVisibility(View.GONE);
		}
		holder.mAmount.setText(String.format(mContext.getResources().getString(R.string.org_price), gift.amount));
	}

	@Override
	public int getItemCount() {
		return mGifts == null ? 0 : mGifts.size();
	}

	public Gift getItem(int position){
		return mGifts == null ? null : mGifts.get(position);
	}


	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

		TextView mGiftName;
		SimpleDraweeView mImgUrl;
		TextView mVipAmount;
		TextView mAmount;
		LinearLayout mVipLay;
		public ViewHolder(View itemView) {
			super(itemView);
			mGiftName = (TextView) itemView.findViewById(R.id.gift_name);
			mImgUrl = (SimpleDraweeView) itemView.findViewById(R.id.img_url);
			mVipAmount = (TextView) itemView.findViewById(R.id.vip_amount);
			mAmount = (TextView) itemView.findViewById(R.id.amount);
			mVipLay = (LinearLayout) itemView.findViewById(R.id.vip_lay);
			mImgUrl.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (mOnItemClickListener != null) {
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
}
