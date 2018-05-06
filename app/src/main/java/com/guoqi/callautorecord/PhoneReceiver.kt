package com.guoqi.callautorecord

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log


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

    private var context: Context? = null
    /*var listener: PhoneStateListener = object : PhoneStateListener() {

        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            super.onCallStateChanged(state, incomingNumber)
            when (state) {
                TelephonyManager.CALL_STATE_IDLE
                -> {
                    if (isHujiao && !isGuaduan) {
                        Log.e(TAG, "等待拨号,然后通话")
                        isHujiao = false
                        isZhujiaoTonghua = true
                    } else if (isZhujiaoTonghua && !isGuaduan ) {
                        Log.e(TAG, "呼叫:挂断电话")
                        isZhujiaoTonghua = false
                        isGuaduan = true
                    } else if (isLaiDian && !isGuaduan) {
                        Log.e(TAG, "等待接听,然后通话")
                        isLaidianTonghua = true
                        isLaiDian = false
                    } else if (isLaidianTonghua && !isGuaduan ) {
                        Log.e(TAG, "被叫:挂断电话")
                        isLaidianTonghua = false
                        isGuaduan = true
                    }

                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    Log.e(TAG, "摘机状态")
                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    // 来电状态，电话铃声响起的那段时间或正在通话又来新电，新来电话不得不等待的那段时间。
                    if (!isLaiDian) {
                        Log.e(TAG, "响铃:来电号码$incomingNumber")
                        isLaiDian = true
                        isGuaduan = false
                    }
                }
                else -> {
                    Log.e(TAG, "其他状态")
                }
            }
        }
    }*/


    override fun onReceive(context: Context, intent: Intent) {
        this.context = context

        // 如果是去电
        if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            Log.e(TAG, "呼叫:$phoneNumber")
            isHujiao = true
            isGuaduan = false
        } else {
            val tm = context.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
            tm.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    companion object {
        val TAG = "CallAutoRecord"

        val callListener = CallListener()

        var isHujiao = false //呼叫
        var isZhujiaoTonghua = false //主叫通话
        var isGuaduan = true //挂断
        var isLaiDian = false //来电
        var isLaidianTonghua = false //来电通话
        var isLaidianZhaiji = false //来电摘机
    }

}
