package task

import app.Task
import data.WaresInfoManager
import util.Http

class RefundTask(private val order: String, private val msg: String, private val goodsType: String) : Runnable
{
    override fun run()
    {
        try {
            Http.refundForResult(msg, order, goodsType)
        } catch (e: Exception) {
            e.printStackTrace()
            Task.DelayHandler.postDelayed(this, 1000)
        }
    }
}