package view

import android.content.Context

import android.util.AttributeSet
import android.view.MotionEvent
import com.bigkoo.convenientbanner.ConvenientBanner

class NoScrollRecyclerView : ConvenientBanner<String>
{
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(e: MotionEvent?): Boolean
    {
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean
    {
        return true
    }
}