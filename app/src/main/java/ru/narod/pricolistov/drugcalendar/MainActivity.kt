package ru.narod.pricolistov.drugcalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import androidx.recyclerview.widget.DividerItemDecoration




enum class DateState {
    INACTIVE, NORMAL, SELECTED
}

class MainActivity : AppCompatActivity() {
//    val date = arrayOf(
//        "01.10.19", "01.11.19",
//        "01.12.19", "01.01.20", "01.02.20", "01.03.20", "01.04.20",
//        "01.05.20", "01.06.20", "01.07.20", "01.08.20", "01.09.20",
//        "01.10.20", "01.11.20", "01.12.20"
//    )

    val data = arrayListOf<Array<DateState>>()

    val date = arrayOf("01.10.19")




    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val layoutManager = PreCachingLayoutManager(this)

        for(i in 0..date.size){
            val dataArray = Array(31) {
                val r = Random()
                when (r.nextInt(3)) {
                    0 -> DateState.INACTIVE
                    1 -> DateState.NORMAL
                    2 -> DateState.SELECTED
                    else -> DateState.NORMAL
                }
            }
            data.add(dataArray)
        }


        val adapter = MyAdapter(date, data)
        adapter.setHasStableIds(true)

        rv.layoutManager = layoutManager
        rv.setItemViewCacheSize(100)

        val dividerItemDecoration = DividerItemDecoration(
            rv.context,
            layoutManager.orientation
        )
        rv.addItemDecoration(dividerItemDecoration)
        rv.adapter = adapter
    }
}
