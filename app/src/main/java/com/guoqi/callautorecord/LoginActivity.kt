package com.guoqi.callautorecord

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initData()

        btn_start.setOnClickListener {
            if (et_name.text.isNotEmpty()) {
                ACache.get(this).put("name", et_name.text.toString())
                var intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.putExtra("name", et_name.text.toString())
                startActivity(intent)
                finish()
            } else {
                et_name.error = "请输入姓名"
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
    }


}
