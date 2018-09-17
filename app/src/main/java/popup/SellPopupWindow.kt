package popup

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import app.App
import com.hontech.icecreamcustomclient.R

class SellPopupWindow
{// 09-05 18:12:01.908 1360-1360/com.hontech.icecreamcustomclient D/DEBUG: 极光推送:{"id":"status","macAdress":["50ff99302319"],"param":{"isSBOpen":false,"status":0}}
    //-05 18:11:02.073 1360-1417/com.hontech.icecreamcustomclient D/更新状态: {"ariesFridge":"normal","ariesPickMoto":"normal","ariesRobotArm1":"normal","ariesRobotArm2":"normal","ariesTemperature":"-24.3125","ariesDoorstatus":"open","ariesRSSI":0,"troubleTemperature":"false","macAddr":"50:ff:99:30:23:19","trouble":"true"}
    // 09-05 18:24:22.587 1360-1360/com.hontech.icecreamcustomclient D/DEBUG: 极光推送:{"id":"status","macAdress":["50ff99302319"],"param":{"isSBOpen":false,"status":0}}
// 09-05 18:23:11.766 1360-1417/com.hontech.icecreamcustomclient D/更新状态: {"ariesFridge":"normal","ariesPickMoto":"normal","ariesRobotArm1":"normal","ariesRobotArm2":"normal","ariesTemperature":"-24.8125","ariesDoorstatus":"open","ariesRSSI":0,"troubleTemperature":"false","macAddr":"50:ff:99:30:23:19","trouble":"true"}

    companion object
    {
        val instance: SellPopupWindow by lazy { SellPopupWindow() }
    }

    private val mView = LayoutInflater.from(App.AppContext).inflate(R.layout.popup_sell_window, null)
    private val mPopupWindow = PopupWindow(mView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true)

    init {
        mPopupWindow.isOutsideTouchable = false
    }

    fun show(view: View)
    {
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    fun dismiss()
    {
        mPopupWindow.dismiss()
    }

}