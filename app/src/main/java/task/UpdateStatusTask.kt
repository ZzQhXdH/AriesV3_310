package task

import app.Task
import app.log
import app.startApp
import data.StatusManager
import util.Http

class UpdateStatusTask : Runnable
{
    override fun run()
    {
        log("检测状态")
        startApp()

        if (StatusManager.instance.isEmpty()) {

            log("温度为空, 请检查下位机")
            Task.DelayHandler.postDelayed(this, 180 * 1000)
            return
        }

        try {
            val ret = StatusManager.instance.toJson()
            log(ret, "更新状态")
            Http.updateStatus(ret)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Task.DelayHandler.postDelayed(this, 180 * 1000)
    }
}