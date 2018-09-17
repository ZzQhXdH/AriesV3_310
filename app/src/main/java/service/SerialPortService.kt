package service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import app.log
import data.OTAManager
import data.StatusManager
import event.DeliverResultEvent
import org.greenrobot.eventbus.EventBus
import util.*


class SerialPortService : IntentService("SerialPort")
{
    companion object
    {
        private var RunFlag = true

        fun start(context: Context)
        {
            RunFlag = true
            val i = Intent(context, SerialPortService::class.java)
            context.startService(i)
        }

        fun stop() {
            RunFlag = false
        }
    }

    private val mReceiverBuffer = ByteArray(64)
    private var mReceiverIndex = 0

    override fun onHandleIntent(intent: Intent)
    {
        try {
            SerialPortManager.instance.open()
        } catch (e: Exception) {
            e.printStackTrace()
            log("串口打开失败")
            return
        }

        log("串口打开成功")
        val buffer = ByteArray(64)
        while (RunFlag)
        {
            val len = SerialPortManager.instance.read(buffer)

            for (i in 0 until len)
            {
                onReceiver(buffer[i])
            }
        }
        SerialPortManager.instance.close()
    }

    private fun onReceiver(byte: Byte)
    {
        if ((mReceiverIndex == 0) && (byte == 0xE1.toByte())) {
            mReceiverBuffer[mReceiverIndex] = byte
            mReceiverIndex ++
            return
        }

        if ((mReceiverIndex > 0) && (mReceiverIndex < mReceiverBuffer.size))
        {
            mReceiverBuffer[mReceiverIndex] = byte
            mReceiverIndex ++
            if (byte == 0xEF.toByte()) {
                onReceiverFinish()
                mReceiverIndex = 0
            }
        }
    }

    private fun onReceiverFinish()
    {
        val buffer = ByteArray(mReceiverIndex)

        System.arraycopy(mReceiverBuffer, 0, buffer, 0, mReceiverIndex)

        val tmp = buffer.toHexString()
        log(tmp, "串口接收")

        if (buffer.isCorrectOfResult() != 0) {
            log("数据解析错误")
            return
        }

        when (buffer.action())
        {
            0x81 -> {} // 机械手校准返回
            0x82 -> {} // 机械手控制返回
            0x83 -> {} // 冰箱门控制返回
            0x84 -> {} // 升降电机控制返回
            0x85 -> {} // 空压机控制返回
            0x86 -> { EventBus.getDefault().post(DeliverResultEvent(buffer)) } // 出货返回
            0x87 -> {} // 设置货道位置返回
            0x88 -> { StatusManager.instance.updateStatus(buffer) } // 状态
            0x89 -> {} // 初始化
            0x8A -> {} // 测试机械手
            0x8B -> {} // 读取货道
            0x8C -> {} // 一键设置货道
            0x8D -> { OTAManager.instance.onNotifyResult(buffer) } // OTA升级通知
            0x8E -> { OTAManager.instance.onNext(buffer) } // OTA升级包
            0x6F -> { if (buffer.argInt(1) != 0) log("数据受到干扰") }
        }
    }

}