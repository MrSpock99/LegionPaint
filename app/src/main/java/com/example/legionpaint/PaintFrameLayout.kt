package com.example.legionpaint

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import com.example.legionpaint.databinding.PaintFrameLayoutBinding
import java.util.Stack

class PaintFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = PaintFrameLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private val colors = listOf(Color.RED, Color.WHITE, Color.GREEN, Color.YELLOW, Color.BLUE)
    private var colorIndex = 0
    private val drawPaths = mutableListOf<List<DrawPoint>>()
    private val drawPath = mutableListOf<DrawPoint>()
    private var drawIndex = 0
    private val deletedDrawPaths = ArrayDeque<List<DrawPoint>>()

    init {
        setWillNotDraw(false)
        binding.vChangeColor.setBackgroundColor(colors[colorIndex])
        binding.vUndo.setOnClickListener {
            undo()
        }
        binding.vRedo.setOnClickListener {
            redo()
        }
        binding.vChangeColor.setOnClickListener {
            changeColor()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawPaths.forEach {
            it.forEach {
                canvas?.drawCircle(it.x, it.y, 10F, it.paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            drawPath.clear()
            drawIndex++
        } else {
            val point = DrawPoint(event?.x ?: 0.0F, event?.y ?: 0.0F)
            point.paint.color = colors[colorIndex]
            drawPath.add(point)
            if (drawPaths.lastIndex == drawIndex) {
                drawPaths[drawIndex] = drawPath.toList()
            } else {
                drawPaths.add(drawPath)
            }
        }
        invalidate()
        return true
    }

    private fun changeColor() {
        if (colorIndex < colors.size - 1) {
            colorIndex++
        } else {
            colorIndex = 0
        }
        binding.vChangeColor.setBackgroundColor(colors[colorIndex])
    }

    private fun undo() {
        if (drawIndex > 0) {
            deletedDrawPaths.add(drawPaths.removeAt(--drawIndex))
            invalidate()
        }
    }

    private fun redo() {
        if (deletedDrawPaths.isNotEmpty()) {
            drawPaths.add(deletedDrawPaths.removeLast())
            drawIndex++
            invalidate()
        }
    }
}