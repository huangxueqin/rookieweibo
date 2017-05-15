package com.huangxueqin.rookieweibo.ui.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huangxueqin.rookieweibo.BaseFragment;
import com.huangxueqin.rookieweibo.LceFragment;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.common.utils.L;
import com.sina.weibo.sdk.openapi.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/4/4.
 */

public class ProfileFragment extends LceFragment {

    @BindView(R.id.avatar)
    ImageView mAvatar;
    @BindView(R.id.user_name)
    TextView mUsername;
    @BindView(R.id.user_intro)
    TextView mUserIntro;
    @BindView(R.id.weibo_num)
    TextView mWeiboCount;
    @BindView(R.id.watch_num)
    TextView mWatchCount;
    @BindView(R.id.fans_num)
    TextView mFansCount;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Glide.with(this).load(mUser.avatar_hd).into(mAvatar);
        mUsername.setText(mUser.screen_name);
        mUserIntro.setText(mUser.description);
        mWeiboCount.setText(String.valueOf(mUser.statuses_count));
        mWatchCount.setText(String.valueOf(mUser.friends_count));
        mFansCount.setText(String.valueOf(mUser.followers_count));
    }
}
