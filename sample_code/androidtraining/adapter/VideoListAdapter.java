package com.qq.xqf1001.androidtraining.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.qq.xqf1001.androidtraining.R;
import com.qq.xqf1001.androidtraining.bean.PythonBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VideoListAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public VideoListAdapter(@Nullable List<String> data) {
        super(R.layout.item_video_list, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, String name) {
        baseViewHolder.setText(R.id.textView,name);
    }
}
