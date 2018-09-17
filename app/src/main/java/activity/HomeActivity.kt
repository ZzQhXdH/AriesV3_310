package activity


import android.animation.ValueAnimator
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.*
import app.App
import app.Task
import app.log
import com.bumptech.glide.Glide
import com.hontech.icecreamcustomclient.R
import com.wang.avi.AVLoadingIndicatorView
import event.WaresChangeEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import data.WaresInfoManager
import event.BarCodeScannerEvent
import event.PayStatusEvent
import popup.DeliverPopupWindow
import popup.PayPopupWindow
import util.setImageAsync


class HomeActivity: AppCompatActivity()
{
    companion object
    {
        var isShow = false
    }

    private val mRecyclerView: RecyclerView by lazy { findViewById<RecyclerView>(R.id.id_home_recycler_view) }
    private val mRecyclerViewAdapter: RecyclerViewAdapter by lazy { RecyclerViewAdapter(::onItemClick) }
    private val mLayoutManager: LinearLayoutManager by lazy { LinearLayoutManager(this@HomeActivity, LinearLayoutManager.VERTICAL, false) }

    private val mUpdateAnimator = ValueAnimator.ofInt(1, 2)

    private val mTimeOut = object: Runnable
    {
        override fun run()
        {
            finishAll()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        App.addActivity(this)
        setContentView(R.layout.activity_home)
        Task.UiHandler.postDelayed(mTimeOut, 60 * 1000)
        onInit()
    }

    private fun finishAll()
    {
        if (PayPopupWindow.isShow || DeliverPopupWindow.isShow)
        {
            Task.UiHandler.removeCallbacks(mTimeOut)
            Task.UiHandler.postDelayed(mTimeOut, 60 * 1000)
            return
        }
        log("超时退出", "HomeActivity超时退出")
        finish()
    }

    private fun stopUpdate()
    {
        mUpdateAnimator.cancel()
        mUpdateAnimator.removeAllUpdateListeners()
    }

    private fun startUpdate()
    {
        if (WaresInfoManager.getWaresInfoNumber() <= 0) {
            return
        }

        mUpdateAnimator.repeatMode = ValueAnimator.RESTART
        mUpdateAnimator.repeatCount = ValueAnimator.INFINITE
        mUpdateAnimator.interpolator = LinearInterpolator()
        mUpdateAnimator.addUpdateListener {
            try {
                mRecyclerView.scrollBy(0, 1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mUpdateAnimator.start()
    }

    private fun onInit()
    {
        EventBus.getDefault().register(this)
        isShow = true
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.addItemDecoration(RecyclerViewItemDecoration())
        mRecyclerView.adapter = mRecyclerViewAdapter
    }

    private fun onItemClick(position: Int)
    {
        WaresInfoManager.SelectIndex = position
        PayPopupWindow.instance.showOfAsyncEvent(mRecyclerView)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean
    {
        if (event.action == KeyEvent.ACTION_DOWN) {
            BarCodeScanner.onScanner(event)
        }
        return super.dispatchKeyEvent(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onPaySuccessEvent(env: PayStatusEvent) // 普通用户扫码
    {
        DeliverPopupWindow.isVip = false
        PayPopupWindow.instance.dismiss()
        DeliverPopupWindow.instance.show(mRecyclerView)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onWaresChangeEvent(env: WaresChangeEvent)
    {
        if (env.status == WaresChangeEvent.OK)
        {
            stopUpdate()

            mRecyclerView.scrollTo(0,0)

            mRecyclerViewAdapter.notifyDataSetChanged()

            startUpdate()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onBarCodeScannerEvent(env: BarCodeScannerEvent) // VIP用户扫码成功
    {
        if (PayPopupWindow.isShow)
        {
            DeliverPopupWindow.isVip = true
            PayPopupWindow.instance.dismiss()
            WaresInfoManager.CurrentOrder = env.order
            DeliverPopupWindow.instance.show(mRecyclerView)
        }
    }

    override fun onResume()
    {
        super.onResume()
        log("开始动画")
        startUpdate()
    }

    override fun onStop()
    {
        super.onStop()
        isShow = false
        log("停止动画")
        stopUpdate()
    }

    override fun onDestroy()
    {
        Task.UiHandler.removeCallbacks(mTimeOut)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
        App.removeActivity(this)
    }

    private class RecyclerViewItem(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        private val mImageView = itemView.findViewById<ImageView>(R.id.id_item_image_view)
        private val mCardView = itemView.findViewById<CardView>(R.id.id_item_card_view)
        private val mTextViewHint = itemView.findViewById<TextView>(R.id.id_item_text_view_hint)
        private val mLoading = itemView.findViewById<AVLoadingIndicatorView>(R.id.id_item_loading)
        private val mTextViewNumber = itemView.findViewById<TextView>(R.id.id_item_text_view_number)

        fun set(position: Int, onItemClick: (position: Int) -> Unit)
        {
            val info = WaresInfoManager.getWaresInfo(position)
            mImageView.setImageAsync(info.minImagePath, mLoading)
            val number = info.amount
            mTextViewNumber.text = "$number"
            if (number <= 0)
            {
                mCardView.setOnClickListener { }
                mTextViewHint.visibility = View.VISIBLE
                return
            }
            mTextViewHint.visibility = View.GONE
            mCardView.setOnClickListener { onItemClick(position) }
        }
    }

    private class RecyclerViewItemDecoration: RecyclerView.ItemDecoration()
    {
        override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView?)
        {
            super.getItemOffsets(outRect, itemPosition, parent)
            outRect.top = 54
        }
    }

    private class RecyclerViewAdapter(private val onItemClick: (position: Int) -> Unit): RecyclerView.Adapter<RecyclerViewItem>()
    {
        override fun onBindViewHolder(holder: RecyclerViewItem, position: Int)
        {
            val num = WaresInfoManager.getWaresInfoNumber()

            if (num <= 0)
            {
                return
            }

            holder.set(position % num, onItemClick)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewItem
        {
            log("CreateViewHolder")
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_menu, parent, false)
            return RecyclerViewItem(view)
        }

        override fun getItemCount(): Int
        {
            val num = WaresInfoManager.getWaresInfoNumber()

            if (num <= 0)
            {
                return 0
            }

            return Int.MAX_VALUE
        }
    }

}