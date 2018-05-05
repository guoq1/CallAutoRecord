package com.guoqi.callautorecord

import android.annotation.SuppressLint
import android.media.MediaRecorder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.guoqi.callautorecord.MainActivity.Companion.recordPath
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CallListener : PhoneStateListener(), MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
    private var number: String? = null
    private var isRecord: Boolean = false
    private var recorder: MediaRecorder? = null

    init {
        this.recorder = MediaRecorder()
    }

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        when (state) {
            TelephonyManager.CALL_STATE_IDLE//空闲状态
            -> {
                number = null
                Log.e("CallAutoRecord", "空闲状态")
                if (recorder != null && isRecord) {
                    recorder!!.stop()//停止录音
                    recorder!!.reset()
                    recorder!!.release()
                    isRecord = false
                    Log.e("CallAutoRecord", "停止录音")
                }
            }
            TelephonyManager.CALL_STATE_OFFHOOK//接听状态
            -> {
                Log.e("CallAutoRecord", "接听状态,开始录音")
                number = incomingNumber
                recorder!!.start() // 开始录音
                isRecord = true
            }
            TelephonyManager.CALL_STATE_RINGING//响铃:来电号码
            -> {
                Log.e("CallAutoRecord", "响铃状态")
                number = incomingNumber
                recodeFun();
            }
        }
        super.onCallStateChanged(state, incomingNumber)
    }

    private fun recodeFun() {
        val recordTitle = number + "_" + getCurrentDate()
        val file = File(recordPath, "$recordTitle.amr")
        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)//存储格式
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)//设置编码
        recorder!!.setOutputFile(file.absolutePath)
        try {
            recorder!!.prepare()
        } catch (e: IOException) {
            Log.e("CallAutoRecord", "RecordService::onStart() IOException attempting recorder.prepare()\n")
            recorder = null
        }
    }

    override fun onError(mr: MediaRecorder, what: Int, extra: Int) {
        isRecord = false
        Log.e("CallRecorder", "RecordService got MediaRecorder onError callback with what: " + what + " extra: " + extra);
        mr.release()
    }

    override fun onInfo(mr: MediaRecorder, what: Int, extra: Int) {
        Log.i("CallRecorder", "RecordService got MediaRecorder onInfo callback with what: " + what + " extra: " + extra);
        isRecord = false
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HHmmss")
        return formatter.format(Date())
    }

}
