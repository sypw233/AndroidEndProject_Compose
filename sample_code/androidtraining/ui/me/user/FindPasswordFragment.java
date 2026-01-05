package com.qq.xqf1001.androidtraining.ui.me.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.qq.xqf1001.androidtraining.R;
import com.qq.xqf1001.androidtraining.base.BaseFragment2;

import java.util.Objects;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class FindPasswordFragment extends BaseFragment2 {

    private EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.fragment_find_password, container, false);
        editText=root.findViewById(R.id.editText);
        Button button = root.findViewById(R.id.button);
        button.setOnClickListener(this::resetPasswordByEmail);
        return root;
    }

    /**
     * 邮箱重置密码
     */
    private void resetPasswordByEmail(View view) {
        String email = editText.getText().toString();
        if (TextUtils.isEmpty(email)){
            editText.setError("邮箱不能为空");
            return;
        }
        BmobUser.resetPasswordByEmail(email, new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Snackbar.make(view, "重置密码请求成功，请到" + email + "邮箱进行密码重置操作",
                            Snackbar.LENGTH_LONG).show();
                    Navigation.findNavController(view).navigateUp();
                } else {
                    Log.e("BMOB", e.toString());
                    Snackbar.make(view, Objects.requireNonNull(e.getMessage()),
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
