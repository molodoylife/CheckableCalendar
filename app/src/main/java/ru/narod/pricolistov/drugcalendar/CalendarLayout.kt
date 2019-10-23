package ru.narod.pricolistov.drugcalendar

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//TODO FIX SET DATA METHOD!
class CalendarLayout : RecyclerView, OnBottomReachedListener {

    private val locale = Locale.getDefault()
    private var myAdapter: MyAdapter? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat(DrugCalendarView.RECEIVED_MONTH_DEFAULT, locale)
    private var mCalendarObserver: CalendarObserver? = null
    private val initMonthes = arrayListOf<String>(dateFormat.format(calendar.time))

    private fun init() {

        calendar.set(Calendar.DAY_OF_MONTH, 1)

        addNewMonths(5)

        myAdapter = MyAdapter(initMonthes)
        val myLayoutManager = PreCachingLayoutManager(context)

        myAdapter?.setHasStableIds(true)

        layoutManager = myLayoutManager

        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val visibleItemCount = myLayoutManager.childCount
                val totalItemCount = myLayoutManager.itemCount
                val pastVisibleItems = myLayoutManager.findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    addNewMonths(5)
                    myAdapter?.notifyDataSetChanged()
                    onBottomReached(initMonthes.size - 1)
                }
            }
        })

        val dividerItemDecoration = DividerItemDecoration(
            context,
            myLayoutManager.orientation
        )
        addItemDecoration(dividerItemDecoration)
        adapter = myAdapter
    }

    private fun addNewMonths(count: Int) {
        for (i in 1..count) {
            calendar.add(Calendar.MONTH, 1)
            initMonthes.add(dateFormat.format(calendar.time))
        }
    }

    //TODO FIX ME!!!

    /**
     *  Just fix me and all will be okay. According to my logic you need to convert received
     *  map to understandable data for [DrugCalendarView] It's [DrugCalendarView.setDateAndData]
     *  method needs "date: String, data: List<DateState>?" as params. You can do it for example in this method.
     * */
    fun setData(data: Map<String, DateState>, calendarObserver: CalendarObserver) {
        mCalendarObserver = calendarObserver
        val dateList = hashSetOf<String>()
        val dataList = arrayListOf<List<DateState>>()
        var stateList = arrayListOf<DateState>()

        var currYear = 0
        var currMonth = -1

        for (dataItem in data) {
            val date = dataItem.key
            calendar.time = dateFormat.parse(date)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val dateToAdd = dateFormat.format(calendar.time)
            if (calendar.get(Calendar.YEAR) !== currYear || calendar.get(Calendar.MONTH) !== currMonth) {
                dateList.add(dateToAdd)
                stateList = arrayListOf()
                dataList.add(stateList)
                currYear = calendar.get(Calendar.YEAR)
                currMonth = calendar.get(Calendar.MONTH)
            }

            stateList.add(dataItem.value)
        }

        myAdapter!!.setData(dataList)
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
        private var dateList: ArrayList<String>
    ) :
        RecyclerView.Adapter<MyAdapter.GridViewHolder>(),
        DrugCalendarView.OnElementSelectedListener {

        var mData: List<List<DateState>>? = null

        fun addDates() {
            val lastPosition = dateList.size - 1
            //dateList.addAll(newPortion)
            notifyDataSetChanged()
        }

        fun setData(dataList: List<List<DateState>>) {
            mData = dataList
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

            val dataForBind = if (!mData.isNullOrEmpty()) mData!![position] else null
            (holder.myItemView as DrugCalendarView).setDateAndData(
                dateList[position],
                dataForBind
            )
            //(holder.myItemView).onElementSelectListener = this
        }

        override fun getItemId(position: Int): Long {
            return dateList[position].hashCode().toLong()
        }

        inner class GridViewHolder(val myItemView: View) : RecyclerView.ViewHolder(myItemView)

        override fun onElementSelect(isElementSelected: Boolean, date: String) {
            mCalendarObserver?.onElementSelect(isElementSelected, date)
        }
    }

    override fun onBottomReached(pos: Int) {
        Log.d("TAG", "On bottom reached! position $pos")
        addNewMonths(5)
        myAdapter?.addDates()
    }
}

enum class DateState {
    INACTIVE, NORMAL, SELECTED
}

interface OnBottomReachedListener {
    fun onBottomReached(pos: Int)
}

