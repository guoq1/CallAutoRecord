package com.guoqi.callautorecord

import android.media.MediaRecorder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.guoqi.callautorecord.PhoneReceiver.Companion.isLaidianZhaiji

class CallListener : PhoneStateListener() {

    var recorder: MediaRecorder? = null

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        super.onCallStateChanged(state, incomingNumber)
        when (state) {
            TelephonyManager.CALL_STATE_IDLE
            -> {
                if (PhoneReceiver.isHujiao && !PhoneReceiver.isGuaduan) {
                    Log.e(PhoneReceiver.TAG, "等待拨号,然后通话")
                    PhoneReceiver.isHujiao = false
                    PhoneReceiver.isZhujiaoTonghua = true
                } else if (PhoneReceiver.isZhujiaoTonghua && !PhoneReceiver.isGuaduan) {
                    Log.e(PhoneReceiver.TAG, "呼叫:挂断电话")
                    PhoneReceiver.isZhujiaoTonghua = false
                    PhoneReceiver.isGuaduan = true
                } else if (PhoneReceiver.isLaiDian && !PhoneReceiver.isGuaduan) {
                    Log.e(PhoneReceiver.TAG, "等待接听,然后通话")
                    PhoneReceiver.isLaidianTonghua = true
                    PhoneReceiver.isLaiDian = false
                } else if (PhoneReceiver.isLaidianTonghua && !PhoneReceiver.isGuaduan && !isLaidianZhaiji) {
                    Log.e(PhoneReceiver.TAG, "被叫:挂断电话")
                    PhoneReceiver.isLaidianTonghua = false
                    PhoneReceiver.isGuaduan = true
                } else if (isLaidianZhaiji) {
                    isLaidianZhaiji = false
                }

            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                Log.e(PhoneReceiver.TAG, "摘机状态")
                if (PhoneReceiver.isLaidianTonghua) {
                    isLaidianZhaiji = true
                }
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                // 来电状态，电话铃声响起的那段时间或正在通话又来新电，新来电话不得不等待的那段时间。
                if (!PhoneReceiver.isLaiDian) {
                    Log.e(PhoneReceiver.TAG, "响铃:来电号码$incomingNumber")
                    PhoneReceiver.isLaiDian = true
                    PhoneReceiver.isGuaduan = false
                }
            }
            else -> {
                Log.e(PhoneReceiver.TAG, "其他状态")
            }
        }
    }
}