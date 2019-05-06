package com.jiabin.picmgrtest;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecy;
    private PicMgrAdapter adapter;
    private ItemTouchHelper helper;
    private PicDragHelperCallback picDragHelperCallback;
    private LinearLayoutManager manager;

    private View delArea;
    private AppCompatImageView delIcon;

    int count = 9;

    private Handler mHandler = new Handler();

    private AnimationSet mShowAction;
    private AnimationSet mHideAction;

    private ScaleAnimation mDelShowScaleAnim;
    private ScaleAnimation mDelHideScaleAnim;

    private TextView btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDelShowScaleAnim = new ScaleAnimation(1.0f, 1.3f, 1.0f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mDelShowScaleAnim.setFillAfter(true);
        mDelShowScaleAnim.setDuration(150);
        mDelHideScaleAnim = new ScaleAnimation(1.3f, 1.0f, 1.3f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mDelHideScaleAnim.setDuration(150);

        ScaleAnimation showScaleAnim = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ScaleAnimation hideScaleAnim = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation showAlphaAnim = new AlphaAnimation(0.0f, 1.0f);
        AlphaAnimation hideAlphaAnim = new AlphaAnimation(1.0f, 0.0f);

        mShowAction = new AnimationSet(true);
        mShowAction.addAnimation(showScaleAnim);
        mShowAction.addAnimation(showAlphaAnim);

        mHideAction = new AnimationSet(true);
        mHideAction.addAnimation(hideScaleAnim);
        mHideAction.addAnimation(hideAlphaAnim);

        mShowAction.setDuration(150);
        mHideAction.setDuration(150);
        mShowAction.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //delArea.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mHideAction.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                delArea.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        btn = (Button) findViewById(R.id.btn);

        mRecy = (RecyclerView) findViewById(R.id.recy);
        delArea = findViewById(R.id.delete_area);
        delIcon = (AppCompatImageView) findViewById(R.id.delete_icon);
        delIcon.setImageResource(R.drawable.ic_edit_delete);

        adapter = new PicMgrAdapter(this, 240);
        adapter.setProportion(1.0f);

        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);

        mRecy.setLayoutManager(manager);

        mRecy.addItemDecoration(new PicItemDecoration(30));

        ArrayList<Pic> list = new ArrayList<>();
        Pic pic;
        for (int i = 0; i < 9; i++) {
            pic = new Pic();
            pic.id = i;
            pic.path = "";
            list.add(pic);
        }

        mRecy.setAdapter(adapter);
        adapter.setList(list);

        adapter.setPicClickListener(new PicMgrAdapter.PicClickListener() {
            @Override
            public void onPicClick(View view, int pos) {
                Toast.makeText(getApplicationContext(), "pos:" + pos + " id:" + adapter.getList().get(pos).id, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAddClick(View view) {
                Pic pic = new Pic();
                pic.id = count;
                adapter.addItem(pic);
                count++;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRecy.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                }, 300);
            }
        });

        picDragHelperCallback = new PicDragHelperCallback(adapter, delArea);
        picDragHelperCallback.setScale(1.3f);//1.3f
        picDragHelperCallback.setAlpha(0.9f);
        helper = new ItemTouchHelper(picDragHelperCallback);
        helper.attachToRecyclerView(mRecy);

        picDragHelperCallback.setDragListener(new PicDragHelperCallback.DragListener() {
            @Override
            public void onDragStart() {
                delArea.clearAnimation();
                delArea.setVisibility(View.VISIBLE);
                delArea.startAnimation(mShowAction);
            }

            @Override
            public void onDragFinish(boolean isInside) {
                delArea.startAnimation(mHideAction);
                delIcon.setImageResource(R.drawable.ic_edit_delete);
                delArea.setBackgroundColor(0x0dffffff);
            }

            @Override
            public void onDragAreaChange(boolean isInside, boolean isIdle) {
                //Log.d("jiabin", "isInside:" + isInside + " | isIdle:" + isIdle);
                if (isIdle) {
                    return;
                }
                if (isInside) {
                    delIcon.setImageResource(R.drawable.ic_edit_deleted);
                    delArea.setBackgroundColor(0x19ffffff);
                    delArea.startAnimation(mDelShowScaleAnim);
                    ShakeUtil.vibrator(MainActivity.this, 100);
                } else {
                    delIcon.setImageResource(R.drawable.ic_edit_delete);
                    delArea.setBackgroundColor(0x0dffffff);
                    delArea.startAnimation(mDelHideScaleAnim);
                }
            }
        });

        adapter.setEmptyAnimatorListener(new PicMgrAdapter.EmptyAnimatorListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation, PicMgrAdapter.PicAddViewHolder holder) {
                float value = (float) animation.getAnimatedValue();
                int color = ColorUtils.setAlphaComponent(0xffffffff, (int) (255 * value));
                holder.itemView.setBackgroundColor(color);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.startEmptyAnimator(mRecy, 1000L, 0.1f, 0.2f, 0.1f, 0.2f, 0.1f);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
