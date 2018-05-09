package com.guoqi.callautorecord

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.guoqi.callautorecord.MainActivity.Companion.IP
import com.guoqi.callautorecord.MyApplication.Companion.context
import com.guoqi.callautorecord.PhoneReceiver.Companion.TAG
import com.guoqi.callautorecord.PhoneReceiver.Companion.file
import com.guoqi.callautorecord.PhoneReceiver.Companion.isLaidianZhaiji
import com.guoqi.callautorecord.PhoneReceiver.Companion.isRecord
import com.guoqi.callautorecord.PhoneReceiver.Companion.number
import com.guoqi.callautorecord.PhoneReceiver.Companion.recorder
import com.guoqi.callautorecord.PhoneReceiver.Companion.vibrator
import com.guoqi.callautorecord.SetActivity.Companion.FILTER
import com.guoqi.callautorecord.SetActivity.Companion.FILTER_TIME
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
        file = File(MainActivity.recordPath, "$recordTitle.3gp")
        FileUtil.createOrExistsFile(file)
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)//存储格式
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
        if (ACache.get(MyApplication.context).getAsObject(SetActivity.VIRBATE) != null && ACache.get(MyApplication.context).getAsObject(SetActivity.VIRBATE) as Boolean) {
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
            if (ACache.get(MyApplication.context).getAsObject(SetActivity.VIRBATE) != null && ACache.get(MyApplication.context).getAsObject(SetActivity.VIRBATE) as Boolean) {
                vibrator.vibrate(100)
            }

            //删除0KB的文件
            Log.e(TAG, "文件名称 = " + file?.name)
            Log.e(TAG, "文件大小 = " + file?.length())
            if (file?.length() == 0L) {
                FileUtil.deleteFile(file)
            }

            //如果开启过滤
            if (ACache.get(MyApplication.context).getAsObject(FILTER) != null && ACache.get(MyApplication.context).getAsObject(FILTER) as Boolean) {
                var filterTime = ACache.get(MyApplication.context).getAsObject(FILTER_TIME) as Int
                if (filterTime != null && getDuration(file?.absolutePath!!) < filterTime * 1000) {
                    FileUtil.deleteFile(file)
                }
            }

            android.os.Handler().postDelayed({
                if (FileUtil.isFileExists(file)) {
                    var item = RecordBean()
                    item.fileName = file!!.name
                    item.filePath = file!!.absolutePath
                    item.name = ACache.get(context).getAsString("name")
                    //录音文件自动上传
                    uploadTape(item)
                }
            }, 3000)

        }
    }

    //上传文件
    private fun uploadTape(record: RecordBean) {
        OkGo.post<String>("$IP/uploadTape")
                .isMultipart(true)
                .params("file", File(record.filePath))
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        Log.e("JSON onSuccess", response?.body().toString())
                        var tapeUrl = response?.body().toString()
                        if (tapeUrl != null && !tapeUrl.isEmpty()) {
                            Log.e(TAG, "上传录音成功")
                            upLoadRecord(record, tapeUrl)
                        } else {
                            Toast.makeText(context, "上传通话成功，返回路径为空", Toast.LENGTH_LONG).show();
                        }
                    }

                    override fun onError(response: Response<String>?) {
                        Log.e("JSON onError", response?.exception?.message)
                        super.onError(response)
                        Log.e(TAG, "上传录音失败")
                        Toast.makeText(context, "上传通话失败", Toast.LENGTH_LONG).show();
                    }
                })

    }

    /**
     * 上传记录
     */
    private fun upLoadRecord(recordBean: RecordBean, tapeUrl: String) {
        val url = "$IP/visitRecord/save"
        var cusPhoneName = recordBean.fileName.split("_")[0]
        var callDate = recordBean.fileName.split("_")[1]
        var year = callDate.substring(0, 4)
        var month = callDate.substring(4, 6)
        var day = callDate.substring(6, 8)
        var hour = callDate.substring(8, 10)
        var min = callDate.substring(10, 12)
        var sec = callDate.substring(12, 14)
        var param = HttpParams()
        param.put("name", ACache.get(context).getAsString("name"))
        param.put("phone", getNativePhoneNumber())
        param.put("customerPhone", cusPhoneName)
        param.put("callData", "$year-$month-$day")
        param.put("callTime", "$hour:$min:$sec")
        param.put("timeLength", getDuration(recordBean.filePath))
        param.put("tapeUrl", tapeUrl)
        OkGo.post<String>(url)
                .params(param)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        Log.e("JSON onSuccess", response?.body().toString())
                        if (response?.body().toString().toLowerCase() == "true") {
                            Toast.makeText(context, "上传成功", Toast.LENGTH_LONG).show()
                            Log.e(TAG, "上传记录成功")
                            //添加到本地上传记录
                            var recordList = ArrayList<RecordBean>()
                            if (ACache.get(context).getAsString("record") != null) {
                                recordList = JSON.parseArray(ACache.get(context).getAsString("record"), RecordBean::class.java) as ArrayList<RecordBean>
                            }
                            recordList.add(recordBean)
                            ACache.get(context).put("record", JSON.toJSONString(recordList))
                            //上传成功,删除本地文件
                            FileUtil.deleteFile(recordBean.filePath)
                            Log.e(TAG, "上传成功后,删除本地文件")


                        } else {
                            Toast.makeText(context, "保存记录失败", Toast.LENGTH_LONG).show()
                            Log.e(TAG, "保存记录失败")
                        }
                    }

                    override fun onError(response: Response<String>?) {
                        Log.e("JSON onError", response?.body().toString())
                        super.onError(response)
                        Toast.makeText(context, "保存记录错误", Toast.LENGTH_LONG).show()
                    }

                })
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

    @SuppressLint("MissingPermission")
    private fun getNativePhoneNumber(): String? {
        var nativePhoneNumber: String? = null
        val telephonyManager = MyApplication.context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        nativePhoneNumber = telephonyManager.line1Number
        return nativePhoneNumber
    }
}