package com.qq.xqf1001.androidtraining.ui.home.python;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.github.leonardoxh.livedatacalladapter.Resource;
import com.qq.xqf1001.androidtraining.bean.PythonBean;
import com.qq.xqf1001.androidtraining.utils.NetUtils;

import java.util.List;

public class PythonViewModel extends ViewModel {
    public LiveData<List<PythonBean>> getPythonList(){
        return Transformations.map(NetUtils.get().getPythonList(), Resource::getResource);
    }
}
