package com.hooloovoochimico.badpiccollageimageview

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface

abstract class FontProvider(protected val resources: Resources) {



    private val fontNameToTypefaceFile: MutableMap<String, String> = mutableMapOf()
    private val typefaces: MutableMap<String, Typeface> = mutableMapOf()



    fun getTypeface(typeface: String?): Typeface? {
        return try{
            typeface?.let {
                if (typefaces[typeface] == null) {
                    typefaces[typeface] = getTypeFace(fontNameToTypefaceFile[typeface])
                        //Typeface.createFromAsset(resources.assets, "fonts/${fontNameToTypefaceFile[typeface]}")
                }
                typefaces[typeface]?: Typeface.DEFAULT
            }?:run {
                Typeface.DEFAULT
            }
        }catch (e:Exception){
            Typeface.DEFAULT
        }
    }

    fun getFontNames() = fontNameToTypefaceFile.keys.toList()

    fun setFonts(fontsToAdd:Map<String,String>){

        fontNameToTypefaceFile.putAll(fontsToAdd)

    }

    fun setFonts(fontsToAdd:List<String>) {
        fontsToAdd.forEach {fontName ->
            if(fontName.endsWith(".ttf") || fontName.endsWith(".TTF")){
                fontNameToTypefaceFile[fontName.substring(0,fontName.length-4)] = fontName
            }

        }
    }

    abstract fun getTypeFace(key:String?): Typeface


}


class Font(
    /**
     * color value (ex: 0xFF00FF)
     */
    var color: Int = Color.BLACK,
    /**
     * name of the font
     */
    var typeface: String? = null,
    /**
     * size of the font, relative to parent
     */
    var size: Float = TextLayer.Limits.INITIAL_FONT_SIZE,

    var bgColor: Int = Color.TRANSPARENT
) {

    fun increaseSize(diff: Float) {
        this.size += diff
    }

    fun decreaseSize(diff: Float) {
        if (size - diff >= Limits.MIN_FONT_SIZE) {
            size -= diff
        }
    }

    private interface Limits {
        companion object {
            const val MIN_FONT_SIZE = 0.01f
        }
    }
}



class FontBuilder{
    var bgColor: Int = 0x00FFFFFF

    var color: Int = Color.BLACK

    var typeface: String? = null

    var size: Float = TextLayer.Limits.INITIAL_FONT_SIZE

    fun build(): Font = Font(color, typeface, size, bgColor)
}

fun getFont(block: FontBuilder.() -> Unit): Font{
    val b = FontBuilder()
    block(b)
    return b.build()
}