package com.hooloovoochimico.badpiccollage

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.hooloovoochimico.badpiccollageimageview.DrawView
import com.manzo.slang.extensions.string
import com.manzo.slang.navigation.toAdapter
import kotlinx.android.synthetic.main.background_eraser_activity.*
import org.koin.android.ext.android.inject

class BackgroundEraserActivity : AppCompatActivity(){

    private val imgVolatileStorage: ImageVolatileStorage by inject()


    private val erasePanelAdapter by lazy {
        getEraseActions().toMutableList().toAdapter(
            rowLayout = R.layout.action_text_panel_item,
            onBindContent = {holder, _, element ->
                with(holder[R.id.imvaction]){
                    (this as? ImageView)?.setImageResource(
                        when(element.action){
                            ActionModelsEnum.ZOOM -> R.drawable.ic_zoom_red
                            ActionModelsEnum.MANUAL_ERASE -> R.drawable.ic_manual_erase_red
                            ActionModelsEnum.MAGIC_ERASE -> R.drawable.ic_magic_erase_red
                            ActionModelsEnum.UNDO -> R.drawable.ic_undo_red
                            ActionModelsEnum.REDO -> R.drawable.ic_redo_red

                            else -> R.drawable.ic_add_red

                        }
                    )
                }

                holder.itemView.isEnabled = element.enabled
                holder.itemView.alpha = if(element.enabled) 1f else 0.6f

                holder.itemView.setOnClickListener {
                    when(element.action){
                        ActionModelsEnum.ZOOM -> draw_view.setAction(DrawView.DrawViewAction.ZOOM)
                        ActionModelsEnum.MANUAL_ERASE -> draw_view.setAction(DrawView.DrawViewAction.MANUAL_CLEAR)
                        ActionModelsEnum.MAGIC_ERASE -> draw_view.setAction(DrawView.DrawViewAction.AUTO_CLEAR)
                        ActionModelsEnum.UNDO -> draw_view.undo()
                        ActionModelsEnum.REDO -> draw_view.redo()

                        else -> R.drawable.ic_add_red

                    }
                }
            }
        )
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.background_eraser_activity)

        actionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        actionBar?.title = string(R.string.erase_background)
        supportActionBar?.title = string(R.string.erase_background)



        draw_view.setStrokeWidth(40)

        imgVolatileStorage.bitmapToErase?.let {
            draw_view.setBitmap(it)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.erase_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if(item?.itemId == android.R.id.home ){
            super.onBackPressed()
        }

        if(item?.itemId == R.id.action_finish){
            setResult(Activity.RESULT_OK)
            saveBitmap()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveBitmap() {
        TODO("shdhsk")
    }
}