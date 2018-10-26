package task

import app.Task
import app.log
import app.checkApplication
import data.StatusManager
import util.Http
import util.Logger
import java.lang.Exception

class UpdateStatusTask : Runnable
{

    companion object
    {
        private const val TIME_OUT = 180 * 1000L
    }

    override fun run()
    {
        checkApplication()

        try {
            val content = StatusManager.instance.toJsonOf3()
            log(content, "STATUS")
            val ret = Http.updateTemperatureOf3(content)
            log(ret, "STATUS")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Logger.updateMemoryInfo()
        Logger.updateCpuInto()

        Task.DelayHandler.postDelayed(this, TIME_OUT)
    }
}