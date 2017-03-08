package com.huangxueqin.rookieweibo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/3/2.
 */

public class GalleryActivity extends BaseActivity {
    String[] mImageUrls;
    int mSelectedIndex;

    @BindView(R.id.image_list)
    RecyclerViewPager mImageList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent bundle = getIntent();
        mImageUrls = bundle.getStringArrayExtra(Cons.IntentKey.IMAGE_LIST);
        mSelectedIndex = bundle.getIntExtra(Cons.IntentKey.SELECT_INDEX, 0);
        setContentView(R.layout.activity_gallery);
        enterImmersiveMode();
        ButterKnife.bind(this);
        mImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mImageList.setAdapter(new ImageAdapter());
        mImageList.scrollToPosition(mSelectedIndex);
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(GalleryActivity.this);
            View itemView = inflater.inflate(R.layout.view_gallery_image, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Glide.with(GalleryActivity.this)
                    .load(mImageUrls[position])
                    .into(holder.image);
        }

        @Override
        public int getItemCount() {
            return mImageUrls.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            PhotoView image;
            public ViewHolder(View itemView) {
                super(itemView);
                image = (PhotoView) itemView.findViewById(R.id.image);
            }
        }
    }

    @Override
    protected void onToolbarClosePress() {
        exitImmersiveMode();
        super.onToolbarClosePress();
    }

    @Override
    public void onBackPressed() {
        exitImmersiveMode();
        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterImmersiveMode();
        }
    }

    private void enterImmersiveMode() {
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void exitImmersiveMode() {
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}


//    private void moveImage(float dx, float dy) {
//        mImageMatrix.postTranslate(dx, dy);
//        mImageView.setImageMatrix(mImageMatrix);
//    }
//
//    private void translateImage(float scale, float tx, float ty) {
//        mImageMatrix.setScale(scale, scale);
//        mImageMatrix.postTranslate(tx, ty);
//        mImageView.setImageMatrix(mImageMatrix);
//    }
//
//    private View.OnTouchListener mImageTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            D("event: " + event);
//            mScaleDetector.onTouchEvent(event);
//
//            final int action = event.getActionMasked();
//            switch (action) {
//                case MotionEvent.ACTION_DOWN: {
//                    final float X = event.getX();
//                    final float Y = event.getY();
//                    mLastTouchX = X;
//                    mLastTouchY = Y;
//                    mActivePointerId = event.getPointerId(0);
//                    break;
//                }
//                case MotionEvent.ACTION_MOVE:
//                    final int activeIndex = event.findPointerIndex(mActivePointerId);
//                    final float X = event.getX(activeIndex);
//                    final float Y = event.getY(activeIndex);
//                    if (mMode == MODE.NORMAL &&
//                            (Math.abs(X - mLastTouchX) >= mTouchSlop || Math.abs(Y - mLastTouchY) >= mTouchSlop) &&
//                            event.getPointerCount() < 2) {
//                        mMode = MODE.DRAG;
//                    }
//                    if (mMode == MODE.DRAG || mMode == MODE.SCALING) {
//                        if (mMode == MODE.DRAG) {
//                            moveImage(X - mLastTouchX, Y - mLastTouchY);
//                        }
//                        mLastTouchX = X;
//                        mLastTouchY = Y;
//                    }
//                    break;
//                case MotionEvent.ACTION_CANCEL:
//                case MotionEvent.ACTION_UP:
//                    if (mMode == MODE.DRAG) {
//                        v.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                mMode = MODE.NORMAL;
//                            }
//                        });
//                    }
//                    mActivePointerId = -1;
//                    break;
//                case MotionEvent.ACTION_POINTER_UP:
//                    final int pointerIndex = event.getActionIndex();
//                    final int pointerId = event.getPointerId(pointerIndex);
//                    if (pointerId == mActivePointerId) {
//                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
//                        mLastTouchX = event.getX(newPointerIndex);
//                        mLastTouchY = event.getY(newPointerIndex);
//                        mActivePointerId = event.getPointerId(newPointerIndex);
//                    }
//                    break;
//            }
//            return mMode != MODE.NORMAL;
//        }
//    };
//
//    private ScaleGestureDetector.SimpleOnScaleGestureListener mScaleListener =
//            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
//                @Override
//                public boolean onScale(ScaleGestureDetector detector) {
//                    mImageMatrix.getValues(tmpValues);
//                    float currentScale = tmpValues[Matrix.MSCALE_X];
//                    float targetScale = (mScaleFactor *= detector.getScaleFactor());
//                    D("currentScale = " + currentScale + ", targetScale = " + targetScale);
//                    if (Math.abs(targetScale-currentScale) > 0.01) {
//                        float currentTx = tmpValues[Matrix.MTRANS_X];
//                        float currentTy = tmpValues[Matrix.MTRANS_Y];
//                        float currentCx = currentTx + mImageWidth * currentScale/2;
//                        float currentCy = currentTy + mImageHeight * currentScale/2;
//                        float targetTx = currentCx - mImageWidth * targetScale / 2;
//                        float targetTy = currentCy - mImageHeight * targetScale / 2;
//                        translateImage(targetScale, targetTx, targetTy);
//                    }
//                    return true;
//                }
//
//                @Override
//                public boolean onScaleBegin(ScaleGestureDetector detector) {
//                    D("scale begin");
//                    mMode = MODE.SCALING;
//                    return super.onScaleBegin(detector);
//                }
//
//                @Override
//                public void onScaleEnd(ScaleGestureDetector detector) {
//                    D("scale end");
//                    mMode = MODE.DRAG;
//                    super.onScaleEnd(detector);
//                }
//            };