package data


import app.log
import protocol.BaseProtocol

class WaresInfo(val name: String,
                val id: String,
                val price: String,
                val goodsTypes: Array<GoodsTypeInfo>,
                val minImagePath: String,
                val maxImagePath: String)
{
    val amount: Int // 库存总数

    init {
        var s = 0
        goodsTypes.forEach {
            s += if (it.isFault()) 0 else it.number
        }
        amount = s
    }

    inline fun isEmpty() = (amount <= 0)

    fun allGoodsTypes(): ArrayList<GoodsTypeInfo>
    {
        val normalList = ArrayList<GoodsTypeInfo>()
        goodsTypes.forEach {

            if (!it.isEmpty())
            {
                if (!it.isFault())
                    normalList.add(it)
            }
        }
        return normalList
    }

    fun createDeliverByteArray(): ByteArray
    {
        val protocol = BaseProtocol(0x06)
        val list = allGoodsTypes()
        protocol.append2(10 * 1000)
        for (info in list)
        {
            protocol.append1(info.row)
            protocol.append1(info.col)
        }
        return protocol.build()
    }

    fun setErrorFlag(row: Int, col: Int)
    {
        val tmp = "$row-$col"

        goodsTypes.forEach {

            if (it.name() == tmp)
            {
                it.faultFlag ++
                it.saveErrorFlag()
                return
            }
        }
    }

    fun clearErrorFlag()
    {
        goodsTypes.forEach {

            it.faultFlag = 0
            it.saveErrorFlag()
        }
    }

    fun logMessage()
    {
        goodsTypes.forEach {
            log(it.toMessage())
        }
    }

}
