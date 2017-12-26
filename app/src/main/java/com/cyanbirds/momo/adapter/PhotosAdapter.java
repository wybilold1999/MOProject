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

import com.cyanbirds.momo.R;
import com.cyanbirds.momo.activity.ViewPagerPhotoViewActivity;
import com.cyanbirds.momo.config.ValueKey;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Cloudsoar(wangyb)
 * @datetime 2015-12-26 18:34 GMT+8
 * @email 395044952@qq.com
 */
public class PhotosAdapter extends ArrayAdapter<String> implements OnItemClickListener {

    private Context mContext;
    private List<String> imgUrls;

    public PhotosAdapter(Context context, List<String> objects,
                         GridView imgGrid) {
        super(context, 0, objects);
        mContext = context;
        imgUrls = objects;
        imgGrid.setOnItemClickListener(this);
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
                    R.layout.item_personal_photo, null);
            holder = new ViewHolder();
            holder.photo = (SimpleDraweeView) convertView
                    .findViewById(R.id.photo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(getItem(position)))
                .setResizeOptions(new ResizeOptions(100, 100))
                .setProgressiveRenderingEnabled(true)
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(holder.photo.getController())
                .setImageRequest(request)
                .build();
        holder.photo.setController(controller);
        return convertView;
    }

    private class ViewHolder {
        SimpleDraweeView photo;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Intent intent = new Intent(mContext, ViewPagerPhotoViewActivity.class);
        intent.putStringArrayListExtra(ValueKey.IMAGE_URL,
                (ArrayList<String>) imgUrls);
        intent.putExtra(ValueKey.POSITION, position);
        mContext.startActivity(intent);
    }
}
