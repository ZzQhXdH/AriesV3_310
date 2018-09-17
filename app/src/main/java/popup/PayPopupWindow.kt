package popup

import android.animation.ValueAnimator
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import app.App
import app.Task
import app.log
import com.hontech.icecreamcustomclient.R
import com.wang.avi.AVLoadingIndicatorView
import data.WaresInfoManager
import event.BarCodeScannerEvent
import event.QrCodeEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import receiver.ServicePushReceiver
import task.QueryPayStateTask
import util.setImageAsync
import util.setImageNoSelectorAsync

class PayPopupWindow
{
    companion object
    {
        val instance: PayPopupWindow by lazy { PayPopupWindow() }
        var isShow = false
    }

    private val mView = LayoutInflater.from(App.AppContext).inflate(R.layout.popup_pay_window, null)
    private val mWaresImage = mView.findViewById<ImageView>(R.id.id_popup_pay_wares_image_view)
    private val mWaresLoading = mView.findViewById<AVLoadingIndicatorView>(R.id.id_popup_pay_wares_loading)
    private val mQrCodeImage = mView.findViewById<ImageView>(R.id.id_popup_qrcode_image_view)
    private val mQrCodeLoading = mView.findViewById<AVLoadingIndicatorView>(R.id.id_popup_qrcode_loading)
    private val mHintTextView = mView.findViewById<TextView>(R.id.id_popup_pay_descriptor_text_view)
    private val mButtonCancel = mView.findViewById<Button>(R.id.id_popup_pay_button_cancel)
    private val mPopupWindow = PopupWindow(mView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, false)
    private val mAnimator = ValueAnimator.ofInt(100, 0)

    init {
        mPopupWindow.animationStyle = R.style.PopupTheme

        mPopupWindow.isOutsideTouchable = false

        mPopupWindow.setOnDismissListener(::onDismiss)

        mButtonCancel.setOnClickListener { mPopupWindow.dismiss() }

        mAnimator.addUpdateListener {
            val v = it.animatedValue as Int
            mButtonCancel.text = "取消付款($v)"
            if (v <= 0) {
                mPopupWindow.dismiss()
            }
        }

        mAnimator.duration = 100 * 1000

        mAnimator.interpolator = LinearInterpolator()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onQrCodeImageEvent(env: QrCodeEvent)
    {
        mQrCodeLoading.hide()

        mQrCodeImage.visibility = View.VISIBLE

        if ((env.state == QrCodeEvent.NETWORK_ERROR))
        {
            log("极光推送状态:${ServicePushReceiver.JPushConnected}")
            mQrCodeImage.setImageResource(R.drawable.ic_network_error2)
            Task.UiHandler.postDelayed({ mPopupWindow.dismiss() }, 5000)
            return
        }
        mQrCodeImage.setImageBitmap(env.bm!!)
        Task.AsyncHandler.postDelayed(QueryPayStateTask(5000), 5000)
    }

    fun dismiss() = mPopupWindow.dismiss()

    private fun setUi()
    {
        val info = WaresInfoManager.getSelectWaresInfo()
        mWaresImage.setImageNoSelectorAsync(info.maxImagePath, mWaresLoading)
        mHintTextView.text = "${info.name}:¥${info.price}"
        mQrCodeLoading.show()
    }

    fun showOfAsyncEvent(view: View)
    {
        if (isShow) {
            return
        }

        isShow = true

        EventBus.getDefault().register(this)

        setUi()

        Task.updateQrCodeImage()

        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        mAnimator.start()
    }

    private fun onDismiss()
    {
        isShow = false
        QueryPayStateTask.QueryFlag = false
        mAnimator.cancel()
        EventBus.getDefault().unregister(this)
    }

}