package ru.narod.pricolistov.drugcalendar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CalendarLayout : RecyclerView {
    private val locale = Locale.getDefault()
    private var myAdapter: MyAdapter? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat(DrugCalendarView.RECEIVED_MONTH_DEFAULT, locale)
    private var mCalendarObserver: CalendarObserver? = null

    private fun init() {

        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstMonth =
            SimpleDateFormat(DrugCalendarView.RECEIVED_MONTH_DEFAULT, locale).format(calendar.time)

        myAdapter = MyAdapter(
            arrayOf(firstMonth),
            arrayListOf(arrayListOf(DateState.NORMAL))
        )
        val myLayoutManager = PreCachingLayoutManager(context)

        myAdapter?.setHasStableIds(true)

        layoutManager = myLayoutManager
        setItemViewCacheSize(100)

        val dividerItemDecoration = DividerItemDecoration(
            context,
            myLayoutManager.orientation
        )
        addItemDecoration(dividerItemDecoration)
        adapter = myAdapter
    }




    fun setData(data: Map<String, DateState>, calendarObserver: CalendarObserver) {
        mCalendarObserver = calendarObserver
        val dateList = hashSetOf<String>()
        val dataList = arrayListOf<List<DateState>>()
        var stateList = arrayListOf<DateState>()

        var currYear = 0
        var currMonth = -1

        for (dataItem in data){
            val date = dataItem.key
            calendar.time = dateFormat.parse(date)
            if (calendar.get(Calendar.YEAR) !== currYear || calendar.get(Calendar.MONTH) !== currMonth) {
                dateList.add(date)
                stateList = arrayListOf()
                dataList.add(stateList)
                currYear = calendar.get(Calendar.YEAR)
                currMonth = calendar.get(Calendar.MONTH)
            }

            stateList.add(dataItem.value)
        }

        myAdapter!!.setData(dateList.toList(), dataList)
    }

    interface CalendarObserver {
        fun onElementSelect(isElementSelected: Boolean, date: String)

        fun onScrolledEnd(date: String)

        fun onScrolledStart(date: String)
    }

    constructor(ctx: Context) : super(ctx) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    inner class MyAdapter(
        private var dateList: Array<String>,
        private var data: List<List<DateState>>
    ) :
        RecyclerView.Adapter<MyAdapter.GridViewHolder>(),
        DrugCalendarView.OnElementSelectedListener {

        fun setData(dates: List<String>, dataList: List<List<DateState>>){
            dateList = dates.toTypedArray()
            data = dataList
            notifyDataSetChanged()
        }


        override fun getItemCount(): Int {
            return if (!dateList.isNullOrEmpty()) dateList.size else 0
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)

            return GridViewHolder(itemView)
        }


        override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
            (holder.myItemView as DrugCalendarView).setDateAndData(
                dateList[position],
                data[position]
            )
            (holder.myItemView as DrugCalendarView).onElementSelectListener = this
        }

        override fun getItemId(position: Int): Long {
            return dateList[position].hashCode().toLong()
        }

        inner class GridViewHolder(val myItemView: View) : RecyclerView.ViewHolder(myItemView)

        override fun onElementSelect(isElementSelected: Boolean, date: String) {
            mCalendarObserver?.onElementSelect(isElementSelected, date)
        }
    }
}

enum class DateState {
    INACTIVE, NORMAL, SELECTED
}

