package activity



import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager

import android.support.v7.app.AppCompatActivity

import android.view.View

import android.widget.*
import app.App
import app.Task
import app.getVersion

import com.hontech.icecreamcustomclient.R
import data.WaresInfoManager
import event.GoodsTypeChageEvent


import event.ReplenishFinishEvent
import fragment.ClearFragment
import fragment.ReplenishFragment
import fragment.SystemSettingFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class DebugActivity : AppCompatActivity()
{
    companion object
    {
        var isShow = false
    }

    private val mAcquireButton: Button by lazy { findViewById<Button>(R.id.id_debug_acquire_deliver_button) }
    private val mFinishButton: Button by lazy { findViewById<Button>(R.id.id_debug_deliver_finish_button) }
    private val mQuitButton: Button by lazy { findViewById<Button>(R.id.id_debug_deliver_quit_button) }
    private val mQuitAllButton: Button by lazy { findViewById<Button>(R.id.id_debug_deliver_quit_all_button) }
    private val mTabLayout: TabLayout by lazy { findViewById<TabLayout>(R.id.id_debug_tab_layout) }
    private val mViewPager: ViewPager by lazy { findViewById<ViewPager>(R.id.id_debug_view_pager) }
    private val mAdapter: ViewPagerAdapter by lazy { ViewPagerAdapter(supportFragmentManager) }
    private val mTextViewTitle: TextView by lazy { findViewById<TextView>(R.id.id_debug_text_view_title) }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        App.addActivity(this)
        setContentView(R.layout.activity_debug)
        EventBus.getDefault().register(this)

        mAcquireButton.setOnClickListener(::onClickAcquire)
        mFinishButton.setOnClickListener(::onClickFinish)
        mQuitButton.setOnClickListener(::onClickQuit)
        mQuitAllButton.setOnClickListener(::onClickQuitAll)

        mViewPager.adapter = mAdapter
        mViewPager.offscreenPageLimit = 3
        mTabLayout.setupWithViewPager(mViewPager)

        mTextViewTitle.text = "MAC=>${App.MacAddress},MachCode=>${WaresInfoManager.MachCode},Version=>${getVersion()}"

        isShow = true
    }

    override fun onStop()
    {
        isShow = false
        super.onStop()
    }

    override fun onDestroy()
    {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
        App.removeActivity(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onUpdateGoodsTypeEvent(env: GoodsTypeChageEvent) // 获取补货清单返回
    {
        Toast.makeText(this, "获取补货清单成功", Toast.LENGTH_SHORT).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED) // 补货完成返回
    fun onReplenishFinishEvent(env: ReplenishFinishEvent)
    {
        Toast.makeText(this, "补货完成", Toast.LENGTH_SHORT).show()
        mTextViewTitle.text = "MAC:${App.MacAddress},MachCode:${WaresInfoManager.MachCode}"
    }

    private fun onClickAcquire(view: View)
    {
        Task.updateGoodsType()
    }

    private fun onClickFinish(view: View)
    {
        WaresInfoManager.clearError()
        Task.replenishFinish()
    }

    private fun onClickQuit(view: View)
    {
        finish()
    }

    private fun onClickQuitAll(view: View)
    {
        val i = Intent(this, MainActivity::class.java)
        i.putExtra(MainActivity.ACTION_QUIT, true)
        startActivity(i)
    }

    private class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager)
    {
        private val mTitles = arrayOf("补货清单", "清出清单", "系统设置")

        private val mFragments = arrayOf(
                ReplenishFragment(),
                ClearFragment(),
                SystemSettingFragment())

        override fun getCount() = mFragments.size

        override fun getItem(position: Int) = mFragments[position]

        override fun getPageTitle(position: Int) = mTitles[position]
    }

}