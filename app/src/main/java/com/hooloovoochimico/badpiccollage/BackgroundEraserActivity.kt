package com.hooloovoochimico.badpiccollage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hooloovoochimico.badpiccollageimageview.DrawView
import com.hooloovoochimico.badpiccollageimageview.getBitmapFromVectorDrawable
import kotlinx.android.synthetic.main.background_eraser_activity.*

class BackgroundEraserActivity : AppCompatActivity(){




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.background_eraser_activity)

        actionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        draw_view.setAction(DrawView.DrawViewAction.MANUAL_CLEAR)
        draw_view.setStrokeWidth(100)
        draw_view.setBitmap(getBitmapFromVectorDrawable(this, R.drawable.psyduck))

    }
}