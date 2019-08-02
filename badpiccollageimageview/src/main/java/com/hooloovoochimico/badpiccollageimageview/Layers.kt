package com.hooloovoochimico.badpiccollageimageview

import androidx.annotation.FloatRange





open class Layer {
    @FloatRange(from = 0.0, to = 360.0)
    var rotationInDegrees: Float = 0.toFloat()

    var scale: Float = 1.toFloat()
    /**
     * top left X coordinate, relative to parent canvas
     */
    var x: Float = 0.toFloat()
    /**
     * top left Y coordinate, relative to parent canvas
     */
    var y: Float = 0.toFloat()
    /**
     * is layer flipped horizontally (by X-coordinate)
     */
    var isFlipped: Boolean = false



    protected open fun reset() {
        this.rotationInDegrees = 0.0f
        this.scale = 1.0f
        this.isFlipped = false
        this.x = 0.0f
        this.y = 0.0f
    }

    fun postScale(scaleDiff: Float) {
        val newVal = scale + scaleDiff
        if (newVal >= getMinScale() && newVal <= getMaxScale()) {
            scale = newVal
        }
    }

    protected open fun getMaxScale(): Float {
        return Limits.MAX_SCALE
    }

    protected open fun getMinScale(): Float {
        return Limits.MIN_SCALE
    }

    fun postRotate(rotationInDegreesDiff: Float) {
        this.rotationInDegrees += rotationInDegreesDiff
        this.rotationInDegrees %= 360.0f
    }

    fun postTranslate(dx: Float, dy: Float) {
        this.x += dx
        this.y += dy
    }

    fun flip() {
        this.isFlipped = !isFlipped
    }

    open fun initialScale(): Float {
        return Limits.INITIAL_ENTITY_SCALE
    }



    internal interface Limits {
        companion object {
            val MIN_SCALE = 0.06f
            val MAX_SCALE = 4.0f
            val INITIAL_ENTITY_SCALE = 0.4f
        }
    }
}

class TextLayer : Layer() {

    var text: String? = null
    var font: Font? = null

    override fun reset() {
        super.reset()
        text = ""
        font = Font()
    }

    override fun getMaxScale(): Float {
        return Limits.MAX_SCALE
    }

    override fun getMinScale(): Float {
        return Limits.MIN_SCALE
    }

    override fun initialScale(): Float {
        return Limits.INITIAL_SCALE
    }

    interface Limits {
        companion object {
            /**
             * limit text size to view bounds
             * so that users don't put small font size and scale it 100+ times
             */
            const val MAX_SCALE = 1.0f
            const val MIN_SCALE = 0.2f

            const val MIN_BITMAP_HEIGHT = 0.13f

            const val FONT_SIZE_STEP = 0.008f

            const val INITIAL_FONT_SIZE = 0.075f
            const val INITIAL_FONT_COLOR = -0x1000000

            const val INITIAL_SCALE = 0.8f // set the same to avoid text scaling
        }
    }

}