package com.qq.xqf1001.androidtraining.ui.video.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qq.xqf1001.androidtraining.R;
import com.qq.xqf1001.androidtraining.adapter.VideoAdapter;
import com.qq.xqf1001.androidtraining.adapter.VideoListAdapter;

import java.util.Arrays;
import java.util.List;

public class VideoListFragment extends Fragment {

    private final VideoDetailFragment videoDetailFragment;
    private List<String> list;
    private String url0="http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4";
    private String url1="http://9890.vod.myqcloud.com/" +
            "9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";

    public VideoListFragment(String[] list, VideoDetailFragment videoDetailFragment) {
        this.list=Arrays.asList(list);
        this.videoDetailFragment=videoDetailFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.fragment_video_list,container,false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        VideoListAdapter videoListAdapter=new VideoListAdapter(list);
        recyclerView.setAdapter(videoListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(root.getContext(),
                DividerItemDecoration.VERTICAL));
        videoListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (position%2==0){
                videoDetailFragment.playNewUrl(url0);
            }else{
                videoDetailFragment.playNewUrl(url1);
            }
        });
        return root;
    }
}
