package com.guoqi.callautorecord

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager

class CallRecorderService : Service() {

    override fun onCreate() {
        super.onCreate()
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(CallListener(), PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)

    }

}
