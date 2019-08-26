package com.hooloovoochimico.badpiccollage

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hooloovoochimico.badpiccollageimageview.DrawView
import com.manzo.slang.extensions.string
import com.manzo.slang.navigation.toAdapter
import kotlinx.android.synthetic.main.background_eraser_activity.*
import org.koin.android.ext.android.inject
import com.alexvasilkov.gestures.Settings.MAX_ZOOM
import com.manzo.slang.extensions.gone
import com.manzo.slang.extensions.toast
import com.manzo.slang.extensions.visible
import com.warkiz.tickseekbar.OnSeekChangeListener
import com.warkiz.tickseekbar.SeekParams
import com.warkiz.tickseekbar.TickSeekBar
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import top.defaults.checkerboarddrawable.CheckerboardDrawable


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
                        ActionModelsEnum.UNDO -> draw_view.undo()
                        ActionModelsEnum.REDO -> draw_view.redo()

                        else -> {
                            enableButton(element.action)
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

        if(action == ActionModelsEnum.MAGIC_ERASE ||
            action == ActionModelsEnum.MANUAL_ERASE) {
            options_panel.visible()

            listener_seekbar.setProgress(if(action == ActionModelsEnum.MAGIC_ERASE) draw_view.colorTolerance else draw_view.strokeWidth)
            deactivateGestureView()
        }else {
            options_panel.gone()
            activateGestureView()
        }

        listener_seekbar.max = if(action == ActionModelsEnum.MAGIC_ERASE) 65f else 100f
        listener_seekbar.min = 10f


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

        options_panel.gone()



        transparent_view.background =  CheckerboardDrawable.create()

       enableButton(ActionModelsEnum.ZOOM)

        draw_view.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        listener_seekbar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams?) {

                if(draw_view.currentAction == DrawView.DrawViewAction.AUTO_CLEAR){
                    draw_view.colorTolerance = seekParams?.progress?.toFloat()?: 10f
                    toast("${string(R.string.tolerance)}: ${draw_view.colorTolerance}")
                }else if(draw_view.currentAction == DrawView.DrawViewAction.MANUAL_CLEAR){
                    draw_view.strokeWidth = (seekParams?.progressFloat?: 40f)
                    toast("${string(R.string.stroke)}: ${draw_view.strokeWidth}")
                }
            }

            override fun onStartTrackingTouch(seekBar: TickSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: TickSeekBar?) {
            }

        }

        imgVolatileStorage.bitmapToErase?.let {
            draw_view.setBitmap(it)
        }

        cancel_action.setOnClickListener {
            enableButton(ActionModelsEnum.ZOOM)
        }



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

            saveBitmap()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveBitmap() {

        progress_wheel.visible()
        progress_wheel.spin()

        val d = Single.defer {
            Single.just(draw_view.getResultBitmap())
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { bitmap, exc ->

                progress_wheel.stopSpinning()

                if(exc== null) {
                    imgVolatileStorage.bitmapToErase = bitmap
                    setResult(Activity.RESULT_OK)
                }else {
                    setResult(Activity.RESULT_CANCELED)
                }

                finish()
            }

    }

    private fun activateGestureView() {
        gesture_view.controller.settings
            .setMaxZoom(3f)
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