package com.hzjy.download;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * @author acer
 * @date 2018/9/11
 */

public class SimplePendulumView extends View {
    private int width;
    private int height;
    private int radius;
    private Paint[] paints = new Paint[7];
    private double T; // 单摆运动周期
    private double interval;
    private double W; // 角速率
    private int count = 0;
    private double angle; // 起始下落角度
    private int l;//摆线长度

    public SimplePendulumView(Context context) {
        this(context, null);
    }

    public SimplePendulumView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimplePendulumView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paints[0] = new Paint();
        paints[0].setAntiAlias(true);
        paints[0].setColor(0xFF2D5471);
        paints[1] = new Paint();
        paints[1].setAntiAlias(true);
        paints[1].setColor(0xFF2D5471);
        paints[2] = new Paint();
        paints[2].setAntiAlias(true);
        paints[3] = new Paint();
        paints[3].setAntiAlias(true);
        paints[4] = new Paint();
        paints[4].setAntiAlias(true);
        paints[5] = new Paint();
        paints[5].setAntiAlias(true);
        paints[5].setColor(0xFFD83A25);
        paints[6] = new Paint();
        paints[6].setAntiAlias(true);
        paints[6].setColor(0xFFD83A25);
        l = pt2px(200);
        radius = pt2px(18);
        T = 2 * Math.PI * Math.sqrt(l / 10);
        interval = T / 60;
        W = 2 * Math.PI / T;
        angle = Math.PI * 2 / 5;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = this.getMeasuredWidth();
        height = this.getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = width / 2;
        int y = (int) (height * 0.47);
        if (interval * count >= T) {
            count = 0;
        }
        if ((interval * count >= 0 && interval * count < T * 0.25) || (interval * count > T * 0.75 && interval * count <= T)) {
            int circleX = -(int) (l * Math.sin(angle) * Math.cos(W * interval * count));
            int circleY = (int) Math.sqrt(l * l - circleX * circleX);
            canvas.save();
            canvas.drawCircle(x - 6 * radius + circleX + pt2px(3), (y - l) + circleY, radius, paints[0]);
            canvas.restore();
            commonDraw(canvas, true, false, x, y);
            canvas.save();
            canvas.drawCircle(x + 6 * radius - pt2px(3), y, radius, paints[6]);
            canvas.restore();
        } else if (interval * count == T * 0.25) {
            canvas.save();
            canvas.drawCircle(x - 6 * radius + pt2px(4), y, radius, paints[0]);
            canvas.restore();
            commonDraw(canvas, true, true, x, y);
            canvas.save();
            canvas.drawCircle(x + 6 * radius - pt2px(2), y, radius, paints[6]);
            canvas.restore();
        } else if (interval * count > T * 0.25 && interval * count < T * 0.75) {
            int circleX = -(int) (l * Math.sin(angle) * Math.cos(W * interval * count));
            int circleY = (int) Math.sqrt(l * l - circleX * circleX);
            canvas.save();
            canvas.drawCircle(x - 6 * radius + pt2px(3), y, radius, paints[0]);
            canvas.restore();
            commonDraw(canvas, true, false, x, y);
            canvas.save();
            canvas.drawCircle(x + 6 * radius + circleX - pt2px(3), (y - l) + circleY, radius, paints[6]);
            canvas.restore();
        } else if (interval * count == T * 0.75) {
            canvas.save();
            canvas.drawCircle(x - 6 * radius + pt2px(2), y, radius, paints[0]);
            canvas.restore();
            commonDraw(canvas, false, true, x, y);
            canvas.save();
            canvas.drawCircle(x + 6 * radius - pt2px(4), y, radius, paints[6]);
            canvas.restore();
        }
        count++;
        invalidate();
    }

    /**
     * @param canvas
     * @param isRight
     * @param isRam   是否撞击
     * @param x
     * @param y
     */
    private void commonDraw(Canvas canvas, boolean isRight, boolean isRam, int x, int y) {
        canvas.save();
        if (isRight && isRam) {
            canvas.drawCircle(x - 4 * radius + pt2px(3), y, radius, paints[1]);
        } else if (!isRight && isRam) {
            canvas.drawCircle(x - 4 * radius + pt2px(1), y, radius, paints[1]);
        } else {
            canvas.drawCircle(x - 4 * radius + pt2px(2), y, radius, paints[1]);
        }
        canvas.restore();

        canvas.save();
        paints[2].setShader(new LinearGradient(x - 3 * radius, 0, x - radius, 0, 0xFF2D5471, 0xFF5C4D5C, Shader.TileMode.CLAMP));
        if (isRight && isRam) {
            canvas.drawCircle(x - 2 * radius + pt2px(2), y, radius, paints[2]);
        } else if (!isRight && isRam) {
            canvas.drawCircle(x - 2 * radius, y, radius, paints[2]);
        } else {
            canvas.drawCircle(x - 2 * radius + pt2px(1), y, radius, paints[2]);
        }
        canvas.restore();

        canvas.save();
        paints[3].setShader(new LinearGradient(x - radius, 0, x + radius, 0, 0xFF5C4D5C, 0xFFA5423C, Shader.TileMode.CLAMP));
        if (isRight && isRam) {
            canvas.drawCircle(x + pt2px(1), y, radius, paints[3]);
        } else if (!isRight && isRam) {
            canvas.drawCircle(x - pt2px(1), y, radius, paints[3]);
        } else {
            canvas.drawCircle(x, y, radius, paints[3]);
        }
        canvas.restore();

        canvas.save();
        paints[4].setShader(new LinearGradient(x + radius, 0, x + 3 * radius, 0, 0xFFA5423C, 0xFFD83A25, Shader.TileMode.CLAMP));
        if (isRight && isRam) {
            canvas.drawCircle(x + 2 * radius, y, radius, paints[4]);
        } else if (!isRight && isRam) {
            canvas.drawCircle(x + 2 * radius - pt2px(2), y, radius, paints[4]);
        } else {
            canvas.drawCircle(x + 2 * radius - pt2px(1), y, radius, paints[4]);
        }
        canvas.restore();

        canvas.save();
        if (isRight && isRam) {
            canvas.drawCircle(x + 4 * radius - pt2px(1), y, radius, paints[5]);
        } else if (!isRight && isRam) {
            canvas.drawCircle(x + 4 * radius - pt2px(3), y, radius, paints[5]);
        } else {
            canvas.drawCircle(x + 4 * radius - pt2px(2), y, radius, paints[5]);
        }
        canvas.restore();
    }

    private int pt2px(float value) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, value, getContext().getResources().getDisplayMetrics()) + 0.5f);
    }
}
