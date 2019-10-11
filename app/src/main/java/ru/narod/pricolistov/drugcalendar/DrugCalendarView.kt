package ru.narod.pricolistov.drugcalendar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min


//TODO refactor me
class DrugCalendarView : View {

    companion object {
        const val GRAY = Color.GRAY
        const val GREEN = Color.GREEN
        const val BLACK = Color.BLACK

        const val CIRCLE_STROKE_WIDTH = 4f
        const val TEXT_STROKE_WIDTH = 3f
        const val TEXT_SIZE_DEFAULT = 16f


        val ALIGN_CENTER = Paint.Align.CENTER
        val ALIGN_LEFT = Paint.Align.LEFT

        const val WEEK_NAME_FORMAT = "EEEEE"
        const val RECEIVED_MONTH_DEFAULT = "dd.MM.yy"
    }

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat(RECEIVED_MONTH_DEFAULT, Locale.getDefault())
    private var numberOfDaysInCurrentMonth = 0
    private var dateFormatMonthTitle = ""
    private val screenWidth = getCurrentScreenWidth()
    private val squareSize = getSquareSideLength()
    private val initOffset = squareSize / 2
    private val initSquareSelectionRadius = initOffset * 0.75f
    private var daysInWeekNames: Array<String>? = null
    private val numberOfFirstDayInWeekInCurrentLocale = calendar.firstDayOfWeek

    private var stillCanSelectThisDate = false
    private var ifFingerTouches = false


    private var date: String? = null

    private var daysInWeekNamesPositions = getPositionsForWeekDayNames()
    private var datePositions: List<DateSquare>? = null

    private var datePressedNow: DateSquare? = null
    private var radiusRippleEffect = 0f


    private var dateForArcAnimation: DateSquare? = null
    private var arcAngle = 0f
    private var arcAnimRect: RectF = RectF(0f, 0f, 0f, 0f)


    private var mWidth = 0
    private var mHeight = 0
    private var selectionColor = Color.BLUE

    private val selectedSquares: MutableSet<DateSquare> = hashSetOf()

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = TEXT_STROKE_WIDTH
        color = BLACK
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            TEXT_SIZE_DEFAULT, resources.displayMetrics
        )
        textAlign = ALIGN_CENTER
    }

    private val textVerticalOffsetToBeDrawnInCenter = (textPaint.descent() + textPaint.ascent()) / 2

    private val yPositionForTextWithOffset = initOffset - textVerticalOffsetToBeDrawnInCenter

    private val textPaintMonthName = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = TEXT_STROKE_WIDTH
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = BLACK
        textAlign = ALIGN_LEFT
        textSize = textPaint.textSize
    }

    private val emptyCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = CIRCLE_STROKE_WIDTH
        color = selectionColor
    }
    private val filledCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = CIRCLE_STROKE_WIDTH
        color = GRAY
    }


    private var animator: ValueAnimator? = null

    init {
        val formatLetterDay = SimpleDateFormat(WEEK_NAME_FORMAT, Locale.getDefault())
        for (i in 1..8) {
            daysInWeekNames = Array(7) {
                calendar.set(Calendar.DAY_OF_WEEK, it)
                formatLetterDay.format(calendar.time)
            }
        }
    }

    private fun initComputation(attr: AttributeSet?) {

        attr?.let {
            val typedArray = context.obtainStyledAttributes(
                attr,
                R.styleable.DrugCalendarView
            )
            textPaint.textSize = typedArray.getDimension(R.styleable.DrugCalendarView_textSize, 16f)
            selectionColor =
                typedArray.getColor(R.styleable.DrugCalendarView_selectedColor, Color.BLACK)
            Log.d("", "")
            typedArray.recycle()
        }
    }

    private fun startAnimationRipple(isElementSelected: Boolean) {
        animator?.cancel()
        filledCirclePaint.color = if (isElementSelected) selectionColor else GRAY
        animator = ValueAnimator.ofInt(0, initSquareSelectionRadius.toInt()).apply {
            duration = 240
            interpolator = DecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                radiusRippleEffect = (valueAnimator.animatedValue as Int).toFloat()
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (!ifFingerTouches) {
                        radiusRippleEffect = 0f
                        if (stillCanSelectThisDate) {
                            addOrRemoveDateWithAnimation()
                        }
                    }
                }
            })
        }
        animator?.start()
    }

    private fun addOrRemoveDateWithAnimation() {
        datePressedNow?.let {
            if (!selectedSquares.contains(it)) {
                startAnimationChecking(it)
            } else {
                selectedSquares.remove(it)
                invalidate()
            }
        }
    }

    private fun startAnimationChecking(selectedDate: DateSquare) {
        animator?.cancel()
        arcAnimRect.left = selectedDate.x - initSquareSelectionRadius
        arcAnimRect.top = selectedDate.y - initSquareSelectionRadius
        arcAnimRect.right = selectedDate.x + initSquareSelectionRadius
        arcAnimRect.bottom = selectedDate.y + initSquareSelectionRadius
        animator = ValueAnimator.ofInt(0, 360).apply {
            duration = 300
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { valueAnimator ->
                arcAngle = (valueAnimator.animatedValue as Int).toFloat()
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    dateForArcAnimation = null
                    arcAngle = 0f
                    selectedSquares.add(selectedDate)
                }
            })
        }
        animator?.start()
    }

    //TODO Refactor me
    override fun onDraw(canvas: Canvas) {


        textPaintMonthName.textAlign = Paint.Align.LEFT
        //TODO Optimize onDraw method

        datePressedNow?.let {
            canvas.drawCircle(it.x, it.y, radiusRippleEffect, filledCirclePaint)
        }

        date?.let {
            canvas.drawArc(
                arcAnimRect,
                90f,
                arcAngle,
                false,
                emptyCirclePaint
            )
        }

        canvas.drawText(
            dateFormatMonthTitle,
            initOffset.toFloat() / 2,
            yPositionForTextWithOffset,
            textPaintMonthName
        )

        textPaintMonthName.textAlign = ALIGN_CENTER


        //TODO Optimize getting x coords for this label
        canvas.drawText(
            "${selectedSquares.size}/$numberOfDaysInCurrentMonth",
            getPositionsForWeekDayNames()[6].x,
            yPositionForTextWithOffset,
            textPaintMonthName
        )

        for (selected in selectedSquares) {
            canvas.drawCircle(selected.x, selected.y, initSquareSelectionRadius, emptyCirclePaint)
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


        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val selectedSquare = getSelectedDateSquare(event.x, event.y)
                selectedSquare?.let {
                    ifFingerTouches = true
                    datePressedNow = it
                    startAnimationRipple(selectedSquares.contains(selectedSquare))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                datePressedNow?.let { pressedItem ->
                    if (!isInArea(event.x, event.y, pressedItem.x, pressedItem.y)) {
                        datePressedNow = null
                        ifFingerTouches = false
                        invalidate()
                        return false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                ifFingerTouches = false

                datePressedNow?.let { pressedItem ->
                    if (isInArea(event.x, event.y, pressedItem.x, pressedItem.y)) {
                        stillCanSelectThisDate = true
                        if (radiusRippleEffect + 1 >= initSquareSelectionRadius) {
                            radiusRippleEffect = 0f
                            addOrRemoveDateWithAnimation()
                        }
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                ifFingerTouches = false
                radiusRippleEffect = 0f
                datePressedNow = null
                stillCanSelectThisDate = false
                invalidate()
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

        numberOfDaysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

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

