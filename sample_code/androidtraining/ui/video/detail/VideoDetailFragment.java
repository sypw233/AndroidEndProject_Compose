package com.qq.xqf1001.androidtraining.ui.video.detail;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.qq.xqf1001.androidtraining.R;
import com.qq.xqf1001.androidtraining.base.BaseFragment2;
import com.qq.xqf1001.androidtraining.utils.NetUtils;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VideoDetailFragment extends BaseFragment2 {

    private OrientationUtils orientationUtils;
    private StandardGSYVideoPlayer detailPlayer;
    private String url = "http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4";
    private boolean isPlay;
    private boolean isPause;
    private GSYVideoOptionBuilder gsyVideoOption;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_video_detail, container, false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String image = arguments.getString("image");
            String name = arguments.getString("name");
            String intro = arguments.getString("intro");
            String[] list = arguments.getStringArray("list");


            detailPlayer = root.findViewById(R.id.detail_player);
            //增加封面
            ImageView imageView = new ImageView(getActivity());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(root).load(NetUtils.BASE_URL+image).into(imageView);

            //增加title
            detailPlayer.getTitleTextView().setVisibility(View.GONE);
            detailPlayer.getBackButton().setVisibility(View.GONE);

            //外部辅助的旋转，帮助全屏
            orientationUtils = new OrientationUtils(getActivity(), detailPlayer);
            //初始化不打开外部的旋转
            orientationUtils.setEnable(false);

            gsyVideoOption = new GSYVideoOptionBuilder();
            gsyVideoOption.setThumbImageView(imageView)
                    .setIsTouchWiget(true)
                    .setRotateViewAuto(false)
                    .setLockLand(false)
                    .setAutoFullWithSize(false)
                    .setShowFullAnimation(false)
                    .setNeedLockFull(true)
                    .setUrl(url)
                    .setCacheWithPlay(false)
                    .setVideoTitle(name)
                    .setVideoAllCallBack(new GSYSampleCallBack() {
                        @Override
                        public void onPrepared(String url, Object... objects) {
                            super.onPrepared(url, objects);
                            //开始播放了才能旋转和全屏
                            orientationUtils.setEnable(true);
                            isPlay = true;
                        }

                        @Override
                        public void onQuitFullscreen(String url, Object... objects) {
                            super.onQuitFullscreen(url, objects);
                            if (orientationUtils != null) {
                                orientationUtils.backToProtVideo();
                            }
                        }
                    }).setLockClickListener(new LockClickListener() {
                @Override
                public void onClick(View view, boolean lock) {
                    if (orientationUtils != null) {
                        //配合下方的onConfigurationChanged
                        orientationUtils.setEnable(!lock);
                    }
                }
            }).build(detailPlayer);

            detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //直接横屏
                    orientationUtils.resolveByClick();
                    //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                    detailPlayer.startWindowFullscreen(getActivity(),
                            true, true);
                }
            });

            TabLayout tabLayout=root.findViewById(R.id.tabLayout);
            ViewPager2 viewPager2=root.findViewById(R.id.viewPager2);
            List<Fragment> fragmentList=new ArrayList<>();
            fragmentList.add(new VideoIntroFragment(intro));
            fragmentList.add(new VideoListFragment(list,this));
            viewPager2.setAdapter(new FragmentStateAdapter(this) {
                @NonNull
                @Override
                public Fragment createFragment(int position) {
                    return fragmentList.get(position);
                }

                @Override
                public int getItemCount() {
                    return fragmentList.size();
                }
            });

            TabLayoutMediator tabLayoutMediator=new TabLayoutMediator(tabLayout, viewPager2,
                    (tab, position)-> {
                        switch (position){
                            case 0:
                                tab.setText("视频简介");
                                break;
                            case 1:
                                tab.setText("视频列表");
                                break;
                        }
            });
            tabLayoutMediator.attach();
        }

        return root;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if (orientationUtils != null) {
                orientationUtils.backToProtVideo();
            }
            if (GSYVideoManager.backFromWindowFull(getActivity())) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        detailPlayer.getCurrentPlayer().onVideoPause();
        super.onPause();
        isPause = true;
    }

    @Override
    public void onResume() {
        detailPlayer.getCurrentPlayer().onVideoResume(false);
        super.onResume();
        isPause = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isPlay) {
            detailPlayer.getCurrentPlayer().release();
        }
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            detailPlayer.onConfigurationChanged(getActivity(), newConfig, orientationUtils,
                    true, true);
        }
    }

    public void playNewUrl(String url) {
        gsyVideoOption.setUrl(url).build(detailPlayer);
        detailPlayer.startPlayLogic();
    }
}
