package com.hooloovoochimico.genericlistbottomsheet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView


internal class GenericBottomSheetAdapter(val context: Context,
                                   @LayoutRes val rowLayout: Int = R.layout.generic_row,
                                   private val bind: ((View, Any) -> Unit)? = null,
                                         private val listener: ((View, Any) -> Unit)? = null,
                                   listItem: List<GenericBottomSheet.Item>? = null): RecyclerView.Adapter<GenericBottomSheetAdapter.GenericAdaperViewHolder>(){


    private val list = mutableListOf<GenericBottomSheet.Item>()

    init {
        list.addAll(listItem?: emptyList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericAdaperViewHolder {
        return GenericAdaperViewHolder(LayoutInflater.from(context).inflate(rowLayout,parent,false))
    }

    override fun getItemCount(): Int  = list.size

    override fun onBindViewHolder(holder: GenericAdaperViewHolder, position: Int) {
        holder.bind(bind, listener,list[position].element)
    }

    fun addAll(items: List<GenericBottomSheet.Item>){
        list.addAll(items)
        notifyDataSetChanged()
    }

    fun updateAll(listItem: List<GenericBottomSheet.Item>?, predicate: (Any?, Any?) -> Boolean) {
        var numberOfMod = 0
        (0..(if(list.size >listItem?.size?:0)list.size else listItem?.size?:0)).forEach { pos ->
            val storedEl = list.getOrNull(pos)
            val newEl = listItem?.getOrNull(pos)

            if(storedEl != null && newEl !=null){
                if (predicate(storedEl.element,newEl.element)) {
                    list.remove(storedEl)
                    list.add(pos, newEl)
                    numberOfMod += 1
                }
            }else if(newEl != null){
                list.add(newEl)
                numberOfMod += 1
            }else{
                list.remove(storedEl)
                numberOfMod += 1
            }
        }

        if(numberOfMod>0) notifyDataSetChanged()
    }


    class GenericAdaperViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bind(action: ((View, Any) -> Unit)? = null,listener: ((View, Any) -> Unit)? = null, any:Any){
            itemView.setOnClickListener {
                listener?.invoke(it,any)
            }
            action?.invoke(itemView,any)
        }

    }
}


internal class GenericBottmSheetAdapterBuilder{

    var context: Context? = null
    var rowLayout: Int = R.layout.generic_row
    var bind:  ((View, Any) -> Unit)? = null
    var listener: ((View, Any) -> Unit)? = null
    internal var listItem: List<GenericBottomSheet.Item>? = null

    internal fun build(): GenericBottomSheetAdapter = GenericBottomSheetAdapter(context!!,rowLayout, bind,listener,listItem)
}

internal fun getAdapter(block: GenericBottmSheetAdapterBuilder.() -> Unit): GenericBottomSheetAdapter{

    val builder = GenericBottmSheetAdapterBuilder()

    block.invoke(builder)

    return builder.build()

}

