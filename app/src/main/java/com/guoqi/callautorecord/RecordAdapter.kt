package com.guoqi.callautorecord

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.guoqi.callautorecord.MainActivity.Companion.IP
import com.guoqi.callautorecord.PhoneReceiver.Companion.TAG
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import java.io.File
import java.io.IOException


class RecordAdapter(var context: Context, var datas: List<RecordBean>) : BaseAdapter() {

    private var needUpload: Boolean = false
    override fun getCount(): Int {
        return if (datas.isEmpty()) 0 else datas.size
    }

    override fun getItem(position: Int): RecordBean? {
        return if (datas.isEmpty()) null else datas[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setNeedUpload(needUpload: Boolean) {
        this.needUpload = needUpload
    }

    /**
     * 更新数据
     */
    fun resetData(datas: List<RecordBean>) {
        this.datas = datas
        this.notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView != null) {
            viewHolder = convertView.tag as ViewHolder
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_item, null)
            viewHolder = ViewHolder()
            viewHolder.ll_item = convertView!!.findViewById<View>(R.id.ll_item) as LinearLayout
            viewHolder.tv_name = convertView!!.findViewById<View>(R.id.tv_name) as TextView
            viewHolder.tv_time = convertView!!.findViewById<View>(R.id.tv_time) as TextView
            viewHolder.tv_upload = convertView!!.findViewById<View>(R.id.tv_upload) as TextView
            viewHolder.tv_del = convertView!!.findViewById<View>(R.id.tv_del) as TextView
            convertView.tag = viewHolder
        }

        if (needUpload) {
            viewHolder.tv_name?.text = datas[position].fileName
            viewHolder.tv_upload?.visibility = View.VISIBLE
            viewHolder.tv_time?.visibility = View.GONE
            viewHolder.tv_upload?.setOnClickListener {
                showConfirmDialog(datas[position], "确定要上传这条通话记录吗？")
            }
            viewHolder.tv_del?.visibility = View.VISIBLE
            viewHolder.tv_del?.setOnClickListener {
                showConfirmDialog(datas[position], "确定要删除这条通话记录吗？")
            }

            viewHolder.ll_item?.setOnClickListener {
                var intent = Intent(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", File(datas[position].filePath))
                    intent.setDataAndType(uri, getMimeType(datas[position].filePath))
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    intent.setDataAndType(Uri.fromFile(File(datas[position].filePath)), getMimeType(datas[position].filePath))
                }
                context.startActivity(intent)
            }
        } else {
            viewHolder.tv_name?.text = datas[position].customerPhone
            viewHolder.tv_time?.text = datas[position].callData + " " + datas[position].callTime
            viewHolder.tv_time?.visibility = View.VISIBLE
            viewHolder.tv_upload?.visibility = View.GONE
            viewHolder.tv_del?.visibility = View.GONE
            viewHolder.ll_item?.setOnClickListener { null }
        }
        return convertView
    }

    internal class ViewHolder {
        var ll_item: LinearLayout? = null
        var tv_name: TextView? = null
        var tv_time: TextView? = null
        var tv_upload: TextView? = null
        var tv_del: TextView? = null
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
        //Log.e("CallAutoRecord", "getFileMime = $mime")
        return mime
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
        param.put("phone", ACache.get(context).getAsString("tel"))
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
                            //上传成功,删除本地文件
                            FileUtil.deleteFile(recordBean.filePath)
                            recordChangeListener?.onRecordChange()
                        } else {
                            Toast.makeText(context, "保存记录失败", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onError(response: Response<String>?) {
                        Log.e("JSON onError", response?.body().toString())
                        super.onError(response)
                        Toast.makeText(context, "保存记录错误", Toast.LENGTH_LONG).show()
                    }

                })
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
                            upLoadRecord(record, tapeUrl)
                        } else {
                            Toast.makeText(context, "上传通话成功，返回路径为空", Toast.LENGTH_LONG).show();
                        }
                    }

                    override fun onError(response: Response<String>?) {
                        Log.e("JSON onError", response?.body().toString())
                        super.onError(response)
                        Toast.makeText(context, "上传通话失败", Toast.LENGTH_LONG).show();
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

    private fun showConfirmDialog(record: RecordBean, str: String) {
        AlertDialog.Builder(context).setTitle("提示")
                .setMessage(str)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", DialogInterface.OnClickListener { _, _ ->
                    if (str.contains("上传")) {
                        uploadTape(record)
                    }
                    if (str.contains("删除")) {
                        FileUtil.deleteFile(record.filePath)
                        recordChangeListener?.onRecordChange()
                    }
                }).show()
    }

    private var recordChangeListener: RecordChangeListener? = null

    interface RecordChangeListener {
        fun onRecordChange()
    }

    fun setOnRecordChangeListener(listener: RecordChangeListener) {
        this.recordChangeListener = listener
    }

}

