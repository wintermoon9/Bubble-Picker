package com.igalata.bubblepicker.rendering

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.ColorInt
import com.igalata.bubblepicker.BubblePickerListener
import com.igalata.bubblepicker.R
import com.igalata.bubblepicker.adapter.BubblePickerAdapter
import com.igalata.bubblepicker.model.Color
import com.igalata.bubblepicker.model.PickerItem
import android.view.GestureDetector


/**
 * Created by irinagalata on 1/19/17.
 */
class BubblePicker : GLSurfaceView {

    @ColorInt
    var background: Int = 0
        set(value) {
            field = value
            renderer.backgroundColor = Color(value)
        }
    @Deprecated(level = DeprecationLevel.WARNING,
            message = "Use BubblePickerAdapter for the view setup instead")
    var items: ArrayList<PickerItem>? = null
        set(value) {
            field = value
            renderer.items = value ?: ArrayList()
        }
    var adapter: BubblePickerAdapter? = null
        set(value) {
            field = value
            if (value != null) {
                renderer.items = ArrayList((0..value.totalCount - 1)
                        .map { value.getItem(it) }.toList())
            }
        }
    var maxSelectedCount: Int? = null
        set(value) {
            renderer.maxSelectedCount = value
        }
    var listener: BubblePickerListener? = null
        set(value) {
            renderer.listener = value
        }
    var bubbleSize = 50
        set(value) {
            if (value in 1..100) {
                renderer.bubbleSize = value
            }
        }
    val selectedItems: List<PickerItem?>
        get() = renderer.selectedItems

    var centerImmediately = false
        set(value) {
            field = value
            renderer.centerImmediately = value
        }

    private val gesture: GestureDetector

    private val renderer = PickerRenderer(this)
    private var startX = 0f
    private var startY = 0f
    private var previousX = 0f
    private var previousY = 0f

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

        gesture = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {

            override fun onLongPress(e: MotionEvent?) {
                super.onLongPress(e)

                e?.let {
                    renderer.decreaseSize(it.x, it.y)
                }
            }

            override fun onSingleTapUp(event: MotionEvent?): Boolean {
                event?.let {
                    if (isClick(it)) renderer.increaseSize(it.x, it.y)
                    renderer.release()
                }

                return super.onSingleTapUp(event)
            }

            override fun onDown(event: MotionEvent?): Boolean {
                event?.let {
                    startX = it.x
                    startY = it.y
                    previousX = it.x
                    previousY = it.y
                }

                return super.onDown(event)
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                e?.let {
                    renderer.createBubble(it.x, it.y)
                }

                return true
            }
        })

        setOnTouchListener { view, motionEvent ->
            gesture.onTouchEvent(motionEvent)
        }

        setZOrderOnTop(true)
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.RGBA_8888)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        attrs?.let { retrieveAttrubutes(attrs) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (isSwipe(event)) {
                    renderer.swipe((previousX - event.x) * 4, (previousY - event.y) * 4)
                    previousX = event.x
                    previousY = event.y
                } else {
                    release()
                }
            }
            else -> release()
        }

        return true
    }

    private fun release() = postDelayed({ renderer.release() }, 0)

    private fun isClick(event: MotionEvent) = Math.abs(event.x - startX) < 20 && Math.abs(event.y - startY) < 20

    private fun isSwipe(event: MotionEvent) = Math.abs(event.x - previousX) > 20 && Math.abs(event.y - previousY) > 20

    private fun retrieveAttrubutes(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BubblePicker)

        if (array.hasValue(R.styleable.BubblePicker_maxSelectedCount)) {
            maxSelectedCount = array.getInt(R.styleable.BubblePicker_maxSelectedCount, -1)
        }

        if (array.hasValue(R.styleable.BubblePicker_backgroundColor)) {
            background = array.getColor(R.styleable.BubblePicker_backgroundColor, -1)
        }

        array.recycle()
    }

}