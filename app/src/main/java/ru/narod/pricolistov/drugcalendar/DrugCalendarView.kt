package ru.narod.pricolistov.drugcalendar

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min


class DrugCalendarView : View {

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
    private var dateFormatMonthTitle = ""
    private val screenWidth = getCurrentScreenWidth()
    private val squareSize = getSquareSideLength()
    private val initOffset = squareSize / 2
    private val initSquareSelectionRadius = initOffset * 0.75f
    private var daysInWeekNames: Array<String>? = null
    private val numberOfFirstDayInWeekInCurrentLocale = calendar.firstDayOfWeek

    private var date: String? = null
    private var datePositions: List<DateSquare>? = null
    private var daysInWeekNamesPositions: List<DateSquare>? = null


    private var dateForUnselect: DateSquare? = null

    private var mWidth = 0
    private var mHeight = 0

    private val selectedSquares: MutableSet<DateSquare> = hashSetOf()

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaintMonthName = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textVerticalOffsetToBeDrawnInCenter = 0f

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 2f
        circlePaint.color = Color.GREEN

        textPaint.strokeWidth = 3f
        textPaint.color = Color.BLACK
        textPaint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            16f, resources.displayMetrics
        )
        textPaint.textAlign = Paint.Align.CENTER


        textPaintMonthName.strokeWidth = 3f
        textPaintMonthName.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaintMonthName.color = Color.BLACK
        textPaintMonthName.textAlign = Paint.Align.LEFT
        textPaintMonthName.textSize = textPaint.textSize

        textVerticalOffsetToBeDrawnInCenter = (textPaint.descent() + textPaint.ascent()) / 2

        val formatLetterDay = SimpleDateFormat("EEEEE", Locale.getDefault())

        for (i in 1..8) {
            daysInWeekNames = Array(7) {
                calendar.set(Calendar.DAY_OF_WEEK, it)
                formatLetterDay.format(calendar.time)
            }
        }

        daysInWeekNamesPositions = getPositionsForWeekDayNames()
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

    override fun onDraw(canvas: Canvas) {

        textPaintMonthName.textAlign = Paint.Align.LEFT
        //TODO Optimize onDraw method

        canvas.drawText(
            dateFormatMonthTitle,
            initOffset.toFloat() / 2,
            initOffset - textVerticalOffsetToBeDrawnInCenter,
            textPaintMonthName
        )

        textPaintMonthName.textAlign = Paint.Align.CENTER


        //TODO Optimize getting x coords for this label
        canvas.drawText(
            "14/28",
            getPositionsForWeekDayNames()[6].x,
            initOffset - textVerticalOffsetToBeDrawnInCenter,
            textPaintMonthName
        )


        dateForUnselect?.let {

            //TODO create separate Paint objects for different typs of drawing
            circlePaint.color = Color.WHITE
            canvas.drawCircle(it.x, it.y, initSquareSelectionRadius, circlePaint)
        }

        for (selected in selectedSquares) {
            circlePaint.color = Color.GREEN
            canvas.drawCircle(selected.x, selected.y, initSquareSelectionRadius, circlePaint)
        }

        daysInWeekNames?.let {
            for (i in 0..7) {
                canvas.drawText(
                    "${it[(i + numberOfFirstDayInWeekInCurrentLocale) % 7]}",
                    daysInWeekNamesPositions!![i].x,
                    daysInWeekNamesPositions!![i].y - textVerticalOffsetToBeDrawnInCenter,
                    textPaint
                )
            }
        }

        datePositions?.let {
            for (date in datePositions!!) {
                canvas.drawText(
                    "${date.date}",
                    date.x,
                    date.y - textVerticalOffsetToBeDrawnInCenter,
                    textPaint
                )
            }
        }
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
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {
                val selectedSquare = getSelectedDateSquare(x, y)
                selectedSquare?.let {
                    dateForUnselect = if (!selectedSquares.add(selectedSquare)) {
                        selectedSquares.remove(selectedSquare)
                        selectedSquare
                    } else
                        null
                    invalidate()
                }
            }
            MotionEvent.ACTION_CANCEL -> {

            }
        }
        return true
    }

    private fun getSelectedDateSquare(x: Float, y: Float): DateSquare? {
        for (date in datePositions!!) {

            //TODO adjust radius and create more productive algorithm for finding selected element
            if (isInArea(x, y, date.x, date.y)) {
                return DateSquare(date.x, date.y, 0)
            }
        }

        return null
    }

    private fun isInArea(touchX: Float, touchY: Float, centerX: Float, centerY: Float): Boolean {
        val rect = RectF(
            centerX - initOffset, centerY - initOffset,
            centerX + initOffset, centerY + initOffset
        )
        return rect.contains(touchX, touchY)
    }

    private fun getPositionsForWeekDayNames(): List<DateSquare> {
        val squareList = ArrayList<DateSquare>()

        for (i in 0..7) {
            val x = (initOffset + i * squareSize).toFloat()
            val y = (initOffset).toFloat() + squareSize
            squareList.add(DateSquare(x, y, 0))
        }

        return squareList
    }

    private fun getPositionsForDates(): List<DateSquare> {

        calendar.time = dateFormat.parse(date!!)

        dateFormatMonthTitle =
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val numFirstDayOfWeek = calendar.firstDayOfWeek

        val dayOfWeekForFirstOfCurrentMonth = calendar.get(Calendar.DAY_OF_WEEK)

        val gapOfFirstDayOfMonth = if (dayOfWeekForFirstOfCurrentMonth >= numFirstDayOfWeek)
            calendar.get(Calendar.DAY_OF_WEEK) - numFirstDayOfWeek
        else
            7 - numFirstDayOfWeek

        val numberOfDaysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val squareList = ArrayList<DateSquare>()

        for (i in 0 until numberOfDaysInCurrentMonth) {
            val x = (initOffset + (gapOfFirstDayOfMonth + i) % 7 * squareSize).toFloat()

            //TODO create vals for values like squareSize * 2 to make code more clear
            val y =
                (initOffset + ((i + gapOfFirstDayOfMonth) / 7) * squareSize).toFloat() + squareSize * 2
            squareList.add(DateSquare(x, y, i + 1))
        }

        return squareList
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
}

