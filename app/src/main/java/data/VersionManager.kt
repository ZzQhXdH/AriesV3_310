package data

import activity.DebugActivity
import activity.HomeActivity
import app.installApk
import app.log

object VersionManager
{
    var UpdateFlag = false

    var ApkPath = ""

    fun updateApk(path: String)
    {
        UpdateFlag = true
        ApkPath = path
        if (DebugActivity.isShow || HomeActivity.isShow) {
            log("正忙 稍后更新")
            return
        }
        installApk(path)
    }

    fun checkUpdate()
    {
        if (UpdateFlag && ApkPath.isNotEmpty())
        {
            log("开始更新apk")
            UpdateFlag = false
            installApk(ApkPath)
            ApkPath = ""
        }
    }

}