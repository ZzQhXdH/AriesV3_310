package event

class WaresChangeEvent(val status: Int)
{
    companion object
    {
        const val OK = 0
        const val NO_REPLEN_FINISH = 1
    }
}