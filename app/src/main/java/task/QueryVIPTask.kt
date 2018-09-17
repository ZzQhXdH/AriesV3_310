package task

import app.log
import data.WaresInfoManager
import event.BarCodeScannerEvent
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import util.Http

class QueryVIPTask(private val code: String) : Runnable
{
    override fun run()
    {
        try {
            val list = code.split("&")

            if (list.size != 2)
            {
                log("二维码错误")
                return
            }

            val count = list[1].toInt()

            if (list[0].length <= count)
            {
                log("二维码格式错误")
                return
            }

            val vipCode = list[0].substring(0 until count)

            log("vipCode:$vipCode")

            val info = WaresInfoManager.getSelectWaresInfo()

            val ret = Http.getVIPStatus(vipCode, info.price)

            log(ret)

            val json = JSONObject(ret)
            val result = json.optInt("result", 0)
            val order = json.optString("order", "")
            if ((result == 1) && order.isNotEmpty())
            {
                WaresInfoManager.VipCode = vipCode
                EventBus.getDefault().post(BarCodeScannerEvent(order))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}