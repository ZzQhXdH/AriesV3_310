package task

import app.Task
import app.log
import data.StatusManager
import org.json.JSONObject
import util.Http

class UpdateTemperatureTask : Runnable
{
    override fun run()
    {
        try {
            val ret = Http.acquireTemperature()
            log("温度设置:$ret")
            val json = JSONObject(ret)
            val min = json.optDouble("lowest", 0.0)
            val max = json.optDouble("highest", 0.0)
            val time = json.optDouble("time", Int.MAX_VALUE.toDouble() / 100) * 60
            StatusManager.instance.set(min.toFloat(), max.toFloat(), time.toInt())
            log("温度:$ret")
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Task.DelayHandler.postDelayed(this, 180 * 1000)
    }
}