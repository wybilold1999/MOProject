package com.cyanbirds.momo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cyanbirds.momo.R;
import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;

import java.util.List;

/**
 * Created by aurel on 24/09/14.
 */
public class BigramHeaderAdapter implements
		StickyHeadersAdapter<BigramHeaderAdapter.ViewHolder> {

	private List<String> items;

	public BigramHeaderAdapter(List<String> items) {
		this.items = items;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.top_header, parent, false);
		return new ViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(ViewHolder headerViewHolder, int position) {
		if (items.get(position).equals("")) {
			headerViewHolder.itemView.setVisibility(View.GONE);
		} else {
			headerViewHolder.title.setText(items.get(position)
					.subSequence(0, 1));
		}
	}

	@Override
	public long getHeaderId(int position) {
		if (items == null || items.size() <= position) {
			return 0;
		}
		if (items.get(position).equals("")) {
			return 0;
		}
		return items.get(position).subSequence(0, 1).hashCode();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {

		TextView title;

		public ViewHolder(View itemView) {
			super(itemView);
			title = (TextView) itemView.findViewById(R.id.title);
		}
	}
}
