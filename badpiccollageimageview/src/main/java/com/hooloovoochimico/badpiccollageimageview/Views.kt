package com.hooloovoochimico.badpiccollageimageview

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.almeros.android.multitouch.MoveGestureDetector
import com.almeros.android.multitouch.RotateGestureDetector

import android.graphics.*
import android.os.Parcelable
import android.view.MotionEvent
import android.view.GestureDetector
import androidx.core.graphics.applyCanvas


class BadPicCollageImageView: ImageView {

    // layers
    val entities = mutableListOf<MotionEntity>()
    var selectedEntity: MotionEntity? = null

    private var selectedLayerPaint: Paint? = null

    // callback
    var motionViewCallback: MotionViewCallback? = null

    // gesture detection
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var rotateGestureDetector: RotateGestureDetector? = null
    private var moveGestureDetector: MoveGestureDetector? = null
    private var gestureDetectorCompat: GestureDetectorCompat? = null


    val isBaseImageLoaded:Boolean
    get() {
       return imgStore.bitmap != null
    }

    val imgStore = ImageStore


    constructor(context: Context) : super(context){
        initV(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        initV(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        initV(context)
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes:Int) : super(context, attrs, defStyleAttr,defStyleRes){
        initV(context)
    }

    private fun initV(context: Context) {
        // I fucking love Android
        setWillNotDraw(false)

        selectedLayerPaint = Paint()
        selectedLayerPaint?.alpha = (255 * Constants.SELECTED_LAYER_ALPHA).toInt()
        selectedLayerPaint?.isAntiAlias = true

        // init listeners
        this.scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        this.rotateGestureDetector = RotateGestureDetector(context, RotateListener())
        this.moveGestureDetector = MoveGestureDetector(context, MoveListener())
        this.gestureDetectorCompat = GestureDetectorCompat(context, TapsListener())

        setOnTouchListener(onTouchListener)

        updateUI()
    }

    fun saveInstanceState() {

        imgStore.entities.addAll(entities)

    }


     fun restoreInstanceState(state: Parcelable?) {
        entities.addAll(imgStore.entities)
        setImageBitmap(imgStore.bitmap)

    }



    fun addEntity(entity: MotionEntity?) {
        if (entity != null) {
            entities.add(entity)
            selectEntity(entity, false)
        }
    }

    fun addEntityAndPosition(entity: MotionEntity?) {
        if (entity != null) {
            initEntityBorder(entity)
            initialTranslateAndScale(entity)
            entities.add(entity)
            selectEntity(entity, true)
        }
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)

        imgStore.bitmap = bm
        imgStore.entities.clear()

    }

    private fun initEntityBorder(entity: MotionEntity) {
        // init stroke
        val strokeSize = resources.getDimensionPixelSize(R.dimen.stroke_size)
        val borderPaint = Paint()
        borderPaint.strokeWidth = strokeSize.toFloat()
        borderPaint.strokeCap = Paint.Cap.ROUND
        borderPaint.isAntiAlias = true
        borderPaint.color = ContextCompat.getColor(context, R.color.stroke_color)

        entity.setBorderPaint(borderPaint)
    }

    override  fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        // dispatch draw is called after child views is drawn.
        // the idea that is we draw background stickers, than child views (if any), and than selected item
        // to draw on top of child views - do it in dispatchDraw(Canvas)
        // to draw below that - do it in onDraw(Canvas)
        selectedEntity?.draw(canvas, selectedLayerPaint)
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)
        drawAllEntities(canvas)
    }

    /**
     * draws all entities on the canvas
     * @param canvas Canvas where to draw all entities
     */
    private fun drawAllEntities(canvas: Canvas) {

        entities.forEach {
            it.draw(canvas,null)
        }
    }

    /**
     * as a side effect - the method deselects Entity (if any selected)
     * @return bitmap with all the Entities at their current positions
     */
    fun getThumbnailImage(): Bitmap {
        selectEntity(null, false)

       /* return imgStore.bitmap?.let {
            it.copy(Bitmap.Config.ARGB_8888, true).applyCanvas {
                drawAllEntities(this)
            }
        }?:run {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.WHITE)
            }
        }*/

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }.applyCanvas {
            drawBitmap(imgStore.bitmap!!,null, RectF(0f,0f,width.toFloat(),height.toFloat()), Paint())
            drawAllEntities(this)
        }

        //return (drawable as  BitmapDrawable).bitmap


        /*//val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val bmp = imgStore.bitmap
        // IMPORTANT: always create white background, cos if the image is saved in JPEG format,
        // which doesn't have transparent pixels, the background will be black
        bmp.eraseColor(Color.WHITE)
        val canvas = Canvas(bmp)
        drawAllEntities(canvas)

        return bmp*/
    }

    private fun updateUI() {
        invalidate()
    }

    private fun handleTranslate(delta: PointF) {
        if (selectedEntity != null) {
            val newCenterX = selectedEntity?.absoluteCenterX()?:0 + delta.x
            val newCenterY = selectedEntity?.absoluteCenterY()?:0 + delta.y
            // limit entity center to screen bounds
            var needUpdateUI = false
            if (newCenterX >= 0 && newCenterX <= width) {
                selectedEntity?.layer?.postTranslate(delta.x / width, 0.0f)
                needUpdateUI = true
            }
            if (newCenterY >= 0 && newCenterY <= height) {
                selectedEntity?.layer?.postTranslate(0.0f, delta.y / height)
                needUpdateUI = true
            }
            if (needUpdateUI) {
                updateUI()
            }
        }
    }

    private fun initialTranslateAndScale(entity: MotionEntity) {
        entity.moveToCanvasCenter()
        entity.layer.scale = entity.layer.initialScale()
    }

    private fun selectEntity(entity: MotionEntity?, updateCallback: Boolean) {
        selectedEntity?.isSelected = false
        entity?.isSelected = true
        selectedEntity = entity
        invalidate()
        if (updateCallback && motionViewCallback != null) {
            motionViewCallback?.onEntitySelected(entity)
        }
    }

    fun unselectEntity() {
        if (selectedEntity != null) {
            selectEntity(null, true)
        }
    }

    private fun findEntityAtPoint(x: Float, y: Float): MotionEntity? {
        var selected: MotionEntity? = null
        val p = PointF(x, y)
        for (i in entities.size - 1 downTo 0) {
            if (entities[i].pointInLayerRect(p)) {
                selected = entities[i]
                break
            }
        }
        return selected
    }

    private fun updateSelectionOnTap(e: MotionEvent) {
        val entity = findEntityAtPoint(e.getX(), e.getY())
        selectEntity(entity, true)
    }

    private fun updateOnLongPress(e: MotionEvent) {
        // if layer is currently selected and point inside layer - move it to front

        selectedEntity?.let {
            val p = PointF(e.x, e.y)
            if (it.pointInLayerRect(p)) {
                bringLayerToFront(it)
            }
        }
    }

    private fun bringLayerToFront(entity: MotionEntity) {
        // removing and adding brings layer to front
        if (entities.remove(entity)) {
            entities.add(entity)
            invalidate()
        }
    }

    private fun moveEntityToBack(entity: MotionEntity?) {
        if (entity == null) {
            return
        }
        if (entities.remove(entity)) {
            entities.add(0, entity)
            invalidate()
        }
    }

    fun flipSelectedEntity() {
        if (selectedEntity == null) {
            return
        }
        selectedEntity?.layer?.flip()
        invalidate()
    }

    fun moveSelectedBack() {
        moveEntityToBack(selectedEntity)
    }

    fun deleteSelectedEntity() {

        selectedEntity?.let {
            if (entities.remove(it)) {
                it.release()
                selectedEntity = null
                invalidate()
            }
        }

    }

    //TextEntity

    private fun getCurrentTextEntity() : TextEntity? {

        return when(selectedEntity) {
            is TextEntity -> selectedEntity as TextEntity
            else -> null
        }
    }

    fun changeSelectedTextEntityFont(font:String){
        getCurrentTextEntity()?.let {
            it.getTextLayer().font?.typeface = font
            it.updateEntity()
            updateUI()
        }
    }

    fun changeSelectedTextEntityColor(color: Int) {
        getCurrentTextEntity()?.let {
            it.getTextLayer().font?.color = color
            it.updateEntity()
            updateUI()
        }
    }

    fun changeSelectedTextEntityBGColor(color: Int) {
        getCurrentTextEntity()?.let {
            it.getTextLayer().font?.bgColor = color
            it.updateEntity()
            updateUI()
        }
    }

    fun increaseSelectedTextEntitySize(){
        getCurrentTextEntity()?.let {
            it.getTextLayer().font?.increaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            it.updateEntity()
            updateUI()
        }
    }

    fun decreaseSelectedTextEntitySize(){
        getCurrentTextEntity()?.let {
            it.getTextLayer().font?.decreaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            it.updateEntity()
            updateUI()
        }
    }

    fun changeSelectedTextEntityText(text: String?) {
        getCurrentTextEntity()?.let {
            it.getTextLayer().text = text?:""
            it.updateEntity()
            updateUI()
        }
    }



    // memory
    fun release() {
        for (entity in entities) {
            entity.release()
        }
    }

    fun setEmptyState() {
        imgStore.clear()
    }

    // gesture detectors

    private val onTouchListener = OnTouchListener { v, event ->
        if (scaleGestureDetector != null) {
            scaleGestureDetector!!.onTouchEvent(event)
            rotateGestureDetector?.onTouchEvent(event)
            moveGestureDetector?.onTouchEvent(event)
            gestureDetectorCompat?.onTouchEvent(event)
        }
        true
    }

    private inner class TapsListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (motionViewCallback != null && selectedEntity != null) {
                motionViewCallback?.onEntityDoubleTap(selectedEntity!!)
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            updateOnLongPress(e)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            updateSelectionOnTap(e)
            return true
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (selectedEntity != null) {
                val scaleFactorDiff = detector.scaleFactor
                selectedEntity?.layer?.postScale(scaleFactorDiff - 1.0f)
                updateUI()
            }
            return true
        }
    }

    private inner class RotateListener : RotateGestureDetector.SimpleOnRotateGestureListener() {
        override fun onRotate(detector: RotateGestureDetector): Boolean {
            if (selectedEntity != null) {
                selectedEntity?.layer?.postRotate(-detector.rotationDegreesDelta)
                updateUI()
            }
            return true
        }
    }

    private inner class MoveListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            handleTranslate(detector.focusDelta)
            return true
        }
    }


    interface Constants {
        companion object {
            const val SELECTED_LAYER_ALPHA = 0.15f
        }
    }

    interface MotionViewCallback {
        fun onEntitySelected(@Nullable entity: MotionEntity?)
        fun onEntityDoubleTap(@NonNull entity: MotionEntity)
    }
}