package com.example.legionpaint

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import com.example.legionpaint.databinding.PaintFrameLayoutBinding

class PaintFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = PaintFrameLayoutBinding.inflate(LayoutInflater.from(context), this, true)
    private val paint = Paint()
    private val colors = listOf(Color.RED, Color.WHITE, Color.GREEN, Color.YELLOW, Color.BLUE)

    private var colorIndex = 0
    private val drawPaths = mutableListOf<List<DrawPoint>>()
    private var drawPath = mutableListOf<DrawPoint>()
    private var drawIndex = 0
    private val deletedDrawPaths = ArrayDeque<List<DrawPoint>>()

    init {
        isSaveEnabled = true
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
                canvas?.drawCircle(it.x, it.y, 10F, paint.apply {
                    color = it.color
                })
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            drawPath.clear()
            drawIndex++
        } else {
            val point = DrawPoint(event?.x ?: 0.0F, event?.y ?: 0.0F, colors[colorIndex])
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

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putInt("colorIndex", colorIndex)
        bundle.putInt("drawIndex", colorIndex)
        bundle.putParcelableArrayList("drawPath", ArrayList(drawPath))
        drawPaths.forEachIndexed { index, item ->
            bundle.putParcelableArrayList("drawPath_${index}", ArrayList(item))
        }
        deletedDrawPaths.forEachIndexed { index, item ->
            bundle.putParcelableArrayList("deletedDrawPath_${index}", ArrayList(item))
        }
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            drawIndex = state.getInt("drawIndex")
            colorIndex = state.getInt("colorIndex")
            drawPath = state.getParcelableArrayList<DrawPoint>("drawPath")?.toMutableList() ?: mutableListOf()
            var i = 0
            while (state.getParcelableArrayList<DrawPoint>("drawPath_${i}") != null) {
                val list = state.getParcelableArrayList("drawPath_${i}") ?: emptyList<DrawPoint>()
                drawPaths.add(list.toMutableList())
                i++
            }
            i = 0
            while (state.getParcelableArrayList<DrawPoint>("deletedDrawPath_${i}") != null) {
                val list = state.getParcelableArrayList("deletedDrawPath_${i}") ?: emptyList<DrawPoint>()
                deletedDrawPaths.add(list.toMutableList())
                i++
            }
        }
        super.onRestoreInstanceState(state)
        invalidate()
    }

    override fun saveHierarchyState(container: SparseArray<Parcelable>?) {
        super.saveHierarchyState(container)
    }

    /*override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        // super.dispatchSaveInstanceState(container)
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>?) {
        // super.dispatchRestoreInstanceState(container)
        dispatchThawSelfOnly(container)
    }*/

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