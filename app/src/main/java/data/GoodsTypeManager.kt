package data

import app.App
import event.GoodsTypeChageEvent
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import util.Http
import app.log
import event.NetworkErrorEvent
import event.ReplenishFinishEvent
import org.json.JSONArray

object GoodsTypeManager
{
    private val mReplenishGoodsTypeInfoList = ArrayList<GoodsType>() // 需要补入商品的货道
    private val mClearGoodsTypeInfoList = ArrayList<GoodsType>() // 需要清除商品的货道

    fun replenishNumber() = mReplenishGoodsTypeInfoList.size

    fun clearNumber() = mClearGoodsTypeInfoList.size

    fun replenishOfIndex(index: Int) = mReplenishGoodsTypeInfoList[index]

    fun clearOfIndex(index: Int) = mClearGoodsTypeInfoList[index]

    private fun parse(json: JSONObject): GoodsType
    {
        val name = json.optString("goodsName")
        val goodsTypeString = json.optString("cargoData")
        val number = json.optInt("isExist", -1)
        val arrs = goodsTypeString.split("-")
        val goodsType = GoodsTypeInfo(arrs[0].toInt(), arrs[1].toInt(), number)
        return GoodsType(name, goodsType)
    }

    private fun parseQc(jsonObject: JSONObject): GoodsType
    {
        val name = jsonObject.optString("waresName")
        val goodsType = jsonObject.optString("cargoDatan")
        val num = jsonObject.optInt("clearData")
        val isPastdue = jsonObject.optString("isPastdue")
        val arrs = goodsType.split("-")
        val type = GoodsTypeInfo(arrs[0].toInt(), arrs[1].toInt(), num)
        return GoodsType(name, type, isPastdue)
    }

    fun replenishFinishOfAsyncEvent()
    {
        val json = JSONObject()
        json.put("macAddr", App.MacAddress)
        val arr = JSONArray()
        for (info in mReplenishGoodsTypeInfoList) {
            arr.put(info.toJson())
        }
        json.put("replenInfos", arr)
        val content = json.toString()
        log(content)
        val result = Http.replenishFinishOfNetwork(content)
        log(result)
        if (result == "{}" || result.isEmpty()) {
            EventBus.getDefault().post(NetworkErrorEvent())
            return
        }
        WaresInfoManager.updateOfAsyncEvent(result)
        EventBus.getDefault().post(ReplenishFinishEvent())
    }

    fun updateOfAsyncEvent()
    {
        mReplenishGoodsTypeInfoList.clear()
        mClearGoodsTypeInfoList.clear()
        val result = Http.getGoodsTypeOfNetwork()
        log(result)
        if (result == "{}" || result.isEmpty()) {
            EventBus.getDefault().post(NetworkErrorEvent())
            return
        }
        val json = JSONObject(result)
        val dataBR = json.optJSONArray("dataBR")
        if (dataBR != null)
        {
            for (i in 0 until dataBR.length())
            {
                val goodsTypeJson = dataBR.getJSONObject(i)
                val goodsType = parse(goodsTypeJson)
                mReplenishGoodsTypeInfoList.add(goodsType)
            }
        }
        val dataQC = json.optJSONArray("dataQC")
        if (dataQC != null)
        {
            for (i in 0 until dataQC.length())
            {
                val goodsTypeJson = dataQC.getJSONObject(i)
                val goodsType = parseQc(goodsTypeJson)
                mClearGoodsTypeInfoList.add(goodsType)
            }
        }
        EventBus.getDefault().post(GoodsTypeChageEvent())
    }
}


















