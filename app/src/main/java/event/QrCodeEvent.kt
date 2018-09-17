package event

import android.graphics.Bitmap

class QrCodeEvent(val state: Int, val bm: Bitmap? = null)
{
    companion object
    {
        const val NETWORK_ERROR = -1
        const val OK = 0
    }
}