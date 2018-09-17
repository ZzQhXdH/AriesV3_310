package data

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import app.App
import app.Task
import app.log
import event.DoorStatusChangeEvent
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import popup.DeliverPopupWindow
import util.Http

import util.argInt

// {"ariesFridge":"normal","ariesPickMoto":"normal","ariesRobotArm1":"normal","ariesRobotArm2":"normal","ariesTemperature":"-23.875","ariesDoorstatus":"close","ariesRSSI":0,"troubleTemperature":"false","macAddr":"50:ff:99:30:23:19","trouble":"false"}

class StatusManager : PhoneStateListener()
{
    companion object
    {
        val instance: StatusManager by lazy { StatusManager() }
    }

    private var mDoorCloseStatus = true // 门关闭

    private var mTemp = "" // 温度
    private var nMinTemp = 0f // 最小
    private var nMaxTemp = 0f // 最大
    private var nTimeOut = Int.MAX_VALUE // 持续时间
    private var mState = 0
    private var mSignalStrength = 0
    private var mFaultCount = 0
    private var mTemperatureFaultFlag = false

    private val mTelephonyManager = App.AppContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    init {
        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        log("开始监听信号强度")
    }

    fun isEmpty() = mTemp.isEmpty()

    fun set(min: Float, max: Float, time: Int)
    {
        nMinTemp = min
        nMaxTemp = max
        nTimeOut = time
        mFaultCount = 0
        log("min:$nMinTemp,max:$nMaxTemp,$nTimeOut")
    }

    fun updateStatus(byteArray: ByteArray)
    {
        val t = (byteArray.argInt(1) shl 7) + byteArray.argInt(2)

        mTemp = if ((t and 0x800) != 0x00) {
            val p = 0x1000 - (t and 0xFFF)
            "-${p * 0.0625f}"
        } else {
            "${t * 0.0625f}"
        }

        val oldState = mState
        val oldTemperature = mTemperatureFaultFlag

        mState = byteArray.argInt(7)

        if (DeliverPopupWindow.isShow) {
            mState = mState and ( 0x04.inv()  )
        }

        if (isTemperatureNormal()) {
            mFaultCount = 0
        } else {
            mFaultCount ++
        }

        val ret = isDoorClose()
        if (ret != mDoorCloseStatus)
        {
            mDoorCloseStatus = ret
            EventBus.getDefault().post(DoorStatusChangeEvent(mDoorCloseStatus))
        }

        mTemperatureFaultFlag = (mFaultCount > (nTimeOut / 5)) // 温度是否正常

        log("FaultCount=$mFaultCount, State=$mState, Temp=$mTemp")

        if ( ((oldState != mState) || (oldTemperature != mTemperatureFaultFlag)) )
        {
            Task.DelayHandler.post {

                val ret = toJson()
                log(ret, "及时更新状态")
                try {
                    Http.updateStatus(ret)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inline fun isTemperatureNormal(): Boolean
    {
        val t = mTemp.toFloat()
        return (t > nMinTemp) && (t < nMaxTemp)
    }

    private inline fun temperatureNormal() = mTemperatureFaultFlag.toString()

    fun isDoorClose() = (mState and 0x20) != 0x00

    private inline fun isFridgeClose() = (mState and 0x04) == 0x00

    private inline fun isFridgeOpen() = (mState and 0x02) == 0x00

    private inline fun isPickMotor() = (mState and 0x10) == 0x00

    private inline fun isCompressor() = (mState and 0x40) == 0x00

    private inline fun door() = if (isDoorClose()) "close" else "open"

    private inline fun fridge() = if (isFridgeClose()) "normal" else "error"

    fun toJson(): String
    {
        val json = JSONObject()

        json.put("ariesFridge", fridge())
        json.put("ariesPickMoto", "normal")
        json.put("ariesRobotArm1", "normal")
        json.put("ariesRobotArm2", "normal")
        json.put("ariesTemperature", mTemp)
        json.put("ariesDoorstatus", door())
        json.put("ariesRSSI", mSignalStrength)
        json.put("troubleTemperature", temperatureNormal())
        json.put("macAddr", App.MacAddress)

        if ( isDoorClose() && isFridgeClose() && (!mTemperatureFaultFlag) ) {
            json.put("trouble", "false")
        } else {
            json.put("trouble", "true")
        }

        return json.toString()
    }

    fun toJson2(): String
    {
        val json = JSONObject()

        json.put("ariesFridge", "normal")
        json.put("ariesPickMoto", "normal")
        json.put("ariesRobotArm1", "normal")
        json.put("ariesRobotArm2", "normal")
        json.put("ariesTemperature", "888")
        json.put("ariesDoorstatus", "close")
        json.put("ariesRSSI", "3")
        json.put("troubleTemperature", "false")
        json.put("macAddr", App.MacAddress)
        json.put("trouble", "false")

        return json.toString()
    }

    override fun onSignalStrengthsChanged(signalStrength: SignalStrength?)
    {
        val tmp = signalStrength.toString()

        val parts = tmp.split(" ")

        val dbm = parts[1].toInt() * 2 - 113

        if (mTelephonyManager.networkType == TelephonyManager.NETWORK_TYPE_LTE)
        {
            if (dbm > 0) {
                mSignalStrength = 0
            } else if (dbm > -60) {
                mSignalStrength = 4
            } else if (dbm > -75) {
                mSignalStrength = 3
            } else if (dbm > -80) {
                mSignalStrength = 2
            } else if (dbm > -90) {
                mSignalStrength = 1
            }
        }
        else
        {
            mSignalStrength = -1
        }

        log("信号强度:$dbm")

        super.onSignalStrengthsChanged(signalStrength)
    }

}