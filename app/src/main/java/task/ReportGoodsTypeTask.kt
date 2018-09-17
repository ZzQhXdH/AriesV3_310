package task

import app.Task
import data.WaresInfoManager
import org.json.JSONObject
import util.Http

//          val ret = Http.queryOrder(order)
//          val json = JSONObject(ret)
//          val info = json.optString("cargolane", "")

class ReportGoodsTypeTask(private val goodsType: String): Runnable
{
    override fun run()
    {
        try
        {
            Http.reportRepetroy(goodsType)
            val ret = Http.queryOrder(WaresInfoManager.CurrentOrder)
            val json = JSONObject(ret)
            val info = json.optString("cargolane", "")
            if (info.isEmpty())
            {
                Task.DelayHandler.postDelayed(this, 1000)
                return
            }
            Task.updateWaresInfo(1000)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Task.DelayHandler.postDelayed(this, 1000)
        }
    }
}
