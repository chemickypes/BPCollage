package com.hooloovoochimico.badpiccollage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hooloovoochimico.badpiccollageimageview.DrawView
import kotlinx.android.synthetic.main.background_eraser_activity.*
import org.koin.android.ext.android.inject

class BackgroundEraserActivity : AppCompatActivity(){

    private val imgVolatileStorage: ImageVolatileStorage by inject()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.background_eraser_activity)

        actionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        draw_view.setAction(DrawView.DrawViewAction.MANUAL_CLEAR)
        draw_view.setStrokeWidth(100)

        imgVolatileStorage.bitmapToErase?.let {
            draw_view.setBitmap(it)
        }


    }
}