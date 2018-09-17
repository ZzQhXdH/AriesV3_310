package util

import android.content.Context
import app.App

object StateSaveManager
{
    private val sharedPreferences = App.AppContext.getSharedPreferences("StateSaveManager", Context.MODE_PRIVATE)
    private const val ONE_NET_DEVICE_KEY = "onenet.device.key"

    fun readDeviceId(): String {
        return sharedPreferences.getString(ONE_NET_DEVICE_KEY, "")
    }

    fun saveDeviceId(v: String) {
        sharedPreferences.edit().putString(ONE_NET_DEVICE_KEY, v).commit()
    }

    fun readString(key: String): String {
        return sharedPreferences.getString(key, "")
    }

    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).commit()
    }

    fun saveBoolean(key: String, value: Boolean)
    {
        sharedPreferences.edit().putBoolean(key, value).commit()
    }

    fun readBoolean(key: String): Boolean
    {
        return sharedPreferences.getBoolean(key, false)
    }

    fun saveInt(key: String, value: Int)
    {
        sharedPreferences.edit().putInt(key, value).commit()
    }

    fun readInt(key: String): Int
    {
        return sharedPreferences.getInt(key, -1)
    }
}