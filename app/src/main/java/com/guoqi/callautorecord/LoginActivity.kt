package com.guoqi.callautorecord

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val PERMISSIONS_STORAGE = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")
    private val PERMISSIONS_RECORD = arrayOf("android.permission.RECORD_AUDIO")
    private val PERMISSIONS_CONTACTS = arrayOf("android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS", "android.permission.GET_ACCOUNTS")
    private val PERMISSIONS_PHONE = arrayOf("android.permission.READ_PHONE_STATE", "android.permission.CALL_PHONE", "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG", "android.permission.PROCESS_OUTGOING_CALLS")
    private var permissionListener = object : PermissionListener {
        override fun granted() {
            initData()
        }

        override fun denied(deniedList: List<String>) {
            if (!deniedList.isEmpty()) {
                //showPermissionManagerDialog(this@MainActivity, "相关")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_start.setOnClickListener {
            if (et_name.text.isEmpty()) {
                et_name.error = "请输入姓名"
            }
            if (et_tel.text.isEmpty()) {
                et_tel.error = "请输入本机号码"
            }
            if (et_name.text.isNotEmpty() && et_tel.text.isNotEmpty()) {
                ACache.get(this).put("name", et_name.text.toString())
                ACache.get(this).put("tel", et_tel.text.toString())
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun initData() {
        if (ACache.get(this).getAsString("name") != null) {
            var name = ACache.get(this).getAsString("name")
            if (name.isNotEmpty()) {
                et_name.setText(name)
                et_name.setSelection(name.length)
            }
        }

        if (ACache.get(this).getAsString("tel") != null) {
            var tel = ACache.get(this).getAsString("tel")
            if (tel.isNotEmpty()) {
                et_tel.setText(tel)
                et_tel.setSelection(tel.length)
            }
        } else {
            if (getNativePhoneNumber() == null) {
                //获取手机号失败
            } else {
                et_tel.setText(getNativePhoneNumber())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNativePhoneNumber(): String? {
        var nativePhoneNumber: String? = null
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        nativePhoneNumber = telephonyManager.line1Number
        return nativePhoneNumber
    }

    override fun onResume() {
        super.onResume()
        initPermission()
    }

    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestRuntimePermissions(PERMISSIONS_STORAGE + PERMISSIONS_RECORD + PERMISSIONS_CONTACTS + PERMISSIONS_PHONE, permissionListener)
        }
    }

    /**
     * 申请权限
     */
    private fun requestRuntimePermissions(permissions: Array<String>, listener: PermissionListener) {
        var permissionList = ArrayList<String>()
        // 遍历每一个申请的权限，把没有通过的权限放在集合中
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !== PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission)
            } else {
                permissionListener?.granted()
            }
        }
        // 申请权限
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 1)
        }
    }

    /**
     * 申请后的处理
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            val deniedList = ArrayList<String>()
            for (i in grantResults.indices) {
                val grantResult = grantResults[i]
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    permissionListener!!.granted()
                } else {
                    deniedList.add(permissions[i])
                }
            }
            if (!deniedList.isEmpty()) {
                permissionListener!!.denied(deniedList)
            } else {
                initData()
            }
        }
    }
}
