package com.guoqi.callautorecord

interface PermissionListener {
//    CALENDAR（日历）
//    READ_CALENDAR
//    WRITE_CALENDAR
//    CAMERA（相机）
//    CAMERA
//    CONTACTS（联系人）
//    READ_CONTACTS
//    WRITE_CONTACTS
//    GET_ACCOUNTS
//    LOCATION（位置）
//    ACCESS_FINE_LOCATION
//    ACCESS_COARSE_LOCATION
//    MICROPHONE（麦克风）
//    RECORD_AUDIO
//    PHONE（手机）
//    READ_PHONE_STATE
//    CALL_PHONE
//    READ_CALL_LOG
//    WRITE_CALL_LOG
//    ADD_VOICEMAIL
//    USE_SIP
//    PROCESS_OUTGOING_CALLS
//    SENSORS（传感器）
//    BODY_SENSORS
//    SMS（短信）
//    SEND_SMS
//    RECEIVE_SMS
//    READ_SMS
//    RECEIVE_WAP_PUSH
//    RECEIVE_MMS
//    STORAGE（存储卡）
//    READ_EXTERNAL_STORAGE
//    WRITE_EXTERNAL_STORAGE

    fun granted()
    fun denied(deniedList: List<String>)
}

