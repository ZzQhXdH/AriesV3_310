package protocol

class BaseProtocol(private val action: Int)
{
    private val mBuffer = ByteArray(180)
    private var nIndex = 0

    fun append1(b: Int): BaseProtocol
    {
        mBuffer[nIndex] = b.toByte()
        nIndex ++
        return this
    }

    fun append2(b: Int): BaseProtocol
    {
        mBuffer[nIndex] = ((b shr 7) and 0x7F).toByte()
        nIndex ++
        mBuffer[nIndex] = (b and 0x7F).toByte()
        nIndex ++
        return this
    }

    fun append4(b: Int): BaseProtocol
    {
        mBuffer[nIndex] = ((b shr 21) and 0x7F).toByte()
        nIndex ++
        mBuffer[nIndex] = ((b shr 14) and 0x7F).toByte()
        nIndex ++
        mBuffer[nIndex] = ((b shr 7) and 0x7F).toByte()
        nIndex ++
        mBuffer[nIndex] = (b and 0x7F).toByte()
        nIndex ++
        return this
    }

    fun appendAll(byteArray: ByteArray): BaseProtocol
    {
        System.arraycopy(byteArray, 0, mBuffer, nIndex, byteArray.size)
        nIndex += byteArray.size
        return this
    }

    fun appendAll(byteArray: ByteArray, size: Int): BaseProtocol
    {
        System.arraycopy(byteArray, 0, mBuffer, nIndex, size)
        nIndex += size
        return this
    }

    fun build(): ByteArray
    {
        val byteArray = ByteArray(nIndex + 5)
        byteArray[0] = 0xE1.toByte()
        byteArray[1] = (nIndex + 5).toByte()
        byteArray[2] = action.toByte()
        var c = 0
        for (i in 0 until nIndex)
        {
            c = c xor mBuffer[i].toInt()
            byteArray[3 + i] = mBuffer[i]
        }
        byteArray[3 + nIndex] = c.toByte()
        byteArray[4 + nIndex] = 0xEF.toByte()

        return byteArray
    }
}