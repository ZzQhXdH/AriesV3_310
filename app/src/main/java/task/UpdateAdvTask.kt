package task

import app.Task
import data.AdvertisingManager
import event.AdvChangeEvent
import event.NetworkErrorEvent
import org.greenrobot.eventbus.EventBus

/**
 * 更新广告
 */
class UpdateAdvTask : Runnable
{
    override fun run()
    {
        try {
            AdvertisingManager.update()
            EventBus.getDefault().post(AdvChangeEvent())
            return
        } catch (e: Exception) {
            e.printStackTrace()
            EventBus.getDefault().post(NetworkErrorEvent())
        }

        Task.DelayHandler.postDelayed(this, 5 * 1000)
    }
}