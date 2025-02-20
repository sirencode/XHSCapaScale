package com.yan.xhscapascale

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class CapaScaleViewTop(context: Context, attrs: AttributeSet? = null, style: Int = 0) :
    View(context, attrs, style) {
    var onViewRemoved: (() -> Unit)? = null
    var onTouchEnd: (() -> Unit)? = null

    var eventListener: ((dx: Float, dy: Float, scale: Double) -> Unit)? = null

    /**
     * 0.不处理事件
     * 1.放大位移模式
     * 2.位移模式
     */
    private var eventModel = 0

    var xx: Int = 0
    var yy: Int = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            if (event.pointerCount < 2) {
                xx = event.rawX.toInt()
                yy = event.rawY.toInt()
            }
            return true
        }

        // 两个手指触摸 才做处理事件
        if (eventModel == 0) {
            eventModel = 1
            parent?.requestDisallowInterceptTouchEvent(true)
        }

        if (eventModel == 0) {
            return super.onTouchEvent(event)
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_UP -> {
                eventModel = 2
                point1 = null
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (eventModel == 2) {
                    point1 = null
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount < 2) {
                    val nowX = event.rawX.toInt()
                    val nowY = event.rawY.toInt()
                    val movedX = nowX - xx
                    val movedY = nowY - yy
                    xx = nowX
                    yy = nowY
                    x += movedX
                    y += movedY
                } else {
                    moveDell(event)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                eventModel = 0
                point1 = null
                point2 = null
                onTouchEnd?.invoke()
            }
        }
        return super.onTouchEvent(event)
    }

    private var point1: PointF? = null
    private var point2: PointF? = null

    private fun eventListener(dx: Float, dy: Float, scale: Double,view: View) {
        x = x + dx
        y = y + dy

        scaleX = scale.toFloat()
        scaleY = scale.toFloat()
    }

    private fun moveDell(event: MotionEvent) {
        if (eventModel == 1) {
            if (point1 == null) {
                point1 = PointF(event.getX(0), event.getY(0))
            }
            if (point2 == null) {
                point2 = PointF(event.getX(1), event.getY(1))
            }
            val curX1 = event.getX(0)
            val curY1 = event.getY(0)

            val curX2 = event.getX(1)
            val curY2 = event.getY(1)

            val originalDistance = Math.sqrt(
                ((point2!!.x - point1!!.x) * (point2!!.x - point1!!.x)).toDouble()
                        + ((point2!!.y - point1!!.y) * (point2!!.y - point1!!.y)).toDouble()
            )
            val curDistance = Math.sqrt(
                ((curX2 - curX1) * (curX2 - curX1)).toDouble()
                        + ((curY2 - curY1) * (curY2 - curY1)).toDouble()
            )
            if (sqrt(curDistance - originalDistance) < 10) {
                return
            }
            val scale = curDistance / originalDistance

            val dx = curX1 - point1!!.x
            val dy = curY1 - point1!!.y

            Log.e(javaClass.name, "dx: $dx    dy: $dy   scale: $scale")

            eventListener?.invoke(dx, dy, scale)

            point1!!.set(event.getX(0), event.getY(0))
            point2!!.set(event.getX(1), event.getY(1))
        } else if (eventModel == 2) {
            if (point1 == null) {
                point1 = PointF(event.getX(0), event.getY(0))
            }

            val dx = event.getX(0) - point1!!.x
            val dy = event.getY(0) - point1!!.y

            Log.e(javaClass.name, "dx: $dx    dy: $dy   scale: 1")

            eventListener?.invoke(dx, dy, 1.0)
            point1!!.set(event.getX(0), event.getY(0))
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onViewRemoved?.invoke()
    }
}
