package com.qq.xqf1001.androidtraining.ui.chart.pie;

import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.qq.xqf1001.androidtraining.R;

import java.util.ArrayList;
import java.util.List;

public class PieFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.fragment_pie, container, false);
        PieChart chart = root.findViewById(R.id.pieChart);
        PieViewModel pieViewModel= new ViewModelProvider(this).get(PieViewModel.class);
        pieViewModel.getPieList().observe(getViewLifecycleOwner(),pieBeans -> {
            //添加数据
            List<PieEntry> entries = new ArrayList<>();
            for (int i = 0; i < pieBeans.size(); i++) {
                entries.add(new PieEntry(pieBeans.get(i).getPercentage(),
                        pieBeans.get(i).getSalaries()));
            }
            //自定义数据样式
            PieDataSet dataSet = new PieDataSet(entries, "工资占比");
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(12f);
            dataSet.setColors(Color.GRAY,Color.MAGENTA,Color.GREEN,Color.BLUE);
            PieData pieData = new PieData(dataSet);
            pieData.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return value+"%";
                }
            });
            chart.setData(pieData);
            chart.setExtraTopOffset(10f);
            chart.setCenterText("点击显示\n相关数据");
            chart.setCenterTextColor(Color.BLACK);
            chart.setCenterTextSize(24f);
            chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    chart.setCenterText(((PieEntry)e).getLabel()+"\n"+((PieEntry)e).getValue()+"%");
                }

                @Override
                public void onNothingSelected() {
                    chart.setCenterText("点击显示\n相关数据");
                }
            });
            chart.invalidate(); // 刷新
            //设置图例
            Legend l = chart.getLegend();
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            //设置描述
            Description description = chart.getDescription();
            description.setText("Android工程师薪资占比情况");
            description.setTextColor(Color.BLACK);
            description.setTextSize(16f);
            description.setTextAlign(Paint.Align.CENTER);
            description.setPosition(540f,100f);
            chart.animateXY(5000,5000);//设置动画
        });
        return root;
    }

}
