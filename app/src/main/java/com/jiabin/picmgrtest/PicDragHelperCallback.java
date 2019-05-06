package com.jiabin.picmgrtest;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.FloatProperty;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import java.util.ArrayList;
import java.util.Collections;

public class PicDragHelperCallback extends ItemTouchHelper.Callback {

    private PicMgrAdapter mAdapter;
    private View delArea;
    private DragListener mDragListener;
    private boolean mIsInside = false;
    private int delPos = -1;
    private RecyclerView.ViewHolder tempHolder;
    private float mScale = 1.2f;
    private float mAlpha = 1.0f;

    private float mInsideScale = 0.86f;
    private float mInsideAlpha = 0.3f;

    private float mMoveScale = mScale;

//    private ScaleAnimation mInScaleAnim;
//    private ScaleAnimation mOutScaleAnim;

    public PicDragHelperCallback(@NonNull PicMgrAdapter adapter, View delArea) {
        mAdapter = adapter;
        this.delArea = delArea;

//        mInScaleAnim = new ScaleAnimation(1.0f, mInsideScale / mMoveScale, 1.0f, mInsideScale / mMoveScale,
//                Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
//        mInScaleAnim.setFillAfter(true);
//        mInScaleAnim.setDuration(300);
//        mOutScaleAnim = new ScaleAnimation(mInsideScale / mMoveScale, 1.0f, mInsideScale / mMoveScale, 1.0f,
//                Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
//        mOutScaleAnim.setDuration(300);

    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags;
        if (viewHolder instanceof PicMgrAdapter.PicAddViewHolder) {
            return 0;
        }
        dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        if (target instanceof PicMgrAdapter.PicAddViewHolder) {
            return false;
        }
        ArrayList list = mAdapter.getList();
        if (list == null || list.size() < 2) {
            return false;
        }
        int from = viewHolder.getAdapterPosition();
        int endPosition = target.getAdapterPosition();
        Log.d("jiabin", "onMove from:" + from + " end:" + endPosition);
        delPos = endPosition;
        Collections.swap(list, from, endPosition);
        mAdapter.notifyItemMoved(from, endPosition);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }


    @Override
    public long getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
        if (mIsInside) {
            return 0;
        }
        return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
    }

    private void startActivatingAnim(View view, float from, float to, long duration) {
        Object tag = view.getTag();
        if (tag instanceof ObjectAnimator) {
            return;
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, scaleProperty, from, to);
        animator.setDuration(duration);
        animator.start();
        view.setTag(animator);
    }

    private boolean isActivatingAniming(View view) {
        Object tag = view.getTag();
        if (tag instanceof ObjectAnimator) {
            ObjectAnimator animator = (ObjectAnimator) tag;
            if (animator.isRunning()) {
                return true;
            }
        }
        return false;
    }

    private void clearActivatingAnim(View view) {
        Object tag = view.getTag();
        if (tag instanceof ObjectAnimator) {
            ObjectAnimator animator = (ObjectAnimator) tag;
            animator.cancel();
            view.setTag(null);
        }
    }

    private ScaleProperty scaleProperty = new ScaleProperty("scale");

    public static class ScaleProperty extends Property<View, Float> {
        public ScaleProperty(String name) {
            super(Float.class, name);
        }

        @Override
        public Float get(View object) {
            return object.getScaleX();
        }

        @Override
        public void set(View object, Float value) {
            object.setScaleX(value);
            object.setScaleY(value);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        // 不在闲置状态
        //Log.d("jiabin", "onSelectedChanged:" + actionState);
        //mActionState = actionState;
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//            viewHolder.itemView.setScaleX(mScale);
//            viewHolder.itemView.setScaleY(mScale);
            clearActivatingAnim(viewHolder.itemView);
            startActivatingAnim(viewHolder.itemView, 1.0f, mScale, 200);
            viewHolder.itemView.setAlpha(mAlpha);
            if (mDragListener != null) {
                mDragListener.onDragStart();
            }
            delPos = viewHolder.getAdapterPosition();
            tempHolder = viewHolder;
            Log.d("jiabin", "onSelectedChanged delPos:" + delPos);
        } else {
            if (mDragListener != null) {
                mDragListener.onDragFinish(mIsInside);
            }
            if (mIsInside && delPos >= 0 && tempHolder != null) {
//                clearActivatingAnim(tempHolder.itemView);
//                tempHolder.itemView.setScaleX(1.0f);
//                tempHolder.itemView.setScaleY(1.0f);
//                tempHolder.itemView.setAlpha(1.0f);

                tempHolder.itemView.setVisibility(View.INVISIBLE);
                mAdapter.removeItemFromDrag(delPos);
                mIsInside = false;
            }
            delPos = -1;
            tempHolder = null;
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (delArea == null || isActivatingAniming(viewHolder.itemView)) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }
        int delAreaWidth = delArea.getWidth();
        int delAreaHeight = delArea.getHeight();

        int[] delLocation = new int[2];
        delArea.getLocationInWindow(delLocation);
        int delAreaX = delLocation[0];
        int delAreaY = delLocation[1];

        int itemWidth = viewHolder.itemView.getWidth();
        int itemHeight = viewHolder.itemView.getHeight();
        int[] itemLocation = new int[2];
        viewHolder.itemView.getLocationInWindow(itemLocation);
        int itemX = itemLocation[0];
        int itemY = itemLocation[1];

        //Log.d("jiabin","itemWidth:" + itemWidth + " | itemHeight:" + itemHeight + " | itemX:" + itemX + " | itemY:" + itemY);

        int scaleItemWidth = (int) (itemWidth * mMoveScale);
        int scaleItemHeight = (int) (itemHeight * mMoveScale);

//        int scaleItemWidth = itemWidth;
//        int scaleItemHeight = itemHeight;

//        int itemRight = itemX + scaleItemWidth;
//        int itemBottom = itemY + scaleItemHeight;

        int centerX = itemX + scaleItemWidth / 2;
        int centerY = itemY + scaleItemHeight / 2;

        boolean isInside = false;
//        if (itemBottom > delAreaY && itemY < delAreaY + delAreaHeight && itemRight > delAreaX && itemX < delAreaX + delAreaWidth) {
//            isInside = true;
//        } else {
//            isInside = false;
//        }
        if (centerY > delAreaY && centerY < delAreaY + delAreaHeight && centerX > delAreaX && centerX < delAreaX + delAreaWidth) {
            isInside = true;
        } else {
            isInside = false;
        }
        if (isInside != mIsInside) {
            if (tempHolder != null) {
                if (isInside) {
                    mMoveScale = mInsideScale;
//                    viewHolder.itemView.setScaleX(mInsideScale);
//                    viewHolder.itemView.setScaleY(mInsideScale);
                    clearActivatingAnim(viewHolder.itemView);
                    startActivatingAnim(viewHolder.itemView, mScale, mInsideScale, 150);
                    viewHolder.itemView.setAlpha(mInsideAlpha);
                    //viewHolder.itemView.clearAnimation();
                    //viewHolder.itemView.startAnimation(mInScaleAnim);
                } else {
                    mMoveScale = mScale;
//                    viewHolder.itemView.setScaleX(mScale);
//                    viewHolder.itemView.setScaleY(mScale);
                    clearActivatingAnim(viewHolder.itemView);
                    startActivatingAnim(viewHolder.itemView, mInsideScale, mScale, 150);
                    viewHolder.itemView.setAlpha(mAlpha);
                    //viewHolder.itemView.clearAnimation();
                    //viewHolder.itemView.startAnimation(mOutScaleAnim);
                }
            }
            if (mDragListener != null) {
                mDragListener.onDragAreaChange(isInside, tempHolder == null);
            }
        }
        mIsInside = isInside;

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        clearActivatingAnim(viewHolder.itemView);
        startActivatingAnim(viewHolder.itemView, mScale, 1.0f, 150);
//        viewHolder.itemView.setScaleX(1.0f);
//        viewHolder.itemView.setScaleY(1.0f);
        viewHolder.itemView.setAlpha(1.0f);
        super.clearView(recyclerView, viewHolder);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // 支持长按拖拽功能
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // 不支持滑动功能
        return false;
    }

    public interface DragListener {
        void onDragStart();

        void onDragFinish(boolean isInside);

        void onDragAreaChange(boolean isInside, boolean isIdle);
    }

    public void setDragListener(DragListener listener) {
        mDragListener = listener;
    }

    /**
     * 设置选中后的放大效果
     *
     * @param scale
     */
    public void setScale(float scale) {
        mScale = scale;
        mMoveScale = mScale;

//        mInScaleAnim = new ScaleAnimation(1.0f, mInsideScale / mMoveScale, 1.0f, mInsideScale / mMoveScale,
//                Animation.RELATIVE_TO_SELF, 0.5f * mMoveScale, Animation.RELATIVE_TO_SELF, 1.5f * mMoveScale / mInsideScale);
//        mInScaleAnim.setFillAfter(true);
//        mInScaleAnim.setDuration(300);
//        mOutScaleAnim = new ScaleAnimation(mInsideScale / mMoveScale, 1.0f, mInsideScale / mMoveScale, 1.0f,
//                Animation.RELATIVE_TO_SELF, 0.5f * mMoveScale, Animation.RELATIVE_TO_SELF, 1.5f * mMoveScale / mInsideScale);
//        mOutScaleAnim.setDuration(300);
    }

    /**
     * 设置选中后的透明效果
     *
     * @param alpha
     */
    public void setAlpha(@FloatRange(from = 0.0f, to = 1.0f) float alpha) {
        mAlpha = alpha;
    }
}
