package receiver

import activity.MainActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.log


class UpdateApkReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context, intent: Intent)
    {
        when (intent.action)
        {
            Intent.ACTION_PACKAGE_REPLACED -> { // 升级了一个安装包
                onReplaced(context)
            }

            Intent.ACTION_PACKAGE_ADDED -> { // 安装了一个程序
                onAdded(intent)
            }

            Intent.ACTION_PACKAGE_REMOVED -> { // 卸载了一个程序
                onRemove(intent)
            }
        }
    }

    private fun onReplaced(context: Context)
    {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun onAdded(intent: Intent)
    {
        log("安装了:${intent.dataString}")
    }

    private fun onRemove(intent: Intent)
    {
        log("卸载了:${intent.dataString}")
    }
}