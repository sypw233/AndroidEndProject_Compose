package com.qq.xqf1001.androidtraining.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.qq.xqf1001.androidtraining.R;
import com.qq.xqf1001.androidtraining.bean.PythonBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PythonAdapter extends BaseQuickAdapter<PythonBean, BaseViewHolder> {

    public PythonAdapter( @Nullable List<PythonBean> data) {
        super(R.layout.item_python, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, PythonBean pythonBean) {
        baseViewHolder.setText(R.id.textView,pythonBean.getAddress());
        baseViewHolder.setText(R.id.textView2,pythonBean.getContent());
        baseViewHolder.setText(R.id.textView3,pythonBean.getOpen_class());
    }
}
