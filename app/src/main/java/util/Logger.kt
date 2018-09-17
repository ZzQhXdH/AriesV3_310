package util

import android.os.Environment
import android.util.Log
import java.io.*
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
}













