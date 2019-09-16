package com.igalata.bubblepicker.physics

import android.util.Log
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import kotlin.math.abs
import kotlin.math.pow

/**
 * Created by irinagalata on 1/26/17.
 */
class CircleBody(val world: World, var position: Vec2, var radius: Float, var increasedRadius: Float, var density: Float) {

    val decreasedRadius: Float = radius

    val normalRadius = radius

    var isIncreasing = false

    var isDecreasing = false

    val finished: Boolean
        get() = !isIncreasing && !isDecreasing

    val isBusy: Boolean
        get() = isIncreasing || isDecreasing

    lateinit var physicalBody: Body

    var isVisible = true

    var weight: Float = MIN_WEIGHT

    private val margin = 0.01f
    private val damping = 25f
    private val shape: CircleShape
        get() = CircleShape().apply {
            m_radius = radius + margin
            m_p.setZero()
        }

    private val fixture: FixtureDef
        get() = FixtureDef().apply {
            this.shape = this@CircleBody.shape
            this.density = this@CircleBody.density
        }

    private val bodyDef: BodyDef
        get() = BodyDef().apply {
            type = BodyType.DYNAMIC
            this.position = this@CircleBody.position
        }

    init {
        while (true) {
            if (world.isLocked.not()) {
                initializeBody()
                break
            }
        }
    }

    private fun initializeBody() {
        physicalBody = world.createBody(bodyDef).apply {
            createFixture(fixture)
            linearDamping = damping
        }
    }

    fun decrease(step: Float) {
        isDecreasing = true
        radius -= step
        reset()

        val decrement = 1 + ((weight - 2) / MAX_WEIGHT)

        Log.d("bubblepicker", "decrement: $decrement")

        if (abs(radius - (normalRadius * decrement)) < step) {
            weight -=1
            clear()
        }
    }

    fun increase(step: Float) {
        isIncreasing = true
        radius += step
        reset()

        val increment = 1 + (weight / MAX_WEIGHT)

        Log.d("bubblepicker", "increment: $increment")

        if (abs(radius - (normalRadius * increment)) < step) {
            weight += 1
            clear()
        }
    }

    private fun reset() {
        physicalBody.fixtureList?.shape?.m_radius = radius + margin
    }

    private fun clear() {
        isIncreasing = false
        isDecreasing = false
    }

    companion object {
        const val MIN_WEIGHT = 1f
        const val MAX_WEIGHT = 5f
    }
}