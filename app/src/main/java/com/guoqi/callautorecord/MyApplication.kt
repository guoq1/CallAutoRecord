package com.guoqi.callautorecord

import android.app.Application
import android.content.Context
import com.lzy.okgo.OkGo

class MyApplication : Application() {

    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext;
        OkGo.getInstance().init(this);
    }
}