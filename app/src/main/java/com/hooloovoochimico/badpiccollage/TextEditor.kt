package com.hooloovoochimico.badpiccollage

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment


fun EditText.onTextChanged(after: (s: Editable) -> Unit = {},
                           before: (string: String, start: Int, count: Int, after: Int) -> Unit = { _, _, _, _ -> },
                           onTextChanged: (string: String, start: Int, count: Int, after: Int) -> Unit) =
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) = after.invoke(s)
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) =
            before.invoke(s.toString(), start, count, after)

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = onTextChanged(s.toString(), start, before, count)
    })


fun EditText.afterTextChange(after: (s: Editable) -> Unit = {}) = addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable) = after.invoke(s)
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
})

/**
 * Transparent Dialog Fragment, with no title and no background
 *
 *
 * The fragment imitates capturing input from keyboard, but does not display anything
 * the result from input from the keyboard is passed through [TextEditorDialogFragment.OnTextLayerCallback]
 *
 *
 * Activity that uses [TextEditorDialogFragment] must implement [TextEditorDialogFragment.OnTextLayerCallback]
 *
 *
 * If Activity does not implement [TextEditorDialogFragment.OnTextLayerCallback], exception will be thrown at Runtime
 */
class TextEditorDialogFragment
private constructor()// empty, use getInstance
    : DialogFragment() {

    protected var editText: EditText? = null

    private var callback: OnTextLayerCallback? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is OnTextLayerCallback) {
            this.callback = activity
        } else {
            throw IllegalStateException(
                activity.javaClass.name
                        + " must implement " + OnTextLayerCallback::class.java.name
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.text_editor_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments
        var text = ""
        if (args != null) {
            text = args.getString(ARG_TEXT)?:""
        }

        editText = view.findViewById(R.id.edit_text_view) as EditText

        initWithTextEntity(text)


        editText?.afterTextChange { s->
            callback?.textChanged(s.toString())
        }

        view.findViewById<View>(R.id.text_editor_root)?.setOnClickListener {
            dismiss()
        }
    }

    private fun initWithTextEntity(text: String) {
        editText!!.setText(text)
        editText!!.post {
            if (editText != null) {
                Selection.setSelection(editText!!.text, editText!!.length())
            }
        }
    }

    override fun dismiss() {
        super.dismiss()

        // clearing memory on exit, cos manipulating with text uses bitmaps extensively
        // this does not frees memory immediately, but still can help
        System.gc()
        Runtime.getRuntime().gc()
    }

    override fun onDetach() {
        // release links
        this.callback = null
        super.onDetach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.requestWindowFeature(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val window = dialog.window
            if (window != null) {
                // remove background
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

                // remove dim
                val windowParams = window.attributes
                window.setDimAmount(0.0f)
                window.attributes = windowParams
            }
        }
    }

    override fun onResume() {
        super.onResume()
        editText!!.post {
            // force show the keyboard
            setEditText(true)
            editText!!.requestFocus()
            val ims = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            ims.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setEditText(gainFocus: Boolean) {
        if (!gainFocus) {
            editText!!.clearFocus()
            editText!!.clearComposingText()
        }
        editText!!.isFocusableInTouchMode = gainFocus
        editText!!.isFocusable = gainFocus
    }

    /**
     * Callback that passes all user input through the method
     * [TextEditorDialogFragment.OnTextLayerCallback.textChanged]
     */
    interface OnTextLayerCallback {
        fun textChanged(text: String)
    }

    companion object {

        const val ARG_TEXT = "editor_text_arg"

        fun getInstance(textValue: String): TextEditorDialogFragment {
            val fragment = TextEditorDialogFragment()
            val args = Bundle()
            args.putString(ARG_TEXT, textValue)
            fragment.arguments = args
            return fragment
        }
    }
}