package task

import app.Task
import app.log
import data.WaresInfoManager
import event.SellChangeEvent
import org.greenrobot.eventbus.EventBus
import util.Http

class UpdateSellStatusTask : Runnable
{
    override fun run()
    {
        try {
            val ret = Http.acquireSellStatus()
            log("销售状态:$ret")
            if (ret.isNotEmpty())
            {
                val state = ret.toInt()
                WaresInfoManager.SellStatus = if (state == SellChangeEvent.STOP) true else false
                EventBus.getDefault().post(SellChangeEvent(state))
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Task.DelayHandler.postDelayed(this, 5000)
    }
}