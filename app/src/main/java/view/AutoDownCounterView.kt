package view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class AutoDownCounterView : View
{
    companion object
    {
        private const val COLOR = 0xEEFF6347
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleArr: Int) : super(context, attrs, defStyleArr)

    private val mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var x0 = 0
    private var y0 = 0
    private var xc0 = 0f
    private var yc0 = 0f
    private val mRectF = RectF()
    private var mMaxValue = 1f

    private var value = 0f
        set(value) {
            field = value
            invalidate()
        }

    fun setMaxValue(v: Float)
    {
        mMaxValue = v
        invalidate()
    }

    fun update(v: Float)
    {
        value = v
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
    {
        val a = if (w < h) w else h
        x0 = (w - a) / 2
        y0 = (h - a) / 2
        mRectF.left = x0 + a.toFloat() / 10
        mRectF.top = y0 + a.toFloat() / 10
        mRectF.right = mRectF.left + a.toFloat() / 10 * 8
        mRectF.bottom = mRectF.top + a.toFloat() / 10 * 8
        xc0 = w / 2f
        yc0 = h / 2f

        mCirclePaint.style = Paint.Style.STROKE
        mCirclePaint.strokeCap = Paint.Cap.ROUND
        mCirclePaint.strokeWidth = a.toFloat() / 10 * 2

        mTextPaint.style = Paint.Style.STROKE
        mTextPaint.textSize = 80f
        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.color = COLOR.toInt()
    }

    override fun onDraw(canvas: Canvas)
    {
        val text = value.toInt().toString()
        val fontMeasureInt = mTextPaint.fontMetricsInt
        canvas.drawText(text,
                xc0,
                yc0 - (fontMeasureInt.top + fontMeasureInt.bottom) / 2,
                mTextPaint)
        mCirclePaint.color = 0x8800ddff.toInt()
        canvas.drawArc(mRectF, 0f, 360f, false, mCirclePaint)
        mCirclePaint.color = COLOR.toInt()
        canvas.drawArc(mRectF, -90f, value / mMaxValue * 360, false, mCirclePaint)
    }
}