package protocol

import event.DeliverResultEvent
import exception.ResultParseException


open class BaseResult(protected val mRawData: ByteArray)
{
    companion object
    {
        private const val ACTION_DELIVER_RESULT = 0x86

        fun parse(rawData: ByteArray): BaseResult
        {
            if (rawData[1] != rawData.size.toByte()) {
                throw ResultParseException()
            }
            val c = rawData[rawData.size - 2].toInt()
            var check = 0
            for (i in 3 until (rawData.size - 2) ) {
                check = rawData[i].toInt() xor check
            }
            if (c != check) {
                throw ResultParseException()
            }
            val action = rawData[2].toInt() and 0xFF

            return when (action)
            {
                ACTION_DELIVER_RESULT -> DeliverResultEvent(rawData)
                else -> BaseResult(rawData)
            }
        }
    }

    protected val action = mRawData[2].toInt()

    fun arg(index: Int) = mRawData[2 + index].toInt()
}