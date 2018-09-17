package event

class VersionProgressChangeEvent(val action: Int, val progress: Int, val maxValue: Int)
{
    companion object
    {
        const val ACTION_START = 0x01
        const val ACTION_END = 0x02
        const val ACTION_PROGRESS = 0x03
    }
}