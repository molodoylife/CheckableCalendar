package ru.narod.pricolistov.drugcalendar

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), CalendarLayout.CalendarObserver {

    private val date = arrayOf(
        "01.10.19", "02.10.19",
        "03.10.19", "04.10.19", "05.10.19", "06.10.19", "07.10.19",
        "08.10.19", "09.10.19", "10.10.19", "11.10.19", "12.10.19",
        "13.10.19", "14.10.19", "15.10.19", "16.10.19"
    )

    private val data = hashMapOf<String, DateState>()

    //val date = arrayOf("01.10.19")


    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (element in date) {
            val r = Random()
            val state = when (r.nextInt(3)) {
                0 -> DateState.INACTIVE
                1 -> DateState.NORMAL
                2 -> DateState.SELECTED
                else -> DateState.NORMAL
            }
            data[element] = state
        }

        cv.setData(data, this)
    }

    override fun onElementSelect(isElementSelected: Boolean, date: String) {
        Log.d("TAG", "Selected $isElementSelected , date $date")
    }

    override fun onScrolledEnd(date: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onScrolledStart(date: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
