package com.yunlei.bubble.witget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread

class BubbleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mBubbleMaxRadius = 30 // 气泡最大半径 px
    private val mBubbleMinRadius = 5 // 气泡最小半径 px
    private val mBubbleMaxSize = 30 // 气泡数量
    private val mBubbleRefreshTime = 20L // 刷新间隔
    private val mBubbleMaxSpeedY = 5 // 气泡速度
    private val mBubbleAlpha = 128 // 气泡画笔

    private var mBubblePaint: Paint = Paint() // 气泡画笔
    private var mRandom = Random()
    @Volatile
    private var mBubbles = mutableListOf<Bubble>()
    private var mBubbleThread: Thread? = null

    init {
        mBubblePaint.color = Color.WHITE
        mBubblePaint.alpha = mBubbleAlpha
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBubble(canvas)
    }

    private fun drawBubble(canvas: Canvas?) {
        mBubbles.forEach {
            canvas?.drawCircle(it.x, it.y, it.newRadius, mBubblePaint)
        }
    }

    private fun bubbleStart() {
        mBubbleThread = thread {
            try {
                while (true) {
                    sleep(mBubbleRefreshTime)
                    createBubble()
                    refreshBubble()
                    postInvalidate()
                }
            } catch (e: InterruptedException) {

            }
        }

    }

    private fun refreshBubble() {
        var removeBubble:Bubble?=null
        mBubbles.forEachIndexed { index, bubble ->
            if (bubble.y - bubble.speedY <= top + bubble.radius) {
//                mBubbles.remove(bubble)
                removeBubble = bubble
            } else {
                bubble.newRadius = bubble.radius+((height-bubble.y)/height)*bubble.radius
                when {
                    bubble.x + bubble.speedX <= left + bubble.newRadius -> {
                        bubble.x = left + bubble.newRadius
                    }
                    bubble.x + bubble.speedX >= right - bubble.newRadius -> {
                        bubble.x = right - bubble.newRadius
                    }
                    else -> {
                        bubble.x = bubble.x + bubble.speedX
                    }
                }
                bubble.y = bubble.y - bubble.speedY
                mBubbles[index] = bubble
            }
        }
        mBubbles.remove(removeBubble)
    }

    private fun createBubble() {
        if (mBubbles.size > mBubbleMaxSize) return
        if (mRandom.nextFloat() < 0.95) return
        val bubble = Bubble()
        bubble.apply {
            radius =
                (mBubbleMinRadius + mRandom.nextInt(mBubbleMaxRadius - mBubbleMinRadius)).toFloat()
            newRadius = radius
            speedY = mRandom.nextFloat() * mBubbleMaxSpeedY + 1
            speedX = mRandom.nextFloat() - 0.5f
            x = (width / 2).toFloat()
            y = height - radius
        }
        mBubbles.add(bubble)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        bubbleStart()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bubbleStop()
    }

    private fun bubbleStop() {
        mBubbleThread?.interrupt()
        mBubbleThread=null
    }

    private inner class Bubble {
        var radius = 0f // 气泡半径 = 0
        var newRadius = 0f // 气泡半径 = 0
        var speedY = 0f // 上升速度 = 0f
        var speedX = 0f // 平移速度 = 0f
        var x = 0f // 气泡x坐标 = 0f
        var y = 0f // 气泡y坐标 = 0f
    }
}