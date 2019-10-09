package ru.narod.pricolistov.drugcalendar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.narod.pricolistov.gridviewexample.PreCachingLayoutManager


class MainActivity : AppCompatActivity() {
    var data = arrayOf("01.10.19", "01.11.19", "01.12.19", "01.01.20", "01.02.20", "01.03.20", "01.04.20",
        "01.05.20", "01.06.20", "01.07.20")


    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val layoutManager = PreCachingLayoutManager(this)
        layoutManager.initialPrefetchItemCount = 5
        val adapter = MyAdapter(this, data)

        rv.setItemViewCacheSize(20)
        adapter.setHasStableIds(true)


        rv.layoutManager = layoutManager
        rv.adapter = adapter








//        val adapter = MyAdapter(this@MainActivity, data)
//        val linearLayoutManager = LinearLayoutManager(this@MainActivity)
//        rv.layoutManager = linearLayoutManager
//        rv.adapter = adapter


        //adjustGrid()
    }
}
