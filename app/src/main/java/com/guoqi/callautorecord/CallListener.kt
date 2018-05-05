package com.guoqi.callautorecord

import android.media.MediaRecorder
import android.os.Environment
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import java.io.File
import java.io.IOException

class CallListener : PhoneStateListener(), MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
    private var number: String? = null
    private var isRecord: Boolean = false
    private var recorder: MediaRecorder? = null

    init {
        this.recorder = MediaRecorder()
    }

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        super.onCallStateChanged(state, incomingNumber)
        when (state) {
            TelephonyManager.CALL_STATE_IDLE//空闲状态
            -> {
                number = null
                if (recorder != null && isRecord) {
                    recorder!!.stop()//停止录音
                    recorder!!.reset()
                    recorder!!.release()
                    isRecord = false
                    Log.e("CallAutoRecorder", "停止录音")
                }
            }
            TelephonyManager.CALL_STATE_OFFHOOK//接听状态
            -> {
                number = incomingNumber
                val recordTitle = number + "_" + System.currentTimeMillis().toString()
                val fileDir = File(Environment.getExternalStorageDirectory().toString() + File.separator + "CallAutoRecorder")
                if (!fileDir.exists()) {
                    fileDir.mkdirs()
                }
                val file = File(fileDir.absolutePath, "$recordTitle.amr")

                recorder!!.reset()
                recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder!!.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)//存储格式
                recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)//设置编码
                recorder!!.setOutputFile(file.absolutePath)
                recorder!!.setOnInfoListener(this)
                recorder!!.setOnErrorListener(this)

                try {
                    recorder!!.prepare()
                } catch (e: IOException) {
                    Log.e("CallAutoRecorder", "RecordService::onStart() IOException attempting recorder.prepare()\n")
                    recorder = null
                    return
                }
                Log.e("CallAutoRecorder", "开始录音")
                recorder!!.start() // 开始录音
                isRecord = true
            }
            TelephonyManager.CALL_STATE_RINGING//响铃:来电号码
            -> number = incomingNumber
            else -> {
            }
        }
    }

    override fun onError(mr: MediaRecorder, what: Int, extra: Int) {
        isRecord = false
        mr.release()
    }

    override fun onInfo(mr: MediaRecorder, what: Int, extra: Int) {
        isRecord = false
    }
}
