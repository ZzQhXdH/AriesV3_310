package event

class SellChangeEvent(val status: Int)
{
    companion object
    {
        const val START = 1
        const val STOP = 0
    }
}