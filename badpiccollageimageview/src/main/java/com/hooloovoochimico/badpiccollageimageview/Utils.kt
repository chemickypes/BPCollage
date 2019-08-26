package com.hooloovoochimico.badpiccollageimageview


import android.content.Context
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.graphics.applyCanvas
import android.R.attr.bitmap
import android.graphics.*


fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
    var drawable = ContextCompat.getDrawable(context, drawableId)

    return Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    ).applyCanvas {
        drawable.setBounds(0, 0, width, height)
        drawable.draw(this)
    }
}

fun getResizedBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap{

    val originalWidth = bitmap.width.toFloat()
    val originalHeight = bitmap.height.toFloat()

    val scale = width / originalWidth

    val xTranslation = 0.0f
    val yTranslation = (height - originalHeight * scale) / 2.0f

    val transformation = Matrix()
    transformation.postTranslate(xTranslation, yTranslation)
    transformation.preScale(scale, scale)

    val paint = Paint()
    paint.isFilterBitmap = true

   return  Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888).applyCanvas {
        drawBitmap(bitmap,transformation,paint)
    }
}


fun getResizedMapIntoViewDim(bitmap: Bitmap, width: Int, height: Int): Bitmap{
    val originalWidth = bitmap.width.toFloat()
    val originalHeight = bitmap.height.toFloat()


    // check if landscape or portrait
    val scale = if(originalWidth >= originalHeight){

        if(width > originalWidth){
            //1f
            height / originalHeight
        }else {
            width / originalWidth
        }
    }else {
        if (height > originalHeight){
            //1f
            width / originalWidth
        }else {
            height / originalHeight
        }
    }

    return Bitmap.createScaledBitmap(bitmap, (originalWidth * scale).toInt(), (originalHeight * scale).toInt(),false)
}

fun getCenterPos(bitmap: Bitmap,width: Int, height: Int): kotlin.Pair<Float,Float>{
    val originalWidth = bitmap.width.toFloat()
    val originalHeight = bitmap.height.toFloat()

    return if(originalWidth > originalHeight){
        Pair(if(width > originalWidth) width/2 - originalWidth/2 else 0f, height/2 - originalHeight/2)
    }else {
        Pair(width/2 - originalWidth/2 ,
            if(height > originalHeight) height/2 - originalHeight/2 else 0f)
    }
}

/**
 * this function checks if tap on view is on the image and if yes returns
 * the real point on image otherwise a Pair of -1,-1
 * @param xOnView x coord of tap on View
 * @param yOnView y coord of tap on View
 * @param image Bitmap in orger to get its dims
 * @param offsetX offset to kwow position of bitmap on the view width
 * @param offsetY offset to kwow bitmap position on the view height
 *
 * @return Pair of Int, the real position on image or Pair(-1,-1) if tap is outside
 */
fun getRealPointOnImage(xOnView: Float, yOnView: Float, image:Bitmap, offsetX : Float,offsetY:Float) :Pair<Int,Int>{
    val rectf = RectF(offsetX, offsetY, offsetX+image.width, offsetY + image.height)
    return if(rectf.contains(xOnView,yOnView)){
        Pair((xOnView - offsetX).toInt(), (yOnView - offsetY).toInt())
    }else{
        Pair(-1,-1)
    }
}


fun Bitmap.crop(x:Int,y:Int, width: Int,height: Int): Bitmap {
    return Bitmap.createBitmap(this,x,y,width, height)
}


object MathUtils {

    /**
     * For more info:
     * [StackOverflow: How to check point is in rectangle](http://math.stackexchange.com/questions/190111/how-to-check-if-a-point-is-inside-a-rectangle)
     *
     * @param pt point to check
     * @param v1 vertex 1 of the triangle
     * @param v2 vertex 2 of the triangle
     * @param v3 vertex 3 of the triangle
     * @return true if point (x, y) is inside the triangle
     */
    fun pointInTriangle(
        @NonNull pt: PointF, @NonNull v1: PointF,
        @NonNull v2: PointF, @NonNull v3: PointF
    ): Boolean {

        val b1 = crossProduct(pt, v1, v2) < 0.0f
        val b2 = crossProduct(pt, v2, v3) < 0.0f
        val b3 = crossProduct(pt, v3, v1) < 0.0f

        return b1 == b2 && b2 == b3
    }

    /**
     * calculates cross product of vectors AB and AC
     *
     * @param a beginning of 2 vectors
     * @param b end of vector 1
     * @param c enf of vector 2
     * @return cross product AB * AC
     */
    private fun crossProduct(@NonNull a: PointF, @NonNull b: PointF, @NonNull c: PointF): Float {
        return crossProduct(a.x, a.y, b.x, b.y, c.x, c.y)
    }

    /**
     * calculates cross product of vectors AB and AC
     *
     * @param ax X coordinate of point A
     * @param ay Y coordinate of point A
     * @param bx X coordinate of point B
     * @param by Y coordinate of point B
     * @param cx X coordinate of point C
     * @param cy Y coordinate of point C
     * @return cross product AB * AC
     */
    private fun crossProduct(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Float {
        return (ax - cx) * (by - cy) - (bx - cx) * (ay - cy)
    }
}