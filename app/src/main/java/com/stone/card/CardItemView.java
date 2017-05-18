package com.stone.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyanbirds.momo.CSApplication;
import com.cyanbirds.momo.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

/**
 * 卡片View项
 *
 * @author xmuSistone
 */
@SuppressLint("NewApi")
public class CardItemView extends FrameLayout {
    private Spring springX, springY;
    public SimpleDraweeView imageView;
    public SimpleDraweeView imgOne;
    public SimpleDraweeView imgTwo;
    public SimpleDraweeView imgThree;
    public SimpleDraweeView imgFour;
    public View maskView;
    private TextView userNameTv;
    private TextView mAge;
    private TextView mConstellation;
    private TextView mDistance;
    private TextView mSignature;
    private TextView mCity;
//    private TextView imageNumTv;
    private CardSlidePanel parentView;
    private View topLayout, bottomLayout;

    private ResizeOptions mResizeOptions;

    public CardItemView(Context context) {
        this(context, null);
    }

    public CardItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.card_item, this);
        imageView = (SimpleDraweeView) findViewById(R.id.card_image_portrait);
        imgOne = (SimpleDraweeView) findViewById(R.id.card_image_one);
        imgTwo = (SimpleDraweeView) findViewById(R.id.card_image_two);
        imgThree = (SimpleDraweeView) findViewById(R.id.card_image_three);
        imgFour = (SimpleDraweeView) findViewById(R.id.card_image_four);
        maskView = findViewById(R.id.maskView);
        userNameTv = (TextView) findViewById(R.id.card_user_name);
        mAge = (TextView) findViewById(R.id.age);
        mConstellation = (TextView) findViewById(R.id.constellation);
        mDistance = (TextView) findViewById(R.id.distance);
        mSignature = (TextView) findViewById(R.id.signature);
        mCity = (TextView) findViewById(R.id.city);
//        imageNumTv = (TextView) findViewById(R.id.card_pic_num);
        topLayout = findViewById(R.id.card_top_layout);
        bottomLayout = findViewById(R.id.card_bottom_layout);
        initSpring();
        mResizeOptions = new ResizeOptions(80, 80);
    }

    private void initSpring() {
        SpringConfig springConfig = SpringConfig.fromBouncinessAndSpeed(15, 20);
        SpringSystem mSpringSystem = SpringSystem.create();
        springX = mSpringSystem.createSpring().setSpringConfig(springConfig);
        springY = mSpringSystem.createSpring().setSpringConfig(springConfig);

        springX.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                int xPos = (int) spring.getCurrentValue();
                setScreenX(xPos);
                parentView.onViewPosChanged(CardItemView.this);
            }
        });

        springY.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                int yPos = (int) spring.getCurrentValue();
                setScreenY(yPos);
                parentView.onViewPosChanged(CardItemView.this);
            }
        });
    }

    public void fillData(CardDataItem itemData) {
        imageView.setImageURI(Uri.parse(itemData.imagePath));
        ImageRequest requestOne = ImageRequestBuilder.newBuilderWithSource(Uri.parse(itemData.pictures.get(0)))
                .setResizeOptions(mResizeOptions)
                .setProgressiveRenderingEnabled(true)
                .build();
        PipelineDraweeController controllerOne = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(imgOne.getController())
                .setImageRequest(requestOne)
                .build();
        imgOne.setController(controllerOne);

        ImageRequest requestTwo = ImageRequestBuilder.newBuilderWithSource(Uri.parse(itemData.pictures.get(1)))
                .setResizeOptions(mResizeOptions)
                .setProgressiveRenderingEnabled(true)
                .build();
        PipelineDraweeController controllerTwo = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(imgTwo.getController())
                .setImageRequest(requestTwo)
                .build();
        imgTwo.setController(controllerTwo);

        ImageRequest requestThree = ImageRequestBuilder.newBuilderWithSource(Uri.parse(itemData.pictures.get(2)))
                .setResizeOptions(mResizeOptions)
                .setProgressiveRenderingEnabled(true)
                .build();
        PipelineDraweeController controllerThree = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(imgThree.getController())
                .setImageRequest(requestThree)
                .build();
        imgThree.setController(controllerThree);

        ImageRequest requestFour = ImageRequestBuilder.newBuilderWithSource(Uri.parse(itemData.pictures.get(3)))
                .setResizeOptions(mResizeOptions)
                .setProgressiveRenderingEnabled(true)
                .build();
        PipelineDraweeController controllerFour = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(imgFour.getController())
                .setImageRequest(requestFour)
                .build();
        imgFour.setController(controllerFour);
        /*imgOne.setImageURI(Uri.parse(itemData.pictures.get(0)));
        imgTwo.setImageURI(Uri.parse(itemData.pictures.get(1)));
        imgThree.setImageURI(Uri.parse(itemData.pictures.get(2)));
        imgFour.setImageURI(Uri.parse(itemData.pictures.get(3)));*/
        userNameTv.setText(itemData.userName);
        mAge.setText(String.valueOf(itemData.age));
        mConstellation.setText(itemData.constellation);
        if (itemData.distance == 0.00) {
            mCity.setVisibility(VISIBLE);
            mDistance.setVisibility(View.GONE);
            mCity.setText("来自" + itemData.city);
        } else {
            mDistance.setVisibility(View.VISIBLE);
            mCity.setVisibility(View.GONE);
            mDistance.setText(itemData.distance + "km");
        }
        mSignature.setText("个性签名：" + itemData.signature);
//        imageNumTv.setText(itemData.imageNum + "");
    }


    /**
     * 动画移动到某个位置
     */
    public void animTo(int xPos, int yPos) {
        setCurrentSpringPos(getLeft(), getTop());
        springX.setEndValue(xPos);
        springY.setEndValue(yPos);
    }

    /**
     * 设置当前spring位置
     */
    private void setCurrentSpringPos(int xPos, int yPos) {
        springX.setCurrentValue(xPos);
        springY.setCurrentValue(yPos);
    }

    public void setScreenX(int screenX) {
        this.offsetLeftAndRight(screenX - getLeft());
    }

    public void setScreenY(int screenY) {
        this.offsetTopAndBottom(screenY - getTop());
    }

    public void setParentView(CardSlidePanel parentView) {
        this.parentView = parentView;
    }

    public void onStartDragging() {
        springX.setAtRest();
        springY.setAtRest();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 兼容ViewPager，触点需要按在可滑动区域才行
            boolean shouldCapture = shouldCapture((int) ev.getX(), (int) ev.getY());
            if (shouldCapture) {
                parentView.getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断(x, y)是否在可滑动的矩形区域内
     * 这个函数也被CardSlidePanel调用
     *
     * @param x 按下时的x坐标
     * @param y 按下时的y坐标
     * @return 是否在可滑动的矩形区域
     */
    public boolean shouldCapture(int x, int y) {
        int captureLeft = topLayout.getLeft() + topLayout.getPaddingLeft();
        int captureTop = topLayout.getTop() + topLayout.getPaddingTop();
        int captureRight = bottomLayout.getRight() - bottomLayout.getPaddingRight();
        int captureBottom = bottomLayout.getBottom() - bottomLayout.getPaddingBottom();

        if (x > captureLeft && x < captureRight && y > captureTop && y < captureBottom) {
            return true;
        }
        return false;
    }
}
