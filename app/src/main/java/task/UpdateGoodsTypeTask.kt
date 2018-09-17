package task

import data.GoodsTypeManager
import event.NetworkErrorEvent
import org.greenrobot.eventbus.EventBus


class UpdateGoodsTypeTask : Runnable
{
    override fun run()
    {
        try {
            GoodsTypeManager.updateOfAsyncEvent()
        } catch (e: Exception) {
            e.printStackTrace()
            EventBus.getDefault().post(NetworkErrorEvent())
        }
    }
}