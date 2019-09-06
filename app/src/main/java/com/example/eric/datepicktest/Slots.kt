package com.example.eric.datepicktest

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import kotlin.properties.Delegates

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */

open class Slots<Bean> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    protected var _wheelViewGroups: MutableList<LoopView> = mutableListOf()

    val currentSelections: List<Bean>
        get() =
            _wheelViewGroups.mapIndexed { index, wheelView ->
                options[index].dataLists[wheelView.selectedItem]
            }


    var onLoopViewSelectedListener: OnLoopViewSelectedListener? = null

    var options: List<WheelViewOptions<Bean>> by Delegates.observable(listOf()) { _, _, new ->
        // clear old items
        _wheelViewGroups.clear()
        removeAllViews()

        // rebuild new items
        new.forEachIndexed { index, option ->
            _wheelViewGroups.add(
                LoopView(context).apply {
                    setItems(
                        option.dataLists.map {
                            option.getStringLambda(it)
                        }
                    )
                    setPadding(option.paddingLeftAndRight, 0, option.paddingLeftAndRight, 0)
                    setIsLoop(option.isLoop)
                    setInitPosition(option.initPosition)
                    setItemsVisibleCount(option.itemVisibleCount)
                    setCenterTextColor(context.resources.getColor(option.centerColorRes))
                    setOuterTextColor(context.resources.getColor(option.outerColorRes))
                    setTextSize(option.textSizeInDp.toFloat())
                    setCenterTextBold(option.centerTextBold)
                    setDividerColor(option.dividerColor)
                    addView(
                        this,
                        LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1f).apply {
                            gravity = Gravity.CENTER
                        })
                    setListener { value ->
                        onLoopViewSelectedListener?.onLoopViewSelected(index, value)
                    }
                })
        }
    }

    init {
        orientation = LinearLayout.HORIZONTAL
    }

    fun setOnLoopViewSelectListener(listener: OnLoopViewSelectedListener) {
        onLoopViewSelectedListener = listener
    }

    interface OnLoopViewSelectedListener {
        fun onLoopViewSelected(position: Int, value: String)
    }
}




class WheelViewOptions<Bean>(
    val dataLists: List<Bean>,
    val itemVisibleCount: Int,
    val textSizeInDp: Int,
    val initPosition: Int = 0,
    val isLoop: Boolean = false,
    val centerColorRes: Int,
    val outerColorRes: Int,
    val centerTextBold: Boolean = true,
    val paddingLeftAndRight: Int = 0,
    val dividerColor: Int = Color.parseColor("#ffffff"),
    val getStringLambda: (Bean) -> String = {
        it.toString()
    })