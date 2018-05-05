package com.guoqi.callautorecord

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

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
     *
     * @param datas
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
            convertView.tag = viewHolder
        }

        viewHolder.tv_name?.text = datas[position].fileName
        return convertView
    }

    internal class ViewHolder {
        var tv_name: TextView? = null
    }
}
