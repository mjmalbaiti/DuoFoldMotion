package com.duofoldmotion.app

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var card: FrameLayout
    private lateinit var title: TextView
    private lateinit var subtitle: TextView

    private var currentScale = 1f
    private var currentRadius = 24f
    private var currentAlpha = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this).apply {
            setBackgroundColor(0xFF000000.toInt())
        }

        card = FrameLayout(this).apply {
            setBackgroundColor(0xFF161616.toInt())
            elevation = 20f
            clipToOutline = true
            outlineProvider = RoundedOutlineProvider(currentRadius)
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        title = TextView(this).apply {
            text = "Duo Fold Motion"
            textSize = 30f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }

        subtitle = TextView(this).apply {
            text = "TriFold continuity prototype"
            textSize = 16f
            setTextColor(0xFFB8B8B8.toInt())
            gravity = Gravity.CENTER
        }

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        fun makeButton(label: String, action: () -> Unit): Button {
            return Button(this).apply {
                text = label
                setOnClickListener { action() }
            }
        }

        controls.addView(makeButton("C") { animateTo(0.72f, 56f, 0.9f, "Closed") })
        controls.addView(makeButton("T") { animateTo(0.86f, 40f, 0.95f, "Transition") })
        controls.addView(makeButton("O") { animateTo(1.0f, 24f, 1.0f, "Open") })
        controls.addView(makeButton("A") { autoSequence() })

        content.addView(title)
        content.addView(subtitle)
        content.addView(controls)

        card.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val margin = 48
        root.addView(
            card,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(margin, margin * 2, margin, margin * 2)
            }
        )

        setContentView(root)
    }

    private fun animateTo(
        targetScale: Float,
        targetRadius: Float,
        targetAlpha: Float,
        state: String
    ) {
        val startScale = currentScale
        val startRadius = currentRadius
        val startAlpha = currentAlpha

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 650
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val p = animator.animatedValue as Float

                currentScale = startScale + (targetScale - startScale) * p
                currentRadius = startRadius + (targetRadius - startRadius) * p
                currentAlpha = startAlpha + (targetAlpha - startAlpha) * p

                card.scaleX = currentScale
                card.scaleY = currentScale
                card.alpha = currentAlpha
                card.outlineProvider = RoundedOutlineProvider(currentRadius)
                card.invalidateOutline()
            }
            start()
        }

        subtitle.text = "$state mode"
    }

    private fun autoSequence() {
        animateTo(0.72f, 56f, 0.9f, "Closed")
        card.postDelayed({
            animateTo(0.86f, 40f, 0.95f, "Transition")
        }, 800)
        card.postDelayed({
            animateTo(1.0f, 24f, 1.0f, "Open")
        }, 1600)
    }
}

class RoundedOutlineProvider(private val radius: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: android.graphics.Outline) {
        outline.setRoundRect(0, 0, view.width, view.height, radius)
    }
}
