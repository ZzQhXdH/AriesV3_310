package service

import android.app.IntentService
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import app.log
import app.resetSystem

class ResetService : IntentService("Reset")
{
    override fun onHandleIntent(intent: Intent)
    {
        log("定时重启")
        resetSystem()
    }
}