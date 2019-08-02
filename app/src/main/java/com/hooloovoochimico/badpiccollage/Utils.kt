package com.hooloovoochimico.badpiccollage

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import com.hooloovoochimico.badpiccollageimageview.FontProvider


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

