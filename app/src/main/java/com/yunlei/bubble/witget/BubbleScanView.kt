package com.yunlei.bubble.witget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class BubbleScanView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mBubbleMaxRadius = 160 // 气泡最大半径 px
    private val mBubbleMinRadius = 60 // 气泡最小半径 px
    private val mBubbleMaxSize = 5 // 气泡数量
    private val mBubbleRefreshTime = 10L // 刷新间隔
    private val mBubbleAlpha = 128 // 气泡画笔

    private var mBubblePaint: Paint = Paint() // 气泡画笔
    private var mRandom = Random()
    @Volatile
    private var mBubbles = mutableListOf<Bubble>()
    private var mBubbleThread: Thread? = null
    private var colorArray = intArrayOf(Color.parseColor("#40E0D0"), Color.parseColor("#ffffff"))

    init {
        mBubblePaint.color = Color.parseColor("#40E0D0")
        mBubblePaint.alpha = mBubbleAlpha
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBubble(canvas)
    }

    private fun drawBubble(canvas: Canvas?) {
        val list = ArrayList(mBubbles)
        list.forEach {
            //            var gradient = RadialGradient(it.x,it.y,it.radius,colorArray, null, Shader.TileMode.REPEAT)
//            mBubblePaint.shader = gradient
            canvas?.drawCircle(it.x, it.y, it.radius, mBubblePaint)
        }
    }

    var j = 0
    private fun bubbleStart() {

        postDelayed({
            if (j % 5 == 0) {
                j = 0
                createBubble()
            }
            refreshBubble()
            postInvalidate()
            j++
            bubbleStart()
        }, mBubbleRefreshTime)

//        mBubbleThread = thread {
//            try {
//                var i = 0
//                while (true) {
//
//                    Thread.sleep(mBubbleRefreshTime)
//                    if (i % 5 == 0) {
//                        i = 0
//                        createBubble()
//                    }
//                    refreshBubble()
//                    postInvalidate()
//                    i++
//                }
//            } catch (e: InterruptedException) {
//
//            }
//        }

    }

    private fun refreshBubble() {
        val delList: MutableList<Bubble> = mutableListOf()
        mBubbles.forEachIndexed { index, bubble ->
            if (abs(bubble.y - getCenterY()) <= bubble.radius
                && abs(bubble.x - getCenterX()) <= bubble.radius
            ) {
                delList.add(bubble)
            } else {
                bubble.boxRadius = (bubble.boxRadius - width / 2f * 0.02).toFloat()
                bubble.radius =
                    (mBubbleMaxRadius - mBubbleMinRadius) * bubble.boxRadius / width * 2f + mBubbleMinRadius
                bubble.y = getBubbleY(bubble.boxRadius, bubble.angle).toFloat()
                bubble.x = getBubbleX(bubble.boxRadius, bubble.angle).toFloat()
                mBubbles[index] = bubble
            }
        }
        delList.forEach {
            mBubbles.remove(it)
        }
    }

    private fun createBubble() {
        if (mBubbles.size > mBubbleMaxSize) return
        val bubble = Bubble()
        bubble.apply {
            radius = mBubbleMaxRadius.toFloat()
            boxRadius = width / 2f
            var angle = mRandom.nextInt(360)
            this.angle = angle
            x = getBubbleX(width / 2f, angle).toFloat()
            y = getBubbleY(width / 2f, angle).toFloat()
        }
        mBubbles.add(bubble)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bubbleStart()

//        postDelayed({
//            bubbleStop()
//        },10*1000)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bubbleStop()
    }

    private fun bubbleStop() {
        mBubbleThread?.interrupt()
        mBubbleThread = null
    }

    private inner class Bubble {
        var radius = 0f // 气泡半径 = 0
        var boxRadius = 0f // 气泡半径 = 0
        var x = 0f // 气泡x坐标 = 0f
        var y = 0f // 气泡y坐标 = 0f
        var angle = 0
    }

    private fun getBubbleX(r: Float, a: Int): Double {
        return width / 2f + r * cos(a * Math.PI / 180)
    }

    private fun getBubbleY(r: Float, a: Int): Double {
        return height / 2f + r * sin(a * Math.PI / 180)
    }

    private fun getCenterX(): Float {
        return width / 2f
    }

    private fun getCenterY(): Float {
        return height / 2f
    }
}