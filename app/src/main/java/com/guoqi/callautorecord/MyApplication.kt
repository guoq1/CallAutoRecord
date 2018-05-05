package com.guoqi.callautorecord

import android.app.Application
import com.lzy.okgo.OkGo

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        OkGo.getInstance().init(this);
    }
}