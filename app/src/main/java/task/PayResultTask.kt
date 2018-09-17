package task

import activity.HomeActivity
import app.Task
import app.log
import data.WaresInfoManager
import event.PayStatusEvent

import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import popup.DeliverPopupWindow
import util.Http


class PayResultTask(private val param: JSONObject) : Runnable
{
    override fun run()
    {
        try {
            val order = param.optString("out_trade_no")

            if (isGoodsTypeExist(order)) { // 已经出货过了
                log("已经出货")
                return
            }

            if (isRefunded(order)) { // 已经退款
                log("已经退款")
                return
            }

            if (order == WaresInfoManager.CurrentOrder)
            { // 出货

                if (DeliverPopupWindow.isShow) {
                    log("已经正在出货")
                    return
                }

                if (HomeActivity.isShow) {
                    EventBus.getDefault().post(PayStatusEvent())
                } else {
                    Http.refundForResult("支付异常", order, "")
                }

            } else {
                Http.refundForResult("支付异常", order, "")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isRefunded(order: String): Boolean
    {
        val ret = Http.queryRefund(order)
        log("退款查询结果:$ret")
        val json = JSONObject(ret)
        val info = json.optInt("result", 0)
        return info > 0
    }

    private fun isGoodsTypeExist(order: String): Boolean
    {
        val ret = Http.queryOrder(order)
        log("货道查询结果:$ret")
        val json = JSONObject(ret)
        val info = json.optString("cargolane", "")
        return !info.isEmpty()
    }

}