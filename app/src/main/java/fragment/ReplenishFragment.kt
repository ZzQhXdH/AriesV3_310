package fragment

import android.graphics.Color
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

class ReplenishFragment : Fragment()
{
    private var mRecyclerView: RecyclerView? = null
    private var mTextViewHint: TextView? = null
    private val mAdapter = RecyclerViewAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_replenish, null)
        initUi(view)
        return view
    }

    private fun initUi(view: View)
    {
        mTextViewHint = view.findViewById(R.id.id_debug_replenish_text_view_hint)
        mRecyclerView = view.findViewById(R.id.id_debug_replenish_recycler_view)
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
        if (GoodsTypeManager.replenishNumber() <= 0) {
            mRecyclerView!!.visibility = View.GONE
            mTextViewHint!!.visibility = View.VISIBLE
            mTextViewHint!!.text = "没有需要补入的数据"
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
        private val mTextName = itemView.findViewById<TextView>(R.id.id_item_replenish_text_view_name)
        private val mTextGoodsType = itemView.findViewById<TextView>(R.id.id_item_replenish_text_view_goods_type)
        private val mTextPlan = itemView.findViewById<TextView>(R.id.id_item_replenish_text_view_replenish_plan)
        private val mTextActual = itemView.findViewById<TextView>(R.id.id_item_replenish_text_view_replenish_actual)
        private val mButtonAdd = itemView.findViewById<Button>(R.id.id_item_replenish_button_add)
        private val mButtonSub = itemView.findViewById<Button>(R.id.id_item_replenish_button_sub)

        fun set(position: Int)
        {
            val info = GoodsTypeManager.replenishOfIndex(position)
            mTextName.text = info.name
            if (info.info.isFault()) {
                mTextGoodsType.setTextColor(Color.RED)
            } else {
                mTextGoodsType.setTextColor(Color.BLUE)
            }
            mTextGoodsType.text = info.info.name()
            mTextPlan.text = "应补:${info.info.number}"
            mTextActual.text = "实补:${info.info.practialNumber}"
            mButtonAdd.setOnClickListener {

                if (info.info.practialNumber >= info.info.number) {
                    return@setOnClickListener
                }
                info.info.practialNumber ++
                mTextActual.text = "实补:${info.info.practialNumber}"
            }

            mButtonSub.setOnClickListener {

                if (info.info.practialNumber <= 0) {
                    return@setOnClickListener
                }
                info.info.practialNumber --
                mTextActual.text = "实补:${info.info.practialNumber}"
            }
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
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_replenish_debug_menu, parent, false)
            return RecyclerViewItem(view)
        }

        override fun getItemCount(): Int
        {
            return GoodsTypeManager.replenishNumber()
        }
    }

}