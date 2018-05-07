package com.guoqi.callautorecord

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.guoqi.callautorecord.PhoneReceiver.Companion.TAG
import com.guoqi.callautorecord.PhoneReceiver.Companion.file
import com.guoqi.callautorecord.PhoneReceiver.Companion.isLaidianZhaiji
import com.guoqi.callautorecord.PhoneReceiver.Companion.isRecord
import com.guoqi.callautorecord.PhoneReceiver.Companion.number
import com.guoqi.callautorecord.PhoneReceiver.Companion.recorder
import com.guoqi.callautorecord.PhoneReceiver.Companion.vibrator
import com.guoqi.callautorecord.SetActivity.Companion.FILTER
import com.guoqi.callautorecord.SetActivity.Companion.FILTER_TIME
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CallListener : PhoneStateListener() {

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        super.onCallStateChanged(state, incomingNumber)
        when (state) {
            TelephonyManager.CALL_STATE_IDLE
            -> {
                if (PhoneReceiver.isHujiao && !PhoneReceiver.isGuaduan) {
                    Log.e(PhoneReceiver.TAG, "等待拨号,然后通话")
                    PhoneReceiver.isHujiao = false
                    PhoneReceiver.isZhujiaoTonghua = true
                    prepareRecord()
                } else if (PhoneReceiver.isZhujiaoTonghua && !PhoneReceiver.isGuaduan) {
                    Log.e(PhoneReceiver.TAG, "呼叫:挂断电话")
                    stopRecord()
                    PhoneReceiver.isZhujiaoTonghua = false
                    PhoneReceiver.isGuaduan = true
                    number = ""
                } else if (PhoneReceiver.isLaiDian && !PhoneReceiver.isGuaduan) {
                    Log.e(PhoneReceiver.TAG, "等待接听,然后通话")
                    PhoneReceiver.isLaidianTonghua = true
                    PhoneReceiver.isLaiDian = false
                } else if (PhoneReceiver.isLaidianTonghua && !PhoneReceiver.isGuaduan && !isLaidianZhaiji) {
                    Log.e(PhoneReceiver.TAG, "被叫:挂断电话")
                    stopRecord()
                    PhoneReceiver.isLaidianTonghua = false
                    PhoneReceiver.isGuaduan = true
                    number = ""
                } else if (isLaidianZhaiji) {
                    isLaidianZhaiji = false
                }

            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                Log.e(PhoneReceiver.TAG, "摘机状态")
                if (PhoneReceiver.isLaidianTonghua) {
                    isLaidianZhaiji = true
                    prepareRecord()
                }
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                // 来电状态，电话铃声响起的那段时间或正在通话又来新电，新来电话不得不等待的那段时间。
                if (!PhoneReceiver.isLaiDian) {
                    number = incomingNumber
                    Log.e(PhoneReceiver.TAG, "响铃:来电号码$incomingNumber")
                    PhoneReceiver.isLaiDian = true
                    PhoneReceiver.isGuaduan = false
                }
            }
        }
    }


    private fun prepareRecord() {
        var recordTitle = number + "_" + getCurrentDate()
        file = File(MainActivity.recordPath, "$recordTitle.amr")
        FileUtil.createOrExistsFile(file)
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)//存储格式
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)//设置编码
        recorder?.setOutputFile(file?.absolutePath)
        try {
            recorder?.prepare()
            startRecord()
        } catch (e: IOException) {
            Log.e(TAG, "RecordService::onStart() IOException attempting recorder.prepare()\n" + e.printStackTrace())
        }

    }

    private fun startRecord() {
        recorder?.start()
        isRecord = true
        if (ACache.get(MyApplication.context).getAsObject(SetActivity.VIRBATE) as Boolean) {
            vibrator.vibrate(100)
        }
        Log.e(TAG, "开始录音")
    }


    private fun stopRecord() {
        if (isRecord && recorder != null) {
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
            isRecord = false
            recorder = null
            Log.e(TAG, "停止录音")
            if (ACache.get(MyApplication.context).getAsObject(SetActivity.VIRBATE) as Boolean) {
                vibrator.vibrate(100)
            }

            //删除0KB的文件
            Log.e(TAG, "文件名称 = " + file?.name)
            Log.e(TAG, "文件大小 = " + file?.length())
            if (file?.length() == 0L) {
                FileUtil.deleteFile(file)
            } else {
                //Thread(UploadTask()).start()  //录音文件自动上传
            }
            //如果开启过滤
            if (ACache.get(MyApplication.context).getAsObject(FILTER) as Boolean) {
                var filterTime = ACache.get(MyApplication.context).getAsObject(FILTER_TIME) as Int
                if (filterTime != null && getDuration(file?.absolutePath!!) < filterTime * 1000) {
                    FileUtil.deleteFile(file)
                }
            }

        }
    }

    private fun getDuration(path: String): Int {
        var player = MediaPlayer();
        try {
            player.setDataSource(path)  //recordingFilePath（）为音频文件的路径
            player.prepare();
        } catch (e: IOException) {
            e.printStackTrace();
        } catch (e: Exception) {
            e.printStackTrace();
        }
        var duration = player.duration;//获取音频的时间
        Log.e(TAG, "### duration: $duration");
        player.release()
        return duration
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyyMMddHHmmss")
        return formatter.format(Date())
    }
}