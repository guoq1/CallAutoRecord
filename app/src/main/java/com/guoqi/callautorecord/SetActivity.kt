package com.guoqi.callautorecord

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_set.*

class SetActivity : AppCompatActivity() {

    companion object {
        val VIRBATE = "vibrate"
        val FILTER = "filter"
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
            if (check) {
                ll_filter.visibility = View.VISIBLE
            } else {
                ll_filter.visibility = View.GONE
            }
        }
//        if (ACache.get(this).getAsObject(FILTER) != null) {
//
//        } else {
//            ll_filter.visibility = View.GONE
//            ACache.get(this).put(FILTER, "all")
//        }
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
