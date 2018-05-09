package com.guoqi.callautorecord

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Vibrator
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.guoqi.callautorecord.PhoneReceiver.Companion.TAG
import com.guoqi.callautorecord.PhoneReceiver.Companion.callListener
import com.guoqi.callautorecord.PhoneReceiver.Companion.vibrator

class CallRecorderService : Service() {


    override fun onCreate() {
        super.onCreate()
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        Log.e(TAG, "启动CallRecordService服务,监听来去电")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "电话录音服务关闭")
    }
}
