package fragment

import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.hontech.icecreamcustomclient.R
import data.GoodsTypeManager
import data.WaresInfoManager
import event.GoodsTypeChageEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ClearFragment : Fragment()
{
    private var mRecyclerView: RecyclerView? = null
    private var mTextViewHint: TextView? = null
    private val mAdapter = RecyclerViewAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_clear, null)
        initUi(view)
        return view
    }

    private fun initUi(view: View)
    {
        mTextViewHint = view.findViewById(R.id.id_debug_clear_text_view_hint)
        mRecyclerView = view.findViewById(R.id.id_debug_clear_recycler_view)
        mRecyclerView!!.adapter = mAdapter
        mRecyclerView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView!!.addItemDecoration(RecyclerViewItemDecoration())
        mRecyclerView!!.visibility = View.GONE
        mTextViewHint!!.visibility = View.VISIBLE
        mTextViewHint!!.text = "请先获取补货清单"
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onGoodsTypeChangeEvent(env: GoodsTypeChageEvent) // 货道改变
    {
        if (GoodsTypeManager.clearNumber() <= 0) {
            mRecyclerView!!.visibility = View.GONE
            mTextViewHint!!.visibility = View.VISIBLE
            mTextViewHint!!.text = "没有需要清出的数据"
            return
        }
        mTextViewHint!!.visibility = View.GONE
        mRecyclerView!!.visibility = View.VISIBLE
        mAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView()
    {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    private class RecyclerViewItem(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        private val mTextName = itemView.findViewById<TextView>(R.id.id_item_clear_text_view_name)
        private val mTextGoodsType = itemView.findViewById<TextView>(R.id.id_item_clear_text_view_goods_type)

        fun set(position: Int)
        {
            val info = GoodsTypeManager.clearOfIndex(position)
            val tmp = if (info.isPastdue == "是") "已经过期" else "没有过期"
            mTextName.text = "${info.name}    $tmp   数量:${info.info.number}"
            mTextGoodsType.text = info.info.name()
        }
    }

    private class RecyclerViewItemDecoration : RecyclerView.ItemDecoration()
    {
        override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView?)
        {
            super.getItemOffsets(outRect, itemPosition, parent)
            outRect.top = 30
        }
    }

    private class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewItem>()
    {
        override fun onBindViewHolder(holder: RecyclerViewItem, position: Int)
        {
            holder.set(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewItem
        {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clear_debug_menu, parent, false)
            return RecyclerViewItem(view)
        }

        override fun getItemCount(): Int
        {
            return GoodsTypeManager.clearNumber()
        }
    }

}