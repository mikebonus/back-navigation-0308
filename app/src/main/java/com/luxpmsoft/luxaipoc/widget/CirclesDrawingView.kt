package com.luxpmsoft.luxaipoc.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import com.luxpmsoft.luxaipoc.R
import java.util.*


class CirclesDrawingView : View {
    /** Main bitmap  */
    private var mBitmap: Bitmap? = null
    private var mMeasuredRect: Rect? = null
    private var circlePaint: Paint? = null
    private var circlePath: Path? = null
    private var mPath: Path? = null
    private var mPaint: Paint? = null
    private var mCanvas: Canvas? = null

    /** Stores data about single circle  */
    private class CircleArea internal constructor(
        var centerX: Int,
        var centerY: Int,
        var radius: Int,
        var rect: RectF? = null,
        var move: Boolean = false,
        var r: Region? = null
    ) {
        override fun toString(): String {
            return "Circle[$centerX, $centerY, $radius]"
        }
    }

    /** Paint to draw circles  */
    private var mCirclePaint: Paint? = null
    private val mRadiusGenerator: Random = Random()

    /** All available circles  */
    private val mCircles = HashSet<CircleArea>(CIRCLES_LIMIT)
    private val mCirclePointer = SparseArray<CircleArea?>(CIRCLES_LIMIT)

    /**
     * Default constructor
     *
     * @param ct [android.content.Context]
     */
    constructor(ct: Context) : super(ct) {
        init(ct)
    }

    constructor(ct: Context, attrs: AttributeSet?) : super(ct, attrs) {
        init(ct)
    }

    constructor(ct: Context, attrs: AttributeSet?, defStyle: Int) : super(ct, attrs, defStyle) {
        init(ct)
    }

    private fun init(ct: Context) {
        // Generate bitmap used for background
        mBitmap = BitmapFactory.decodeResource(ct.getResources(), R.drawable.defect)
        mCirclePaint = Paint()
        mCirclePaint?.setColor(Color.BLUE)
        mCirclePaint?.setStrokeWidth(10f)
        mCirclePaint?.setStyle(Paint.Style.STROKE)

        mPaint = Paint()
        mPaint?.setAntiAlias(true)
        mPaint?.setDither(true)
        mPaint?.setColor(Color.GREEN)
        mPaint?.setStyle(Paint.Style.STROKE)
        mPaint?.setStrokeJoin(Paint.Join.ROUND)
        mPaint?.setStrokeCap(Paint.Cap.ROUND)
        mPaint?.setStrokeWidth(8f)
        mPath = Path()
        circlePaint = Paint()
        circlePath = Path()
        circlePaint?.isAntiAlias = true
        circlePaint?.color = Color.GREEN
        circlePaint?.style = Paint.Style.STROKE
        circlePaint?.strokeJoin = Paint.Join.MITER
        circlePaint?.strokeWidth = 4f
    }

    override fun onDraw(canv: Canvas) {
        // background bitmap to cover all area
        mBitmap?.let {
            canv.drawBitmap(it, null, mMeasuredRect!!, null)
        }

//        canv.drawPath(mPath!!, mPaint!!)

        for (circle in mCircles) {
            canv.drawCircle(circle.centerX.toFloat(), circle.centerY.toFloat(),
                circle.radius.toFloat(), mCirclePaint!!)
//            circle?.rect?.let { canv.drawRect(it, circlePaint!!) }
        }
    }

    private var mX = 0f
    private var mY = 0f

    private fun touch_start(x: Float, y: Float) {
        mPath?.reset()
        mPath?.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touch_move(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath?.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
            circlePath?.reset()
            circlePath?.addCircle(mX, mY, 30f, Path.Direction.CW)
            Log.e("OKOK", mX.toString()+" "+mY)
        }
    }

    private fun touch_up() {
        mPath?.lineTo(mX, mY)
        circlePath?.reset()
        // commit the path to our offscreen
//        mCanvas!!.drawPath(mPath, mPaint)
        // kill this so we don't double draw
//        getCoordinates(x.toInt(), y.toInt())
    }

    fun getAreaFromPath(sourcePath: Path): RectF? {
        val rectF = RectF()
        sourcePath.computeBounds(rectF, true)
        return rectF
    }

   override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = false
        var touchedCircle: CircleArea?
        var xTouch: Int
        var yTouch: Int
        var pointerId: Int
        var actionIndex = event.actionIndex
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // it's the first pointer, so clear all existing pointers data
                clearCirclePointer()
                xTouch = event.getX(0).toInt()
                yTouch = event.getY(0).toInt()

                // check if we've touched inside some circle
                touchedCircle = obtainTouchedCircle(xTouch, yTouch)
                onTouch(xTouch, yTouch, touchedCircle.r)
                touchedCircle.centerX = xTouch
                touchedCircle.centerY = yTouch
                mCirclePointer.put(event.getPointerId(0), touchedCircle)
                touch_start(event.getX(0), event.getY(0))
                invalidate()
                handled = true
            }

            MotionEvent.ACTION_POINTER_DOWN-> {
                Log.w(TAG, "Pointer down")
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerCount = event.pointerCount
                Log.w(TAG, "Move")
                actionIndex = 0
                while (actionIndex < pointerCount) {

                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex)
                    xTouch = event.getX(actionIndex).toInt()
                    yTouch = event.getY(actionIndex).toInt()
                    touchedCircle = mCirclePointer[pointerId]
                    if (null != touchedCircle) {
                        touchedCircle.centerX = xTouch
                        touchedCircle.centerY = yTouch
                        Log.e(TAG, "MoveIIII")
//                        touchedCircle.rect?.let {
//                            it.left = event.getX(actionIndex)
//                            it.top = event.getY(actionIndex)
//                        }
                    } else {
                    }
                    actionIndex++
                }
                touch_move(event.getX(0), event.getY(0))

                invalidate()
                handled = true
            }
            MotionEvent.ACTION_UP -> {
                val pointerCount = event.pointerCount
                actionIndex = 0
                while (actionIndex < pointerCount) {
                    pointerId = event.getPointerId(actionIndex)
                    touchedCircle = mCirclePointer[pointerId]
                    if (null != touchedCircle && !touchedCircle.move) {
                        touch_up()
                        touchedCircle.rect = getAreaFromPath(mPath!!)!!
                        touchedCircle.r = Region()
                        touchedCircle.r?.setPath(
                            mPath!!,
                            Region(
                                touchedCircle.rect?.left!!.toInt(),
                                touchedCircle.rect?.top!!.toInt(),
                                touchedCircle.rect?.right!!.toInt(),
                                touchedCircle.rect?.bottom!!.toInt(),
                            )
                        )

                    }
                    actionIndex++
                }
                clearCirclePointer()
                invalidate()
                handled = true
            }
            MotionEvent.ACTION_CANCEL -> handled = true
            else -> {}
        }
        return super.onTouchEvent(event) || handled
    }

    /**
     * Clears all CircleArea - pointer id relations
     */
    private fun clearCirclePointer() {
        Log.w(TAG, "clearCirclePointer")
        mCirclePointer.clear()
    }

    fun onTouch(xTouch: Int, yTouch: Int, region: Region?): Boolean {
        region?.let {
            val point = Point()
            point.x = xTouch
            point.y = yTouch
            invalidate()
            Log.d(TAG, "point: $point")
            if (region.contains(point.x, point.y)) Log.e(TAG, "Touch IN") else Log.e(TAG, "Touch OUT")
        }
        return true
    }

    /**
     * Search and creates new (if needed) circle based on touch area
     *
     * @param xTouch int x of touch
     * @param yTouch int y of touch
     *
     * @return obtained [CircleArea]
     */
    private fun obtainTouchedCircle(xTouch: Int, yTouch: Int): CircleArea {
        var touchedCircle = getTouchedCircle(xTouch, yTouch)
        if (null == touchedCircle) {
            touchedCircle =
                CircleArea(xTouch, yTouch, mRadiusGenerator.nextInt(RADIUS_LIMIT) + RADIUS_LIMIT, null)
            if (mCircles.size === CIRCLES_LIMIT) {
                Log.w(TAG, "Clear all circles, size is " + mCircles.size)
                // remove first circle
                mCircles.clear()
            }
            Log.w(TAG, "Added circle $touchedCircle")
            mCircles.add(touchedCircle)
        }
        return touchedCircle
    }

    /**
     * Determines touched circle
     *
     * @param xTouch int x touch coordinate
     * @param yTouch int y touch coordinate
     *
     * @return [CircleArea] touched circle or null if no circle has been touched
     */
    private fun getTouchedCircle(xTouch: Int, yTouch: Int): CircleArea? {
        var touched: CircleArea? = null
        for (circle in mCircles) {
            circle.move = false
            if ((circle.centerX - xTouch) * (circle.centerX - xTouch) + (circle.centerY - yTouch) * (circle.centerY - yTouch) <= circle.radius * circle.radius) {
                circle.move = true
                touched = circle
                break
            }
        }
        return touched
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mMeasuredRect = Rect(0, 0, getMeasuredWidth(), getMeasuredHeight())
    }
    
    companion object {
        private const val TAG = "CirclesDrawingView"

        // Radius limit in pixels
        private const val RADIUS_LIMIT = 100
        private const val CIRCLES_LIMIT = 100
        private const val TOUCH_TOLERANCE = 4f
    }
}