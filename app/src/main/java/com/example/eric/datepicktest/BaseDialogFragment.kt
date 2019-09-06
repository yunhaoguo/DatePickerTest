package com.example.eric.datepicktest

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */

abstract class BaseDialogFragment(
    protected var _cancelable: Boolean = false,
    private var _cancelableOnTouchOutside: Boolean = false) : DialogFragment() {

    open val backgroundRes: Int = 0

    companion object {
        const val CANCELABLE_KEY = "riki_"
        const val CANCELABLE_ON_TOUCH_OUTSIDE = "pudge_"

        fun safelyShow(dialogFragment: BaseDialogFragment, manager: FragmentManager?, tag: String) {
            try {
                dialogFragment.show(manager, tag)
            } catch (e: Exception) {
                //Logger.d(tag, "catch exception when DialogFragment#Show() $e")
            }
        }
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            _cancelable = it.getBoolean(CANCELABLE_KEY, _cancelable)
            _cancelableOnTouchOutside = it.getBoolean(CANCELABLE_KEY, _cancelableOnTouchOutside)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(CANCELABLE_KEY, _cancelable)
        outState.putBoolean(CANCELABLE_ON_TOUCH_OUTSIDE, _cancelableOnTouchOutside)
    }

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (backgroundRes != 0) {
            dialog.window?.setBackgroundDrawableResource(backgroundRes)
        }
        return createView(inflater, container)
    }

    abstract fun createView(layoutInflater: LayoutInflater, container: ViewGroup?): View

    override fun onStart() {
        super.onStart()
        dialog.apply {
            setCancelable(_cancelable)
            setCanceledOnTouchOutside(_cancelableOnTouchOutside)
            window?.let { window ->
                window.attributes = window.attributes.apply {
                    width = getWidth()
                    height = getHeight()
                    gravity = getGravity()
                }
            }
        }
    }

    open fun getGravity(): Int {
        return Gravity.CENTER
    }

    open fun getWidth(): Int {
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

    open fun getHeight(): Int {
        return WindowManager.LayoutParams.WRAP_CONTENT
    }

}