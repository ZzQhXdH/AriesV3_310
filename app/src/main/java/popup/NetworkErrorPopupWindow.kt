package popup

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import app.App
import app.Task
import com.hontech.icecreamcustomclient.R

class NetworkErrorPopupWindow : PopupWindow(), Runnable
{
    companion object
    {
        var isShow = false
    }

    init {
        contentView = LayoutInflater.from(App.AppContext).inflate(R.layout.popup_network_error, null)
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.MATCH_PARENT
        isOutsideTouchable = false
        isFocusable = false
    }

    fun show(parent: View)
    {
        showAtLocation(parent, Gravity.CENTER, 0, 0)
        isShow = true
        Task.UiHandler.postDelayed( this, 10 * 1000)
    }

    override fun run()
    {
        isShow = false
        dismiss()
    }
}
