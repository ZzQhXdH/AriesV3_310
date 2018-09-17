package app

import android.os.Handler
import android.os.HandlerThread
import task.GetQrCodeImageTask
import task.ReplenishFinishTask
import task.UpdateGoodsTypeTask
import task.UpdateWaresTask

object Task
{
    private val AsyncTask = HandlerThread("Async")
    private val DelayTask = HandlerThread("Delay")
    val AsyncHandler: Handler
    val DelayHandler: Handler
    val UiHandler: Handler

    init {
        UiHandler = Handler()
        AsyncTask.start()
        DelayTask.start()
        AsyncHandler = Handler(AsyncTask.looper)
        DelayHandler = Handler(DelayTask.looper)
    }

    fun stop()
    {
        AsyncTask.quit()
        DelayTask.quit()
    }

    fun updateQrCodeImage() = AsyncHandler.post(GetQrCodeImageTask())

    fun updateWaresInfo(interval: Long) = AsyncHandler.post(UpdateWaresTask(interval))

    fun updateGoodsType() = AsyncHandler.post(UpdateGoodsTypeTask())

    fun replenishFinish() = AsyncHandler.post(ReplenishFinishTask())

}