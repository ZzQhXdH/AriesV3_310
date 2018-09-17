package task


import app.log
import data.OTAManager
import data.VersionManager

import org.json.JSONObject
import util.Http


class UpdateVersionTask(private val param: JSONObject) : Runnable
{
    override fun run()
    {
        try {

            val url = param.optString("lowerfileUrl")

            if (url.isNotEmpty()) {
                log("开始下载更新MCU文件")
                OTAManager.instance.update(url, "")
            }

            val curl = param.optString("clientfileUrl")

            if (curl.isNotEmpty()) {

                log("开始下载更新文件")
                val file = Http.downLoadFile("client.apk", curl)
                log("更新文件下载完成")
                VersionManager.updateApk(file.path)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}