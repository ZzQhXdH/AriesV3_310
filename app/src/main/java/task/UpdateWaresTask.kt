package task

import app.Task
import data.WaresInfoManager
import event.NetworkErrorEvent
import org.greenrobot.eventbus.EventBus

class UpdateWaresTask(private val interval: Long) : Runnable
{
    override fun run()
    {
        try {
            WaresInfoManager.updateOfAsyncEvent()
        } catch (e: Exception) {
            e.printStackTrace()
            EventBus.getDefault().post(NetworkErrorEvent())
            Task.AsyncHandler.postDelayed(this, interval)
        }
    }
}