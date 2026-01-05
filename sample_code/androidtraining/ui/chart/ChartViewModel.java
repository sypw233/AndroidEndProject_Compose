package com.qq.xqf1001.androidtraining.ui.chart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.qq.xqf1001.androidtraining.R;
import com.qq.xqf1001.androidtraining.bean.BoomMenuItemBean;

import java.util.ArrayList;
import java.util.List;

public class ChartViewModel extends ViewModel {

    private MutableLiveData<List<BoomMenuItemBean>> boomMenuItemList;

    public ChartViewModel() {
        String[] texts={"Java","PHP","Android","黑马程序员.Python",
                "黑马程序员.C/C++","黑马程序员.iOS","黑马程序员.前端与移动开发",
                "黑马程序员.UI设计","黑马程序员.网络营销"};
        int[] imageId={R.drawable.bat, R.drawable.bear,R.drawable.bee,
                R.drawable.butterfly,R.drawable.cat,R.drawable.dolphin,R.drawable.eagle,
                R.drawable.horse,R.drawable.elephant};
        boomMenuItemList = new MutableLiveData<>();
        List<BoomMenuItemBean> list= new ArrayList<>();
        for (int i = 0; i < texts.length; i++) {
            list.add(new BoomMenuItemBean(texts[i],imageId[i]));
        }
        boomMenuItemList.setValue(list);
    }

    public LiveData<List<BoomMenuItemBean>> getBoomMenuItemList() {
        return boomMenuItemList;
    }
}
