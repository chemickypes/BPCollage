package com.hooloovoochimico.badpiccollage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hooloovoochimico.badpiccollageimageview.FontProvider


class FontsAdapter(context: Context, private val fontProvider: FontProvider) :
    ArrayAdapter<String>(context, 0, fontProvider.getFontNames()) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val cvView: View
        val vh: ViewHolder
        if (convertView == null) {
            cvView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            vh = ViewHolder(cvView)
            cvView.tag = vh
        } else {
            cvView = convertView
            vh = convertView.tag as ViewHolder
        }

        val fontName = getItem(position)

        vh.textView?.typeface = fontProvider.getTypeface(fontName)
        vh.textView?.text = fontName
        vh.textView?.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary))

        return cvView
    }

    fun getFontName(pos: Int) = getItem(pos)

    private class ViewHolder internal constructor(rootView: View) {

        internal var textView: TextView?  =null

        init {
            textView = rootView.findViewById(android.R.id.text1) as TextView
        }
    }
}