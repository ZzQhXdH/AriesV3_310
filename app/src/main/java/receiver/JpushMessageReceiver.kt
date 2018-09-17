package receiver

import android.content.Context
import app.log
import cn.jpush.android.api.JPushMessage
import cn.jpush.android.service.JPushMessageReceiver


class JpushMessageReceiver : JPushMessageReceiver()
{
    override fun onCheckTagOperatorResult(p0: Context?, p1: JPushMessage?)
    {
        super.onCheckTagOperatorResult(p0, p1)
        log("onCheckTagOperatorResult")
    }

    override fun onTagOperatorResult(p0: Context?, p1: JPushMessage?)
    {
        super.onTagOperatorResult(p0, p1)
        log("onTagOperatorResult")
    }

    override fun onMobileNumberOperatorResult(p0: Context?, p1: JPushMessage?)
    {
        super.onMobileNumberOperatorResult(p0, p1)
        log("onMobileNumberOperatorResult")
    }

    override fun onAliasOperatorResult(p0: Context?, p1: JPushMessage?)
    {
        super.onAliasOperatorResult(p0, p1)
        log("onAliasOperatorResult")
    }
}