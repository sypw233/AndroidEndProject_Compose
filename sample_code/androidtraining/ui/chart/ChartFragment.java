package com.qq.xqf1001.androidtraining.ui.chart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.qq.xqf1001.androidtraining.R;
import com.qq.xqf1001.androidtraining.base.BaseFragment2;
import com.qq.xqf1001.androidtraining.bean.BoomMenuItemBean;

public class ChartFragment extends BaseFragment2 {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ChartViewModel chartViewModel =new ViewModelProvider(this).get(ChartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chart, container, false);
        BoomMenuButton bmb = root.findViewById(R.id.bmb);
        chartViewModel.getBoomMenuItemList().observe(getViewLifecycleOwner(), boomMenuItemBeans -> {
            for (BoomMenuItemBean boomMenuItemBean : boomMenuItemBeans) {
                TextInsideCircleButton.Builder builder = new TextInsideCircleButton.Builder();
                builder.normalImageRes(boomMenuItemBean.getImageId())
                        .normalText(boomMenuItemBean.getTitle())
                        .listener(index -> {
                            switch (index){
                                case 0:
                                    Navigation.findNavController(root).navigate(
                                            R.id.action_navigation_chart_to_lineFragment);
                                    break;
                                case 1:
                                    Navigation.findNavController(root).navigate(
                                            R.id.action_navigation_chart_to_barFragment);
                                    break;
                                case 2:
                                    Navigation.findNavController(root).navigate(
                                            R.id.action_navigation_chart_to_pieFragment);
                                    break;
                            }
                        });
                bmb.addBuilder(builder);
            }
        });
        return root;
    }
}
