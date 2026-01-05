package com.qq.xqf1001.androidtraining.ui.home.python;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qq.xqf1001.androidtraining.R;
import com.qq.xqf1001.androidtraining.adapter.PythonAdapter;
import com.qq.xqf1001.androidtraining.base.BaseFragment2;

public class PythonFragment extends BaseFragment2 {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_python, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(root.getContext(),
                DividerItemDecoration.VERTICAL));
        PythonAdapter pythonAdapter=new PythonAdapter(null);
        recyclerView.setAdapter(pythonAdapter);
        PythonViewModel mViewModel = new ViewModelProvider(this).get(PythonViewModel.class);
        mViewModel.getPythonList().observe(getViewLifecycleOwner(), pythonAdapter::setList);
        return root;
    }

}
