package com.yan.xhscapascale

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import kotlin.math.sqrt

class CapaScaleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var onViewRemoved: (() -> Unit)? = null
    var onTouchEnd: (() -> Unit)? = null
    var onTouchStart: (() -> Unit)? = null

    var eventListener: ((dx: Float, dy: Float, scale: Double) -> Unit)? = null

    /**
     * 0.不处理事件
     * 1.放大位移模式
     * 2.位移模式
     */
    private var eventModel = 0
    private lateinit var bottomView: View

    var coverView:CapaScaleViewTop? = null
    var add:Boolean = false

    /**
     * 最终的缩放大小
     */
    private var finalScale = 1F

    /**
     * touch 结束的 恢复动画
     */
    private var animator: ValueAnimator? = null

    fun setBottomView(bottomView: View) {
            this.bottomView = bottomView
    }

    private fun eventListener(dx: Float, dy: Float, scale: Double,view: View) {
        coverView?.let{
            it.x = it.x + dx
            it.y = it.y + dy

            finalScale *= scale.toFloat()

            if(finalScale <= 1.0f) {
                hide(it,view)
            } else {
                it.scaleX = finalScale
                it.scaleY = it.scaleX
            }
        }
    }

    private fun hide(c:View,view:View) {
        if (c != null) {
            if (animator == null) {
                animator = ValueAnimator.ofFloat(1F, 0F)
            }
            val tempAM = animator!!
            tempAM.duration = 250

            val xy = IntArray(2)
            view.getLocationInWindow(xy)

            val viewX = c.x
            val viewY = c.y
            tempAM.interpolator = OvershootInterpolator(0.5F)
            tempAM.addUpdateListener { animation ->
                val value: Float = animation.animatedValue as Float
                val tempOffsetScale = (finalScale - 1) * value + 1
                c.scaleX = tempOffsetScale
                c.scaleY = c.scaleX

                c.x = (viewX - xy[0]) * value + xy[0]
                c.y = (viewY - xy[1]) * value + xy[1]
            }

            tempAM.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    view.visibility = VISIBLE
                    finalScale = 1F
                    ((context as Activity).window.decorView as ViewGroup).removeView( c)
                    add = false
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
            tempAM.start()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            return true
        }

        // 两个手指触摸 才做处理事件
        if (eventModel == 0 && event.pointerCount == 2) {
            eventModel = 1
            parent?.requestDisallowInterceptTouchEvent(true)
            if (!add) {
                animator?.end()
                coverView = CapaScaleViewTop(context)
                coverView?.setBackgroundResource(R.mipmap.ic_launcher)
                val llp = ViewGroup.MarginLayoutParams(bottomView.layoutParams)
                coverView?.layoutParams = llp
                val xy = IntArray(2)
                bottomView.getLocationInWindow(xy)
                coverView?.x = xy[0].toFloat()
                coverView?.y = xy[1].toFloat()
                coverView?.eventListener = { dx: Float, dy: Float, scale: Double ->
                    eventListener(dx, dy, scale, bottomView)
                }
                ((context as Activity).window.decorView as ViewGroup).addView(coverView!!)
                bottomView.visibility = GONE
                add = true
            }
            onTouchStart?.invoke()
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
            MotionEvent.ACTION_MOVE -> moveDell(event)

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

    private fun moveDell(event: MotionEvent) {
        if (eventModel == 1) {
            if (event.pointerCount < 2) {
                return
            }
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

            val originalDistance = sqrt(
                ((point2!!.x - point1!!.x) * (point2!!.x - point1!!.x)).toDouble()
                        + ((point2!!.y - point1!!.y) * (point2!!.y - point1!!.y)).toDouble()
            )
            val curDistance = sqrt(
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

//            eventListener?.invoke(dx, dy, scale)
            eventListener(dx, dy, scale,bottomView)

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
