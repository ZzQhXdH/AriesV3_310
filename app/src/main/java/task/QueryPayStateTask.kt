package task

import app.Task
import data.WaresInfoManager
import event.PayStatusEvent
import org.greenrobot.eventbus.EventBus
import popup.DeliverPopupWindow
import util.Http

class QueryPayStateTask(private val interval: Long) : Runnable
{
    companion object
    {
        var QueryFlag = true
    }

    init {
        QueryFlag = true
    }

    private val mOrder = WaresInfoManager.CurrentOrder

    override fun run()
    {
        if ((!QueryFlag) || DeliverPopupWindow.isShow) {
            return
        }

        try {
            val ret = Http.queryPayStatusOfNetwork(mOrder)
            if (ret) {
                EventBus.getDefault().post(PayStatusEvent())
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Task.AsyncHandler.postDelayed(this, interval)
    }
}