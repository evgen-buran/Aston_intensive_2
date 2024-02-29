package com.buranchikov.astoncolorwheel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.transition.Transition
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.min

class ColorWheel(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    val TAG = "myLog"
//    private val imageView by find<ImageView>(R.id.imageView)
//private val imageView: ImageView by lazy {
//    findViewById(R.id.imageView)
//}

    private var imageView: ImageView? = null

    private val colors = listOf(
        R.color.red,
        R.color.orange,
        R.color.yellow,
        R.color.green,
        R.color.cyan,
        R.color.blue,
        R.color.violet,
    )
    private val texts = listOf("красный", "желтый", "голубой", "фиолетовый")
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

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    var xColor = 0f
    var yColor = 0f

    private var currentAngle = 0f
    private var spinning = false
    private var stopAngle = 0f
    private var currentItem: ItemWheel? = null
    private val random = Random()

    init {

        setOnClickListener {
            imageView = rootView.findViewById(R.id.imageView)
            if (!spinning) {
                spinning = true
                stopAngle = currentAngle + random.nextInt(360).toFloat()
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
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (min(width, height) / 2) * 0.7f
        val angle = 360f / colors.size
        xColor = centerX + radius - 5
        yColor = centerY

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
        // Рисуем текст или загружаем и отображаем изображение над колесом
        if (!spinning && currentItem is ItemWheel.StringItem) {
            val text = (currentItem as ItemWheel.StringItem).value
            canvas.drawText(
                text,
                centerX,
                centerY - (textPaint.descent() + textPaint.ascent()) / 2,
                textPaint
            )
        } else if (!spinning && currentItem is ItemWheel.ImgItem) {

            val imageUrl = (currentItem as ItemWheel.ImgItem).url
            imageView?.let {
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

        val pixelColor = bitmap.getPixel(xColor.toInt(), yColor.toInt())
        val hexColor = Integer.toHexString(pixelColor)
        currentItem = when (hexColor) {
            "ffff0000" -> ItemWheel.StringItem(texts[0])
            "ffffff00" -> ItemWheel.StringItem(texts[1])
            "ff00ffff" -> ItemWheel.StringItem(texts[2])
            "ffff00ff" -> ItemWheel.StringItem(texts[3])
            else -> ItemWheel.ImgItem(images[Random().nextInt(9)])
        }

        invalidate()
    }

}

