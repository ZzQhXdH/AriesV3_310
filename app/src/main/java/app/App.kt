package app


import activity.DebugActivity
import activity.HomeActivity
import activity.MainActivity
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import cn.jpush.android.api.JPushInterface

import com.hontech.icecreamcustomclient.R
import data.StatusManager
import service.SerialPortService
import task.UpdateStatusTask
import task.UpdateTemperatureTask
import util.Http
import util.Logger
import java.io.PrintWriter
import java.net.NetworkInterface
import android.support.v4.app.AlarmManagerCompat.setExact
import android.os.Build
import android.support.v4.app.AlarmManagerCompat.setAlarmClock
import android.text.format.Time
import service.ResetService
import task.ResetTask
import util.HDMIManager
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*

class App: Application()
{
    companion object
    {
        private const val DEFAULT_MAC = "50:ff:99:30:23:19"

        const val PROCESS_NAME = "com.hontech.icecreamcustomclient"

        lateinit var AppContext: Context
            private set

        var ResetFlag = true

        var ResetFlagAll = true

        private val ActivityList = ArrayList<Activity>()

        val MacAddress = getLocalEthernetMacAddress() ?: DEFAULT_MAC

        fun addActivity(activity: Activity) = ActivityList.add(activity)

        fun removeActivity(activity: Activity) = ActivityList.remove(activity)

        fun finishAllActivity()
        {
            for (activity in ActivityList)
            {
                activity.finish()
            }
        }
    }

    private fun onException(thread: Thread, throwable: Throwable)
    {
        throwable.printStackTrace()
        val out = ByteArrayOutputStream()
        val ps = PrintStream(out)
        throwable.printStackTrace(ps)
        ps.flush()
        ps.close()
        out.close()
        val msg = out.toString("utf-8")
        Logger.toFile(msg, "异常线程:${thread.name}-----")
        Logger.updateException(msg)
        Task.DelayHandler.post(::resetApp)
    }

    override fun onCreate()
    {
        super.onCreate()
        val pn = getProcessName(this)
        log("进程名称:$pn")
        if (pn != PROCESS_NAME) {
            return
        }

        AppContext = applicationContext
        Thread.setDefaultUncaughtExceptionHandler(::onException)
        initJPush()
        SerialPortService.start(this)

        log("Mac地址:$MacAddress")

        Task.DelayHandler.post(::restart) // 重启标志
        Task.DelayHandler.post(UpdateTemperatureTask()) // 获取温度阈值
        Task.DelayHandler.postDelayed(UpdateStatusTask(), 30000) // 更新状态
        setResetSystem() // 设置重启
        Logger.updateVersion()
    }
}

fun restart()
{
    try
    {
        val json = StatusManager.instance.toJson2()
        Http.updateStatus(json)
        log("启动")
    }
    catch (e: Exception)
    {
        e.printStackTrace()
    }
}

fun initJPush()
{
    log("初始化JPUSH")
    val tagSet = HashSet<String>()
    tagSet.add(App.MacAddress.replace(":", ""))
    JPushInterface.setTags(App.AppContext, 0, tagSet)
    JPushInterface.setDebugMode(false)
    JPushInterface.init(App.AppContext)
}

inline fun log(msg: String, tag: String = "DEBUG")
{
    Log.d(tag, msg)
}

fun getLocalEthernetMacAddress(): String?
{
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        interfaces.iterator().forEach {
            if (it.name == "eth0")
            {
                return it.hardwareAddress.toMacAddress()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun ByteArray.toMacAddress(): String
{
    val sb = StringBuilder()
    this.forEachIndexed { index, byte->
        if (index == (size - 1))
        {
            sb.append(String.format("%02x", byte.toInt() and 0xFF))
        }
        else
        {
            sb.append(String.format("%02x:", byte.toInt() and 0xFF))
        }
    }
    return sb.toString()
}

fun resetSystem()
{
    val s = arrayOf("su", "-c", "reboot")
    try {
        Runtime.getRuntime().exec(s).waitFor()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

//fun closeSystem()
//{
//    val s = arrayOf("su", "-c", "reboot -p")
//    try {
//        Runtime.getRuntime().exec(s).waitFor()
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
//}


fun installApk(path: String)
{
    log("开始安装:$path")
    val prcess = Runtime.getRuntime().exec("su")
    val pw = PrintWriter(prcess.outputStream)
    pw.println("pm install -r $path")
    pw.flush()
    pw.close()
    val v = prcess.waitFor()
    log("安装返回值:$v")
}

fun checkApplication()
{
    log("MainActivity:${MainActivity.isShow}")
    log("HomeActivity:${HomeActivity.isShow}")
    log("DebugActivity:${DebugActivity.isShow}")

    if ((!MainActivity.isShow) && (!HomeActivity.isShow) && (!DebugActivity.isShow))
    {
        resetApp()
    }
}

fun resetApp()
{
    log("开始重启App")

    val i = Intent(App.AppContext, MainActivity::class.java)
    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    val pi = PendingIntent.getActivity(App.AppContext, 0, i, 0)
    val am = App.AppContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500, pi)

    App.finishAllActivity()
    android.os.Process.killProcess(android.os.Process.myPid())
}

fun getVersionCode(): Int
{
    val pi = App.AppContext.packageManager.getPackageInfo(App.AppContext.packageName, 0)
    return pi.versionCode
}

fun getVersion(): String
{
    val pi = App.AppContext.packageManager.getPackageInfo(App.AppContext.packageName, 0)
    return pi.versionName
}

/**
 * 获取进程名称
 */
fun getProcessName(context: Context = App.AppContext): String
{

    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    val runningApps = am.getRunningAppProcesses()

    for (proInfo in runningApps)
    {
        if (proInfo.pid == android.os.Process.myPid())
        {
            return proInfo.processName
        }
    }
    return ""
}

fun setResetSystem()
{
    val target = Calendar.getInstance()
    target.set(Calendar.HOUR_OF_DAY, 3)
    target.set(Calendar.MINUTE, 0)
    target.set(Calendar.SECOND, 0)
    val targetTime = target.timeInMillis

    val currentTime = Calendar.getInstance().timeInMillis
    val am = App.AppContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(App.AppContext, ResetService::class.java)
    val pi = PendingIntent.getService(App.AppContext, 0, intent, 0)

    val resetTime = if (targetTime > currentTime) targetTime else targetTime + ResetTask.DAY_TIME
    log("重启时间：$resetTime")

    am.set(AlarmManager.RTC_WAKEUP, resetTime, pi)
}
