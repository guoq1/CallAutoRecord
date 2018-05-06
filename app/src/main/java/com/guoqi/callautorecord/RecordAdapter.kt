package com.guoqi.callautorecord

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.guoqi.callautorecord.MainActivity.Companion.IP
import com.lzy.okgo.model.HttpParams
import java.io.IOException


class RecordAdapter(var context: Context, var datas: List<RecordBean>) : BaseAdapter() {

    override fun getCount(): Int {
        return if (datas.isEmpty()) 0 else datas.size
    }

    override fun getItem(position: Int): RecordBean? {
        return if (datas.isEmpty()) null else datas[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
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
            viewHolder.tv_name = convertView!!.findViewById<View>(R.id.tv_name) as TextView
            viewHolder.tv_upload = convertView!!.findViewById<View>(R.id.tv_upload) as TextView
            viewHolder.tv_del = convertView!!.findViewById<View>(R.id.tv_del) as TextView
            convertView.tag = viewHolder
        }

        viewHolder.tv_name?.text = datas[position].fileName
        viewHolder.tv_upload?.setOnClickListener {
            Upload(datas[position])
        }
        viewHolder.tv_del?.setOnClickListener {

        }
        return convertView
    }

    internal class ViewHolder {
        var tv_name: TextView? = null
        var tv_upload: TextView? = null
        var tv_del: TextView? = null
    }


    private fun Upload(recordBean: RecordBean) {
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
        param.put("phone", getNativePhoneNumber())
        param.put("customerPhone", cusPhoneName)
        param.put("callData", "$year-$month-$day")
        param.put(" callTime", "$hour:$min:$sec")
        param.put(" timeLength", getDuration(recordBean.filePath))
        param.put(" tapeUrl", "")
//        OkGo.post<String>(url)
//                .params(param)
//                .execute(object : StringCallback() {
//                    override fun onSuccess(response: Response<String>?) {
//                        Log.e("JSON", response?.body().toString())
//                        //true
//                    }
//
//                    override fun onError(response: Response<String>?) {
//                        Log.e("JSON", response?.body().toString())
//                        super.onError(response)
//                    }
//
//                })
    }


    @SuppressLint("MissingPermission")
    private fun getNativePhoneNumber(): String? {
        var nativePhoneNumber: String? = null
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        nativePhoneNumber = telephonyManager.line1Number
        return nativePhoneNumber
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
        Log.e("CallAutoRecord", "### duration: $duration");
        player.release()
        return duration
    }

}
