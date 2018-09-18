package activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore

import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import app.App

import app.Task
import app.log
import app.resetApp
import com.bumptech.glide.Glide

import com.hontech.icecreamcustomclient.R
import data.*

import event.*

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import popup.*
import service.SerialPortService
import task.UpdateAdvTask
import task.UpdateSellStatusTask
import util.Http

import util.showToast
import view.FillVideoView



class MainActivity : AppCompatActivity()
{
    companion object
    {
        private const val WRITE_EXTERNAL_REQ = 0x01
        const val ACTION_QUIT = "action.quit"
        var isShow = false
        const val HDMI_ACTION = "android.intent.action.HDMI_PLUGGED"
    }

    private var isInit = false

    private val mButtonGoBuy: Button by lazy { findViewById<Button>(R.id.id_main_button) }

    private val mButtonLogin: Button by lazy { findViewById<Button>(R.id.id_main_log_button) }

    private val mVideoManager: VideoAndImageManager by lazy {
         VideoAndImageManager(findViewById(R.id.id_main_video_view), findViewById(R.id.id_main_image_view))
    }

    private val mNetwordView: NetworkStatusView by lazy {
        NetworkStatusView(findViewById<ImageView>(R.id.id_main_image_view_network_status),
                findViewById<TextView>(R.id.id_main_text_view_network_status))
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        App.addActivity(this)
        setContentView(R.layout.activity_main)
        requestPermission()
        initHdmi()
    }

    private fun onInit()
    {
        EventBus.getDefault().register(this)

        mButtonGoBuy.setOnClickListener(::onBuyClick)

        mButtonLogin.setOnLongClickListener(::onEnterDebugLongClick)

        isInit = true

        Task.updateWaresInfo(5000)
        Task.DelayHandler.post(UpdateAdvTask())
        Task.DelayHandler.post(UpdateSellStatusTask())
    }

    private fun initHdmi()
    {
        val filter = IntentFilter(HDMI_ACTION)
        registerReceiver(mHdmiReceiver, filter)
    }

    private val mHdmiReceiver = object: BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            if (intent.action == HDMI_ACTION)
            {
                val ret = intent.getBooleanExtra("state", false)
                log("HDMI 状态:$ret")
            }
        }
    }

    override fun onStart()
    {
        super.onStart()
        log("onStart")
        App.ResetFlag = true
        App.ResetFlagAll = true
    }

    override fun onStop()
    {
        super.onStop()
        log("onStop")
        if (App.ResetFlag) {
            resetApp()
        }
    }

    override fun onResume()
    {
        super.onResume()
        log("onResume")
        isShow = true
        if (WaresInfoManager.SellStatus) // 如果停售
        {
            SellPopupWindow.instance.show(mButtonGoBuy)
        }
        OTAManager.instance.updateOfCheck()
        VersionManager.checkUpdate()
        mVideoManager.resume()
    }

    override fun onPause()
    {
        super.onPause()
        log("onPause")
        isShow = false
        mVideoManager.pause()
    }

    override fun onDestroy()
    {
        unregisterReceiver(mHdmiReceiver)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
        App.removeActivity(this)
        if (App.ResetFlagAll) {
            resetApp()
        }
    }

    private fun onEnterDebugLongClick(view: View): Boolean
    {
        LoginKeyPopupWindow.instance.show(mButtonLogin)
        return true
    }

    private fun onBuyClick(view: View)
    {
        if (!StatusManager.instance.isDoorClose())
        {
            showToast("请先关闭大门", this)
            return
        }

        App.ResetFlag = false
        val i = Intent(this, HomeActivity::class.java)
        startActivity(i)
    }

    private fun requestPermission()
    {
        val ret = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (ret != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MainActivity.WRITE_EXTERNAL_REQ)
            return
        }
        onInit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (requestCode == MainActivity.WRITE_EXTERNAL_REQ))
        {
            onInit()
        }
    }

    override fun onNewIntent(intent: Intent)
    {
        super.onNewIntent(intent)
        val ret = intent.getBooleanExtra(ACTION_QUIT, false)
        if (ret) {
            App.ResetFlag = false
            App.ResetFlagAll = false
            finish()
            return
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onDoorStatusChangeEvent(env: DoorStatusChangeEvent)
    {
        if (!env.closeStatus) {
            mButtonGoBuy.setBackgroundColor(0xFFFF0000.toInt())
        } else {
            mButtonGoBuy.setBackgroundColor(0x00FFFFFF)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onVersionUpdateProgressChageEvent(env: VersionProgressChangeEvent)
    {
        val action = env.action

        when (action)
        {
            VersionProgressChangeEvent.ACTION_END -> SystemOTAPopupWinodw.instance.dismiss()

            VersionProgressChangeEvent.ACTION_START -> SystemOTAPopupWinodw.instance.show(mButtonGoBuy, env.maxValue.toFloat())

            VersionProgressChangeEvent.ACTION_PROGRESS -> SystemOTAPopupWinodw.instance.setProgress(env.progress)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onSellChangeEvent(env: SellChangeEvent)
    {
        if (env.status != SellChangeEvent.STOP) {
            SellPopupWindow.instance.dismiss()
        }

        if (DebugActivity.isShow || HomeActivity.isShow) {
            return
        }

        if (env.status == SellChangeEvent.STOP) {
            SellPopupWindow.instance.show(mButtonGoBuy)
            return
        }
        SellPopupWindow.instance.dismiss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onUserLoginEvent(env: UserLoginResultEvent)
    {
        if (env.flag)
        {
            App.ResetFlag = false
            val i = Intent(this, DebugActivity::class.java)
            startActivity(i)
            showToast("登陆成功", this)
            return
        }
        showToast("密码错误", this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onNetworkErrorEvent(env: NetworkErrorEvent)
    {
        showToast("网络错误", this)
        mNetwordView.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onAdvChangeEvent(env: AdvChangeEvent)
    {
        mVideoManager.start()
    }

    private class VideoAndImageManager(private val videoView: FillVideoView, private val imageView: ImageView)
        : MediaPlayer.OnCompletionListener,
            Runnable,
            MediaPlayer.OnInfoListener,
            MediaPlayer.OnErrorListener,
            MediaPlayer.OnPreparedListener
    {
        private var nVideoIndex = 0
        private var nPictureIndex = 0
        private var isPlayVideo = false
        private var isStart = false

        fun pause()
        {
            if ( (!isStart) || AdvertisingManager.getAllVideo().isEmpty()) {
                return
            }
            if (isPlayVideo) {
                videoView.pause()
            } else {
                Task.UiHandler.removeCallbacks(this)
            }
        }

        fun resume()
        {
            val videos = AdvertisingManager.getAllVideo()
            val pictures = AdvertisingManager.getAllPicture()
            if ( (!isStart) || videos.isEmpty()) {
                return
            }

            if (isPlayVideo)
            {
                if (nVideoIndex >= videos.size) {
                    nVideoIndex = 0
                }
                playVideo(videos[nVideoIndex])
            } else {
                if (nPictureIndex >= pictures.size) {
                    nPictureIndex = 0
                }
                playPicture(pictures[nPictureIndex])
            }

        }

        fun start()
        {
            isStart = true

            videoView.setOnErrorListener(this)
            videoView.setOnCompletionListener(this)
            videoView.setOnPreparedListener(this)

            do { // test video
                val videos = AdvertisingManager.getAllVideo()
                if (videos.isEmpty()) {
                    break
                }
                playVideo(videos[nVideoIndex])
                return
            } while (false)

            do {
                val pictures = AdvertisingManager.getAllPicture()
                if (pictures.isEmpty()) {
                    break
                }
                playPicture(pictures[nPictureIndex])
            } while (false)
        }

        override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean
        {
            log("视频播放异常")
            videoView.stopPlayback()
            return true
        }

        override fun onCompletion(mp: MediaPlayer)
        {
            nVideoIndex ++

            val videos = AdvertisingManager.getAllVideo()

            if (videos.isEmpty()) {
                return
            }

            if (nVideoIndex >= videos.size) // 视频全部播放完了
            {
                val pictures = AdvertisingManager.getAllPicture()
                if (pictures.isEmpty()) { // 没有图片
                    nVideoIndex = 0
                    playVideo(videos[nVideoIndex]) // 重新播放视频
                } else { // 开始播放图片
                    nPictureIndex = 0
                    playPicture(pictures[nPictureIndex])
                }
                return
            }

            playVideo(videos[nVideoIndex])
        }

        override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean
        {
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                videoView.setBackgroundColor(Color.TRANSPARENT)
            }
            return true
        }

        override fun onPrepared(mp: MediaPlayer?)
        {
            videoView.setOnInfoListener(this)
        }

        override fun run()
        {
            nPictureIndex ++

            val pictures = AdvertisingManager.getAllPicture()

            if (pictures.isEmpty()) {
                return
            }

            if (nPictureIndex >= pictures.size) // 图片全部播放完了
            {
                val videos = AdvertisingManager.getAllVideo()

                if (videos.isEmpty()) { // 没有视频, 重新播放图片
                    nPictureIndex = 0
                    playPicture(pictures[nPictureIndex])
                } else {
                    nVideoIndex = 0
                    playVideo(videos[nVideoIndex])
                }
                return
            }

            playPicture(pictures[nPictureIndex])
        }

        private fun playVideo(video: Video)
        {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            videoView.setUp(video.url)
            videoView.start()
            isPlayVideo = true
        }

        private fun playPicture(picture: Picture)
        {
            imageView.visibility = View.VISIBLE
            videoView.visibility = View.GONE
            videoView.pause()
            Glide.with(imageView.context).load(picture.url).into(imageView)
            Task.UiHandler.postDelayed(this, picture.timeOut)
            isPlayVideo = false
        }
    }

    private class NetworkStatusView(private val imageView: ImageView, private val textView: TextView) : Runnable
    {
        private var isShow = false

        fun show()
        {
            if (isShow) {
                Task.UiHandler.removeCallbacks(this)
                Task.UiHandler.postDelayed(this, 10 * 1000)
                return
            }
            isShow = true
            imageView.visibility = View.VISIBLE
            textView.visibility = View.VISIBLE
            Task.UiHandler.postDelayed(this, 10 * 1000)
        }

        private inline fun hide()
        {
            isShow = false
            imageView.visibility = View.GONE
            textView.visibility = View.GONE
        }

        override fun run()
        {
            hide()
        }
    }

}