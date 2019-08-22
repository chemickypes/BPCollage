package com.hooloovoochimico.badpiccollageimageview

import android.graphics.*
import android.text.TextPaint
import androidx.core.graphics.withSave

import android.text.Layout
import android.text.StaticLayout
import androidx.core.graphics.applyCanvas




abstract  class MotionEntity(
    val layer: Layer,
    protected var canvasWidth: Int = 1,
   protected var canvasHeight: Int = 1
) {

    protected val matrix = Matrix()

    var isSelected = false

    protected var holyScale = 1f

    private val destPoints = FloatArray(10) // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0

    protected val srcPoints = FloatArray(10)  // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0

    private var borderPaint = Paint()

    private val pA = PointF()
    private val pB = PointF()
    private val pC = PointF()
    private val pD = PointF()


    /**
     * S - scale matrix, R - rotate matrix, T - translate matrix,
     * L - result transformation matrix
     *
     *
     * The correct order of applying transformations is : L = S * R * T
     *
     *
     * See more info: [Game Dev: Transform Matrix multiplication order](http://gamedev.stackexchange.com/questions/29260/transform-matrix-multiplication-order)
     *
     *
     * Preconcat works like M` = M * S, so we apply preScale -> preRotate -> preTranslate
     * the result will be the same: L = S * R * T
     *
     *
     * NOTE: postconcat (postScale, etc.) works the other way : M` = S * M, in order to use it
     * we'd need to reverse the order of applying
     * transformations : post holy scale ->  postTranslate -> postRotate -> postScale
     */
    protected fun updateMatrix() {
        // init matrix to E - identity matrix
        matrix.reset()

        val topLeftX = layer.x * canvasWidth
        val topLeftY = layer.y * canvasHeight

        val centerX = topLeftX + getWidth() * holyScale * 0.5f
        val centerY = topLeftY + getHeight() * holyScale * 0.5f

        // calculate params
        var rotationInDegree = layer.rotationInDegrees
        var scaleX = layer.scale
        val scaleY = layer.scale
        if (layer.isFlipped) {
            // flip (by X-coordinate) if needed
            rotationInDegree *= -1.0f
            scaleX *= -1.0f
        }

        // applying transformations : L = S * R * T

        // scale
        matrix.preScale(scaleX, scaleY, centerX, centerY)

        // rotate
        matrix.preRotate(rotationInDegree, centerX, centerY)

        // translate
        matrix.preTranslate(topLeftX, topLeftY)

        // applying holy scale - S`, the result will be : L = S * R * T * S`
        matrix.preScale(holyScale, holyScale)
    }

    fun absoluteCenterX(): Float {
        val topLeftX = layer.x * canvasWidth
        return topLeftX + getWidth() * holyScale * 0.5f
    }

    fun absoluteCenterY(): Float {
        val topLeftY = layer.y * canvasHeight

        return topLeftY + getHeight() * holyScale * 0.5f
    }

    fun absoluteCenter(): PointF {
        val topLeftX = layer.x * canvasWidth
        val topLeftY = layer.y * canvasHeight

        val centerX = topLeftX + getWidth() * holyScale * 0.5f
        val centerY = topLeftY + getHeight() * holyScale * 0.5f

        return PointF(centerX, centerY)
    }

    fun moveToCanvasCenter() {
        moveCenterTo(PointF(canvasWidth * 0.5f, canvasHeight * 0.5f))
    }

    fun moveCenterTo(moveToCenter: PointF) {
        val currentCenter = absoluteCenter()
        layer.postTranslate(
            1.0f * (moveToCenter.x - currentCenter.x) / canvasWidth,
            1.0f * (moveToCenter.y - currentCenter.y) / canvasHeight
        )
    }


    /**
     * For more info:
     * [StackOverflow: How to check point is in rectangle](http://math.stackexchange.com/questions/190111/how-to-check-if-a-point-is-inside-a-rectangle)
     *
     * NOTE: it's easier to apply the same transformation matrix (calculated before) to the original source points, rather than
     * calculate the result points ourselves
     * @param point point
     * @return true if point (x, y) is inside the triangle
     */
    fun pointInLayerRect(point: PointF): Boolean {

        updateMatrix()
        // map rect vertices
        matrix.mapPoints(destPoints, srcPoints)

        pA.x = destPoints[0]
        pA.y = destPoints[1]
        pB.x = destPoints[2]
        pB.y = destPoints[3]
        pC.x = destPoints[4]
        pC.y = destPoints[5]
        pD.x = destPoints[6]
        pD.y = destPoints[7]

        return MathUtils.pointInTriangle(point, pA, pB, pC) || MathUtils.pointInTriangle(point, pA, pD, pC)
    }

    /**
     * http://judepereira.com/blog/calculate-the-real-scale-factor-and-the-angle-of-rotation-from-an-android-matrix/
     *
     * @param canvas Canvas to draw
     * @param drawingPaint Paint to use during drawing
     */
    fun draw(canvas: Canvas, drawingPaint: Paint?) {

        updateMatrix()


        canvas.withSave {
            drawContent(this, drawingPaint)

            if (isSelected) {
                // get alpha from drawingPaint
                val storedAlpha = borderPaint.alpha
                if (drawingPaint != null) {
                    borderPaint.alpha = drawingPaint.alpha
                }
                drawSelectedBg(canvas)
                // restore border alpha
                borderPaint.alpha = storedAlpha
            }
        }

    }

    private fun drawSelectedBg(canvas: Canvas) {
        matrix.mapPoints(destPoints, srcPoints)

        canvas.drawLines(destPoints, 0, 8, borderPaint)

        canvas.drawLines(destPoints, 2, 8, borderPaint)
    }


    fun setBorderPaint(borderPaint: Paint) {
        this.borderPaint = borderPaint
    }


    open fun release() {
        // free resources here
    }




    protected abstract fun drawContent(canvas: Canvas, drawingPaint: Paint?)

    abstract fun getWidth(): Int

    abstract fun getHeight(): Int


}

class ImageEntity(layer:Layer = Layer(), val bitmap: Bitmap,
                  canvasWidth: Int = 1,
                   canvasHeight: Int = 1): MotionEntity(layer, canvasWidth, canvasHeight){

    init {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()

        val widthAspect = 1.0f * canvasWidth / width
        val heightAspect = 1.0f * canvasHeight / height
        // fit the smallest size
        holyScale = Math.min(widthAspect, heightAspect)

        // initial position of the entity
        srcPoints[0] = 0f; srcPoints [1] = 0f
        srcPoints[2] = width; srcPoints [3] = 0f
        srcPoints[4] = width; srcPoints [5] = height
        srcPoints[6] = 0f; srcPoints [7] = height
        srcPoints[8] = 0f; srcPoints [8] = 0f
    }
    override fun drawContent(canvas: Canvas, drawingPaint: Paint?) {
        canvas.drawBitmap(bitmap, matrix, drawingPaint)
    }

    override fun getWidth(): Int  =  bitmap.width

    override fun getHeight(): Int  = bitmap.height

    override fun release() {
        /*if (!bitmap.isRecycled) {
            bitmap.recycle()
        }*/
    }

}

class TextEntity(layer:Layer = TextLayer(),
                 private  val fontProvider: FontProvider,
                 canvasWidth: Int = 1,
                 canvasHeight: Int = 1): MotionEntity(layer, canvasWidth, canvasHeight){
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var bitmap : Bitmap? = null
    init {
        updateEntity(false)
    }

    fun getTextLayer() : TextLayer = layer as TextLayer


    private fun updateEntity(moveToPreviousCenter: Boolean) {

        // save previous center
        val oldCenter = absoluteCenter()

        val newBmp = createBitmap(getLayer(), bitmap)

        // recycle previous bitmap (if not reused) as soon as possible
        if (bitmap != null && bitmap != newBmp && !bitmap?.isRecycled!!) {
            bitmap?.recycle()
        }


        this.bitmap = newBmp

        val width = bitmap?.width ?:1
        val height = bitmap?.height ?:1

        val widthAspect = 1.0f * canvasWidth / width

        // for text we always match text width with parent width
        this.holyScale = widthAspect

        // initial position of the entity
        srcPoints[0] = 0.toFloat()
        srcPoints[1] = 0.toFloat()
        srcPoints[2] = width.toFloat()
        srcPoints[3] = 0.toFloat()
        srcPoints[4] = width.toFloat()
        srcPoints[5] = height.toFloat()
        srcPoints[6] = 0.toFloat()
        srcPoints[7] = height.toFloat()
        srcPoints[8] = 0.toFloat()
        srcPoints[8] = 0.toFloat()

        if (moveToPreviousCenter) {
            // move to previous center
            moveCenterTo(oldCenter)
        }
    }


    /**
     * If reuseBmp is not null, and size of the new bitmap matches the size of the reuseBmp,
     * new bitmap won't be created, reuseBmp it will be reused instead
     *
     * @param textLayer text to draw
     * @param reuseBmp  the bitmap that will be reused
     * @return bitmap with the text
     */

    private fun createBitmap(textLayer: TextLayer, reuseBmp: Bitmap?): Bitmap {

        val boundsWidth = canvasWidth

        // init params - size, color, typeface
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = canvasWidth * (textLayer.font?.size?:1f)
        textPaint.bgColor = textLayer.font?.bgColor?:Color.TRANSPARENT
        textPaint.color = textLayer.font?.color?:Color.BLACK
        textPaint.typeface = fontProvider.getTypeface(textLayer.font?.typeface)

        // drawing text guide : http://ivankocijan.xyz/android-drawing-multiline-text-on-canvas/
        // Static layout which will be drawn on canvas
        val sl = StaticLayout(
            textLayer.text, // - text which will be drawn
            textPaint,
            boundsWidth, // - width of the layout
            Layout.Alignment.ALIGN_CENTER, // - layout alignment
            1f, // 1 - text spacing multiply
            1f, // 1 - text spacing add
            true
        ) // true - include padding



        // calculate height for the entity, min - Limits.MIN_BITMAP_HEIGHT
        val boundsHeight = sl.height

        // create bitmap not smaller than TextLayer.Limits.MIN_BITMAP_HEIGHT
        val bmpHeight = (canvasHeight * Math.max(
            TextLayer.Limits.MIN_BITMAP_HEIGHT,
            1.0f * boundsHeight / canvasHeight
        )).toInt()

        // create bitmap where text will be drawn
        val bmp: Bitmap
        if (reuseBmp != null && reuseBmp.width == boundsWidth
            && reuseBmp.height == bmpHeight
        ) {
            // if previous bitmap exists, and it's width/height is the same - reuse it
            bmp = reuseBmp
            bmp.eraseColor(Color.TRANSPARENT) // erase color when reusing
        } else {
            bmp = Bitmap.createBitmap(boundsWidth, bmpHeight, Bitmap.Config.ARGB_8888)
        }

        bmp.applyCanvas {
            // move text to center if bitmap is bigger that text
            if (boundsHeight < bmpHeight) {
                //calculate Y coordinate - In this case we want to draw the text in the
                //center of the canvas so we move Y coordinate to center.
                val textYCoordinate = ((bmpHeight - boundsHeight) / 2).toFloat()
                translate(0f, textYCoordinate)
            }

            //draws static layout on canvas
            drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR)
            drawColor(textLayer.font?.bgColor?:Color.TRANSPARENT,PorterDuff.Mode.DST_OVER)
            sl.draw(this)



        }


        return bmp
    }


    fun getLayer(): TextLayer {
        return layer as TextLayer
    }

    override fun drawContent(canvas: Canvas, drawingPaint: Paint?) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap!!, matrix, drawingPaint)
        }
    }

    override fun getWidth(): Int {

        return bitmap?.let {
            it.width
        }?:run {
            0
        }
    }

    override fun getHeight(): Int {
        return bitmap?.let {
            it.height
        }?:run {
            0
        }
    }

    fun updateEntity() {
        updateEntity(true)
    }

    override fun release() {

        bitmap?.let {
            if(!it.isRecycled) it.recycle()
        }

    }
}