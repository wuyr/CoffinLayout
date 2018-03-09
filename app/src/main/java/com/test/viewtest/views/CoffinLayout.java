package com.test.viewtest.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.test.viewtest.R;

/**
 * Created by wuyr on 18-3-8 下午10:36.
 */

/**
 * 棺材布局: 滑盖那种
 */
public class CoffinLayout extends ViewGroup {

    public static final int STATE_HALF = 0, //棺材盖半开状态
            STATE_COVER = 1,//棺材盖盖住
            STATE_NAKED = 2, //棺材盖打开
            STATE_OPENING = 3,//棺材盖正在打开
            STATE_CLOSING = 4;//棺材盖正在关闭

    private static final int ANIMATION_DURATION = 500;//过渡动画时长
    private int mCurrentStatus;//当前状态
    private Scroller mScroller;//平滑滚动辅助
    private VelocityTracker mVelocityTracker;//手指滑动速率搜集
    private View mElevationView;//阴影view
    private View mLidView;//棺材盖
    private View mBottomView;//棺材底
    private View mResidualView;//棺材盖打开后, 显示在底部的view (一般用来点击合上棺材盖)
    private View mTransitionView;//棺材盖下面那一张白布 (可以设置颜色)
    private View mHeaderView;//头部
    private View mTopBar;//顶栏
    private View mBottomBar;//底栏
    private int mTouchSlop;//触发滑动的最小距离
    private int mLidOffset;//棺材盖的偏移量
    private int mLidElevation;//棺材盖阴影
    private int mTriggerOffset;//触发棺材盖开关的距离
    private int mLastY;//记录上次触摸事件的y坐标
    private int mScrollOffset;//记录Scroller上次的y坐标
    private int mLidViewOffset,//棺材盖的当前偏移量
            mBottomViewOffset, //棺材底的当前偏移量
            mHeaderViewOffset;//头部的当前偏移量
    private int mTransitionColor;//过渡颜色
    private boolean isNewScroll;//用来判断是否要打断上一次未完成的动画 true:打断, 反之
    private boolean isBeingDragged;//已经开始了拖动
    private OnStateChangedListener mOnStateChangedListener;//状态变更的监听

    public CoffinLayout(Context context) {
        this(context, null);
    }

    public CoffinLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoffinLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CoffinLayout, defStyleAttr, 0);
        mLidElevation = a.getDimensionPixelOffset(R.styleable.CoffinLayout_lid_elevation, 0);
        mLidOffset = a.getDimensionPixelOffset(R.styleable.CoffinLayout_lid_offset, 0);
        int resId;
        resId = a.getResourceId(R.styleable.CoffinLayout_residual_view, 0);
        if (resId > 0) {
            mResidualView = LayoutInflater.from(context).inflate(resId, this, false);
        }
        resId = a.getResourceId(R.styleable.CoffinLayout_header_view, 0);
        if (resId > 0) {
            mHeaderView = LayoutInflater.from(context).inflate(resId, this, false);
        }
        resId = a.getResourceId(R.styleable.CoffinLayout_top_bar, 0);
        if (resId > 0) {
            mTopBar = LayoutInflater.from(context).inflate(resId, this, false);
        }
        resId = a.getResourceId(R.styleable.CoffinLayout_bottom_bar, 0);
        if (resId > 0) {
            mBottomBar = LayoutInflater.from(context).inflate(resId, this, false);
        }
        mTransitionColor = a.getColor(R.styleable.CoffinLayout_transition_color, Color.WHITE);
        mLidOffset -= mLidElevation;
        mTriggerOffset = a.getDimensionPixelOffset(R.styleable.CoffinLayout_trigger_open_offset, mLidOffset / 2);
        mTriggerOffset += mLidOffset;
        a.recycle();
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        init();
    }

    private void init() {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0x88444444, 0x00000000});
        mElevationView = new View(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, mLidElevation);
        mElevationView.setLayoutParams(layoutParams);
        mElevationView.setBackground(gradientDrawable);
        mScroller = new Scroller(getContext());
        mVelocityTracker = VelocityTracker.obtain();
        mTransitionView = new View(getContext());
        mTransitionView.setBackgroundColor(mTransitionColor);
    }

    /**
     * 打开棺材盖
     */
    public void openCoffin() {
        if (mCurrentStatus == STATE_OPENING) {
            return;
        }
        abortScrollerAnimation();
        isNewScroll = true;
        int offset = getBottom() - mLidView.getTop();
        mScroller.startScroll(0, 0, 0, offset, ANIMATION_DURATION);
        mCurrentStatus = STATE_OPENING;
        notifyListener();
        invalidate();
        if (mBottomBar != null) {
            hideBottomBar();
        }
        if (mTopBar != null) {
            if (mTopBar.getTranslationY() == 0) {
                hideTopBar();
            } else{
                postDelayed(() -> {
                    if (mTopBar.getTranslationY() == 0) {
                        hideTopBar();
                    }
                }, ANIMATION_DURATION);
            }
        }
    }

    /**
     * 关闭棺材盖
     */
    public void closeCoffin() {
        if (mCurrentStatus == STATE_CLOSING) {
            return;
        }
        abortScrollerAnimation();
        isNewScroll = true;
        int offset = mLidOffset - mLidView.getTop();
        mScroller.startScroll(0, 0, 0, offset, ANIMATION_DURATION);
        mTransitionView.setVisibility(VISIBLE);
        mHeaderView.setVisibility(VISIBLE);
        if (mCurrentStatus == STATE_NAKED) {
            showHeaderView();
        }
        mCurrentStatus = STATE_CLOSING;
        notifyListener();
        invalidate();
        if (mBottomBar != null && mBottomBar.getTranslationY() > 0) {
            showBottomBar();
        }
        if (mTopBar != null && mTopBar.getTranslationY() < 0) {
            showTopBar();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isLidOpeningOrClosing()) {
                    abortScrollerAnimation();
                } else {
                    return false;
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrentStatus == STATE_NAKED) {
                    offsetBottomView(y);
                } else {
                    offsetLidView(y);
                }
                if (mCurrentStatus != STATE_NAKED) {
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float velocityY = mVelocityTracker.getYVelocity();
                    //根据手指滑动的速率和方向来判断是否要隐藏或显示TopBar
                    if (Math.abs(velocityY) > 4000) {
                        if (velocityY > 0) {
                            if (mTopBar != null && mTopBar.getTranslationY() == -mTopBar.getLayoutParams().height) {
                                showTopBar();
                            }
                        } else {
                            if (mTopBar != null && mTopBar.getTranslationY() == 0) {
                                hideTopBar();
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                boolean isHandle = false;
                if (mCurrentStatus == STATE_HALF) {
                    //大于触发距离, 则打开棺材盖, 反之
                    if (mLidView.getTop() >= mTriggerOffset) {
                        openCoffin();
                        isHandle = true;
                    } else if (mLidView.getTop() > mLidOffset) {
                        closeCoffin();
                        isHandle = true;
                    }
                }
                //没有触发打开或关闭棺材盖的动画, 则开始惯性滚动
                if (!isHandle) {
                    mVelocityTracker.computeCurrentVelocity(1000);
                    mScroller.fling(0, 0, 0, (int) mVelocityTracker.getYVelocity(),
                            0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    invalidate();
                }
                //标记状态
                isBeingDragged = false;
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //正在播放开关动画: 拦截
        if (isLidOpeningOrClosing()) {
            return true;
        }
        //已经开始拖动: 拦截
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (isBeingDragged)) {
            return true;
        }
        //爸爸需要拦截: 拦截
        if (super.onInterceptTouchEvent(ev)) {
            return true;
        }
        //不能拖动: 放行
        if (!canScroll()) {
            return false;
        }
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //停止惯性滚动并刷新y坐标
                if (!isLidOpeningOrClosing()) {
                    abortScrollerAnimation();
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int offset = y - mLastY;
                //判断是否触发拖动事件
                if (Math.abs(offset) > mTouchSlop) {
                    mLastY = y;
                    isBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                isBeingDragged = false;
                break;
        }
        return isBeingDragged;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            //子view想要多高,就给它多高
            view.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }
        //测量棺材底
        View view = ((ViewGroup) mBottomView).getChildAt(0);
        if (view != null) {
            view.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }
        mTransitionView.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        switch (getChildCount()) {
            case 6:
                //BottomBar当然是放在底部了
                mBottomBar.layout(0, b - mBottomBar.getLayoutParams().height, r, b);
            case 5:
                //TopBar当然是顶部了
                mTopBar.layout(0, 0, r, mTopBar.getLayoutParams().height);
            case 4:
                //顶部 + 偏移量
                mHeaderView.layout(0, mHeaderViewOffset, r, mHeaderViewOffset + mHeaderView.getLayoutParams().height);
                mHeaderView.setTranslationY(0);
            case 3:
            case 2:
                //棺材盖: 棺材盖固定的偏移量 + 当前的偏移量
                mLidView.layout(0, mLidOffset + mLidViewOffset, r, mLidOffset + mLidViewOffset + mLidView.getMeasuredHeight());
                if (mResidualView != null) {
                    //棺材盖上面用来切换开关的view: 放在底部
                    mResidualView.layout(0, b, r, b + mResidualView.getLayoutParams().height);
                }
            case 1:
                //棺材底: 顶部 + 偏移量
                mBottomView.layout(0, mBottomViewOffset, r, mBottomViewOffset + mBottomView.getMeasuredHeight());
                //过渡view: 与棺材底偏移量相反 (因为它要始终显示在屏幕内)
                mTransitionView.layout(0, -mBottomViewOffset, r, -mBottomViewOffset + mTransitionView.getHeight());
                break;
            default:
                break;
        }
    }

    /**
     * 计算平滑滚动
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            //是新的一轮则刷新offset
            if (isNewScroll) {
                isNewScroll = false;
                mScrollOffset = y;
            }
            //未开盖: 滚动棺材盖
            if (mCurrentStatus != STATE_NAKED) {
                //判断是否还可以滚动
                if (mLidView != null && mLidView.getBottom() >= getBottom()) {
                    int offset = y - mScrollOffset;
                    //判断是否越界: 如果越界,则本次偏移量为可以滑动的最大值
                    if (mLidView.getBottom() + offset < getBottom()) {
                        offset = getBottom() - mLidView.getBottom();
                    } else if (mScroller.getCurrVelocity() > 0 && offset > 0) {//手指滑动, 并且是向下滑
                        if (mLidView.getTop() + offset >= mLidOffset && !isLidOpeningOrClosing()) {
                            offset = mLidOffset - mLidView.getTop();
                        }
                    }
                    offsetChildView(offset);
                }
            } else {//已开盖: 滚动棺材底
                //判断是否还可以滚动
                if (mBottomView != null && mBottomView.getBottom() >= getBottom()
                        && mBottomView.getTop() <= getTop()) {
                    int offset = y - mScrollOffset;
                    //判断是否越界: 如果越界,则本次偏移量为可以滑动的最大值
                    if (mBottomView.getBottom() + offset < getBottom()) {
                        offset = getBottom() - mBottomView.getBottom();
                    } else if (mBottomView.getTop() + offset > getTop()) {
                        offset = getTop() - mBottomView.getTop();
                    }
                    mBottomViewOffset += offset;
                    mBottomView.offsetTopAndBottom(offset);
                    mTransitionView.offsetTopAndBottom(-offset);
                }
            }
            mScrollOffset = y;
            invalidate();
        }
        if (mScroller.isFinished()) {
            isNewScroll = true;
            //滚动结束, 更新状态
            if (mCurrentStatus == STATE_OPENING) {
                mTransitionView.setVisibility(INVISIBLE);
                mHeaderView.setVisibility(INVISIBLE);
                if (mResidualView != null) {
                    showResidualView();
                }
                mCurrentStatus = STATE_NAKED;
                notifyListener();
            } else if (mCurrentStatus == STATE_CLOSING) {
                int offset = getTop() - mBottomView.getTop();
                mBottomViewOffset += offset;
                mBottomView.offsetTopAndBottom(offset);
                mTransitionView.offsetTopAndBottom(-offset);
                if (mResidualView != null) {
                    mResidualView.setTranslationY(0);
                }
                mCurrentStatus = STATE_HALF;
                notifyListener();
            }
        }
    }

    private void addTopBar(int index) {
        if (mTopBar != null) {
            LayoutParams layoutParams = mTopBar.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = generateDefaultLayoutParams();
            }
            super.addView(mTopBar, index, layoutParams);
            setTopBarBackgroundAlpha(1);
        }
    }

    private void addBottomBar(int index) {
        if (mBottomBar != null) {
            LayoutParams layoutParams = mBottomBar.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = generateDefaultLayoutParams();
            }
            super.addView(mBottomBar, index, layoutParams);
        }
    }

    private void addHeaderView(int index) {
        if (mHeaderView != null) {
            LayoutParams layoutParams = mHeaderView.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = generateDefaultLayoutParams();
            }
            super.addView(mHeaderView, index, layoutParams);
        }
    }

    private void addResidualView(int index) {
        if (mResidualView != null) {
            LayoutParams layoutParams = mResidualView.getLayoutParams();
            if (layoutParams == null) {
                layoutParams = generateDefaultLayoutParams();
            }
            super.addView(mResidualView, index, layoutParams);
        }
    }

    /**
     * 给他包装一下, 加上一个过渡的view
     *
     * @param view 棺材底
     * @return 包装后的棺材底
     */
    private View packingBottomView(View view) {
        if (mTransitionView != null && view != null) {
            FrameLayout frameLayout = new FrameLayout(getContext());
            frameLayout.addView(view);
            frameLayout.addView(mTransitionView);
            return frameLayout;
        }
        return null;
    }

    /**
     * 给他包装一下, 加上阴影
     *
     * @param view 棺材盖
     * @return 包装后的棺材盖
     */
    private View packingLidView(View view) {
        if (mElevationView != null && view != null) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(mElevationView);
            linearLayout.addView(view);
            return linearLayout;
        }
        return null;
    }

    public View getTopBar() {
        return mTopBar;
    }

    public View getBottomBar() {
        return mBottomBar;
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public View getResidualView() {
        return mResidualView;
    }

    public boolean isLidOpeningOrClosing() {
        return mCurrentStatus == STATE_OPENING || mCurrentStatus == STATE_CLOSING;
    }

    public int getCurrentStatus() {
        return mCurrentStatus;
    }

    public int getLidViewOffset() {
        return mLidViewOffset;
    }

    public void showBottomBar() {
        startValueAnimation(mBottomBar, mBottomBar.getLayoutParams().height, 0);
    }

    public void hideBottomBar() {
        startValueAnimation(mBottomBar, 0, mBottomBar.getLayoutParams().height);
    }

    private void showResidualView() {
        startValueAnimation(mResidualView, 0, -mResidualView.getLayoutParams().height);
    }

    private void showHeaderView() {
        startValueAnimation(mHeaderView, Math.abs(mBottomView.getTop()) >
                mHeaderView.getHeight() ? -mHeaderView.getHeight() : mBottomView.getTop(), 0);
    }

    private void showTopBar() {
        startValueAnimation(mTopBar, -mTopBar.getLayoutParams().height, 0);
    }

    private void hideTopBar() {
        startValueAnimation(mTopBar, 0, -mTopBar.getLayoutParams().height);
    }

    /**
     * 执行动画
     *
     * @param target 要执行动画的view
     * @param startY 开始值
     * @param endY   结束值
     */
    private void startValueAnimation(View target, int startY, int endY) {
        ValueAnimator animator = ValueAnimator.ofInt(startY, endY).setDuration(ANIMATION_DURATION);
        animator.addUpdateListener(animation -> target.setTranslationY((int) animation.getAnimatedValue()));
        animator.start();
    }

    /**
     * 打断滚动动画
     */
    private void abortScrollerAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }

    /**
     * 设置TopBar的不透明度
     *
     * @param percent 透明度的百分比
     */
    private void setTopBarBackgroundAlpha(float percent) {
        if (mTopBar != null) {
            percent = 1F - percent;
            mTopBar.getBackground().setAlpha((int) (255F * percent));
        }
    }

    /**
     * 判断是否可以滑动
     */
    private boolean canScroll() {
        return mCurrentStatus != STATE_NAKED && (mLidView.getTop() < getTop() || mLidView.getBottom() > getBottom())
                || mCurrentStatus == STATE_NAKED && (mBottomView.getTop() < getTop() || mBottomView.getBottom() > getBottom());
    }

    private void notifyListener() {
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onStateChanged(mCurrentStatus);
        }
    }

    /**
     * 更新棺材盖的位置
     *
     * @param y 偏移量
     */
    private void offsetLidView(int y) {
        if (mLidView != null && mLidView.getBottom() >= getBottom()) {
            int offset = y - mLastY;
            //判断是否越界
            if (mLidView.getBottom() + offset < getBottom()) {
                offset = getBottom() - mLidView.getBottom();
            }
            if (offset > 0 && mLidView.getTop() > mLidOffset) {
                offset /= 2;
            }
            //更新需要联动的其他view
            offsetChildView(offset);
            //更新状态
            int newState = mLidView.getTop() <= getTop() ? STATE_COVER : STATE_HALF;
            if (mCurrentStatus != newState) {
                mCurrentStatus = newState;
                notifyListener();
            }
        }
        mLastY = y;
    }

    /**
     * 更新棺材盖和其他需要联动的View的位置
     *
     * @param offset 偏移量
     */
    private void offsetChildView(int offset) {
        //不是正在打开或关闭状态,并且棺材盖当前位置高于默认的偏移量
        if (!isLidOpeningOrClosing() && mLidView.getTop() < mLidOffset) {
            int bottomViewOffset = offset / 2;//损失一半
            //判断越界
            if (mBottomView.getTop() > getTop() || mBottomView.getTop() + bottomViewOffset > getTop()) {
                bottomViewOffset = getTop() - mBottomView.getTop();
            }
            //更新BottomView和HeaderView的位置
            mBottomViewOffset += bottomViewOffset;
            mBottomView.offsetTopAndBottom(bottomViewOffset);
            mHeaderViewOffset += bottomViewOffset;
            mHeaderView.offsetTopAndBottom(bottomViewOffset);
            mTransitionView.offsetTopAndBottom(-bottomViewOffset);
        }
        //更新棺材盖的位置
        mLidViewOffset += offset;
        mLidView.offsetTopAndBottom(offset);
        //更新TopBar的透明度
        float percent = (float) mLidViewOffset / (getBottom() - mLidOffset);
        mTransitionView.setAlpha(1F - percent);
        percent = (float) (mLidView.getTop() - mTopBar.getHeight()) / (mLidOffset - mTopBar.getHeight());
        if (percent > 1F) {
            percent = 1F;
        }
        if (percent < 0) {
            percent = 0;
        }
        setTopBarBackgroundAlpha(percent);
    }

    /**
     * 更新棺材盖的位置
     *
     * @param y 偏移量
     */
    private void offsetBottomView(int y) {
        //判断是否还能滚动
        if (mBottomView != null && mBottomView.getBottom() >= getBottom()
                && mBottomView.getTop() <= getTop()) {
            int offset = y - mLastY;
            //判断越界
            if (mBottomView.getBottom() + offset < getBottom()) {
                offset = getBottom() - mBottomView.getBottom();
            } else if (mBottomView.getTop() + offset > getTop()) {
                offset = getTop() - mBottomView.getTop();
            }
            //更新位置
            mBottomViewOffset += offset;
            mBottomView.offsetTopAndBottom(offset);
            mTransitionView.offsetTopAndBottom(-offset);
        }
        mLastY = y;
    }

    @Override
    public void addView(View child) {
        addView(child, -1);
    }

    @Override
    public void addView(View child, int index) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = generateDefaultLayoutParams();
            if (params == null) {
                throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
            }
        }
        addView(child, index, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        final LayoutParams params = generateDefaultLayoutParams();
        params.width = width;
        params.height = height;
        addView(child, -1, params);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addView(child, -1, params);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        switch (getChildCount()) {
            case 0:
                mBottomView = child = packingBottomView(child);
                break;
            case 1:
                addResidualView(index);
                mLidView = child = packingLidView(child);
                addHeaderView(index);
                if (child != null) {
                    super.addView(child, index, params);
                }
                addTopBar(index);
                addBottomBar(index);
                return;
            case 6:
                throw new IllegalStateException("CoffinLayout child can't > 2");
            default:
                break;
        }
        if (child != null) {
            super.addView(child, index, params);
        }
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mOnStateChangedListener = listener;
    }

    public interface OnStateChangedListener {
        void onStateChanged(int newState);
    }
}
