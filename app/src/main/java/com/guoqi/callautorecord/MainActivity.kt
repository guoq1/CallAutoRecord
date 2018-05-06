package com.guoqi.callautorecord

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_STORAGE = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")
    private val PERMISSIONS_RECORD = arrayOf("android.permission.RECORD_AUDIO")
    private val PERMISSIONS_CONTACTS = arrayOf("android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS", "android.permission.GET_ACCOUNTS")
    private val PERMISSIONS_PHONE = arrayOf("android.permission.READ_PHONE_STATE", "android.permission.CALL_PHONE", "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG", "android.permission.PROCESS_OUTGOING_CALLS")
    private var permissionListener = object : PermissionListener {
        override fun granted() {
        }

        override fun denied(deniedList: List<String>) {
            if (!deniedList.isEmpty()) {
                showPermissionManagerDialog(this@MainActivity, "相关")
            }
        }
    }

    companion object {
        val recordPath = Environment.getExternalStorageDirectory().absolutePath + File.separator + "CallAutoRecord"
    }

    private lateinit var pd: ProgressDialog
    private var recordList = ArrayList<RecordBean>()
    private lateinit var recordAdapter: RecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initPermission()
        initProgressDialog()

        initClick()
        recordAdapter = RecordAdapter(this, recordList)
        lv_record.adapter = recordAdapter
        initData()
        //initRecord()
        //上传
        getCurrentTime()
    }

    private fun getCurrentTime() {
        loadDialog()
        val url = "http://47.93.160.233/jsyf-oa/system/getCurrentTime.json"
        OkGo.post<String>(url)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        Log.e("JSON", response?.body().toString())
                        removeDialog()
                    }

                    override fun onError(response: Response<String>?) {
                        Log.e("JSON", response?.body().toString())
                        removeDialog()
                        super.onError(response)
                    }

                })
    }

    private fun initProgressDialog() {
        pd = ProgressDialog(this)
        pd.setMessage("加载中，请稍后")
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pd.setCanceledOnTouchOutside(false)
        pd.setCancelable(true)
    }

    private fun initData() {
        var fileList = FileUtil.listFilesInDir(getPath())
        if (fileList != null && fileList.isNotEmpty()) {
            recordList.clear()
            for (file in fileList) {
                var item = RecordBean()
                item.fileName = file.name
                item.filePath = file.absolutePath
                recordList.add(item)
            }
        }
        if (recordList.isEmpty()) {
            //显示空
            tv_none.visibility = View.VISIBLE
            lv_record.visibility = View.GONE
        } else {
            //隐藏空
            tv_none.visibility = View.GONE
            lv_record.visibility = View.VISIBLE

            recordAdapter.resetData(recordList)
        }
    }

    private fun initClick() {
        fab.setOnClickListener { _ ->
            toCallPhoneUI("")
        }
        lv_record.setOnItemClickListener { adapterView, view, i, l ->
            var item = adapterView.adapter.getItem(i) as RecordBean
            //Snackbar.make(view, item.fileName, Snackbar.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", File(item.filePath))
                intent.setDataAndType(uri, getMimeType(item.filePath))
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                intent.setDataAndType(Uri.fromFile(File(item.filePath)), getMimeType(item.filePath))
            }
            startActivity(intent)
        }
    }

    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestRuntimePermissions(PERMISSIONS_STORAGE + PERMISSIONS_RECORD + PERMISSIONS_CONTACTS + PERMISSIONS_PHONE, permissionListener)
        }
    }

    private fun getPath(): String {
        val file = File(recordPath)
        return if (file.mkdirs()) {
            recordPath
        } else recordPath
    }

    private fun getDuration(path: String) {
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
        Log.e("CallAutoRecord", "### duration: $duration");
        player.release()
    }


    private fun initRecord() {
        var serviceIntent = Intent(this@MainActivity, CallRecorderService::class.java)
        startService(serviceIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                initData()
                true
            }
            else -> super.onOptionsItemSelected(item)
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


    fun showPermissionManagerDialog(context: Context, str: String) {
        AlertDialog.Builder(context).setTitle("获取" + str + "权限被禁用")
                .setMessage("请在 设置-应用管理-" + context.getString(R.string.app_name) + "-权限管理 (将" + str + "权限打开)")
                .setNegativeButton("取消", null)
                .setPositiveButton("去设置", DialogInterface.OnClickListener { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:" + context.packageName)
                    context.startActivity(intent)
                }).show()
    }

    /**
     * 跳转至拨号界面
     */
    private fun toCallPhoneUI(phoneNumber: String) {
        phoneNumber.isEmpty().let {
            val uri = Uri.parse("tel:$phoneNumber")
            val call = Intent(Intent.ACTION_DIAL, uri)
            startActivity(call)
        }
    }

    private fun getMimeType(filePath: String?): String {
        val mmr = MediaMetadataRetriever()
        var mime = "application/*"
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath)
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            } catch (e: IllegalStateException) {
                return mime
            } catch (e: IllegalArgumentException) {
                return mime
            } catch (e: RuntimeException) {
                return mime
            }

        }
        Log.e("CallAutoRecord", "getFileMime = $mime")
        return mime
    }

    fun loadDialog() {
        pd.show()
    }

    fun removeDialog() {
        if (pd.isShowing) {
            pd.dismiss()
        }
    }
}
