package fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android_serialport_api.SerialPortFinder
import app.log
import com.hontech.icecreamcustomclient.R
import service.SerialPortManager
import util.Http

class SystemSettingFragment : Fragment()
{
    private lateinit var mTextViewCurrentUrl: TextView
    private lateinit var mSpinnerUrl: Spinner
    private lateinit var mTextViewCurrentSerialPort: TextView
    private lateinit var mSpinnerSerialPort: Spinner
    private lateinit var mButtonTest: Button
    private lateinit var mButtonUrl: Button

    private var mSelectedUrl = ""
    private var mSelectedPath = ""

    private var mAllUrls: Array<String>? = null
    private var mAllSerialPorts = arrayOf("")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_system_setting, null)
        initUi(view)
        return view
    }

    private fun initUi(view: View)
    {
        mTextViewCurrentUrl = view.findViewById(R.id.id_fragment_setting_text_view_current_url)
        mTextViewCurrentSerialPort = view.findViewById(R.id.id_fragment_setting_text_view_current_serial_port)
        mSpinnerUrl = view.findViewById(R.id.id_fragment_setting_spinner_url)
        mSpinnerSerialPort = view.findViewById(R.id.id_fragment_setting_spinner_serial_port)
        mButtonTest = view.findViewById(R.id.id_fragment_setting_button_test)
        mButtonUrl = view.findViewById(R.id.id_fragment_setting_button_url)

        mAllUrls = context!!.resources.getStringArray(R.array.url_array)

        val adapter = ArrayAdapter.createFromResource(context, R.array.url_array, R.layout.spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinnerUrl.adapter = adapter

        mButtonTest.setOnClickListener(::onClickTest)
        mButtonUrl.setOnClickListener(::onClickUrl)

        try {
            mAllSerialPorts = SerialPortFinder().allDevicesPath
        } catch (e: Exception) {
            e.printStackTrace()
            mAllSerialPorts = arrayOf("")
        }

        val adapters = ArrayAdapter<String>(context, R.layout.spinner_item, mAllSerialPorts)
        adapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mSpinnerSerialPort.adapter = adapters

        mTextViewCurrentUrl.text = "当前域名:${Http.BASE_URL}"

        mSpinnerUrl.onItemSelectedListener = onUrlItemSelectedListener
        mSpinnerSerialPort.onItemSelectedListener = onSerialPortItemSelectedListener
        mTextViewCurrentSerialPort.text = "当前串口:${SerialPortManager.instance.SerialPortPath}"
    }

    private val onUrlItemSelectedListener = object: AdapterView.OnItemSelectedListener
    {
        override fun onNothingSelected(parent: AdapterView<*>?)
        {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
        {
            val tmp = mAllUrls!![position]
            mSelectedUrl = tmp
        }
    }

    private val onSerialPortItemSelectedListener = object: AdapterView.OnItemSelectedListener
    {
        override fun onNothingSelected(parent: AdapterView<*>?)
        {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long)
        {
            mSelectedPath = mAllSerialPorts!![position]
        }
    }

    private fun onClickTest(view: View)
    {
        val ret = SerialPortManager.instance.openOfTest(mSelectedPath)
        if (ret) {
            SerialPortManager.instance.setPath(mSelectedPath)
            Toast.makeText(context, "当前串口:$mSelectedPath", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(context, "打开失败:$mSelectedPath", Toast.LENGTH_SHORT).show()
    }

    private fun onClickUrl(view: View)
    {
        mTextViewCurrentUrl.text = "当前域名:$mSelectedUrl"
        Http.setBaseUrl(mSelectedUrl)
        Toast.makeText(context, "当前域名:$mSelectedUrl", Toast.LENGTH_SHORT).show()
    }


}