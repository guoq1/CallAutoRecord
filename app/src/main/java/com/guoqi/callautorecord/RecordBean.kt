package com.guoqi.callautorecord

import java.io.Serializable

/**
 * Created by GUOQI on 2017/12/29.
 */
class RecordBean : Serializable {

    var id: String = ""
    var fileName: String = ""
    var filePath: String = ""
    var name: String = ""
    var customerPhone: String = ""
    var callData: String = ""
    var callTime: String = ""
    var timeLength: String = ""
    var tapeUrl: String = ""

    constructor()
}