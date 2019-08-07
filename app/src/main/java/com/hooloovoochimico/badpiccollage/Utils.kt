package com.hooloovoochimico.badpiccollage

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.text.style.TypefaceSpan
import com.hooloovoochimico.badpiccollageimageview.FontProvider
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.R.attr.font
import android.graphics.Paint
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat


fun getImgName() : String {

    return "BadPicImage_${System.currentTimeMillis()}"

}

object BPCFontProvider {

    private var fontProvider: FontProvider?  =null

    fun getFontProvider(context: Context) : FontProvider{
        if(fontProvider == null) fontProvider = ImplFontProvider(context.resources)

        return fontProvider!!
    }

    fun init(context: Context, fonts: Map<String,String>){

        getFontProvider(context).setFonts(fonts)
    }

    fun init(context: Context, block: () -> Map<String,String>) {
        init(context,block.invoke())
    }

}

private class ImplFontProvider(resources: Resources): FontProvider(resources){
    override fun getTypeFace(key: String?): Typeface {
        return Typeface.createFromAsset(resources.assets, "fonts/$key")
    }

}


fun getCropImageTitle(context: Context) : String {

    val cropTitleImage = "Crop Photo"
    val myTypeface = Typeface.create(
        ResourcesCompat.getFont(context, R.font.nunito),
        Typeface.BOLD
    )
    val string = SpannableString(cropTitleImage)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        string.setSpan(TypefaceSpan(myTypeface), 0, cropTitleImage.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }else {
        string.setSpan(CustomTypefaceSpan("nunito",myTypeface), 0, cropTitleImage.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    return string.toString()

}


class CustomTypefaceSpan(family: String, private val newType: Typeface) : TypefaceSpan(family) {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        val oldStyle: Int
        val old = paint.typeface
        oldStyle = old?.style ?: 0

        val fake = oldStyle and tf.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }

        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }

        paint.typeface = tf
    }
}

