package view


import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView

class AutoCountTextView : TextView
{
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private val mCounterAnimator = ValueAnimator.ofInt(60, 0)

    private var mTimeOutNotify = {}

    fun setNotify(notify: () -> Unit)
    {
        mTimeOutNotify = notify
    }

    init {
        mCounterAnimator.duration = 60 * 1000
        mCounterAnimator.interpolator = LinearInterpolator()
        mCounterAnimator.addUpdateListener {
            val v = it.animatedValue as Int
            text = v.toString()
            if (v == 0) {
                mTimeOutNotify()
            }
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean)
    {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) {
            mCounterAnimator.cancel()
            mCounterAnimator.start()
        }
    }

    override fun onWindowVisibilityChanged(visibility: Int)
    {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != View.VISIBLE) {
            mCounterAnimator.cancel()
        }
    }
}