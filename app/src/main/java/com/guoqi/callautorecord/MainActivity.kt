package com.guoqi.callautorecord

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    companion object {
        val IP = "http://www.119sx.cn"
        val recordPath = Environment.getExternalStorageDirectory().absolutePath + File.separator + "CallAutoRecord"
    }

    private lateinit var pd: ProgressDialog
    private var recordList = ArrayList<RecordBean>()
    private var uploadList = ArrayList<RecordBean>()
    private lateinit var recordAdapter: RecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        initProgressDialog()

        initClick()
        recordAdapter = RecordAdapter(this, recordList)
        lv_record.adapter = recordAdapter
        recordAdapter.setOnRecordChangeListener(object : RecordAdapter.RecordChangeListener {
            override fun onRecordChange() {
                getUploadList()
                changeTab(1)
            }
        })
        initRecord()
        changeTab(0)
    }

    override fun onResume() {
        super.onResume()
        setTitle()
    }

    private fun setTitle() {
        if (ACache.get(this).getAsString("name") == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        } else {
            toolbar.title = resources.getString(R.string.app_name) + (" (" + ACache.get(this).getAsString("name") + ")")
            initData()
        }
    }

    private fun initProgressDialog() {
        pd = ProgressDialog(this)
        pd.setMessage("加载中，请稍后")
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pd.setCanceledOnTouchOutside(false)
        pd.setCancelable(true)
    }

    private fun changeTab(pos: Int) {
        if (pos == 0) {
            tv_record.setBackgroundResource(android.R.color.holo_green_dark)
            tv_record.setTextColor(resources.getColor(android.R.color.white))
            tv_upload.setBackgroundResource(android.R.color.darker_gray)
            tv_upload.setTextColor(resources.getColor(android.R.color.white))
            if (recordList.isEmpty()) {
                //显示空
                tv_none.visibility = View.VISIBLE
                lv_record.visibility = View.GONE
            } else {
                //隐藏空
                tv_none.visibility = View.GONE
                lv_record.visibility = View.VISIBLE
                recordAdapter.setNeedUpload(false)
                recordAdapter.resetData(recordList)
            }
        } else {
            tv_upload.setBackgroundResource(android.R.color.holo_green_dark)
            tv_upload.setTextColor(resources.getColor(android.R.color.white))
            tv_record.setBackgroundResource(android.R.color.darker_gray)
            tv_record.setTextColor(resources.getColor(android.R.color.white))
            if (recordList.isEmpty()) {
                //显示空
                tv_none.visibility = View.VISIBLE
                lv_record.visibility = View.GONE
            } else {
                //隐藏空
                tv_none.visibility = View.GONE
                lv_record.visibility = View.VISIBLE
                recordAdapter.setNeedUpload(true)
                recordAdapter.resetData(uploadList)
            }
        }

    }

    private fun initData() {
        //已上传的
        getRecordHistory()
        //未上传的
        getUploadList()
    }

    private fun getUploadList() {
        uploadList.clear()
        var fileList = FileUtil.listFilesInDir(getPath())
        if (fileList != null && fileList.isNotEmpty()) {
            for (file in fileList) {
                if (file.length() > 0) {
                    var item = RecordBean()
                    item.fileName = file.name
                    item.filePath = file.absolutePath
                    uploadList.add(item)
                } else {
                    FileUtil.deleteFile(file)
                }
            }
        }
    }

    private fun getRecordHistory() {
        if (this == null || this.isFinishing) return
        pd.show()
        val url = "$IP/visitRecord/findForList"
        OkGo.post<String>(url)
                .params("param", ACache.get(this).getAsString("name"))
                .params("pageNo", 1)
                .params("pageSize", Int.MAX_VALUE)
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>?) {
                        Log.e("JSON onSuccess", response?.body().toString())
                        var obj = JSON.parseObject(response?.body().toString())
                        recordList = JSON.parseArray(obj.getString("content"), RecordBean::class.java) as ArrayList<RecordBean>
                        changeTab(0)
                        if (pd != null && pd.isShowing) {
                            pd.dismiss()
                        }
                    }

                    override fun onError(response: Response<String>?) {
                        super.onError(response)
                    }
                })

    }

    private fun initClick() {
        tv_record.setOnClickListener { changeTab(0) }
        tv_upload.setOnClickListener { changeTab(1) }
        fab.setOnClickListener { _ ->
            toCallPhoneUI("")
        }
    }


    private fun getPath(): String {
        val file = File(recordPath)
        return if (file.mkdirs()) {
            recordPath
        } else recordPath
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
            R.id.action_refresh -> {
                initData()
                Toast.makeText(this, "刷新成功", Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this@MainActivity, SetActivity::class.java))
                true
            }
            R.id.action_exit -> {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    fun loadDialog() {
        pd.show()
    }

    fun removeDialog() {
        if (pd.isShowing) {
            pd.dismiss()
        }
    }
}
