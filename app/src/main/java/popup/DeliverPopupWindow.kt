package popup

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import app.App
import app.Task
import app.log
import com.hontech.icecreamcustomclient.R
import com.wang.avi.AVLoadingIndicatorView
import data.GoodsTypeInfo

import data.WaresInfoManager
import event.BarCodeScannerEvent
import event.DeliverResultEvent
import event.WaresChangeEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import service.SerialPortManager
import task.ChargeVIPTask
import task.RefundTask
import task.ReportGoodsTypeTask
import util.*
import view.DownCounterView

class DeliverPopupWindow
{
    companion object
    {
        val instance: DeliverPopupWindow by lazy { DeliverPopupWindow() }
        var isVip = false
        var isShow = false
    }

    private val mView = LayoutInflater.from(App.AppContext).inflate(R.layout.popup_deliver_window, null)
    private val mImageView = mView.findViewById<ImageView>(R.id.id_popup_pay_wares_image_view)
    private val mLoading = mView.findViewById<AVLoadingIndicatorView>(R.id.id_popup_pay_wares_loading)
    private val mCounterView = mView.findViewById<DownCounterView>(R.id.id_popup_deliver_counter_view)
    private val mTextViewHint = mView.findViewById<TextView>(R.id.id_popup_pay_descriptor_text_view)
    private val mPopupWindow = PopupWindow(mView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, false)

    init {
        mPopupWindow.animationStyle = R.style.PopupTheme
        mPopupWindow.isOutsideTouchable = false
        mPopupWindow.setOnDismissListener(::onDismiss)
    }

    private fun onTimeOut() = onError("出货超时退款")

    private fun onError(msg: String)
    {
        mCounterView.stopAnimator()
        val info = WaresInfoManager.getSelectWaresInfo()

        if (isVip)
        {
            mTextViewHint.text = "${info.name} 出货失败!"
            Task.UiHandler.postDelayed( {
                mPopupWindow.dismiss()
            }, 5000 )
            return
        }

        mTextViewHint.text = "${info.name} 出货失败\r\n将于24小时内退款!"
        Task.UiHandler.postDelayed( {
            mPopupWindow.dismiss()
        }, 5000 )

        Task.DelayHandler.post(RefundTask(WaresInfoManager.CurrentOrder, msg, ""))
    }

    private fun onSuccess(row: Int, col: Int)
    {
        mCounterView.stopAnimator()
        mTextViewHint.text = "出货成功!\r\n谢谢惠顾"

        if (isVip) {
            val info = WaresInfoManager.getSelectWaresInfo()
            Task.AsyncHandler.post(ChargeVIPTask(WaresInfoManager.CurrentOrder, info.price)) // VIP扣款
        }
        Task.AsyncHandler.post(ReportGoodsTypeTask("$row-$col"))
    }

    private fun setUi()
    {
        val info = WaresInfoManager.getSelectWaresInfo()
        info.logMessage()
        mImageView.setImageNoSelectorAsync(info.maxImagePath, mLoading)
        mCounterView.startAnimator(::onTimeOut)
        mTextViewHint.text = "${info.name}\r\n正在出货请稍后..."

        val byteArray = info.createDeliverByteArray()
        SerialPortManager.instance.write(byteArray)
    }

    private fun onDeliverError(row: Int, col: Int)
    {
        WaresInfoManager.getSelectWaresInfo().setErrorFlag(row, col)
        mCounterView.stopAnimator()
        mCounterView.startAnimator(::onTimeOut)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onWaresChangeEvent(env: WaresChangeEvent)
    {
        if (env.status == WaresChangeEvent.OK)
        {
            Task.UiHandler.postDelayed( { mPopupWindow.dismiss() }, 5000 )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onDeliverEvent(env: DeliverResultEvent)
    {
        if (env.isStatus)
        {
            when (env.state)
            {
                0x00 -> onSuccess(env.row, env.col)
                0x01 -> onDeliverError(env.row, env.col)
                0x02 -> onError("出货失败退款")
            }
            return
        }
    }

    fun show(view: View)
    {
        if (isShow) {
            return
        }

        isShow = true
        EventBus.getDefault().register(this)
        setUi()
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    private fun onDismiss()
    {
        isShow = false
        EventBus.getDefault().unregister(this)
    }
}