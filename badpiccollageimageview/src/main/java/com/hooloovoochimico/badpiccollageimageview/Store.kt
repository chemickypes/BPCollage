package com.hooloovoochimico.badpiccollageimageview

import android.graphics.Bitmap


object ImageStore {
    fun clear() {
        bitmap = null
        entities.clear()
    }

    var bitmap:Bitmap? = null
    val entities = mutableListOf<MotionEntity>()
}