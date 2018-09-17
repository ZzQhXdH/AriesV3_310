package task

import event.QrCodeEvent
import org.greenrobot.eventbus.EventBus
import util.Http
import util.QRCodeUtil

class GetQrCodeImageTask : Runnable
{
    override fun run()
    {
        try {
            val content = Http.getQrCodeContentOfNetwork()
            val bm = QRCodeUtil.create(content)
            EventBus.getDefault().post(QrCodeEvent(QrCodeEvent.OK, bm))
        } catch (e: Exception) {
            e.printStackTrace()
            EventBus.getDefault().post(QrCodeEvent(QrCodeEvent.NETWORK_ERROR))
        }
    }
}