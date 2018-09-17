package receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.*


import cn.jpush.android.api.JPushInterface
import data.StatusManager
import org.json.JSONArray
import org.json.JSONObject
import task.*
import util.Logger


// {"id":"status","macAdress":["543400000000"],"param":{"isSBOpen":false,"status":0}}
// {"id":"updateclientAndlower","macAdress":["543400000000"],"param":{"lowerfileUrl":"http:\/\/localhost:88\/bg-uc\/tempfile\/AriesMCU.bin"}}

class ServicePushReceiver : BroadcastReceiver()
{
    companion object
    {
        var JPushConnected = true

        private const val ID_PAY_RESULT = "paysucces"
        private const val ID_PUSH_PRICE = "pushprice"
        private const val ID_TEMPER_SET = "setTemperature"
        private const val ID_STATUS = "status" // 再售, 停售
        private const val ID_RESTART = "restart"
        private const val ID_MCU_UPDATE = "updateclientAndlower" // 客户端 单片机升级
        private const val ID_ADV = "advertise"
    }

    private fun setTemperature(arr: JSONArray)
    {
        val json = arr.optJSONObject(0)
        val max = json.optDouble("endTime", 0.0)
        val min = json.optDouble("startTime", 0.0)
        val time = json.optDouble("more", 0.0) * 60
        StatusManager.instance.set(min.toFloat(), max.toFloat(), time.toInt())
    }

    override fun onReceive(context: Context, intent: Intent)
    {
        when (intent.action)
        {
            JPushInterface.ACTION_MESSAGE_RECEIVED -> onJPush(intent)

            JPushInterface.ACTION_CONNECTION_CHANGE -> onJPushConnectChange(intent)
        }
    }

    private fun onJPushConnectChange(intent: Intent)
    {
        val bundle = intent.extras
        JPushConnected = bundle.getBoolean(JPushInterface.EXTRA_CONNECTION_CHANGE, false)
        val msg = "极光推送连接状态:$JPushConnected"
        log(msg)
        Logger.toFile(msg, "推送状态")
    }

    private fun onJPush(intent: Intent)
    {
        val arg = intent.extras
        val msg = arg.getString(JPushInterface.EXTRA_MESSAGE)
        log("极光推送:$msg")

        try {
            val json = JSONObject(msg)
            val id = json.optString("id")

            when (id)
            {
                ID_PAY_RESULT -> {
                    val param = json.optJSONObject("param")
                    Task.DelayHandler.post(PayResultTask(param))
                }

                ID_PUSH_PRICE -> {
                    Task.DelayHandler.post(UpdateWaresTask(1000))
                }

                ID_TEMPER_SET -> {
                    val param = json.optJSONArray("param")
                    setTemperature(param)
                }

                ID_RESTART -> resetSystem()

                ID_STATUS -> {
                   // val param = json.optJSONObject("param")
                    Task.DelayHandler.post(UpdateSellStatusTask())
                }

                ID_MCU_UPDATE -> {
                    val param = json.optJSONObject("param")
                    Task.DelayHandler.post(UpdateVersionTask(param))
                }

                ID_ADV -> {
                    //Task.DelayHandler.post(UpdateAdvTask())
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}