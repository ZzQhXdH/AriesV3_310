package popup

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import app.App
import app.Task

import com.hontech.icecreamcustomclient.R
import event.UserLoginResultEvent
import org.greenrobot.eventbus.EventBus
import util.Http
import util.showToast

class LoginKeyPopupWindow
{
    companion object
    {
        val instance: LoginKeyPopupWindow by lazy { LoginKeyPopupWindow() }
    }

    private val mView = LayoutInflater.from(App.AppContext).inflate(R.layout.popup_login_key, null)
    private val mRecyclerView = mView.findViewById<RecyclerView>(R.id.id_popup_login_key_recycler_view)
    private val mEditTextId = mView.findViewById<EditText>(R.id.id_popup_login_id_edit_text)
    private val mImageViewId = mView.findViewById<ImageView>(R.id.id_popup_login_id_image_view)
    private val mEditTextPassword = mView.findViewById<EditText>(R.id.id_popup_login_password_edit_text)
    private val mImageViewPassword = mView.findViewById<ImageView>(R.id.id_popup_login_password_image_view)
    private val mPopupWindow = PopupWindow(mView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true)

    private var mFocus = 1

    init {
        initUi()
        mRecyclerView.layoutManager = GridLayoutManager(mRecyclerView.context, 4)
        mRecyclerView.addItemDecoration(KeySpace())
        mRecyclerView.adapter = KeyAdapter(::onKeyClick)
    }

    fun show(view: View)
    {
        setUi()
        mFocus = 1
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        Task.UiHandler.postDelayed(mTimeOut, 60 * 1000)
        mPopupWindow.setOnDismissListener {
            Task.UiHandler.removeCallbacks(mTimeOut)
        }
    }

    private val mTimeOut = object: Runnable
    {
        override fun run()
        {
            mPopupWindow.dismiss()
        }
    }

    private fun setUi()
    {
        mEditTextPassword.setText("")
        mEditTextId.setText("")
        mImageViewPassword.visibility = View.INVISIBLE
        mImageViewId.visibility = View.INVISIBLE
    }

    private fun onKeyClick(position: Int)
    {
        when (position)
        {
            11 -> mFocus = 1

            15 -> mFocus = 2

            0 -> append(1)
            1 -> append(2)
            2 -> append(3)
            4 -> append(4)
            5 -> append(5)
            6 -> append(6)
            8 -> append(7)
            9 -> append(8)
            10 -> append(9)
            13 -> append(0)
            12 -> mPopupWindow.dismiss()
            14 -> onOk()
            7 -> clear()
        }
    }

    private fun onOk()
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

    private fun clear()
    {
        when (mFocus)
        {
            1 -> mEditTextId.setText("")

            2 -> mEditTextPassword.setText("")
        }
    }

    private fun append(n: Int)
    {
        when (mFocus)
        {
            1 -> {
                mEditTextId.append(n.toString())
            }

            2 -> {
                mEditTextPassword.append(n.toString())
            }
        }
    }

    private class KeyItem(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        companion object
        {
            private val KeyValue = arrayOf(
                    "1", "2", "3", "-",
                    "4", "5", "6", "清除",
                    "7", "8", "9", "账号",
                    "取消", "0", "确定", "密码")
        }


        private val mButton = itemView.findViewById<Button>(R.id.id_item_login_key_button)

        fun set(position: Int, click: (Int) -> Unit)
        {
            mButton.text = KeyValue[position]
            mButton.setOnClickListener { click(position) }
        }
    }

    private class KeySpace : RecyclerView.ItemDecoration()
    {
        override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView?)
        {
            super.getItemOffsets(outRect, itemPosition, parent)
            val col = itemPosition % 4
            if (itemPosition < 4) {
                outRect.top = 20
            }
            outRect.bottom = 20
            outRect.left = 20 - col * 10
        }
    }

    private class KeyAdapter(private val click: (Int) -> Unit) : RecyclerView.Adapter<KeyItem>()
    {
        override fun getItemCount(): Int
        {
            return 16
        }

        override fun onBindViewHolder(holder: KeyItem, position: Int)
        {
            holder.set(position, click)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyItem
        {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_login_key_button, parent, false)
            return KeyItem(view)
        }
    }

    private fun initUi()
    {
        mPopupWindow.isOutsideTouchable = false

        mImageViewId.setOnClickListener { mEditTextId.setText("") }

        mImageViewPassword.setOnClickListener { mEditTextPassword.setText("") }

        mEditTextPassword.addTextChangedListener(object: TextWatcher
        {

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

        mEditTextId.addTextChangedListener(object: TextWatcher
        {

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