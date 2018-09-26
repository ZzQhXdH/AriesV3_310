package task

import app.Task
import app.log
import app.startApp
import data.StatusManager
import util.HDMIManager
import util.Http

class UpdateStatusTask : Runnable
{
    private var mCurrentCounter = -1

    override fun run()
    {
        startApp()

        val counter = StatusManager.instance.getUpdateCounter()

        val err = if (mCurrentCounter == counter) true else false

        mCurrentCounter = counter

        try {
            val s1 = HDMIManager.getState()
            val s2 = HDMIManager.getConnect()
            val s3 = HDMIManager.getEdidRead()

            val ret = if (err) {
                StatusManager.instance.toJsonOfError(s1, s2, s3)
            } else {
                StatusManager.instance.toJson(s1, s2, s3)
            }

            log(ret, "更新状态")
            Http.updateStatus(ret)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Task.DelayHandler.postDelayed(this, 180 * 1000)
    }
}