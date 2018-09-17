package data

import app.log
import event.AdvChangeEvent
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import util.Http


data class Video(val url: String)

data class Picture(val url: String, val timeOut: Long)

object AdvertisingManager
{

    private val mVideoList = ArrayList<Video>()
    private val mPictureList = ArrayList<Picture>()

    fun isEmpty(): Boolean
    {
        return mVideoList.isEmpty() && mPictureList.isEmpty()
    }

    fun getAllVideo() = mVideoList

    fun getAllPicture() = mPictureList

    fun update()
    {
        val tmp = Http.acquireAdv()
        log("广告:$tmp")

        do {
            val json = JSONObject(tmp)
            val video = json.optString("videoUrl", "")
            if (video.isNotEmpty()) {
                mVideoList.clear()
                mVideoList.add( Video((video)) )
            }
            val timeOut = json.optInt("playTime", 0)
            if (timeOut <= 0) {
                break
            }
            val photoArray = json.optJSONArray("photo")
            if ((photoArray == null) || (photoArray.length() == 0)) {
                break
            }
            for (i in 0 until photoArray.length())
            {
                val url = photoArray.optString(i, "")
                if (url.isNotEmpty())
                {
                    mPictureList.add( Picture(url, timeOut.toLong() * 1000) )
                }
            }
        } while (false)

        EventBus.getDefault().post(AdvChangeEvent())
    }

}