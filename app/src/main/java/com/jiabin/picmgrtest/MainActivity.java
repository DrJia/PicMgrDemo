package com.jiabin.picmgrtest;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecy;
    private PicMgrAdapter adapter;
    private ItemTouchHelper helper;
    private PicDragHelperCallback picDragHelperCallback;
    private LinearLayoutManager manager;

    private TextView delArea;

    int count = 9;

    private Handler mHandler = new Handler();

    Animation mShowAction;
    Animation mHideAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mHideAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);

        mShowAction.setDuration(300);
        mHideAction.setDuration(300);
        mShowAction.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                delArea.setVisibility(View.VISIBLE);
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

        mRecy = (RecyclerView) findViewById(R.id.recy);
        delArea = (TextView) findViewById(R.id.delete_area);

        adapter = new PicMgrAdapter(this,240);
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
                Toast.makeText(getApplicationContext(), "pos:" + pos + " id:" , Toast.LENGTH_SHORT).show();
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
        picDragHelperCallback.setScale(1.5f);
        helper = new ItemTouchHelper(picDragHelperCallback);
        helper.attachToRecyclerView(mRecy);

        picDragHelperCallback.setDragListener(new PicDragHelperCallback.DragListener() {
            @Override
            public void onDragStart() {
                delArea.startAnimation(mShowAction);
            }

            @Override
            public void onDragFinish() {
                delArea.startAnimation(mHideAction);
            }

            @Override
            public void onDragAreaChange(boolean isInside) {
                if(isInside){
                    delArea.setText("松手即可删除");
                    delArea.setBackgroundColor(0x9fff0000);
                    delArea.setTextColor(0x9fffffff);
                }else {
                    delArea.setText("拖动到此处删除");
                    delArea.setBackgroundColor(0x7fff0000);
                    delArea.setTextColor(0x7fffffff);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
