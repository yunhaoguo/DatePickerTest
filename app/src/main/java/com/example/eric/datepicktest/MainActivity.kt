package com.example.eric.datepicktest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BirthdayChooseDialogFragment.OnChooseDateCallBack {
    override fun onDateChoose(date: String) {
        tv.text = date
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn.setOnClickListener {
            selectDate()
        }
    }

    private fun selectDate() {
        BirthdayChooseDialogFragment().apply {
            val bundle = Bundle()
            bundle.putString("birthday", "1990-01-01")
            arguments = bundle
            setOnDateChooseCallBack(this@MainActivity)
        }.show(supportFragmentManager, "birthday pick dialog")
    }

}
