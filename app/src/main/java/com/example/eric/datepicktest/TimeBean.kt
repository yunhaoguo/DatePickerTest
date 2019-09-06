package com.example.eric.datepicktest

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntRange
import java.util.*

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */
data class TimeBean(
    val year: Int,
    @IntRange(from = 1, to = 12) val month: Int,
    @IntRange(from = 1, to = 31) val day: Int,
    @IntRange(from = 0, to = 23) val hour: Int,
    @IntRange(from = 0, to = 59) val minute: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt())

    fun before(timeBean: TimeBean): Boolean = getLong() < timeBean.getLong()

    private fun getLong(): Int =
        minute + (hour shl 6) + (day shl 11) + (month shl 16) + (year shl 21)

    override fun toString(): String {
        return "TimeBean(year=$year, month=$month, day=$day, hour=$hour, minute=$minute)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(year)
        parcel.writeInt(month)
        parcel.writeInt(day)
        parcel.writeInt(hour)
        parcel.writeInt(minute)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TimeBean> {
        fun getCurrent(): TimeBean {
            val date = Date(System.currentTimeMillis())
            return TimeBean(
                date.year + 1900,
                date.month + 1,
                date.date,
                date.hours,
                date.minutes
            )
        }

        override fun createFromParcel(parcel: Parcel): TimeBean {
            return TimeBean(parcel)
        }

        override fun newArray(size: Int): Array<TimeBean?> {
            return arrayOfNulls(size)
        }
    }


}