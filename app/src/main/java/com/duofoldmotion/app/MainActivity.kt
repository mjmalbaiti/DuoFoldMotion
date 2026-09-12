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
    private lateinit var stage: LinearLayout

    // V6: LEFT + CENTER live inside the same parent.
    private lateinit var leftCenterGroup: LinearLayout

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

        stage = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            clipChildren = false
            clipToPadding = false
        }

        leftCenterGroup = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            clipChildren = false
            clipToPadding = false
        }

        leftPanel = createPanel(
            "01",
            "CONTINUITY",
            "Apps remain visually connected"
        )

        centerPanel = createPanel(
            "02",
            "DUO FOLD",
            "Active screen"
        )

        rightPanel = createPanel(
            "03",
            "MOTION",
            "Adaptive folding interface"
        )

        // First mechanical section:
        // LEFT + CENTER are physically grouped.
        leftCenterGroup.addView(leftPanel, innerPanelParams())
        leftCenterGroup.addView(centerPanel, innerPanelParams())

        // Group occupies 2/3 of the open display.
        stage.addView(
            leftCenterGroup,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                2f
            )
        )

        // RIGHT occupies final 1/3.
        stage.addView(
            rightPanel,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }
        )

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
            animateTo(0.5f, "FIRST FOLD")
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
            applyFoldProgress(0f)
            reactToWidth(root.width)
        }

        root.addOnLayoutChangeListener {
                _,
                left,
                _,
                right,
                _,
                oldLeft,
                _,
                oldRight,
                _ ->

            val newWidth = right - left
            val oldWidth = oldRight - oldLeft

            if (newWidth > 0 && abs(newWidth - oldWidth) > 20) {
                root.post {
                    configurePivots()

                    if (autoTracking) {
                        reactToWidth(newWidth)
                    } else {
                        applyFoldProgress(progress)
                    }
                }
            }
        }
    }

    private fun innerPanelParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        ).apply {
            setMargins(4, 0, 4, 0)
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

            elevation = 12f
            clipChildren = false
            clipToPadding = false

            cameraDistance =
                resources.displayMetrics.density * 16000f
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

    private fun button(
        textValue: String,
        action: () -> Unit
    ): Button {

        return Button(this).apply {
            text = textValue
            minWidth = 0
            minimumWidth = 0
            setOnClickListener { action() }
        }
    }

    private fun configurePivots() {

        // Hinge 1:
        // RIGHT EDGE of LEFT panel.
        leftPanel.pivotX = leftPanel.width.toFloat()
        leftPanel.pivotY = leftPanel.height / 2f

        // CENTER itself remains flat inside the first group.
        centerPanel.pivotX = centerPanel.width / 2f
        centerPanel.pivotY = centerPanel.height / 2f

        // Hinge 2:
        // RIGHT EDGE of the complete LEFT+CENTER group.
        leftCenterGroup.pivotX =
            leftCenterGroup.width.toFloat()
        leftCenterGroup.pivotY =
            leftCenterGroup.height / 2f

        // RIGHT panel is the final stationary face.
        rightPanel.pivotX = 0f
        rightPanel.pivotY = rightPanel.height / 2f

        val distance =
            resources.displayMetrics.density * 16000f

        leftPanel.cameraDistance = distance
        centerPanel.cameraDistance = distance
        rightPanel.cameraDistance = distance
        leftCenterGroup.cameraDistance = distance
    }

    private fun animateTo(
        target: Float,
        label: String
    ) {

        animator?.cancel()

        val start = progress

        animator = ValueAnimator.ofFloat(start, target).apply {

            // Slightly longer so the mechanical sequence is visible.
            duration = 1050

            interpolator =
                AccelerateDecelerateInterpolator()

            addUpdateListener {
                applyFoldProgress(
                    it.animatedValue as Float
                )
            }

            start()
        }

        stateLabel.text = label
    }

    private fun applyFoldProgress(value: Float) {

        progress = value.coerceIn(0f, 1f)

        configurePivots()

        /*
         * V6 HIERARCHICAL TRIFOLD
         *
         * 0.0 -> 0.5
         * LEFT folds over CENTER.
         *
         * 0.5 -> 1.0
         * the complete LEFT+CENTER parent folds over RIGHT.
         */

        val firstFold =
            (progress / 0.5f).coerceIn(0f, 1f)

        val secondFold =
            ((progress - 0.5f) / 0.5f)
                .coerceIn(0f, 1f)

        // Nearly 180 degrees = real panel-over-panel fold.
        val firstAngle = 178f * firstFold
        val secondAngle = 178f * secondFold

        /*
         * Small Z arcs prevent the two surfaces from
         * visually intersecting while crossing 90 degrees.
         */
        val firstArc =
            1f - abs((2f * firstFold) - 1f)

        val secondArc =
            1f - abs((2f * secondFold) - 1f)

        // ---------- FOLD 1 ----------
        //
        // LEFT rotates around its RIGHT edge.
        // No translationX is required:
        // its pivot is the real hinge position.
        leftPanel.rotationY = firstAngle
        leftPanel.translationX = 0f
        leftPanel.translationZ = 26f * firstArc

        centerPanel.rotationY = 0f
        centerPanel.translationX = 0f
        centerPanel.translationZ = 0f

        // ---------- FOLD 2 ----------
        //
        // This is the crucial V6 difference:
        // LEFT and CENTER are already children of this parent,
        // so rotating the parent carries BOTH together.
        leftCenterGroup.rotationY = secondAngle
        leftCenterGroup.translationX = 0f
        leftCenterGroup.translationZ =
            34f * secondArc

        // RIGHT is the final stationary screen.
        rightPanel.rotationY = 0f
        rightPanel.translationX = 0f
        rightPanel.translationZ = 4f

        /*
         * Keep physical sizes constant.
         * Perspective must come from the hinges,
         * not artificial scale shrinking.
         */
        leftPanel.scaleX = 1f
        leftPanel.scaleY = 1f

        centerPanel.scaleX = 1f
        centerPanel.scaleY = 1f

        rightPanel.scaleX = 1f
        rightPanel.scaleY = 1f

        leftCenterGroup.scaleX = 1f
        leftCenterGroup.scaleY = 1f

        leftPanel.alpha = 1f
        centerPanel.alpha = 1f
        rightPanel.alpha = 1f
        leftCenterGroup.alpha = 1f

        /*
         * Once Fold 1 is complete, LEFT is physically
         * resting over CENTER.
         *
         * At the end of Fold 2, RIGHT stays slightly in
         * front, giving a clean single-panel CLOSED result.
         */
        if (progress >= 0.995f) {
            leftCenterGroup.translationZ = 0f
            rightPanel.translationZ = 6f
        }
    }

    private fun reactToWidth(widthPx: Int) {

        if (widthPx <= 0) return

        if (
            lastWidth != 0 &&
            abs(widthPx - lastWidth) < 20
        ) return

        lastWidth = widthPx

        val widthDp =
            widthPx / resources.displayMetrics.density

        when {
            widthDp >= 900f ->
                animateTo(
                    0f,
                    "AUTO • OPEN"
                )

            widthDp >= 600f ->
                animateTo(
                    0.5f,
                    "AUTO • FIRST FOLD"
                )

            else ->
                animateTo(
                    1f,
                    "AUTO • CLOSED"
                )
        }
    }

    private fun autoDemo() {

        autoTracking = false

        animateTo(
            0f,
            "DEMO • OPEN"
        )

        root.postDelayed({
            animateTo(
                0.5f,
                "DEMO • LEFT → CENTER"
            )
        }, 1200)

        root.postDelayed({
            animateTo(
                1f,
                "DEMO • CENTER → RIGHT"
            )
        }, 2600)

        root.postDelayed({
            animateTo(
                0.5f,
                "DEMO • UNFOLD 2"
            )
        }, 4200)

        root.postDelayed({
            animateTo(
                0f,
                "AUTO • OPEN"
            )

            autoTracking = true

        }, 5600)
    }

    override fun onConfigurationChanged(
        newConfig: Configuration
    ) {

        super.onConfigurationChanged(newConfig)

        root.postDelayed({

            configurePivots()

            if (autoTracking) {
                reactToWidth(root.width)
            } else {
                applyFoldProgress(progress)
            }

        }, 120)
    }
}