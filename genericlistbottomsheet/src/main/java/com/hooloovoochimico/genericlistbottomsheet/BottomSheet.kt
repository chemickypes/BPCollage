package com.hooloovoochimico.genericlistbottomsheet

import android.content.Context
import android.view.View
import androidx.annotation.IntegerRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.lang.RuntimeException




class GenericBottomSheet(context: Context,
                                  @LayoutRes rowLayout: Int = R.layout.generic_row,
                                  bind: ((View, Any) -> Unit)? = null,
                         listener: ((View, Any) -> Unit)? = null,
                                  listItem: List<Any>? = null){


    private var mBottomSheetDialog: BottomSheetDialog? = null

    private var genericBottomSheetAdapter: GenericBottomSheetAdapter? = null

    init {
        genericBottomSheetAdapter = getAdapter {
            this.context = context
            this.bind = bind
            this.listItem = listItem?.map {
                Item(it.toString(),it)
            }?: emptyList()
            this.listener = { view, item ->
                listener?.invoke(view,item)
                dismiss()
            }

            this.rowLayout = rowLayout

        }
    }


    var recycler : RecyclerView? = null

    init {
        val contentView = View.inflate(context, R.layout.layout_generic_bottom_dialog, null)
        mBottomSheetDialog = BottomSheetDialog(context)
        mBottomSheetDialog?.setContentView(contentView)


        recycler = contentView.findViewById<RecyclerView>(R.id.recycler_list).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL,false)
        }

        recycler?.adapter   = genericBottomSheetAdapter.apply {
            this?.notifyDataSetChanged()
        }


    }

    fun dismiss(){
        mBottomSheetDialog?.dismiss()
    }

    fun show(){
        mBottomSheetDialog?.show()
    }

    fun addAll(list: List<Any>): GenericBottomSheet{

        genericBottomSheetAdapter?.addAll(list.map {
            Item(it.toString(),it)
        })
        return this

    }

    fun updateAll(listItem: List<Any>?, predicate: (Any?, Any?)-> Boolean = { _, _ ->true}){
        genericBottomSheetAdapter?.updateAll(listItem?.map {
            Item(it.toString(),it)
        },predicate)
    }



    internal class Item(val text:String, val element:Any)
}


class GenericBottomSheetBuilder {

    var context: Context? = null
    var rowLayout: Int = R.layout.generic_row
    var bind:  ((View, Any) -> Unit)? = null
    var listener: ((View, Any) -> Unit)? = null
    var listItem: List<Any>? = null


    fun build(): GenericBottomSheet{
        return context?.let {
            GenericBottomSheet(it, rowLayout, bind, listener,listItem)
        }?:run {
            throw RuntimeException("Context must be not null!")
        }
    }


}

fun getGenericBottomSheet(block: GenericBottomSheetBuilder.() -> Unit) : GenericBottomSheet{
    val b = GenericBottomSheetBuilder()
    block.invoke(b)
    return b.build()
}