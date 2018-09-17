package task

import data.GoodsTypeManager
import event.NetworkErrorEvent
import org.greenrobot.eventbus.EventBus

class ReplenishFinishTask : Runnable
{
    override fun run()
    {
        try {
            GoodsTypeManager.replenishFinishOfAsyncEvent()
        } catch (e: Exception) {
            e.printStackTrace()
            EventBus.getDefault().post(NetworkErrorEvent())
        }
    }
}