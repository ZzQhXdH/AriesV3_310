package event

import protocol.BaseResult

class DeliverResultEvent(rawData: ByteArray) : BaseResult(rawData)
{
    val isStatus = rawData.size == 8
    val row = mRawData[3].toInt()
    val col = mRawData[4].toInt()
    val state = mRawData[5].toInt()
}