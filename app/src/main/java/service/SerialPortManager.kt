package service

import android_serialport_api.SerialPort
import app.log
import util.StateSaveManager
import util.toHexString
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class SerialPortManager
{
    companion object
    {
        val instance: SerialPortManager by lazy { SerialPortManager() }
       // private const val DEFAULT_PATH = "/dev/ttyS3"
        private const val DEFAULT_PATH = "/dev/ttymxc2"
        private const val PATH_KEY = "path.key.serial_port"
    }

    var SerialPortPath = DEFAULT_PATH

    private var mPort: SerialPort? = null
    private var mOut: OutputStream? = null
    private var mIn: InputStream? = null

    init {
        SerialPortPath = StateSaveManager.readString(PATH_KEY)
        if (SerialPortPath.isEmpty()) {
            SerialPortPath = DEFAULT_PATH
        }
    }

    fun setPath(path: String) {
        SerialPortPath = path
        StateSaveManager.saveString(PATH_KEY, path)
    }

    fun openOfTest(path: String): Boolean
    {
        val file = File(path)
        try {
            val port = SerialPort(file, 9600, 0)
            port.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun open()
    {
        val file = File(SerialPortPath)
        mPort = SerialPort(file, 9600, 0)
        mOut = mPort!!.outputStream
        mIn = mPort!!.inputStream
    }

    fun close()
    {
        mPort?.close()
    }

    fun write(byteArray: ByteArray)
    {
        log(byteArray.toHexString(), "串口发送")
        mOut?.write(byteArray)
    }

    fun read(byteArray: ByteArray): Int
    {
        return mIn?.read(byteArray) ?: 0
    }

}