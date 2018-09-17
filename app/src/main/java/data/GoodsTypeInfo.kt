package data

import app.log
import util.Logger
import util.StateSaveManager

class GoodsTypeInfo(val row: Int, val col: Int, var number: Int, var practialNumber: Int = number)
{
    var faultFlag = 0 // 故障标志 0: 正常

    init {
        val ret = StateSaveManager.readInt(toString())
        if ( ret < 0) { // 首次安装
            StateSaveManager.saveInt(toString(), 0)
        } else {
            faultFlag = ret
        }

        if (isFault())
        {
            val msg = "${toString()}:已经故障:$faultFlag"
            log(msg)
            Logger.toFile(msg, "货道故障")
        }
    }

    inline fun saveErrorFlag()
    {
        StateSaveManager.saveInt(name(), faultFlag)
    }

    inline fun isEmpty() = (number <= 0)

    inline fun isFault() = (faultFlag >= 2)

    override fun toString(): String
    {
        return "$row-$col"
    }

    inline fun name() = toString()

    fun toMessage(): String
    {
        return "$row-$col=>$number:$faultFlag"
    }
}