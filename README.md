# CallAutoRecord

#### 项目介绍
通话自动录音


#### 使用说明

1. 通过接收广播和PhoneStateListener实现
2. 支持主叫和被叫录音
3. 增加录音震动提示
4. 自动删除时长为0的录音文件
5. 可过滤,不保存15s-60s录音(根据需要自行修改成自定义时长)
6. 简单处理封装6.0运行权限
7. 播放录音音频调用系统音乐播放器,兼容7.0,使用FileProvider


#### 效果图

1. 录音列表
![1](/CallAutoRecord /screenSpot/WechatIMG363.png)

2. 自定义功能(上传/删除录音)
![2](https://gitee.com/madaigou/CallAutoRecord/blob/master/screenSpot/WechatIMG362.jpeg)

3. 简单设置页
![3](https://gitee.com/madaigou/CallAutoRecord/blob/master/screenSpot/WechatIMG361.png)


#### 缕一遍流程,打印log日志

> 一开始为了缕清逻辑,写了多个标记位,洁癖患者可以改造一下,对不起,对不起!

1. 主叫
![1](https://gitee.com/madaigou/CallAutoRecord/blob/master/screenSpot/1.png)

2. 被叫
![2](https://gitee.com/madaigou/CallAutoRecord/blob/master/screenSpot/2.png)

#### 主要代码

##### 重写PhoneStateListener()
```java
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


```

##### 启动服务
```java
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
```

