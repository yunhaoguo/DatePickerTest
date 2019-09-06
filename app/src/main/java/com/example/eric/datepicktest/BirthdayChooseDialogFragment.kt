package com.example.eric.datepicktest

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModelProviders
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */

class BirthdayChooseDialogFragment : BaseDialogFragment(true, true), Slots.OnLoopViewSelectedListener {


    private lateinit var slots: BirthdaySlotsView

    private var currentDay: Int
    private var currentMonth: Int
    private var currentYear: Int

    private var birthdayViewModel: BirthdayChooseViewModel? = null
    private var onChooseDateCallBack: OnChooseDateCallBack? = null

    init {
        val date = TimeBean.getCurrent()
        currentDay = date.day
        currentMonth = date.month
        currentYear = date.year
    }

    override fun createView(layoutInflater: LayoutInflater, container: ViewGroup?): View {
        //dialog?.window?.setBackgroundDrawable(ContextCompat.getDrawable(context!!, R.drawable.bg_birthday_calendar_pick))
        return layoutInflater.inflate(R.layout.dialog_fragment_time_select, container, false).apply {
            val monthList = mutableListOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val newDayList = List(31) { index ->
                (index + 1).toString()
            }
            slots = findViewById<BirthdaySlotsView>(R.id.time_select_wheel_vg).apply {
                this.options = listOf(
                    WheelViewOptions(monthList, 3, 16, 0, false, R.color.c1, R.color.c4, centerTextBold = false, paddingLeftAndRight = 20, dividerColor = Color.parseColor("#ff5e00")),
                    WheelViewOptions(newDayList, 3, 16, 0, false, R.color.c1, R.color.c4, centerTextBold = false, paddingLeftAndRight = 20, dividerColor = Color.parseColor("#ff5e00")),
                    WheelViewOptions((START_YEAR..(currentYear - MIN_AGE)).map { it.toString() }, 3, 16, 0, false, R.color.c1, R.color.c4, centerTextBold = false, paddingLeftAndRight = 20, dividerColor = Color.parseColor("#ff5e00"))
                )
                setOnLoopViewSelectListener(this@BirthdayChooseDialogFragment)
            }
            findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dismissAllowingStateLoss()
            }
            findViewById<View>(R.id.btn_set).setOnClickListener {
                birthdayViewModel?.let {
                    val monthResult = StringBuilder(it.monthLiveData.value.toString())
                    if (monthResult.length < 2) {
                        monthResult.insert(0, "0")
                    }
                    val dayResult = StringBuilder(it.dayLiveData.value.toString())
                    if (dayResult.length < 2) {
                        dayResult.insert(0, "0")
                    }
                    //保证用户不会因为快速操作导致设置超出限制的日期
                    val result = "${it.yearLiveData.value}-${monthResult}-${dayResult}"
                    val date = SimpleDateFormat("yyyy-mm-dd").parse(result)
                    val needResult = "${currentYear - 13}-${if (currentMonth < 10) "0$currentMonth" else currentMonth}-${if (currentDay < 10) "0$currentDay" else currentDay}"
                    val needDate = SimpleDateFormat("yyyy-mm-dd").parse(needResult)
                    if (needDate.after(date)) {
                        onChooseDateCallBack?.onDateChoose(result)
                        dismiss()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initData()
    }

    private fun initData() {
        val dateStr = arguments?.getString("birthday")
        if (!dateStr.isNullOrEmpty()) {
            val dateArr = dateStr.split("-")
            //0-年 1-月 2-日
            slots.setSelectedItemValue(DAY_SELECTED, dateArr[2].toInt())
            slots.setSelectedItemValue(MONTH_SELECTED, dateArr[1].toInt())
            slots.setSelectedItemValue(YEAR_SELECTED, dateArr[0].toInt())
        } else {
            birthdayViewModel?.let {
                slots.setSelectedItemValue(DAY_SELECTED, INITIAL_DAY)
                slots.setSelectedItemValue(MONTH_SELECTED, INITIAL_MONTH)
                slots.setSelectedItemValue(YEAR_SELECTED, START_YEAR)
            }
        }
    }

    private fun initViewModel() {
        birthdayViewModel = ViewModelProviders.of(this).get(BirthdayChooseViewModel::class.java)

        birthdayViewModel?.yearLiveData?.observe(this, androidx.lifecycle.Observer {
            if (it == currentYear - MIN_AGE) {
                //年份为最大可选年时更新月份list
                slots.updateMonthData(currentMonth, MONTH_MAP.keys.toList())
            } else {
                slots.updateMonthData(MONTHS_IN_A_YEAR, MONTH_MAP.keys.toList())
            }
        })

        birthdayViewModel?.monthLiveData?.observe(this, androidx.lifecycle.Observer {
            if (birthdayViewModel?.yearLiveData?.value == currentYear - MIN_AGE && it == currentMonth) {
                //年份为最大可选年且月份为最大可选月时更新天数list
                slots.updateDayData(currentDay - 1)
            } else {
                updateDayData()
            }
        })
    }

    private fun updateDayData() {
        val days = getDaysOfMonth()
        if (days != -1) {
            slots.updateDayData(days)
        }
    }

    private fun getDaysOfMonth(): Int {
        val calendar = Calendar.getInstance()
        birthdayViewModel?.let {
            if (it.yearLiveData.value != null && it.monthLiveData.value != null) {
                calendar.set(it.yearLiveData.value!!, it.monthLiveData.value!!, 0)
                return calendar.get(Calendar.DAY_OF_MONTH)
            }
        }
        return -1
    }

    override fun onLoopViewSelected(position: Int, value: String) {
        when (position) {
            MONTH_SELECTED -> {
                if (value.isDigitsOnly()) {
                    birthdayViewModel?.monthLiveData?.value = value.toInt()
                } else {
                    birthdayViewModel?.monthLiveData?.value = MONTH_MAP[value]
                }
            }
            DAY_SELECTED -> {
                birthdayViewModel?.dayLiveData?.value = value.toInt()
            }
            YEAR_SELECTED -> {
                birthdayViewModel?.yearLiveData?.value = value.toInt()
            }
        }
    }

    fun setOnDateChooseCallBack(callback: OnChooseDateCallBack) {
        this.onChooseDateCallBack = callback
    }

    interface OnChooseDateCallBack {
        fun onDateChoose(date: String)
    }

    companion object {
        const val MONTH_SELECTED = 0
        const val DAY_SELECTED = 1
        const val YEAR_SELECTED = 2
        const val START_YEAR = 1900
        const val MIN_AGE = 13
        const val INITIAL_MONTH = 1
        const val INITIAL_DAY = 1
        const val MONTHS_IN_A_YEAR = 12
        val MONTH_MAP = mapOf(Pair("Jan", 1), Pair("Feb", 2), Pair("Mar", 3), Pair("Apr", 4), Pair("May", 5),
            Pair("Jun", 6), Pair("Jul", 7), Pair("Aug", 8), Pair("Sep", 9), Pair("Oct", 10), Pair("Nov", 11), Pair("Dec", 12))
    }

}