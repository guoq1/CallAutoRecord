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

![1](/screenshot/WechatIMG363.png)

2. 自定义功能(上传/删除录音)

![2](/screenshot/WechatIMG362.jpeg)

3. 简单设置页

![3](/screenshot/WechatIMG361.png)


#### 缕一遍流程,打印log日志

> 一开始为了缕清逻辑,写了多个标记位,洁癖患者可以改造一下,对不起,对不起!

1. 主叫

![1](/screenshot/1.png)

2. 被叫

![2](/screenshot/2.png)

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
                } else if (PhoneReceiver.isZhujiaoTonghua && !PhoneReceiver.isGuaduan) {
                    Log.e(PhoneReceiver.TAG, "呼叫:挂断电话")
                    stopRecord()
                    PhoneReceiver.isZhujiaoTonghua = false
                    PhoneReceiver.isGuaduan = true
                    number = ""
                } else if (PhoneReceiver.isLaiDian && !PhoneReceiver.isGuaduan && isLaidianZhaiji) {
                    Log.e(PhoneReceiver.TAG, "接听电话,然后通话")
                    PhoneReceiver.isLaidianTonghua = true
                    PhoneReceiver.isLaiDian = false
                    isLaidianZhaiji = false
                } else if (PhoneReceiver.isLaidianTonghua && !PhoneReceiver.isGuaduan) {
                    Log.e(PhoneReceiver.TAG, "被叫:挂断电话")
                    stopRecord()
                    PhoneReceiver.isLaidianTonghua = false
                    PhoneReceiver.isGuaduan = true
                    number = ""
                }

            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (PhoneReceiver.isZhujiaoZhaiji) {
                    Log.e(PhoneReceiver.TAG, "主叫:摘机状态")
                    isZhujiaoZhaiji = false
                    if (ACache.get(context).getAsObject(SetActivity.RULE) == null
                            || (ACache.get(context).getAsObject(SetActivity.RULE) as Int == 0)
                            || (ACache.get(context).getAsObject(SetActivity.RULE) as Int == 1)
                    ) {
                        prepareRecord()
                    }
                }
                if (PhoneReceiver.isLaiDian && !isLaidianZhaiji) {
                    Log.e(PhoneReceiver.TAG, "被叫:摘机状态")
                    isLaidianZhaiji = true
                    if (ACache.get(context).getAsObject(SetActivity.RULE) == null
                            || (ACache.get(context).getAsObject(SetActivity.RULE) as Int == 0)
                            || (ACache.get(context).getAsObject(SetActivity.RULE) as Int == 2)
                    ) {
                        prepareRecord()
                    }
                }
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                // 来电状态，电话铃声响起的那段时间或正在通话又来新电，新来电话不得不等待的那段时间。
                if (!PhoneReceiver.isLaiDian) {
                    number = incomingNumber
                    Log.e(PhoneReceiver.TAG, "响铃:来电号码$incomingNumber")
                    PhoneReceiver.isLaiDian = true
                    PhoneReceiver.isGuaduan = false
                    isLaidianZhaiji = false
                }
            }
        }
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

