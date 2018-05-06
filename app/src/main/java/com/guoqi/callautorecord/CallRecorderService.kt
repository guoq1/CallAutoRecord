package com.guoqi.callautorecord

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.IBinder
import android.os.Vibrator
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CallRecorderService : Service() {

    var number: String? = null
    var isRecord: Boolean = false
    var recorder: MediaRecorder? = null
    lateinit var vibrator: Vibrator

    override fun onCreate() {
        super.onCreate()
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(CallListener(), PhoneStateListener.LISTEN_CALL_STATE)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)

    }


    inner class CallListener : PhoneStateListener(), MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

        var context: Context = this@CallRecorderService

        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    number = null
                    Log.e("CallAutoRecord", "空闲状态")
                    Toast.makeText(context, "空闲状态", Toast.LENGTH_LONG).show();
                    if (recorder != null && isRecord) {
                        recorder!!.stop()//停止录音
                        recorder!!.reset()
                        recorder!!.release()
                        isRecord = false
                        Log.e("CallAutoRecord", "停止录音")
                        vibrator.vibrate(100)

                        Thread(UploadTask()).start()  //将录音文件上传
                    }
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    Log.e("CallAutoRecord", "接听状态,开始录音")
                    Toast.makeText(context, "接听状态,开始录音", Toast.LENGTH_LONG).show();
                    number = incomingNumber
                    recorder!!.start() // 开始录音
                    isRecord = true
                    vibrator.vibrate(100)
                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    Log.e("CallAutoRecord", "响铃状态")
                    Toast.makeText(context, "响铃状态", Toast.LENGTH_LONG).show();
                    number = incomingNumber
                    recodeFun()
                }
            }
            super.onCallStateChanged(state, incomingNumber)
        }

        private fun recodeFun() {
            var recordTitle = number + "_" + getCurrentDate()
            var file = File(MainActivity.recordPath, "$recordTitle.3gp")
            FileUtil.createOrExistsFile(file)
            recorder = MediaRecorder()
            recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)//存储格式
            recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)//设置编码
            recorder!!.setOutputFile(file.absolutePath)
            try {
                recorder!!.prepare()
            } catch (e: IOException) {
                Log.e("CallAutoRecord", "RecordService::onStart() IOException attempting recorder.prepare()\n")
                recorder = null
            }
        }


        private inner class UploadTask : Runnable {

            override fun run() {
                //上传文件操作
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
            val formatter = SimpleDateFormat("yyyyMMddHHmmss")
            return formatter.format(Date())
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "退出电话录音", Toast.LENGTH_LONG).show();
    }
}
