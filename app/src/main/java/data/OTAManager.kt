package data


import activity.DebugActivity
import activity.HomeActivity
import app.Task
import app.log
import event.VersionProgressChangeEvent
import org.greenrobot.eventbus.EventBus
import protocol.BaseProtocol
import service.SerialPortManager
import util.Http
import util.argInt
import util.toHexString

class OTAManager
{
    companion object
    {
        val instance: OTAManager by lazy { OTAManager() }
    }

    private var mUpdateUrl = ""
    private var mUpdateFlag = false
    var isUpdate = false
    private var mUpdateContent: ByteArray? = null
    private var mAlreadySize = 0
    private val mBuffer = ByteArray(128)
    private var mNextId = 0

    private fun next()
    {
        log("$mNextId-$mAlreadySize")

        EventBus.getDefault().post(VersionProgressChangeEvent(VersionProgressChangeEvent.ACTION_PROGRESS,
                mNextId,
                0))

        val length = mUpdateContent!!.size - mAlreadySize
        val len = (if (length > 128) 128 else length)

        System.arraycopy(mUpdateContent, mAlreadySize, mBuffer, 0, len)

        mAlreadySize += len

        val bytes = BaseProtocol(0x0e).append2(mNextId).appendAll(mBuffer, len).build()
        SerialPortManager.instance.write(bytes)

        if (mAlreadySize == mUpdateContent!!.size) {
            log("升级完成")
            mUpdateContent = null
            EventBus.getDefault().post(VersionProgressChangeEvent(VersionProgressChangeEvent.ACTION_END,
                    0,
                    0))
            isUpdate = false
            return
        }

        mNextId ++
    }

    fun onNotifyResult(byteArray: ByteArray)
    {
        if (byteArray.argInt(1) == 0x00)
        {
            log("开始升级下位机")
            mAlreadySize = 0
            mNextId = 0
            next()
        }
    }

    fun onNext(byteArray: ByteArray)
    {
        if (byteArray.argInt(3) == 0x00)
        {
            if (mAlreadySize == mUpdateContent!!.size) {
                log("升级完成")
                EventBus.getDefault().post(VersionProgressChangeEvent(VersionProgressChangeEvent.ACTION_END,
                        0,
                        0))
                isUpdate = false
                return
            }
            next()
        }
    }

    fun updateOfCheck()
    {
        if ((!mUpdateFlag) || mUpdateUrl.isEmpty()) {
            return
        }
        mUpdateFlag = false
        Task.DelayHandler.post(::onUpdate)
    }

    fun update(url: String, version: String)
    {
        if (isUpdate) {
            return
        }

        if (HomeActivity.isShow || DebugActivity.isShow)
        {
            mUpdateUrl = url
            mUpdateFlag = true
            return
        }

        isUpdate = true

        mUpdateContent = Http.downLoadOTA(url)

        if (mUpdateContent!!.size < 1024)
        {
            isUpdate = false
            mUpdateUrl = ""
            log("下载文件错误")
            return
        }

        val size = mUpdateContent!!.size

        EventBus.getDefault().post(VersionProgressChangeEvent(VersionProgressChangeEvent.ACTION_START,
                0,
                (size + 127) / 128))

        log("下载升级文件成功,总共:${size}字节")

        mAlreadySize = 0

        val bytes = BaseProtocol(0x0D).append4(size).build()

        SerialPortManager.instance.write(bytes)
    }

    private fun onUpdate()
    {
        if (isUpdate) {
            return
        }

        isUpdate = true

        mUpdateContent = Http.downLoadOTA(mUpdateUrl)

        val size = mUpdateContent!!.size

        EventBus.getDefault().post(VersionProgressChangeEvent(VersionProgressChangeEvent.ACTION_START,
                0,
                (size + 127) / 128))

        log("下载升级文件成功,总共:${size}字节")

        mAlreadySize = 0

        val bytes = BaseProtocol(0x0D).append4(size).build()

        SerialPortManager.instance.write(bytes)
    }
}