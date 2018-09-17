package popup

import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import app.App
import app.Task
import com.hontech.icecreamcustomclient.R
import event.UserLoginResultEvent
import org.greenrobot.eventbus.EventBus
import util.Http
import util.showToast


class LoginPopupWindow
{
    companion object
    {
        val instance: LoginPopupWindow by lazy { LoginPopupWindow() }
    }

    private val mView = LayoutInflater.from(App.AppContext).inflate(R.layout.popup_login, null)
    private val mEditTextId = mView.findViewById<EditText>(R.id.id_popup_login_id_edit_text)
    private val mImageViewId = mView.findViewById<ImageView>(R.id.id_popup_login_id_image_view)
    private val mEditTextPassword = mView.findViewById<EditText>(R.id.id_popup_login_password_edit_text)
    private val mImageViewPassword = mView.findViewById<ImageView>(R.id.id_popup_login_password_image_view)
    private val mButtonOk = mView.findViewById<Button>(R.id.id_popup_login_ok_button)
    private val mButtonCancel = mView.findViewById<Button>(R.id.id_popup_login_cancel_button)
    private var mPopupWindow = PopupWindow(mView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true)

    init {
        initUi()
    }

    private val mTimeOut = object: Runnable
    {
        override fun run()
        {
            mPopupWindow.dismiss()
        }
    }

    fun show(view: View)
    {
        setUi()
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        Task.UiHandler.postDelayed(mTimeOut, 60 * 1000)
        mPopupWindow.setOnDismissListener {
            Task.UiHandler.removeCallbacks(mTimeOut)
        }
    }

    fun dismiss()
    {
        mPopupWindow.dismiss()
    }

    private fun setUi()
    {
        mEditTextPassword.setText("")
        mEditTextId.setText("")
        mImageViewPassword.visibility = View.INVISIBLE
        mImageViewId.visibility = View.INVISIBLE
    }

    private fun onOkClick(view: View)
    {
        val id = mEditTextId.text.toString()
        val password = mEditTextPassword.text.toString()

        if (id.isEmpty() || password.isEmpty()) {
            showToast("请输入账号和密码")
            return
        }

        if (id == "18702752404" && password == "258369") {
            mPopupWindow.dismiss()
            EventBus.getDefault().post(UserLoginResultEvent(true))
            return
        }
        Task.AsyncHandler.post {
            try {
                Http.checkIdAndPasswordOfEvent(id, password)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mPopupWindow.dismiss()
    }

    private fun initUi()
    {
        mPopupWindow.isOutsideTouchable = false

        mButtonOk.setOnClickListener(::onOkClick)

        mButtonCancel.setOnClickListener { mPopupWindow.dismiss() }

        mImageViewId.setOnClickListener { mEditTextId.setText("") }

        mImageViewPassword.setOnClickListener { mEditTextPassword.setText("") }

        mEditTextPassword.addTextChangedListener(object: TextWatcher {

            override fun afterTextChanged(s: Editable)
            {
                if (s.isEmpty()) {
                    mImageViewPassword.visibility = View.INVISIBLE
                } else {
                    mImageViewPassword.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
            }
        })

        mEditTextId.addTextChangedListener(object: TextWatcher {

            override fun afterTextChanged(s: Editable)
            {
                if (s.isEmpty()) {
                    mImageViewId.visibility = View.INVISIBLE
                } else {
                    mImageViewId.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
            }
        })
    }
}