package ru.narod.pricolistov.drugcalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.narod.pricolistov.gridviewexample.PreCachingLayoutManager


enum class DateState{
    INACTIVE, NORMAL, SELECTED
}

class MainActivity : AppCompatActivity() {
//    var date = arrayOf("01.10.19", "01.11.19",
//        "01.12.19", "01.01.20", "01.02.20", "01.03.20", "01.04.20",
//        "01.05.20", "01.06.20", "01.07.20", "01.08.20", "01.09.20",
//        "01.10.20", "01.11.20", "01.12.20")

    val date = arrayOf("01.10.19")

    val data = Array(31){
        when(it){
            0->DateState.INACTIVE
            1->DateState.NORMAL
            2->DateState.SELECTED
            else->DateState.NORMAL
        }
    }




    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val layoutManager = PreCachingLayoutManager(this)
        val adapter = MyAdapter(date, data)

        rv.layoutManager = layoutManager
        rv.adapter = adapter
    }
}
