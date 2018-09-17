package view

import android.content.Context

import android.util.AttributeSet
import android.view.View

import android.widget.VideoView
import com.danikula.videocache.HttpProxyCacheServer

class FillVideoView : VideoView
{
    private val httpProxy = HttpProxyCacheServer.Builder(context)
            .maxCacheFilesCount(100)
            .maxCacheSize(1024 * 1024 * 1024)
            .build()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        val width = getDefaultSize(0, widthMeasureSpec)
        val height = getDefaultSize(0, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    fun setUp(url: String)
    {
        pause()
        val localUrl = httpProxy.getProxyUrl(url)
        setVideoPath(localUrl)
    }

}