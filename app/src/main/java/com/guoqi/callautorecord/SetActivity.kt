package com.guoqi.callautorecord

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import kotlinx.android.synthetic.main.activity_set.*

class SetActivity : AppCompatActivity() {

    companion object {
        val VIRBATE = "vibrate"
        val RULE = "rule"
        val FILTER = "filter"
        val FILTER_TIME = "filter_time"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initSet()
    }

    private fun initSet() {
        setVibrate()

        setRule()

        setFilter()

        setFilterTime()
    }

    //默认双向录音
    private fun setRule() {
        cb_callAll.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(RULE, 0)
                resetCheckRule(cb_callAll)
            }
        }
        cb_callCome.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(RULE, 1)
                resetCheckRule(cb_callCome)
            }
        }
        cb_callOut.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(RULE, 2)
                resetCheckRule(cb_callOut)
            }
        }

        if (ACache.get(this).getAsObject(RULE) != null) {
            when (ACache.get(this).getAsObject(RULE) as Int) {
                0 -> {
                    resetCheckRule(cb_callAll)
                }
                1 -> {
                    resetCheckRule(cb_callCome)
                }
                2 -> {
                    resetCheckRule(cb_callOut)
                }
            }
        } else {
            ACache.get(this).put(FILTER_TIME, 0)
            resetCheckRule(cb_callAll)
        }

    }

    //设置开启过滤,默认全部上传
    private fun setFilter() {
        sw_filter.setOnCheckedChangeListener { _, check ->
            ACache.get(this).put(FILTER, check)
            if (check) {
                ll_filter.visibility = View.VISIBLE
            } else {
                ll_filter.visibility = View.GONE
            }
        }
        if (ACache.get(this).getAsObject(FILTER) != null) {
            sw_filter.isChecked = ACache.get(this).getAsObject(FILTER) as Boolean
            if (sw_filter.isChecked) {
                ll_filter.visibility = View.VISIBLE
            } else {
                ll_filter.visibility = View.GONE
            }
        } else {
            ll_filter.visibility = View.GONE
            ACache.get(this).put(FILTER, false)
        }
    }

    //默认开启录音震动
    private fun setVibrate() {
        sw_vibrate.setOnCheckedChangeListener { _, check ->
            ACache.get(this).put(VIRBATE, check)
        }
        if (ACache.get(this).getAsObject(VIRBATE) != null) {
            sw_vibrate.isChecked = ACache.get(this).getAsObject(VIRBATE) as Boolean
        } else {
            ACache.get(this).put(VIRBATE, true)
            sw_vibrate.isChecked = true
        }
    }

    //设置过滤规则
    private fun setFilterTime() {
        cb_all.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(FILTER_TIME, 0)
                resetCheck(cb_all)
            }
        }
        cb_15.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(FILTER_TIME, 15)
                resetCheck(cb_15)
            }
        }
        cb_30.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(FILTER_TIME, 30)
                resetCheck(cb_30)
            }
        }
        cb_60.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(FILTER_TIME, 60)
                resetCheck(cb_60)
            }
        }
        if (ACache.get(this).getAsObject(FILTER_TIME) != null) {
            when (ACache.get(this).getAsObject(FILTER_TIME) as Int) {
                0 -> {
                    resetCheck(cb_all)
                }
                15 -> {
                    resetCheck(cb_15)
                }
                30 -> {
                    resetCheck(cb_30)
                }
                60 -> {
                    resetCheck(cb_60)
                }
            }
        } else {
            ACache.get(this).put(FILTER_TIME, 0)
            resetCheck(cb_all)
        }
    }

    private fun resetCheck(cb: CheckBox) {
        cb_all.isChecked = false
        cb_15.isChecked = false
        cb_30.isChecked = false
        cb_60.isChecked = false
        //选中
        cb.isChecked = true
    }

    private fun resetCheckRule(cb: CheckBox) {
        cb_callAll.isChecked = false
        cb_callCome.isChecked = false
        cb_callOut.isChecked = false
        //选中
        cb.isChecked = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
