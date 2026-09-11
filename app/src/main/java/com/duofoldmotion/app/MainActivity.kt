package com.duofoldmotion.app

import android.animation.ValueAnimator
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var root: FrameLayout
    private lateinit var leftPanel: FrameLayout
    private lateinit var centerPanel: FrameLayout
    private lateinit var rightPanel: FrameLayout
    private lateinit var stateLabel: TextView

    private var progress = 0f
    private var lastWidth = 0
    private var autoTracking = true
    private var animator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            clipChildren = false
            clipToPadding = false
        }

        val stage = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            clipChildren = false
            clipToPadding = false
        }

        leftPanel = createPanel("01", "CONTINUITY", "Apps remain visually connected")
        centerPanel = createPanel("02", "DUO FOLD", "Active screen")
        rightPanel = createPanel("03", "MOTION", "Adaptive folding interface")

        stage.addView(leftPanel, panelParams())
        stage.addView(centerPanel, panelParams())
        stage.addView(rightPanel, panelParams())

        root.addView(
            stage,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(30, 38, 30, 120)
            }
        )

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(12, 8, 12, 18)
        }

        stateLabel = TextView(this).apply {
            text = "OPEN"
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(18, 0, 18, 0)
        }

        controls.addView(button("C") {
            autoTracking = false
            animateTo(1f, "CLOSED")
        })

        controls.addView(button("T") {
            autoTracking = false
            animateTo(0.52f, "TRANSITION")
        })

        controls.addView(button("O") {
            autoTracking = false
            animateTo(0f, "OPEN")
        })

        controls.addView(button("A") {
            autoDemo()
        })

        controls.addView(stateLabel)

        root.addView(
            controls,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            )
        )

        setContentView(root)

        root.post {
            configurePivots()
            reactToWidth(root.width)
        }

        root.addOnLayoutChangeListener { _, left, _, right, _, oldLeft, _, oldRight, _ ->
            val newWidth = right - left
            val oldWidth = oldRight - oldLeft

            if (newWidth > 0 && abs(newWidth - oldWidth) > 20) {
                configurePivots()

                if (autoTracking) {
                    reactToWidth(newWidth)
                }
            }
        }
    }

    private fun panelParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        ).apply {
            setMargins(6, 0, 6, 0)
        }
    }

    private fun createPanel(
        number: String,
        heading: String,
        description: String
    ): FrameLayout {

        val panel = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                setColor(Color.rgb(22, 22, 24))
                cornerRadius = 34f
            }

            elevation = 16f
            cameraDistance = resources.displayMetrics.density * 12000f
            clipChildren = false
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
        }

        val numberView = TextView(this).apply {
            text = number
            textSize = 14f
            setTextColor(Color.rgb(120, 120, 125))
            gravity = Gravity.CENTER
        }

        val title = TextView(this).apply {
            text = heading
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val body = TextView(this).apply {
            text = description
            textSize = 14f
            setTextColor(Color.rgb(175, 175, 180))
            gravity = Gravity.CENTER
        }

        content.addView(numberView)
        content.addView(title)
        content.addView(body)

        panel.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        return panel
    }

    private fun button(textValue: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = textValue
            minWidth = 0
            minimumWidth = 0
            setOnClickListener { action() }
        }
    }

    private fun configurePivots() {
        leftPanel.pivotX = leftPanel.width.toFloat()
        leftPanel.pivotY = leftPanel.height / 2f

        centerPanel.pivotX = centerPanel.width / 2f
        centerPanel.pivotY = centerPanel.height / 2f

        rightPanel.pivotX = 0f
        rightPanel.pivotY = rightPanel.height / 2f
    }

    private fun animateTo(target: Float, label: String) {
        animator?.cancel()

        val start = progress

        animator = ValueAnimator.ofFloat(start, target).apply {
            duration = 760
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener {
                applyFoldProgress(it.animatedValue as Float)
            }

            start()
        }

        stateLabel.text = label
    }

    private fun applyFoldProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        configurePivots()

        // Sequential fold: left -> right
        val phase1 = (progress / 0.5f).coerceIn(0f, 1f)
        val phase2 = ((progress - 0.5f) / 0.5f).coerceIn(0f, 1f)

        leftPanel.cameraDistance = 14000f
        centerPanel.cameraDistance = 14000f
        rightPanel.cameraDistance = 14000f

        // Stage 1: left panel folds onto center
        leftPanel.rotationY = 88f * phase1
        leftPanel.translationX = leftPanel.width * 0.50f * phase1
        leftPanel.translationZ = 8f * phase1
        leftPanel.alpha = 1f

        // Center remains mostly fixed during first fold
        centerPanel.rotationY = 0f
        centerPanel.translationX = 0f
        centerPanel.translationZ = 12f * phase1

        // Stage 2: folded left+center group moves/folds toward right
        val groupShift = centerPanel.width * 0.42f * phase2
        centerPanel.translationX = groupShift
        centerPanel.rotationY = 18f * phase2
        centerPanel.translationZ = 18f * phase2

        leftPanel.translationX =
            leftPanel.width * 0.50f * phase1 + groupShift
        leftPanel.rotationY =
            88f * phase1 + 18f * phase2

        // Right panel is final destination / closing face
        rightPanel.rotationY = -82f * phase2
        rightPanel.translationX = -rightPanel.width * 0.42f * phase2
        rightPanel.translationZ = 6f * phase2
        rightPanel.alpha = 1f

        leftPanel.scaleX = 1f
        leftPanel.scaleY = 1f
        centerPanel.scaleX = 1f
        centerPanel.scaleY = 1f
        rightPanel.scaleX = 1f
        rightPanel.scaleY = 1f

        val shade = (22 + 8 * progress).toInt()
        centerPanel.background = GradientDrawable().apply {
            setColor(Color.rgb(shade, shade, shade + 2))
            cornerRadius = 34f
        }
    }

    private fun reactToWidth(widthPx: Int) {
        if (widthPx <= 0) return

        if (lastWidth != 0 && abs(widthPx - lastWidth) < 20) return
        lastWidth = widthPx

        val widthDp = widthPx / resources.displayMetrics.density

        when {
            widthDp >= 900f -> animateTo(0f, "AUTO • OPEN")
            widthDp >= 600f -> animateTo(0.52f, "AUTO • HALF")
            else -> animateTo(1f, "AUTO • CLOSED")
        }
    }

    private fun autoDemo() {
        autoTracking = false

        animateTo(0f, "DEMO • OPEN")

        root.postDelayed({
            animateTo(0.52f, "DEMO • FOLDING")
        }, 900)

        root.postDelayed({
            animateTo(1f, "DEMO • CLOSED")
        }, 1800)

        root.postDelayed({
            animateTo(0.52f, "DEMO • OPENING")
        }, 2900)

        root.postDelayed({
            animateTo(0f, "AUTO • OPEN")
            autoTracking = true
        }, 3900)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        root.postDelayed({
            configurePivots()

            if (autoTracking) {
                reactToWidth(root.width)
            }
        }, 120)
    }
}
