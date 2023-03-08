package com.luxpmsoft.luxaipoc.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.widget.ImageView
import com.luxpmsoft.luxaipoc.R
import com.luxpmsoft.luxaipoc.model.defect_detect.ColorBall
import com.luxpmsoft.luxaipoc.model.defect_detect.IDrawListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

@SuppressLint("AppCompatCustomView")
class DrawImageView: ImageView {
    var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private var mPath: Path? = null
    private var mPaint: Paint? = null
    private var mBitmapPaint: Paint? = null
    private var circlePaint: Paint? = null
    private var circlePath: Path? = null
    private var mMeasuredRect: Rect? = null
    var sessionInfoFile: File? = null
    var fosSessionInfoData: FileOutputStream? = null
    val mCirclePointer = SparseArray<CircleArea>()
    val mCircles = HashSet<CircleArea>()
    var circleZoom = false
    var isMove = false
    var isDraw = false
    private var mBitmap1: Bitmap? = null
    var circleGlobal: CircleArea? = null
    private var multitouchStartDist = 0.0
    var iDrawListener: IDrawListener? = null

    /** Stores data about single circle  */
    class CircleArea internal constructor(
        var centerX: Float,
        var centerY: Float,
        var centerX1: Float? =0F,
        var centerY1: Float? =0F,
        var radius: Int? = 0,
        var rect: RectF? = null,
        var move: Boolean = false,
        var r: Region? = null,
        var path: Path,
        var colorBalls: ArrayList<ColorBall?>? = ArrayList(),
        var groupId: Int = 1,
        var balID: Int = 2
    ) {
        override fun toString(): String {
            return "Circle[$centerX, $centerY, $radius]"
        }
    }

    constructor(ct: Context) : super(ct) {
        init(ct)
    }

    constructor(ct: Context, attrs: AttributeSet?) : super(ct, attrs) {
        init(ct)
    }

    constructor(ct: Context, attrs: AttributeSet?, defStyle: Int) : super(ct, attrs, defStyle) {
        init(ct)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.e("OKOK", "SizeChange")
        mBitmap1 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap1!!)

    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // background bitmap to cover all area

        mBitmap?.let {
            canvas.drawBitmap(it, null, mMeasuredRect!!, mPaint)
        }
        canvas.drawPath(mPath!!, mPaint!!)
        canvas.drawPath(circlePath!!, circlePaint!!)
        sessionInfoFile?.let {
            if (it.exists()) {
                it.delete()
            }
            it.createNewFile()
            fosSessionInfoData = FileOutputStream(it, true)
        }

        for (circle in mCircles) {
            if (!isMove) {
                circle.rect?.let {
                    if (circleZoom) {
                        if (circle == circleGlobal) {
                            circle.rect = circleGlobal?.rect
                            circle.groupId = circleGlobal?.groupId!!
                            circle.balID = circleGlobal?.balID!!
                            circle.colorBalls = circleGlobal?.colorBalls
                            var left: Int
                            var top: Int
                            var right: Int
                            var bottom: Int
                            left = circle?.colorBalls?.get(0)?.point?.x!!
                            top =circle?.colorBalls?.get(0)?.point?.y!!
                            right = circle?.colorBalls?.get(0)?.point?.x!!
                            bottom = circle?.colorBalls?.get(0)?.point?.y!!
                            for (i in 1 until circle?.colorBalls?.size!!) {
                                left = if (left > circle?.colorBalls?.get(i)?.point!!.x)
                                    circle?.colorBalls?.get(i)?.point!!.x else left
                                top = if (top >  circle?.colorBalls?.get(i)?.point!!.y)
                                    circle?.colorBalls?.get(i)?.point!!.y else top
                                right = if (right <  circle?.colorBalls?.get(i)?.point!!.x)
                                    circle?.colorBalls?.get(i)?.point!!.x else right
                                bottom = if (bottom <  circleGlobal?.colorBalls?.get(i)?.point!!.y)
                                    circle?.colorBalls?.get(i)?.point!!.y else bottom
                            }
                            circle.rect?.left = left.toFloat()+10
                            circle.rect?.top = top.toFloat()+10
                            circle.rect?.right = right.toFloat()+10
                            circle.rect?.bottom = bottom.toFloat()+10
                            //
                            circle.centerX = left.toFloat()+10
                            circle.centerX1 = right.toFloat()+10
                            circle.centerY = top.toFloat()+10
                            circle.centerY1 = bottom.toFloat()+10

                            canvas.drawRect(circle.rect?.left!!.toFloat(),
                                circle.rect?.top!!.toFloat(),
                                circle.rect?.right!!.toFloat(),
                                circle.rect?.bottom!!.toFloat(), circlePaint!!)
                            circle?.colorBalls?.forEachIndexed { index, element ->
                                canvas.drawBitmap(
                                    element?.bitmap!!, element?.point?.x!!.toFloat()-10, element?.point?.y!!.toFloat()-10,
                                    mPaint
                                )
                            }
                        } else {
                            canvas.drawRect(it, circlePaint!!)
                            circle.colorBalls?.forEachIndexed { index, element ->
                                canvas.drawBitmap(
                                    element?.bitmap!!, element?.point?.x!!.toFloat()-10, element?.point?.y!!.toFloat()-10,
                                    mPaint
                                )
                            }
                        }
                    } else {
                        canvas.drawRect(it, circlePaint!!)
                    }
                }
            } else {
                if (circle == circleGlobal) {
                    var l: Float
                    var t: Float
                    var r: Float
                    var b: Float
                    if (circle.centerX < circle.centerX1!!) {
                        l = circle.centerX
                        r = circle.centerX1!!
                    } else {
                        l = circle.centerX1!!
                        r = circle.centerX!!
                    }

                    if (circle.centerY < circle.centerY1!!) {
                        t = circle.centerY
                        b = circle.centerY1!!
                    } else {
                        t = circle.centerY1!!
                        b = circle.centerY!!
                    }
                    circle.rect?.left = l
                    circle.rect?.top = t
                    circle.rect?.right = r
                    circle.rect?.bottom = b
                    canvas.drawRect(l, t, r, b, mPaint!!)
                    circle.colorBalls?.forEachIndexed { index, element ->
                        if (index == 0) {
                            element?.point?.x = l.toInt()-10
                            element?.point?.y = t.toInt()-10

                        }
                        if (index == 1) {
                            element?.point?.x = l.toInt()-10
                            element?.point?.y = b.toInt()-10
                        }
                        if (index == 2) {
                            element?.point?.x = r.toInt()-10
                            element?.point?.y = b.toInt()-10
                        }
                        if (index == 3) {
                            element?.point?.x = r.toInt()-10
                            element?.point?.y = t.toInt()-10
                        }
                    }
                } else {
                    canvas.drawRect(circle.rect!!, circlePaint!!)
                }
            }
            getCoordinates(circle.rect?.centerX()?.toInt()!!,
                circle.rect?.centerY()?.toInt()!!, circle.rect!!)
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
        }
    }

    private fun touch_up() {
        mPath?.lineTo(mX, mY)
        circlePath?.reset()
        // commit the path to our offscreen
//        mCanvas!!.drawPath(mPath, mPaint)
        // kill this so we don't double draw
        val rf = getAreaFromPath(mPath!!)
        if (rf!!.width() >= 30 && rf!!.height() >= 30) {
            var r = Region()
            r.setPath(
                mPath!!,
                Region(
                    rf?.left!!.toInt(),
                    rf.top.toInt(),
                    rf.right.toInt(),
                    rf.bottom.toInt(),
                )
            )

            var colorBalls: ArrayList<ColorBall?>? = ArrayList()
            val points = arrayOfNulls<Point>(4)
            points[0] = Point()
            points[0]?.x = rf.left.toInt()-10
            points[0]?.y = rf.top.toInt()-10

            points[1] = Point()
            points[1]?.x = rf.left.toInt()-10
            points[1]?.y = rf.bottom.toInt()-10

            points[2] = Point()
            points[2]?.x = rf.right.toInt()-10
            points[2]?.y = rf.bottom.toInt()-10

            points[3] = Point()
            points[3]?.x = rf.right.toInt()-10
            points[3]?.y = rf.top.toInt()-10
            // declare each ball with the ColorBall class
            for (i in points.indices) {
                colorBalls?.add(
                    ColorBall(
                        BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.gray_circle
                        ),
                        context,
                        points[i],
                        i
                    )
                )
            }

            circleGlobal?.radius = 1
            circleGlobal?.rect = rf
            circleGlobal?.r = r
            circleGlobal?.path = mPath!!
            circleGlobal?.colorBalls = colorBalls

            mCircles.add(circleGlobal!!)
            isDraw = true
            iDrawListener?.onDrawListener(isDraw)
            getCoordinates(rf.centerX().toInt(), rf.centerY().toInt(), rf)
        }
        circleGlobal = null
        mPath?.reset()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x: Float
        val y: Float
        val action: Int
        val pointerIndex: Int

        action = event.actionMasked
        pointerIndex = event.actionIndex
        x = event.getX(pointerIndex)
        y = event.getY(pointerIndex) - 50
        var actionIndex = event.actionIndex
        var pointerId: Int
        var circle: CircleArea? = null
        if (event.pointerCount == 2) {
//            when (action) {
//                MotionEvent.ACTION_POINTER_DOWN -> {
//                    if (circleZoom) {
//                        multitouchStartDist = getMultitouchDist(event)
//                    }
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    if (circleZoom) {
//                        circle = onTouch(x.toInt(), y.toInt())
//                        circle?.let {
//                            val dist: Double = getMultitouchDist(event)
//                            val factor: Double = dist / multitouchStartDist
//                            val centreX: Float
//                            val centreY: Float
//                            val diffX: Float
//                            val diffY: Float
//                            Log.e("OKOK81", factor.toString()+" ")
//                            centreX = circle?.centerX!! + (circle?.centerX1!! - circle?.centerX!!) / 2
//                            centreY = circle?.centerY!! + (circle?.centerY1!! - circle?.centerY!!) / 2
//                            diffX = centreX -  circle?.centerX!!
//                            diffY = centreY - circle?.centerY!!
//                            circle?.centerX = (centreX - diffX * factor).toFloat()
//                            circle?.centerX1 = (centreX + diffX * factor).toFloat()
//                            circle?.centerY = (centreY - diffY * factor).toFloat()
//                            circle?.centerY1 = (centreY + diffY * factor).toFloat()
//                        }
//                    }
//                }
//            }
        } else {
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    multitouchStartDist = 0.0
                    circle = onTouch(x.toInt(), y.toInt())
                    if (circle == null) {
                        if (!circleZoom && !isMove) {
                            circleGlobal = CircleArea(centerX = x, centerY = y, path = mPath!!)
                            touch_start(x, y)
                        }
                    } else {
                        circleGlobal = circle
                        if (circleZoom) {
                            //resize rectangle
                            circleGlobal?.balID = -1
                            circleGlobal?.groupId = -1
                            for (i in circleGlobal?.colorBalls?.indices?.reversed()!!) {
                                val ball: ColorBall = circleGlobal?.colorBalls?.get(i)!!
                                // check if inside the bounds of the ball (circle)
                                // get the center for the ball
                                val centerX: Int = ball.point?.x!! + ball.bitmap?.width!!
                                val centerY: Int = ball.point?.y!! + ball.bitmap?.height!!
                                // calculate the radius from the touch to the center of the
                                // ball
                                val radCircle = Math
                                    .sqrt(
                                        ((centerX - x) * (centerX - x) + (centerY - y)
                                                * (centerY - y)).toDouble()
                                    )
                                if (radCircle < ball?.bitmap?.width!!+30) {
                                    circleGlobal?.balID = ball.id
                                    if (circleGlobal?.balID == 1 || circleGlobal?.balID == 3) {
                                        circleGlobal?.groupId = 2
                                    } else {
                                        circleGlobal?.groupId = 1
                                    }
                                    invalidate()
                                    break
                                }
                            }
                        }
                    }
                    mCirclePointer.put(event.getPointerId(0), circle)
                }

                MotionEvent.ACTION_MOVE -> {
                    val pointerCount = event.pointerCount
                    actionIndex = 0
                    while (actionIndex < pointerCount) {
                        pointerId = event.getPointerId(actionIndex)
                        mCirclePointer?.let {
                            if (circle == null) {
                                circle = it[pointerId]
                            }
                        }

                        actionIndex++
                    }
                    if (circle == null) {
                        if (!circleZoom && !isMove) {
                            circleGlobal?.centerX1 = x
                            circleGlobal?.centerY1 = x
                            touch_move(x, y)
                        }
                    } else {
                        if (circleZoom) {
                            if (circleGlobal?.balID!! > -1) {
                                // move the balls the same as the finger
                                circleGlobal?.colorBalls?.get(circleGlobal?.balID!!)?.point!!.x = x.toInt()
                                circleGlobal?.colorBalls?.get(circleGlobal?.balID!!)?.point!!.y = y.toInt()
                                if (circleGlobal?.groupId == 1) {
                                    circleGlobal?.colorBalls?.get(1)?.point!!.x = circleGlobal?.colorBalls?.get(0)?.point!!.x
                                    circleGlobal?.colorBalls?.get(1)?.point!!.y = circleGlobal?.colorBalls?.get(2)?.point!!.y
                                    circleGlobal?.colorBalls?.get(3)?.point!!.x = circleGlobal?.colorBalls?.get(2)?.point!!.x
                                    circleGlobal?.colorBalls?.get(3)?.point!!.y = circleGlobal?.colorBalls?.get(0)?.point!!.y
                                } else {
                                    circleGlobal?.colorBalls?.get(0)?.point!!.x = circleGlobal?.colorBalls?.get(1)?.point!!.x
                                    circleGlobal?.colorBalls?.get(0)?.point!!.y = circleGlobal?.colorBalls?.get(3)?.point!!.y
                                    circleGlobal?.colorBalls?.get(2)?.point!!.x = circleGlobal?.colorBalls?.get(3)?.point!!.x
                                    circleGlobal?.colorBalls?.get(2)?.point!!.y = circleGlobal?.colorBalls?.get(1)?.point!!.y
                                }
                            }
                        }
                        if (isMove) {
                            val centreX: Float
                            val centreY: Float
                            val diffX: Float
                            val diffY: Float
                            centreX = circle?.centerX!! + (circle?.centerX1!! - circle?.centerX!!) / 2
                            centreY = circle?.centerY!! + (circle?.centerY1!! - circle?.centerY!!) / 2
                            diffX = x - centreX
                            diffY = y - centreY
                            circle?.centerX = circle?.centerX!! + diffX
                            circle?.centerX1 = circle?.centerX1!! + diffX
                            circle?.centerY = circle?.centerY!! + diffY
                            circle?.centerY1 = circle?.centerY1!! + diffY
                        }

                    }
                }
                MotionEvent.ACTION_UP -> {
                    val pointerCount = event.pointerCount
                    actionIndex = 0
                    while (actionIndex < pointerCount) {
                        pointerId = event.getPointerId(actionIndex)
                        mCirclePointer?.let {
                            if (circle == null) {
                                circle = it[pointerId]
                            }
                        }
                        actionIndex++
                    }
                    if (circle == null) {
                        if (!circleZoom && !isMove) {
                            circleGlobal?.centerX1 = x
                            circleGlobal?.centerY1 = y
                            touch_up()
                        }
                    }
                    circleGlobal = null
                }
            }
        }
        invalidate()
        return true
    }

    fun getMultitouchDist(event: MotionEvent): Double {
        val diffX = Math.abs(event.getX(0) - event.getX(1))
        val diffY = Math.abs(event.getY(0) - event.getY(1))
        return Math.sqrt(Math.pow(diffX.toDouble(), 2.0) + Math.pow(diffY.toDouble(), 2.0))
    }

    fun onTouch(xTouch: Int, yTouch: Int): CircleArea? {
        val point = Point()
        point.x = xTouch
        point.y = yTouch
        for (circle in mCircles) {
            circle.rect?.let {
                if ((it.centerX() - xTouch) * (it.centerX() - xTouch) + (it.centerY() - yTouch) * (it.centerY() - yTouch) <= it.width() * it.height()) {
                    return circle
                }
            }
        }

        return null
    }

    fun getCoordinates(x: Int, y: Int, rect: RectF) {
        mBitmap?.let {
            val xv = (x + (rect.width()/2)) / it.width
            val yv = (y + (rect.height()/2)) / it.height
            val width = rect.width() / it.width
            val height = rect.height() / it.height

            try {
                fosSessionInfoData?.write(
                    "0 ".plus(xv.toString().plus(" $yv $width $height;\n")).toByteArray(StandardCharsets.UTF_8)
                )

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getAreaFromPath(sourcePath: Path): RectF? {
        val rectF = RectF()
        sourcePath.computeBounds(rectF, true)
        return rectF
    }

    companion object {
        private const val TOUCH_TOLERANCE = 4f
    }

    private fun init(ct: Context) {
        // Generate bitmap used for background
//        mBitmap = BitmapFactory.decodeResource(ct.getResources(), R.drawable.defect)
        mPaint = Paint()
        mPaint?.isAntiAlias = true
        mPaint?.isDither = true
        mPaint?.color = Color.GREEN
        mPaint?.style = Paint.Style.STROKE
        mPaint?.strokeJoin = Paint.Join.ROUND
        mPaint?.strokeCap = Paint.Cap.ROUND
        mPaint?.strokeWidth = 8f
        mPath = Path()
        mBitmapPaint = Paint(Paint.DITHER_FLAG)
        circlePaint = Paint()
        circlePath = Path()
        circlePaint?.isAntiAlias = true
        circlePaint?.color = Color.GREEN
        circlePaint?.style = Paint.Style.STROKE
        circlePaint?.strokeJoin = Paint.Join.MITER
        circlePaint?.strokeWidth = 4f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mMeasuredRect = Rect(0, 0, getMeasuredWidth(), getMeasuredHeight())
    }

    fun setOnDrawListener(iDrawListener: IDrawListener?) {
        this.iDrawListener = iDrawListener
    }
}
