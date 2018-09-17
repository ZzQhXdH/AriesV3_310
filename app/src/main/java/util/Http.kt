package util


import android.os.Environment
import app.App
import app.log


import data.WaresInfoManager
import event.UserLoginResultEvent

import okhttp3.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

object Http
{
  //  private const val TEST_URL = "http://hfrd.hontech-rdcenter.com:8080"
 //   var TEST_URL = "http://test.hontech-rdcenter.com:8080"

    private val DEFAULT_URL = "http://test.hontech-rdcenter.com:8080"

    var BASE_URL = "http://test.hontech-rdcenter.com:8080"

    private var QUERY_REFUND_URL = "$BASE_URL/bg-uc/jf/com/pm/searchRefundStatus.json" // 查询退款
        get() = "$BASE_URL/bg-uc/jf/com/pm/searchRefundStatus.json"

    private var QUERY_ORDER_URL = "$BASE_URL/bg-uc/jf/com/pm/getCargoLane.json" // 查询订单接口
        get() = "$BASE_URL/bg-uc/jf/com/pm/getCargoLane.json"

    private var QR_URL = "$BASE_URL/bg-uc/jf/com/pm/getSilvermerchant.json" // 二维码接口
        get() = "$BASE_URL/bg-uc/jf/com/pm/getSilvermerchant.json"

    private var QUERY_PAY_URL = "$BASE_URL/bg-uc/jf/com/pm/searchMacState.json" // 支付结果查询接口
        get() = "$BASE_URL/bg-uc/jf/com/pm/searchMacState.json"

    private var REFUND_URL = "$BASE_URL/bg-uc/jf/com/pm/returnRefundSilvermerchant.json" // 退款接口
        get() = "$BASE_URL/bg-uc/jf/com/pm/returnRefundSilvermerchant.json"

    private var GOODS_TYPE_URL = "$BASE_URL/bg-uc/replenishment/detail-inter/data.json" // 获取补货清单接口
        get() = "$BASE_URL/bg-uc/replenishment/detail-inter/data.json"

    private var REPLENISH_FINISH_URL = "$BASE_URL/bg-uc/replenishment/client/replen-data/finish.json" // 补货完成接口
        get() = "$BASE_URL/bg-uc/replenishment/client/replen-data/finish.json"

    private var WARES_URL = "$BASE_URL/bg-uc/goodssearch/goods-info/list.json" // 获取库存接口
        get() = "$BASE_URL/bg-uc/goodssearch/goods-info/list.json"

    private var REPETROY_URL = "$BASE_URL/bg-uc/replenishment/work-off/quantity.json" // 库存扣减接口
        get() = "$BASE_URL/bg-uc/replenishment/work-off/quantity.json"

    private var CHECK_ID_PASSWORD_URL = "$BASE_URL/bg-uc/checkMain/main-info/check.json" // 校验用户
        get() = "$BASE_URL/bg-uc/checkMain/main-info/check.json"

    private var STATUS_URL = "$BASE_URL/bg-uc/sbzt/receiveby.json" // 状态上传接口
        get() = "$BASE_URL/bg-uc/sbzt/receiveby.json"

    private var ADV_URL = "$BASE_URL/bg-uc/advertise/advertiseGive.json" // 获取广告
        get() = "$BASE_URL/bg-uc/advertise/advertiseGive.json"

    private var TEMPERATURE_URL = "$BASE_URL/bg-uc/sbzt/give.json" // 获取温度
        get() = "$BASE_URL/bg-uc/sbzt/give.json"

    private var SELL_STATUS_URL = "$BASE_URL/bg-uc/sbzt/giveStatus.json" // 获取停售or在售状态
        get() = "$BASE_URL/bg-uc/sbzt/giveStatus.json"

    private var PAST_DUE_URL = "$BASE_URL/bg-uc/jf/com/charging/dateComparison.json" // 判断服务器是否过期
        get() = "$BASE_URL/bg-uc/sbzt/giveStatus.json"

    private var VIP_CHECK = "$BASE_URL/bg-uc/jf/com/free/vipFree.json" // VIP判断
        get() = "$BASE_URL/bg-uc/jf/com/free/vipFree.json"

    private var VIP_CHECK_CHARGE = "$BASE_URL/bg-uc/jf/com/free/updateVipPrice.json" // VIP扣款
        get() = "$BASE_URL/bg-uc/jf/com/free/updateVipPrice.json"

//    private val VIP_CHECK = "http://10.1.8.45:8080/bg-uc/jf/com/free/vipFree.json"
//
//    private val VIP_CHECK_CHARGE = "http://10.1.8.45:8080/bg-uc/jf/com/free/updateVipPrice.json"

//    private val PAST_DUE_URL = "http://192.168.1.105:8080/bg-uc/jf/com/charging/dateComparison.json"

//    private var STATUS_URL = "http://192.168.1.119:8080/bg-uc/sbzt/receiveby.json"
//
//    private var ADV_URL = "http://192.168.1.119:8080/bg-uc/advertise/advertiseGive.json" // 获取广告
//
//    private var TEMPERATURE_URL = "http://192.168.1.119:8080/bg-uc/sbzt/give.json" // 获取温度


    private const val ONE_NET_URL = "http://api.heclouds.com"
    private const val ONE_NET_API_KEY = "7I8WxYzcadfwZ3jJiZZymv8ntWQ="

    private const val BASE_URL_KEY = "base.url.key"

    init {
        BASE_URL = StateSaveManager.readString(BASE_URL_KEY)
        if (BASE_URL.isEmpty()) {
            BASE_URL = DEFAULT_URL
        }
    }

    fun setBaseUrl(url: String) {
        BASE_URL = url
        StateSaveManager.saveString(BASE_URL_KEY, url)
    }


    private val mHttpClient = OkHttpClient.Builder()
                                          .connectTimeout(10, TimeUnit.SECONDS)
                                          .writeTimeout(10, TimeUnit.SECONDS)
                                          .readTimeout(10, TimeUnit.SECONDS)
                                          .build()

    private val mediaType = MediaType.parse("application/json, charset=utf-8")

    private fun post(url: String, content: String): String
    {
        val body = RequestBody.create(mediaType, content)
        val request = Request.Builder().url(url).post(body).build ()
        val call = mHttpClient.newCall(request)
        val response = call.execute()
        return response.body()?.string() ?: throw IOException("network error")
    }

    /**
     *  1 -> 在有效期内
     *  0 -> 服务器异常
     * -1 -> 需要续费
     */
    fun acquirePastDue(): String
    {
        val body = FormBody.Builder().add("macAddr", App.MacAddress).build()
        val req = Request.Builder().post(body).url(PAST_DUE_URL).build()
        return mHttpClient.newCall(req).execute().body()?.string() ?: ""
    }

    fun acquireAdv(): String
    {
        val json = JSONObject()
        json.put("macAddr", App.MacAddress)
        val content = json.toString()
        log("获取广告:$content")
        return post(ADV_URL, content)
    }

    fun queryRefund(order: String): String
    {
        val body = FormBody.Builder().add("out_trade_no", order).build()
        val request = Request.Builder().post(body).url(QUERY_REFUND_URL).build()
        return mHttpClient.newCall(request).execute().body()?.string() ?: "error"
    }

    fun queryOrder(order: String): String
    {
        val body = FormBody.Builder().add("out_trade_no", order).build()
        val request = Request.Builder().post(body).url(QUERY_ORDER_URL).build()
        return mHttpClient.newCall(request).execute().body()?.string() ?: "error"
    }

    fun acquireTemperature(): String
    {
        val json = JSONObject()
        json.put("macAddr", App.MacAddress)
        return post(TEMPERATURE_URL, json.toString())
    }

    fun updateStatus(json: String) = post(STATUS_URL, json)

    fun newCreateDeviceOfOneNet(): String
    {
        val json = JSONObject()
        json.put("title", App.MacAddress.replace(":", ""))
        json.put("desc", "AriesClient")
        json.put("private", true)

        val body = RequestBody.create(mediaType, json.toString())
        val request = Request.Builder()
                .addHeader("api-key", ONE_NET_API_KEY)
                .url("$ONE_NET_URL/devices")
                .post(body).build()

        val call = mHttpClient.newCall(request)
        val response = call.execute()
        val result = response.body()?.string() ?: throw IOException("net work error")

        log(result)

        val jsonObject = JSONObject(result)
        val errno = jsonObject.optInt("errno")
        if (errno != 0) {
            throw IOException("add devices error")
        }
        val dataObject = jsonObject.optJSONObject("data")
        val id = dataObject.optString("device_id")

        return id
    }

    fun checkIdAndPasswordOfEvent(id: String, password: String)
    {
        val jsonObject = JSONObject()
        jsonObject.put("emplCode", id)
        jsonObject.put("password", password)
        jsonObject.put("macAddr", App.MacAddress)

        val result = post(CHECK_ID_PASSWORD_URL, jsonObject.toString())
        log(result)
        val json = JSONObject(result)
        val ret = json.optBoolean("success")
        EventBus.getDefault().post(UserLoginResultEvent(ret))
    }

    /**
     * 退款 new
     */
    fun refundForResult(msg: String, order: String, goodsType: String)
    {
        val body = MultipartBody.Builder()
                .addFormDataPart("out_trade_no", order)
                .addFormDataPart("macAddr", App.MacAddress)
                .addFormDataPart("refund_remark", msg)
                .addFormDataPart("cargoData", goodsType)
                .build()
        val request = Request.Builder().post(body).url(REFUND_URL).build()
        val s = mHttpClient.newCall(request).execute().body()?.string() ?: ""
        log("退款结果:$s")
    }


    /**
     * 获取二维码
     */
    fun getQrCodeContentOfNetwork(): String
    {
        val info = WaresInfoManager.getSelectWaresInfo()
        val jsonObject = JSONObject()
        jsonObject.put("macAddress", App.MacAddress)
        jsonObject.put("tradename", info.name)
        jsonObject.put("price", info.price)
        jsonObject.put("ID", info.id)
        log(jsonObject.toString())
        val body = FormBody.Builder()
                .add("goods", jsonObject.toString())
                .build()
        val request = Request.Builder().post(body).url(QR_URL).build()
        val s = mHttpClient.newCall(request).execute().body()?.string() ?: ""
        if (s.isEmpty()) {
            throw IOException("network error")
        }
        log(s)
        val json = JSONObject(s)
        WaresInfoManager.CurrentOrder = json.optString("order")
        val qr = json.optString("silvermerchant")
        return qr
    }

    // vipcode,machCode,price
    fun getVIPStatus(vipCode: String, price: String): String
    {
        log("vipCode:$vipCode,machCode:${WaresInfoManager.MachCode},price:$price")
        val body = FormBody.Builder()
                .add("vipcode", vipCode)
                .add("machCode", WaresInfoManager.MachCode)
                .add("price", price)
                .build()
        val request = Request.Builder().url(VIP_CHECK).post(body).build()
        return mHttpClient.newCall(request).execute().body()?.string() ?: "error"
    }

    // out_trade_no,price
    fun chargeVip(order: String): String
    {
        val info = WaresInfoManager.getSelectWaresInfo()

        val body = FormBody.Builder()
                .add("waresId", info.id)
                .add("waresName", info.name)
                .add("vipcode", WaresInfoManager.VipCode)
                .add("out_trade_no", order)
                .add("macaddress", App.MacAddress)
                .add("machCode", WaresInfoManager.MachCode)
                .add("price", info.price)
                .build()
        val request = Request.Builder().url(VIP_CHECK_CHARGE).post(body).build()
        return mHttpClient.newCall(request).execute().body()?.string() ?: "error"
    }

    /**
     * 获取库存
     */
    fun getWaresOfNetwork(): String
    {
        val jsonObject = JSONObject()
        jsonObject.put("macAddr", App.MacAddress)
        val content = jsonObject.toString()
        log(content)
        return post(WARES_URL, content)
    }

    /**
     * 获取货道
     */
    fun getGoodsTypeOfNetwork(): String
    {
        val jsonObject = JSONObject()
        jsonObject.put("macAddr", App.MacAddress)
        log(jsonObject.toString())
        return post(GOODS_TYPE_URL, jsonObject.toString())
    }

    /**
     * 扣减库存
     */
    fun reportRepetroy(goodsType: String): String
    {
        val jsonObject = JSONObject()
        jsonObject.put("macAddr", App.MacAddress)
        jsonObject.put("cargoData",  goodsType)
        jsonObject.put("out_trade_no", WaresInfoManager.CurrentOrder)
        log(jsonObject.toString())
        val res = post(REPETROY_URL, jsonObject.toString())
        log(res)
        return res
    }

    /**
     * 补货完成
     */
    fun replenishFinishOfNetwork(content: String) = post(REPLENISH_FINISH_URL, content)

    fun queryPayStatusOfNetwork(order: String): Boolean
    {
        val body = MultipartBody.Builder()
                .addFormDataPart("macaddress", App.MacAddress)
                .addFormDataPart("out_trade_no", order)
                .build()
        val request = Request.Builder()
                .url(QUERY_PAY_URL)
                .post(body)
                .build()
        val result = mHttpClient.newCall(request).execute().body()?.string() ?: ""
        if (result.isEmpty()) {
            log("返回为空")
            return false
        }
        log(result)
        val jsonObject = JSONObject(result)
        val state = jsonObject.optString("macstate", "")
        val order = jsonObject.optString("out_trade_no", "")
        if (state == "paymentsuccess" && order == WaresInfoManager.CurrentOrder) {
            return true
        }
        return false
    }

    fun downLoadFile(name: String, url: String): File
    {
        log("开始下载:$name")
        val request = Request.Builder().get().url(url).build()
        val call = mHttpClient.newCall(request)
        val response = call.execute()
        val bytes = response.body()!!.bytes()
        log("下载的数据大小:${bytes.size}")
        val file = File(Environment.getExternalStorageDirectory(), name)
        if (file.exists()) {
            file.delete()
        }
        val ret = file.createNewFile()
        log("创建文件:$ret")
        val out = FileOutputStream(file, false)
        out.write(bytes)
        out.flush()
        out.close()

        return file
    }

    fun downLoadOTA(url: String): ByteArray
    {
        val request = Request.Builder().get().url(url).build()
        val call = mHttpClient.newCall(request)
        val response = call.execute()
        if (response.code() != 200) {
            log("下载文件错误:${response.code()}")
            return byteArrayOf(0)
        }
        return response.body()!!.bytes()
    }

    fun acquireSellStatus(): String
    {
        val json = JSONObject()
        json.put("macAddr", App.MacAddress)
        return post(SELL_STATUS_URL, json.toString())
    }

}