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
        sw_vibrate.setOnCheckedChangeListener { _, check ->
            ACache.get(this).put(VIRBATE, check)
        }
        if (ACache.get(this).getAsObject(VIRBATE) != null) {
            sw_vibrate.isChecked = ACache.get(this).getAsObject(VIRBATE) as Boolean
        } else {
            //默认开启录音震动
            ACache.get(this).put(VIRBATE, true)
            sw_vibrate.isChecked = true
        }

        sw_filter.setOnCheckedChangeListener { _, check ->
            ACache.get(this).put(FILTER, check)
            if (check) {
                ll_filter.visibility = View.VISIBLE
            } else {
                ll_filter.visibility = View.GONE
            }
        }

        cb_all.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(FILTER_TIME, "all")
                resetCheck(cb_all)
            }
        }
        cb_15.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(FILTER_TIME, "15")
                resetCheck(cb_15)
            }
        }
        cb_30.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(FILTER_TIME, "30")
                resetCheck(cb_30)
            }
        }
        cb_60.setOnCheckedChangeListener { _, b ->
            if (b) {
                ACache.get(this).put(FILTER_TIME, "60")
                resetCheck(cb_60)
            }
        }
        if (ACache.get(this).getAsObject(FILTER) != null) {
            sw_filter.isChecked = ACache.get(this).getAsObject(FILTER) as Boolean
            if (sw_filter.isChecked) {
                ll_filter.visibility = View.VISIBLE
                //判断选中的是哪个过滤规则
                if (ACache.get(this).getAsString(FILTER_TIME) != null) {
                    when (ACache.get(this).getAsString(FILTER_TIME)) {
                        "all" -> {
                            resetCheck(cb_all)
                        }
                        "15" -> {
                            resetCheck(cb_15)
                        }
                        "30" -> {
                            resetCheck(cb_30)
                        }
                        "60" -> {
                            resetCheck(cb_60)
                        }
                    }
                } else {
                    ACache.get(this).put(FILTER_TIME, "all")
                    resetCheck(cb_all)
                }
            } else {
                ll_filter.visibility = View.GONE
            }
        } else {
            //默认全部上传
            ll_filter.visibility = View.GONE
            ACache.get(this).put(FILTER, false)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
