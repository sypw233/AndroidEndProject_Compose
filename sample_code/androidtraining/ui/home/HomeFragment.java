package com.qq.xqf1001.androidtraining.ui.home;

        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.LinearLayout;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.fragment.app.Fragment;
        import androidx.lifecycle.ViewModelProvider;
        import androidx.navigation.Navigation;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.qq.xqf1001.androidtraining.R;
        import com.qq.xqf1001.androidtraining.adapter.HomeAdapter;
        import com.qq.xqf1001.androidtraining.adapter.ImageAdapter;
        import com.qq.xqf1001.androidtraining.adapter.ImageTitleNumAdapter;
        import com.qq.xqf1001.androidtraining.bean.NewsBean;
        import com.scwang.smart.refresh.layout.api.RefreshLayout;
        import com.youth.banner.Banner;
        import com.youth.banner.adapter.BannerAdapter;
        import com.youth.banner.transformer.ZoomOutPageTransformer;

        import java.util.ArrayList;
        import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private HomeAdapter homeAdapter;
    private Banner<?, BannerAdapter<?,?>> banner;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        RecyclerView recyclerView=root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        homeAdapter=new HomeAdapter(null);
        recyclerView.setAdapter(homeAdapter);
        homeAdapter.setEmptyView(R.layout.empty_home);
        View headerView = inflater.inflate(R.layout.header_home, container, false);
        homeAdapter.setHeaderView(headerView);
        homeAdapter.setHeaderWithEmptyEnable(true);
        banner =  headerView.findViewById(R.id.banner);
        banner.addBannerLifecycleObserver(this)//添加生命周期观察者
                .setPageTransformer(new ZoomOutPageTransformer())
                .start();
        List<Integer> list=new ArrayList<>();
        for (int i = 0; i <3 ; i++) {
            list.add(R.drawable.pic_item_list_default);
        }
        banner.setAdapter(new ImageAdapter(list));
        RefreshLayout refreshLayout = root.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(layout -> {
            layout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            getAdList();
            getNewsList();
        });
        refreshLayout.setOnLoadMoreListener(layout -> {
            layout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
            Toast.makeText(getActivity(), "没有更多数据了", Toast.LENGTH_SHORT).show();
        });
        getAdList();//测试时临时注释
        getNewsList();//测试时临时注释
        LinearLayout linearLayout_python = headerView.findViewById(R.id.linearLayout_python);
        linearLayout_python.setOnClickListener(v ->Navigation.findNavController(v)
                .navigate(R.id.action_navigation_home_to_pythonFragment));
        homeAdapter.setOnItemClickListener((adapter, view, position) -> {
            Bundle bundle=new Bundle();
            bundle.putString("url",homeAdapter.getData().get(position).getNewsUrl());
            Navigation.findNavController(view).navigate(R.id.action_navigation_home_to_webFragment,
                    bundle);
        });
        return root;
    }

    private void getAdList() {
        homeViewModel.getAdList().observe(getViewLifecycleOwner(), newsBeans -> {
            banner.setAdapter(new ImageTitleNumAdapter(newsBeans));
            banner.setOnBannerListener((data, position) -> {
                Bundle bundle=new Bundle();
                bundle.putString("url",((NewsBean)data).getNewsUrl());
                Navigation.findNavController(banner)
                        .navigate(R.id.action_navigation_home_to_webFragment,bundle);
            });
        });
    }

    private void getNewsList() {
        homeViewModel.getNewsList().observe(getViewLifecycleOwner(), newsBeans -> {
            homeAdapter.setList(newsBeans);
        });
    }
}
