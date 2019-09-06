package com.example.eric.datepicktest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */
class BirthdayChooseViewModel : ViewModel() {

    var yearLiveData: MutableLiveData<Int> = MutableLiveData()
    var monthLiveData: MutableLiveData<Int> = MutableLiveData()
    var dayLiveData: MutableLiveData<Int> = MutableLiveData()

}