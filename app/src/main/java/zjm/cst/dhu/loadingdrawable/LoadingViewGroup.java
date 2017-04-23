package zjm.cst.dhu.loadingdrawable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zjm on 2017/4/22.
 */

public class LoadingViewGroup extends ViewGroup {

    public LoadingViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int getSize(int minSize, int measureSpec) {
        int res = minSize;
        int mode = MeasureSpec.getMode(measureSpec);
        int defaultSize = MeasureSpec.getSize(measureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:
                res = Math.min(defaultSize, minSize);
                break;
            case MeasureSpec.EXACTLY:
                res = defaultSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                res = minSize;
                break;
        }
        return res;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childWidth = 0;
        int childHeight = 0;
        int minChild = 0;
        if (getChildCount() > 0) {
            View childView = getChildAt(0);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            childWidth = childView.getMeasuredWidth();
            childHeight = childView.getMeasuredHeight();
            minChild = Math.max(childHeight, childWidth);
        }
        int width = getSize((int) (minChild), widthMeasureSpec);
        int height = getSize((int) (minChild), heightMeasureSpec);
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childWidth = 0;
        int childHeight = 0;
        if (getChildCount() > 0) {
            View childView = getChildAt(0);
            childWidth = childView.getMeasuredWidth();
            childHeight = childView.getMeasuredHeight();
            childView.layout(getMeasuredWidth() / 2 - childWidth / 2, getMeasuredHeight() / 2 - childHeight / 2, getMeasuredWidth() / 2 + childWidth / 2, getMeasuredHeight() / 2 + childHeight / 2);
        }
    }
}
