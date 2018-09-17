package popup

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import app.App
import app.Task
import com.hontech.icecreamcustomclient.R
import view.AutoDownCounterView
import view.DownCounterView

class SystemOTAPopupWinodw
{
    companion object
    {
        val instance: SystemOTAPopupWinodw by lazy { SystemOTAPopupWinodw() }
    }

    private val mView = LayoutInflater.from(App.AppContext).inflate(R.layout.popup_system_ota, null)
    private val mTextView = mView.findViewById<TextView>(R.id.id_popup_system_ota_text_view)
    private val mCounterView = mView.findViewById<AutoDownCounterView>(R.id.id_popup_system_ota_counter_view)

    private val mPopupWindow = PopupWindow(mView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true)

    init {
        mPopupWindow.isOutsideTouchable = false
    }

    fun setTitle(text: String)
    {
        mTextView.text = text
    }

    fun setProgress(p: Int)
    {
        mCounterView.update(p.toFloat())
    }

    fun dismiss()
    {
        mPopupWindow.dismiss()
        Task.UiHandler.removeCallbacks(::dismiss)
    }

    fun show(view: View, maxValue: Float)
    {
        mCounterView.setMaxValue(maxValue)
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        mTextView.text = "正在系统升级..."
        Task.UiHandler.postDelayed(::dismiss, 5 * 60 * 1000)
    }

}