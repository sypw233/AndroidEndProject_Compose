package com.qq.xqf1001.androidtraining.ui.video;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.github.leonardoxh.livedatacalladapter.Resource;
import com.qq.xqf1001.androidtraining.bean.NewsBean;
import com.qq.xqf1001.androidtraining.bean.VideoBean;
import com.qq.xqf1001.androidtraining.utils.NetUtils;

import java.util.List;

public class VideoViewModel extends ViewModel {

    public LiveData<List<VideoBean>> getVideoList(){
        return Transformations.map(NetUtils.get().getVideoList(), Resource::getResource);
    }
}
