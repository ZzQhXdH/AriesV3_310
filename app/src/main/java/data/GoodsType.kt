package data

import org.json.JSONObject

class GoodsType(val name: String, val info: GoodsTypeInfo, val isPastdue: String = "否")
{
    fun toJson(): JSONObject
    {
        val json = JSONObject()
        json.put("goodsName", name) // 商品名称
        json.put("cargoData", info.name()) // 货道名称
        json.put("isExist", info.practialNumber) // 该货道对应的数量
        return json
    }
}