package util

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.util.Log
import app.App
import app.Task
import app.log
import data.WaresInfoManager
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object Logger {

    private const val LOG_FILE_NAME = "Logger.txt"
    private var mOut: PrintStream? = null
    private val mDateFormat = SimpleDateFormat("yyyy-MM-dd hh-mm-ss")

    init {
        val dir = Environment.getExternalStorageDirectory()
        val file = File(dir, LOG_FILE_NAME)
        if (!file.exists()) {
            file.createNewFile()
        }
        val fw = FileOutputStream(file, true)
        mOut = PrintStream(fw, true)
    }

    fun toFile(msg: String, tag: String)
    {
        val time = mDateFormat.format(Calendar.getInstance().time)
        mOut!!.append(time).append(":").appendln(tag).appendln(msg)
    }

    fun updateException(msg: String)
    {
        val task = OneNetDebug("Exception", msg)
        Task.DelayHandler.post(task)
    }

    fun updateCpuInto()
    {
//        try {
//            val file = File("/proc/cpuinfo")
//            val s = file.readText()
//            Task.DelayHandler.post(OneNetDebug("CpuInfo", s))
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    fun updateMemoryInfo()
    {

        try {
            val am = App.AppContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            val json = JSONObject()
            json.put("availMem", mi.availMem / 1024f / 1024f)
            json.put("lowMemory", mi.lowMemory)
            json.put("threshold", mi.threshold / 1024f / 1024f)
            json.put("totalMem", mi.totalMem / 1024f / 1024f)
            Task.DelayHandler.post(OneNetDebug("MemoryInfo", json.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateVersion()
    {
        try {
            val info = App.AppContext.packageManager.getPackageInfo(App.AppContext.packageName, 0)
            val json = JSONObject()
            json.put("versionCode", info.versionCode)
            json.put("versionName", info.versionName)
            Task.DelayHandler.post(OneNetDebug("VersionInfo", json.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateResetMessage(msg: String)
    {
        try {
            Task.DelayHandler.post(OneNetDebug("Reset", msg))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


class OneNetDebug(val id: String, val msg: String) : Runnable
{
    override fun run()
    {
        val json = JSONObject()
        json.put("${App.MacAddress}-$id", msg)
        log(msg, "上传日志")
        try {
            Http.updateLogger(json.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}










