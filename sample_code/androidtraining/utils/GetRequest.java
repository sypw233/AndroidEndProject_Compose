package com.qq.xqf1001.androidtraining.utils;

import androidx.lifecycle.LiveData;

import com.github.leonardoxh.livedatacalladapter.Resource;
import com.qq.xqf1001.androidtraining.bean.NewsBean;
import com.qq.xqf1001.androidtraining.bean.PythonBean;
import com.qq.xqf1001.androidtraining.bean.VideoBean;

import java.util.List;

import retrofit2.http.GET;

public interface GetRequest {

    @GET("home_news_list_data.json")
    LiveData<Resource<List<NewsBean>>> getNewsList();

    @GET("home_ad_list_data.json")
    LiveData<Resource<List<NewsBean>>> getAdList();

    @GET("python_list_data.json")
    LiveData<Resource<List<PythonBean>>> getPythonList();

    @GET("video_list_data.json")
    LiveData<Resource<List<VideoBean>>> getVideoList();

}
