package data

import event.WaresChangeEvent
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import util.Http
import app.log

object WaresInfoManager
{
    private val mWaresInfoList = ArrayList<WaresInfo>()

    var SellStatus = false

    var SelectIndex = 0 // 当前用户选择的商品

    fun getSelectWaresInfo() = mWaresInfoList[SelectIndex]

    var CurrentOrder = "" // 当前用户选择的商品的订单号

    var MachCode = "" // 机器编号

    var VipCode = "" // VIP码

    fun getWaresInfo(index: Int) = mWaresInfoList[index]

    fun getWaresInfoNumber() = mWaresInfoList.size

    fun clearError()
    {
        mWaresInfoList.forEach { it.clearErrorFlag() }
    }

    private fun parseGoodsType(json: JSONObject): GoodsTypeInfo
    {
        val type = json.optString("cargoData")
        val number = json.optInt("goodsNum")
        val types = type.split("-")
        return GoodsTypeInfo(types[0].toInt(), types[1].toInt(), number)
    }

    private fun parse(json: JSONObject): WaresInfo
    {
        val id = json.optString("WaresId")
        val name = json.optString("WaresName")
        val price = json.optString("WaresPrice")
        val min = json.optString("WaresImage1")
        val max = json.optString("WaresImage2")
        val arr = json.optJSONArray("GoodsType")

        val goodsTypes = Array(arr.length()) {
            val json = arr.getJSONObject(it)
            parseGoodsType(json)
        }

        return WaresInfo(name, id, price, goodsTypes, min, max)
    }

    fun updateOfAsyncEvent(result: String)
    {
        mWaresInfoList.clear()
        log(result)
        if (result == "no_replen_finished" || result.isEmpty() || result == "{}")
        {
            EventBus.getDefault().post(WaresChangeEvent(WaresChangeEvent.NO_REPLEN_FINISH))
            return
        }
        val json = JSONObject(result)
        MachCode = json.optString("MachCode")
        if (MachCode.isEmpty()) {
            MachCode = json.optString("machCode")
        }
        val jsonArray = json.optJSONArray("arr")
        for (i in 0 until jsonArray.length())
        {
            val tmp = jsonArray.getJSONObject(i)
            val obj = parse(tmp)
            mWaresInfoList.add(obj)
        }
        EventBus.getDefault().post(WaresChangeEvent(WaresChangeEvent.OK))
    }

    fun updateOfAsyncEvent()
    {
        mWaresInfoList.clear()
        val result = Http.getWaresOfNetwork()
        log(result)
        if (result == "no_replen_finished" || result.isEmpty() || result == "{}")
        {
            EventBus.getDefault().post(WaresChangeEvent(WaresChangeEvent.NO_REPLEN_FINISH))
            return
        }
        val json = JSONObject(result)
        MachCode = json.optString("MachCode")
        if (MachCode.isEmpty()) {
            MachCode = json.optString("machCode")
        }
        val jsonArray = json.optJSONArray("arr")
        for (i in 0 until jsonArray.length())
        {
            val tmp = jsonArray.getJSONObject(i)
            val obj = parse(tmp)
            mWaresInfoList.add(obj)
        }
        EventBus.getDefault().post(WaresChangeEvent(WaresChangeEvent.OK))
    }
}