package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private String currentUsername;
    private ImageView userAvatar;
    private TextView userName;
    private View favoriteLayout;
    private View historyLayout;
    private View editProfileLayout;
    private View buttonLogout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUsername = getCurrentUsername();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 初始化视图
        userAvatar = view.findViewById(R.id.userAvatar);
        userName = view.findViewById(R.id.userName);
        favoriteLayout = view.findViewById(R.id.favoriteLayout);
        historyLayout = view.findViewById(R.id.historyLayout);
        editProfileLayout = view.findViewById(R.id.editProfileLayout);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        // 加载用户信息
        loadUserProfile();

        // 设置点击事件
        setupClickListeners(view);

        return view;
    }

    private void loadUserProfile() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "未登录");
        userName.setText(username);

        // 这里可以加载用户头像，如果有的话
        // userAvatar.setImageBitmap(...);
    }

    private void setupClickListeners(View view) {
        // 收藏点击事件
        view.findViewById(R.id.layout_favorite).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FavoriteListActivity.class);
            startActivity(intent);
        });

        // 历史浏览点击事件
        view.findViewById(R.id.historyLayout).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BrowseHistoryActivity.class);
            startActivity(intent);
        });

        // 修改个人信息点击事件
        editProfileLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // 退出登录点击事件
        buttonLogout.setOnClickListener(v -> {
            // 清除用户登录信息
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // 跳转到登录页面
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private String getCurrentUsername() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", "");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}