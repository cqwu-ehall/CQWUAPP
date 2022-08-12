package com.ucpeo.meal;

import android.content.Context;
import android.util.AttributeSet;

import com.race604.flyrefresh.FlyRefreshLayout;

public class FlyRefreshLayoutEx extends FlyRefreshLayout {
    Context context;

    public FlyRefreshLayoutEx(Context context) {
        super(context);
        this.context = context;
    }

    public FlyRefreshLayoutEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public FlyRefreshLayoutEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    public void startRefresh() {
        setActionDrawable(context.getResources().getDrawable(com.race604.flyrefresh.R.mipmap.ic_send));
        super.startRefresh();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setActionDrawable(context.getResources().getDrawable(R.drawable.icon));
    }
}
