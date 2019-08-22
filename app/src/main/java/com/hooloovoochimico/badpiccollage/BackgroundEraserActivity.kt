package com.hooloovoochimico.badpiccollage

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hooloovoochimico.badpiccollageimageview.DrawView
import com.manzo.slang.extensions.string
import com.manzo.slang.navigation.toAdapter
import kotlinx.android.synthetic.main.background_eraser_activity.*
import org.koin.android.ext.android.inject
import com.alexvasilkov.gestures.Settings.MAX_ZOOM



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

                holder.itemView.isActivated = element.enabled
                holder.itemView.alpha = if(element.enabled) 1f else 0.6f

                holder.itemView.setOnClickListener {
                    when(element.action){
                        ActionModelsEnum.ZOOM -> {

                            enableButton(ActionModelsEnum.ZOOM)
                            activateGestureView()

                        }
                        ActionModelsEnum.MANUAL_ERASE -> {
                            enableButton(ActionModelsEnum.MANUAL_ERASE)
                            deactivateGestureView()

                        }
                        ActionModelsEnum.MAGIC_ERASE -> {
                            enableButton(ActionModelsEnum.MAGIC_ERASE)
                            deactivateGestureView()

                        }
                        ActionModelsEnum.UNDO -> draw_view.undo()
                        ActionModelsEnum.REDO -> draw_view.redo()

                        else -> {

                        }

                    }
                }
            }
        )
    }

    private fun enableButton(action: ActionModelsEnum){

        draw_view.setAction(when(action) {
            ActionModelsEnum.ZOOM -> DrawView.DrawViewAction.ZOOM
            ActionModelsEnum.MAGIC_ERASE -> DrawView.DrawViewAction.AUTO_CLEAR
            else -> DrawView.DrawViewAction.MANUAL_CLEAR
        })

        erasePanelAdapter.dataset.forEach {
            if(it.action ==  ActionModelsEnum.UNDO ||
                it.action ==  ActionModelsEnum.REDO) return

            it.enabled = it.action == action
        }

        erasePanelAdapter.notifyDataSetChanged()
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.background_eraser_activity)

        actionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        actionBar?.title = string(R.string.erase_background)
        supportActionBar?.title = string(R.string.erase_background)


        text_action_panel.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        text_action_panel.adapter = erasePanelAdapter


        //draw_view.initDrawView()

        draw_view.setStrokeWidth(40)

        imgVolatileStorage.bitmapToErase?.let {
            draw_view.setBitmap(it)
        }

        deactivateGestureView()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.erase_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if(item?.itemId == android.R.id.home ){
            setResult(RESULT_CANCELED)
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
        imgVolatileStorage.bitmapToErase = draw_view.getResultBitmap()
    }

    private fun activateGestureView() {
        gesture_view.controller.settings
            .setMaxZoom(MAX_ZOOM)
            .setDoubleTapZoom(-1f) // Falls back to max zoom level
            .setPanEnabled(true)
            .setZoomEnabled(true)
            .setDoubleTapEnabled(true)
            .setOverscrollDistance(0f, 0f).overzoomFactor = 2f
    }

    private fun deactivateGestureView() {
        gesture_view.controller.settings
            .setPanEnabled(false)
            .setZoomEnabled(false).isDoubleTapEnabled = false
    }
}