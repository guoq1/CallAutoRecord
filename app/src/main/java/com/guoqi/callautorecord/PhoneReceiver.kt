package com.guoqi.callautorecord

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Vibrator
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import java.io.File


/**
 * 来去电 广播
 *
 * 主叫流程:
 *      呼叫:拨出号码$phoneNumber
 *      摘机状态(每次都会调用,因为主叫是你主动发起电话)
 *      等待拨号,然后通话                            //开始事件,等待也算做通话
 *      ...
 *      挂断电话(自己或者对方挂断都会调用)              //结束事件
 *
 * 被叫流程:
 *      响铃:来电号码$incomingNumber
 *      等待接听,然后通话
 *      ...
 *      摘机状态 (自己拒接或对方主动挂掉不会调用此方法)   //开始事件,接听时算通话
 *      挂断电话(自己或者对方挂断都会调用)              //结束事件
 *
 * @author GQ
 */
class PhoneReceiver : BroadcastReceiver() {
    var context: Context? = null
    override fun onReceive(context: Context, intent: Intent) {
        this.context = context

        // 如果是去电
        if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            Log.e(TAG, "呼叫:$phoneNumber")
            number = phoneNumber
            isHujiao = true
            isGuaduan = false
        } else {
            val tm = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
            tm.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    companion object {
        val TAG = "CallAutoRecord"

        lateinit var vibrator: Vibrator
        val callListener = CallListener()

        var isHujiao = false //呼叫
        var isZhujiaoTonghua = false //主叫通话
        var isGuaduan = true //挂断
        var isLaiDian = false //来电
        var isLaidianTonghua = false //来电通话
        var isLaidianZhaiji = false //来电摘机

        var number: String = ""
        var isRecord: Boolean = false
        var recorder :MediaRecorder? = null
        var file: File? = null
    }

}
