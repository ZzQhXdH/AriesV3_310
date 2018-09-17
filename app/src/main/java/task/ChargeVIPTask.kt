package task

import app.Task
import app.log
import util.Http

class ChargeVIPTask(private val order: String, private val price: String) : Runnable
{
    override fun run()
    {
        try {
            log("vip扣款:$order,$price")
            val ret = Http.chargeVip(order)
            log(ret)
        } catch (e: Exception) {
            e.printStackTrace()
            Task.AsyncHandler.postDelayed(this, 1000)
        }
    }
}