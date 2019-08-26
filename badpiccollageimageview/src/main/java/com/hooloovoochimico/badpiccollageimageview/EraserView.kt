package com.hooloovoochimico.badpiccollageimageview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Pair
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.withSave

import java.lang.ref.WeakReference
import java.util.Stack


class DrawView(c: Context, attrs: AttributeSet) : View(c, attrs) {

    private var livePath: Path? = null
    private var pathPaint: Paint? = null

    var currentBitmap: Bitmap? = null
        private set
    private val cuts = Stack<Pair<Pair<Path, Paint>?, Bitmap?>>()
    private val undoneCuts = Stack<Pair<Pair<Path, Paint>?, Bitmap?>>()

    private var pathX: Float = 0.toFloat()
    private var pathY: Float = 0.toFloat()

    private var undoButton: Button? = null
    private var redoButton: Button? = null

    private var loadingModal: View? = null

    var currentAction: DrawViewAction? = null

    var bitmapX = 0f
    var bitmapY = 0f

    var colorTolerance: Float = COLOR_TOLERANCE
    set(value) {
        field = when {
            value < 10f -> 10f
            value > 65f -> 65f
            else -> value
        }
    }

    var magicEraseCallback : (Boolean) -> Unit = {_:Boolean -> }

    var strokeWidth: Float = 40f
    set(value) {
        field = when{
            value < 10 -> 10f
            value > 100 -> 100f
            else -> value
        }
        setStrokeWidthP(field)
    }

    enum class DrawViewAction {
        AUTO_CLEAR,
        MANUAL_CLEAR,
        ZOOM
    }

    init {
        initDrawView()
    }

    fun setButtons(undoButton: Button, redoButton: Button) {
        this.undoButton = undoButton
        this.redoButton = redoButton
    }

    fun initDrawView(){
        livePath = Path()
        pathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        pathPaint!!.isDither = true
        pathPaint!!.color = Color.TRANSPARENT
        pathPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        pathPaint!!.style = Paint.Style.STROKE
        pathPaint!!.strokeJoin = Paint.Join.ROUND
        pathPaint!!.strokeCap = Paint.Cap.ROUND
        pathPaint!!.strokeWidth = 40f

        //isDrawingCacheEnabled = true
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }



    override fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight)

        resizeBitmap(newWidth, newHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawOnCanvas(canvas)

    }


    private fun drawOnCanvas(canvas:Canvas){
        canvas.withSave {
            if (currentBitmap != null) {

                drawBitmap(currentBitmap!!,
                    bitmapX,
                    bitmapY,
                    null)

                for (action in cuts) {
                    if (action.first != null) {
                        drawPath(action.first!!.first, action.first!!.second)
                    }
                }

                if (currentAction == DrawViewAction.MANUAL_CLEAR) {
                    drawPath(livePath!!, pathPaint!!)
                }
            }
        }
    }

    private fun touchStart(x: Float, y: Float) {
        pathX = x
        pathY = y

        undoneCuts.clear()
        redoButton?.isEnabled = false

        if (currentAction == DrawViewAction.AUTO_CLEAR) {

            val (realImageX, realImageY) = getRealPointOnImage(x,y,currentBitmap!!,bitmapX,bitmapY)

            if( realImageX > -1 && realImageY > -1 ) {
                AutomaticPixelClearingTask(this, colorTolerance,magicEraseCallback).execute(realImageX, realImageY)
            }
        } else {
            livePath!!.moveTo(x, y)
        }

        invalidate()
    }

    private fun touchMove(x: Float, y: Float) {
        if (currentAction == DrawViewAction.MANUAL_CLEAR) {
            val dx = Math.abs(x - pathX)
            val dy = Math.abs(y - pathY)
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                livePath!!.quadTo(pathX, pathY, (x + pathX) / 2, (y + pathY) / 2)
                pathX = x
                pathY = y
            }
        }
    }


    private fun touchUp() {
        if (currentAction == DrawViewAction.MANUAL_CLEAR) {
            livePath!!.lineTo(pathX, pathY)
            cuts.push(Pair(Pair(livePath!!, pathPaint!!), null))
            livePath = Path()
            undoButton?.isEnabled = true
        }
    }

    fun undo() {
        if (cuts.size > 0) {

            val cut = cuts.pop()

            if (cut.second != null) {
                undoneCuts.push(Pair(null, currentBitmap))
                this.currentBitmap = cut.second
            } else {
                undoneCuts.push(cut)
            }

            if (cuts.isEmpty()) {
                undoButton?.isEnabled = false
            }

            redoButton?.isEnabled = true

            invalidate()
        }
        //toast the user
    }

    fun redo() {
        if (undoneCuts.size > 0) {

            val cut = undoneCuts.pop()

            if (cut.second != null) {
                cuts.push(Pair(null, currentBitmap))
                this.currentBitmap = cut.second
            } else {
                cuts.push(cut)
            }

            if (undoneCuts.isEmpty()) {
                redoButton?.isEnabled = false
            }

            undoButton?.isEnabled = true

            invalidate()
        }
        //toast the user
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {

        performClick()

        if (currentBitmap != null && currentAction != DrawViewAction.ZOOM) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchStart(ev.x, ev.y)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    touchMove(ev.x, ev.y)
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    touchUp()
                    invalidate()
                    return true
                }
            }
        }

        return super.onTouchEvent(ev)
    }

    private fun resizeBitmap(width: Int, height: Int) {
        if (width > 0 && height > 0 && currentBitmap != null) {
            //currentBitmap = getResizedBitmap(this.currentBitmap!!, width, height)
            currentBitmap = getResizedMapIntoViewDim(this.currentBitmap!!, width, height)
            currentBitmap!!.setHasAlpha(true)

            val (rbitmapX , rbitmapY) = getCenterPos(currentBitmap!!,width, height)
            bitmapX = rbitmapX
            bitmapY = rbitmapY
            invalidate()
        }
    }



    fun getResultBitmap() : Bitmap {

        //write entire view and then crop
        return Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888).applyCanvas {
            drawOnCanvas(this)
        }.crop(bitmapX.toInt(),bitmapY.toInt(),currentBitmap!!.width, currentBitmap!!.height)


    }

    fun setBitmap(bitmap: Bitmap) {

        pathPaint!!.color = Color.TRANSPARENT


        this.currentBitmap = bitmap
        resizeBitmap(width, height)
    }

    fun setAction(newAction: DrawViewAction) {
        this.currentAction = newAction
    }

    private fun setStrokeWidthP(strokeWidth: Float) {

        pathPaint = Paint(pathPaint)
        pathPaint!!.strokeWidth = strokeWidth
    }

    //fun getStokeWidth() = pathPaint!!.strokeWidth

    fun setLoadingModal(loadingModal: View) {
        this.loadingModal = loadingModal
    }



    private class AutomaticPixelClearingTask internal constructor(drawView: DrawView, val colorTolerance: Float,
                                                                  val callBackAction: ((Boolean) -> Unit)? = null) : AsyncTask<Int, Void, Bitmap>() {

        private val drawViewWeakReference: WeakReference<DrawView> = WeakReference(drawView)

        override fun onPreExecute() {
            super.onPreExecute()
            callBackAction?.invoke(true)
            drawViewWeakReference.get()?.loadingModal?.visibility = VISIBLE
            drawViewWeakReference.get()?.cuts?.push(Pair(null, (drawViewWeakReference.get() as DrawView).currentBitmap ))
        }

        override fun doInBackground(vararg points: Int?): Bitmap {
            val oldBitmap = drawViewWeakReference.get()?.currentBitmap

            val colorToReplace = oldBitmap?.getPixel(points[0]!!, points[1]!!)?: -1

            val width = oldBitmap?.width ?: 0
            val height = oldBitmap?.height ?: 0
            val pixels = IntArray(width * height)
            oldBitmap?.getPixels(pixels, 0, width, 0, 0, width, height)

            val rA = Color.alpha(colorToReplace)
            val rR = Color.red(colorToReplace)
            val rG = Color.green(colorToReplace)
            val rB = Color.blue(colorToReplace)

            var pixel: Int

            // iteration through pixels
            for (y in 0 until height) {
                for (x in 0 until width) {
                    // get current index in 2D-matrix
                    val index = y * width + x
                    pixel = pixels[index]
                    val rrA = Color.alpha(pixel)
                    val rrR = Color.red(pixel)
                    val rrG = Color.green(pixel)
                    val rrB = Color.blue(pixel)

                    if (rA - colorTolerance < rrA && rrA < rA + colorTolerance && rR - colorTolerance < rrR && rrR < rR + colorTolerance &&
                        rG - colorTolerance < rrG && rrG < rG + colorTolerance && rB - colorTolerance < rrB && rrB < rB + colorTolerance
                    ) {
                        pixels[index] = Color.TRANSPARENT
                    }
                }
            }

            val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            newBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

            return newBitmap
        }

        override fun onPostExecute(result: Bitmap) {
            super.onPostExecute(result)
            callBackAction?.invoke(false)
            drawViewWeakReference.get()?.currentBitmap = result
            drawViewWeakReference.get()?.undoButton?.isEnabled = true
            drawViewWeakReference.get()?.loadingModal?.visibility = INVISIBLE
            drawViewWeakReference.get()?.invalidate()
        }
    }

    companion object {

        private val TOUCH_TOLERANCE = 4f
        private val COLOR_TOLERANCE = 20f
    }
}