package service

import android.app.IntentService
import android.content.Intent
import app.Task
import app.log
import app.resetSystem
import util.Logger


class ResetService : IntentService("Reset")
{
    override fun onHandleIntent(intent: Intent)
    {
        log("定时重启")
        Logger.updateResetMessage("定时重启")
        Task.DelayHandler.post(::resetSystem)
    }
}