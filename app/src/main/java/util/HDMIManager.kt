package util

import app.Task
import app.log
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.*


object HDMIManager : Runnable
{
    private const val STATE = "/sys/class/switch/hdmi/state"
    private const val CONNECT = "/sys/class/display/HDMI/connect"
    private const val EDIDREAD = "/sys/class/display/HDMI/edidread"
    private const val ENABLE = "/sys/class/display/HDMI/enable"
    private var state = true

    fun getState(): Int
    {
        val file = File(STATE)
        if (!file.exists()) {
            log("State文件不存在")
            return -1
        }
        return try {
            val scanner = Scanner(file)
            val v = scanner.nextInt()
            log("State:$v")
            scanner.close()
            v
        } catch (e: Exception) {
            e.printStackTrace()
            -2
        }
    }

    fun getConnect(): Int
    {
        val file = File(CONNECT)
        if (!file.exists()) {
            log("Connect文件不存在")
            return -1
        }
        return try {
            val scanner = Scanner(file)
            val v = scanner.nextInt()
            log("Connect:$v")
            scanner.close()
            v
        } catch (e: Exception) {
            e.printStackTrace()
            -2
        }
    }

    fun getEdidRead(): Int
    {
        val file = File(EDIDREAD)
        if (!file.exists()) {
            log("EdidRead文件不存在")
            return -1
        }
        return try {
            val scanner = Scanner(file)
            val v = scanner.nextInt()
            log("EdidRead:$v")
            scanner.close()
            v
        } catch (e: Exception) {
            e.printStackTrace()
            -2
        }
    }

    fun enable()
    {
        val file = File(ENABLE)
        if (!file.exists()) {
            log("ENABLE文件不存在")
            return
        }
        try {
            val write = FileWriter(file)
            write.write("1")
            write.flush()
            write.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    fun disable()
    {
        val file = File(ENABLE)
        if (!file.exists()) {
            log("ENABLE文件不存在")
            return
        }
        try {
            val write = FileWriter(file)
            write.write("0")
            write.flush()
            write.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }

    override fun run()
    {
        getState()
        getConnect()
        getEdidRead()
    }
}
