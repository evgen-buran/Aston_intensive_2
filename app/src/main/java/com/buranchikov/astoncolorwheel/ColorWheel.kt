package com.buranchikov.astoncolorwheel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.min

class ColorWheel(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var imageView: ColorImageView? = null
    private var btnReset: Button? = null
    private var savedAngle = 0f
    private var isAnimating = false

    private val colors = listOf(
        R.color.red,
        R.color.orange,
        R.color.yellow,
        R.color.green,
        R.color.cyan,
        R.color.blue,
        R.color.violet,
    )
    private val texts = listOf(
        context.getString(R.string.redColor),
        context.getString(R.string.yellowColor),
        context.getString(R.string.cyanColor),
        context.getString(R.string.violetColor)
    )
    private val images = listOf(
        "https://loremflickr.com/640/360",
        "https://placekitten.com/640/360",
        "https://placebear.com/640/360",
        "https://placebeard.it/640x360",
        "https://placebeard.it/640/480",
        "https://loremflickr.com/640/360",
        "https://baconmockup.com/300/200/",
        "https://placebeard.it/640x360"
    )

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 50f
        textAlign = Paint.Align.CENTER
    }
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var testColorX = 0f
    private var testColorY = 0f
    private var currentAngle = 0f
    private var spinning = false
    private var stopAngle = 0f
    private var currentItem: ItemWheel? = null
    private val random = Random()
    private val minRotate = 120
    private val maxRotate = 540

    init {
        setOnClickListener {
            if (!spinning) {
              startAnimation()
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()?.let { state ->
            Bundle().apply {
                putParcelable("superState", state)
                putFloat("savedAngle", currentAngle)
                putBoolean("isAnimating", spinning)
            }
        }
    }
    override fun onRestoreInstanceState(state: Parcelable?) {
        var newState = state
        if (state is Bundle) {
            savedAngle = state.getFloat("savedAngle", 0f)
            isAnimating = state.getBoolean("isAnimating", false)
            newState = state.getParcelable("superState")
        }
        super.onRestoreInstanceState(newState)
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isAnimating) {
            startAnimation()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        btnReset = rootView.findViewById(R.id.btnReset)
        (btnReset as Button).setOnClickListener {
            clearText()
            imageView?.let { clearImageView(it) }
        }
        imageView = rootView.findViewById(R.id.imageView)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (min(width, height) / 2) * 0.8f
        val angle = 360f / colors.size
        testColorX = centerX + radius - 5
        testColorY = centerY

        val pointerPath = Path().apply {
            moveTo((centerX + radius + 10), centerY)
            lineTo((centerX + radius + 10) + 50, centerY + 30)
            lineTo((centerX + radius + 10) + 50, (centerY + 30) - 60)
            close()
        }

        for (i in colors.indices) {
            arcPaint.color = ContextCompat.getColor(context, colors[i])
            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                (currentAngle - angle / 2) + i * angle,
                angle,
                true,
                arcPaint
            )
        }
        canvas.drawPath(pointerPath, pointerPaint)
        if (!spinning && currentItem is ItemWheel.StringItem) {
            (imageView as ColorImageView).visibility = INVISIBLE
            val text = (currentItem as ItemWheel.StringItem).value
            canvas.drawRect(
                centerX - radius * 0.7f,
                centerY - radius * 0.1f,
                centerX + radius * 0.7f,
                centerY + radius * 0.1f,
                rectPaint
            )
            canvas.drawText(
                text,
                centerX,
                centerY - (textPaint.descent() + textPaint.ascent()) / 2,
                textPaint
            )
        } else if (!spinning && currentItem is ItemWheel.ImgItem) {

            val imageUrl = (currentItem as ItemWheel.ImgItem).url
            imageView?.let {
                (imageView as ColorImageView).visibility = VISIBLE
                Glide.with(context)
                    .load(imageUrl)
                    .into(it)
            }

        }
    }


    private fun showResult() {
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)

        val pixelColor = bitmap.getPixel(testColorX.toInt(), testColorY.toInt())
        val hexColor = Integer.toHexString(pixelColor)
        currentItem = when (hexColor) {
            "ffff0000" -> ItemWheel.StringItem(texts[0])
            "ffffff00" -> ItemWheel.StringItem(texts[1])
            "ff00ffff" -> ItemWheel.StringItem(texts[2])
            "ffff00ff" -> ItemWheel.StringItem(texts[3])
            else -> ItemWheel.ImgItem(images[Random().nextInt(8)])
        }
        invalidate()
    }

    private fun clearText() {
        currentItem = null
        invalidate()
    }

    private fun clearImageView(imageView: ColorImageView?) {
        imageView?.setImageDrawable(null)
    }

    private fun startAnimation() {
        spinning = true
        stopAngle = currentAngle + minRotate + random.nextInt(maxRotate).toFloat()
        CoroutineScope(Dispatchers.Main).launch {
            while (currentAngle < stopAngle) {
                currentAngle += 5f
                invalidate()
                delay(15)
            }
            spinning = false
            showResult()
        }
    }

}

