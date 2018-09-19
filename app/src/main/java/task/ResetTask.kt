package task

import app.Task
import app.log
import app.resetSystem
import java.util.*

class ResetTask : Runnable
{
    companion object
    {
        private val RESET_HOUR = 12
        const val INTERVAL = 60 * 60 * 1000.toLong() // 一个小时的毫秒数
        const val DAY_TIME = 24 * 60 * 60 * 1000.toLong() // 一天的毫秒数
    }

    override fun run()
    {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24小时制
        val minute = calendar.get(Calendar.MINUTE)
        log("hour:$hour:minute:$minute")
        if (hour == RESET_HOUR)
        {
            resetSystem()
        }
        Task.DelayHandler.postDelayed(this, INTERVAL)
    }
}