package com.cyanbirds.momo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.entity.MemberBuy;

import java.util.List;


/**
 * @author Cloudsoar(wangyb)
 * @datetime 2015-12-26 18:34 GMT+8
 * @email 395044952@qq.com
 */
public class MemberBuyAdapter extends
		RecyclerView.Adapter<MemberBuyAdapter.ViewHolder> {

	private List<MemberBuy> mMemberBuyList;
	private OnItemClickListener mOnItemClickListener;
	private Context mContext;

	public MemberBuyAdapter(Context context, List<MemberBuy> memberBuys) {
		this.mContext = context;
		this.mMemberBuyList = memberBuys;
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_member_buy, parent, false);
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		MemberBuy memberBuy = mMemberBuyList.get(position);
		if (memberBuy == null) {
			return;
		}
		if (position == 0) {
			holder.mHot.setVisibility(View.VISIBLE);
		}
		holder.mDateLimit.setText(memberBuy.months);
		holder.mLimit.setText(String.format(mContext.getResources().getString(R.string.vip_limit), memberBuy.months));
		holder.mPreferential.setText(memberBuy.descreption);
		holder.mBuy.setText("￥" + memberBuy.price);
	}

	@Override
	public int getItemCount() {
		return mMemberBuyList == null ? 0 : mMemberBuyList.size();
	}


	class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView mDateLimit;
		TextView mLimit;
		TextView mPreferential;
		Button mBuy;
		ImageView mHot;
		public ViewHolder(View itemView) {
			super(itemView);
			mDateLimit = (TextView) itemView.findViewById(R.id.date);
			mLimit = (TextView) itemView.findViewById(R.id.limit);
			mPreferential = (TextView) itemView.findViewById(R.id.preferential);
			mBuy = (Button) itemView.findViewById(R.id.buy);
			mHot = (ImageView) itemView.findViewById(R.id.iv_hot);
			mBuy.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.buy:
					int position = getAdapterPosition();
					if (mOnItemClickListener != null) {
						mOnItemClickListener.onItemClick(v, position);
					}
					break;
			}
		}
	}

	public MemberBuy getItem(int position){
		return mMemberBuyList == null ? null : mMemberBuyList.get(position);
	}

	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.mOnItemClickListener = onItemClickListener;
	}
}
