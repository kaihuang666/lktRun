package com.kai.lktMode.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kai.lktMode.activity.MainActivity;

public class MyFragment extends Fragment {
    private LocalBroadcastManager localBroadcastManager;
    private View mView;
    private Context context;
    private static final String TAG = MyFragment.class.getSimpleName();

    private boolean isFragmentVisible;
    private boolean isReuseView;
    private boolean isFirstVisible;
    private View rootView;
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (rootView == null) {
            return;
        }
        if (isFirstVisible && isVisibleToUser) {
            onFragmentFirstVisible();
            isFirstVisible = false;
        }
        if (isVisibleToUser) {
            onFragmentVisibleChange(true);
            isFragmentVisible = true;
            return;
        }
        if (isFragmentVisible) {
            isFragmentVisible = false;
            onFragmentVisibleChange(false);
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariable();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //如果setUserVisibleHint()在rootView创建前调用时，那么
        //就等到rootView创建完后才回调onFragmentVisibleChange(true)
        //保证onFragmentVisibleChange()的回调发生在rootView创建完成之后，以便支持ui操作
        if (rootView == null) {
            rootView = view;
            if (getUserVisibleHint()) {
                if (isFirstVisible) {
                    onFragmentFirstVisible();
                    isFirstVisible = false;
                }
                onFragmentVisibleChange(true);
                isFragmentVisible = true;
            }
        }
        localBroadcastManager= LocalBroadcastManager.getInstance(getContext());
        super.onViewCreated(isReuseView && rootView != null ? rootView : view, savedInstanceState);
    }
    private void initVariable() {
        isFirstVisible = true;
        isFragmentVisible = false;
        rootView = null;
        isReuseView = true;
    }

    /**
     *
     * @param isReuse
     */
    protected void reuseView(boolean isReuse) {
        isReuseView = isReuse;
    }

    /**
     * 去除setUserVisibleHint()多余的回调场景，保证只有当fragment可见状态发生变化时才回调
     * 回调时机在view创建完后，所以支持ui操作，解决在setUserVisibleHint()里进行ui操作有可能报null异常的问题
     *
     * 可在该回调方法里进行一些ui显示与隐藏
     *
     * @param isVisible true  不可见 -> 可见
     *                  false 可见  -> 不可见
     */
    protected void onFragmentVisibleChange(boolean isVisible) {
        Log.d("viii",isVisible+"");
    }

    /**
     * 在fragment首次可见时回调，可用于加载数据，防止每次进入都重复加载数据
     */
    protected void onFragmentFirstVisible() {
        Log.d(MyFragment.TAG,"预加载完毕");
    }

    protected boolean isFragmentVisible() {
        return isFragmentVisible;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Nullable
    @Override
    public Context getContext() {
        return context;
    }

    public MainActivity getMain(){
        return (MainActivity)getActivity();
    }
    public void Refresh(){

    }
    public boolean isPassed(){
        return false;
    }
    public void next(){

    }

}
