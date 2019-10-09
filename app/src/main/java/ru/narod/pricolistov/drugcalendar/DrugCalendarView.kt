package ru.narod.pricolistov.drugcalendar

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min


class DrugCalendarView : View {

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
    private val screenWidth = getCurrentScreenWidth()
    private val squareSize = getSquareSideLength()
    private val initOffset = squareSize / 2

    private var date: String? = null
    private var datePositions: List<DateSquare>? = null

    private var mWidth = 0
    private var mHeight = 0

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        textPaint.strokeWidth = 3f
        textPaint.color = Color.BLACK

        textPaint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            30f, resources.displayMetrics
        )

        textPaint.textAlign = Paint.Align.CENTER

        //calendar.set(Calendar.DAY_OF_WEEK, i + 1)
    }

    private fun initComputation(attr: AttributeSet?) {

        attr?.let {
            val typedArray = context.obtainStyledAttributes(
                attr,
                R.styleable.DrugCalendarView,
                0, 0
            )
            textPaint.textSize = typedArray.getDimension(0, 16f)
        }
    }

    constructor(ctx: Context) : super(ctx) {
        Log.d("", "")
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initComputation(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initComputation(attrs)
    }

    override fun onDraw(canvas: Canvas) {

        datePositions?.let {
            for ((index, date) in datePositions!!.withIndex()) {
                canvas.drawText("$index", date.x, date.y, textPaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = screenWidth
        var desiredHeight = 100

        datePositions?.let {
            if (it.isNotEmpty()) {
                desiredHeight = (it[it.size - 1].y + initOffset).toInt()
            }
        }

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int

        //Measure Width
        width = when (widthMode) {
            MeasureSpec.EXACTLY -> //Must be this size
                widthSize
            MeasureSpec.AT_MOST -> //Can't be bigger than...
                min(desiredWidth, widthSize)
            else -> //Be whatever you want
                desiredWidth
        }

        //Measure Height
        height = when (heightMode) {
            MeasureSpec.EXACTLY -> //Must be this size
                heightSize
            MeasureSpec.AT_MOST -> //Can't be bigger than...
                min(desiredHeight, heightSize)
            else -> //Be whatever you want
                desiredHeight
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = w
        mHeight = h
    }


    private fun getCurrentScreenWidth(): Int {
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    private fun getSquareSideLength(): Int {
        return screenWidth / 7
    }

    fun setDate(date: String) {
        this.date = date
        datePositions = getPositionsForDates()
    }

    private fun getPositionsForDates(): List<DateSquare> {

        calendar.time = dateFormat.parse(date!!)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val numFirstDayOfWeek = calendar.firstDayOfWeek

        val gapOfFirstDayOfMonth = (calendar.get(Calendar.DAY_OF_WEEK) - numFirstDayOfWeek)
        val numberOfDaysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val squareList = ArrayList<DateSquare>()

        for (i in 0..numberOfDaysInCurrentMonth) {
            val x = (initOffset + (gapOfFirstDayOfMonth + i) % 7 * squareSize).toFloat()
            val y =
                (initOffset + ((i + gapOfFirstDayOfMonth) / 7) * squareSize).toFloat() + initOffset
            squareList.add(DateSquare(x, y))
        }

        return squareList
    }
}

