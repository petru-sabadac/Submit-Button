package com.sabadac.submitbuttonanimation

import android.animation.*
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView

class SubmitButtonAnimation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    TextView(context, attrs, defStyleAttr), View.OnClickListener {

    private val minCornerRadius = 70
    private val minRingSize = 3
    private val maxRingSize = 6
    private val maxButtonWidth = 300
    private val minButtonWidth = 70
    private val colorMaxValue = 255

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var buttonRectF = RectF()

    private var doneBitmapAlpha = 0
    private val doneBitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_done)

    private val bitmap = Bitmap.createBitmap(1600, 1600, Bitmap.Config.ARGB_8888)
    private val bitmapCanvas = Canvas(bitmap)

    private var ringColor = ContextCompat.getColor(context, R.color.animColor)
    private var buttonTextColor = ContextCompat.getColor(context, R.color.animColor)

    private val animationDuration = 500L
    private var isRunning = false
    private var angle = 0f
    private var alpha = colorMaxValue

    private var buttonWidth = maxButtonWidth
    private var buttonHeight = minButtonWidth
    private var ringSize = minRingSize
    private var cornerRadius = minCornerRadius
    private val animatorSet = AnimatorSet()

    init {
        this.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (!isRunning) {
            isRunning = true


            animatorSet.playSequentially(
                fillButtonBefore(),
                textBounce(),
                shrinkButton(),
                ringFill(),
                scaleButtonAfter(),
                fillButtonAfter()
            )
            animatorSet.start()
        }
    }

    private fun fillButtonBefore(): ValueAnimator? {
        val colorAnimator =
            ValueAnimator.ofObject(ArgbEvaluator(), ContextCompat.getColor(context, R.color.animColor), Color.WHITE)
        colorAnimator.duration = animationDuration / 2
        colorAnimator.addUpdateListener { animation ->
            alpha = (colorMaxValue - (animation.animatedFraction * colorMaxValue)).toInt()
            buttonTextColor = animation.animatedValue as Int
            invalidate()
        }

        return colorAnimator
    }

    private fun textBounce(): ValueAnimator? {
        val textValueAnimator = ValueAnimator.ofFloat(22f, 18f, 22f)
        textValueAnimator.duration = animationDuration / 2
        textValueAnimator.addUpdateListener { animation ->
            setTextSize(TypedValue.COMPLEX_UNIT_SP, animation.animatedValue as Float)
            invalidate()
        }
        return textValueAnimator
    }

    private fun shrinkButton(): ValueAnimator? {
        val valueAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            ContextCompat.getColor(context, R.color.animColor),
            ContextCompat.getColor(context, R.color.ringBackground)
        )
        valueAnimator.duration = animationDuration
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.addUpdateListener { animation ->
            alpha = (0 + (animation.animatedFraction * colorMaxValue)).toInt()
            buttonTextColor = Color.argb(colorMaxValue - alpha, colorMaxValue, colorMaxValue, colorMaxValue)
            buttonWidth =
                    (maxButtonWidth - animation.animatedFraction * (maxButtonWidth - minButtonWidth)).toInt()
            ringSize = (minRingSize + animation.animatedFraction * (maxRingSize - minRingSize)).toInt()
            cornerRadius =
                    (minCornerRadius + animation.animatedFraction * (minButtonWidth - minCornerRadius)).toInt()
            ringColor = animation.animatedValue as Int
            invalidate()
        }

        return valueAnimator
    }

    private fun ringFill(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = animationDuration * 2
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                ringColor = ContextCompat.getColor(context, R.color.animColor)
            }
        })
        valueAnimator.addUpdateListener { animation ->
            angle = animation.animatedFraction * 360f
            invalidate()
        }
        return valueAnimator
    }

    private fun scaleButtonAfter(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = animationDuration
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                buttonTextColor = Color.argb(0, colorMaxValue, colorMaxValue, colorMaxValue)
            }
        })
        valueAnimator.addUpdateListener { animation ->
            buttonWidth =
                    (minButtonWidth + animation.animatedFraction * (maxButtonWidth - minButtonWidth)).toInt()
            ringSize = (maxRingSize - animation.animatedFraction * (maxRingSize - minRingSize)).toInt()
            alpha = (colorMaxValue - animation.animatedFraction * colorMaxValue).toInt()
            cornerRadius =
                    (minCornerRadius + animation.animatedFraction * (minButtonWidth - minCornerRadius)).toInt()
            doneBitmapAlpha = colorMaxValue - alpha
            invalidate()
        }
        return valueAnimator
    }

    private fun fillButtonAfter(): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(0, 255)
        valueAnimator.duration = animationDuration
        valueAnimator.startDelay = 2 * animationDuration
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                isRunning = false

            }
        })
        valueAnimator.addUpdateListener { animation ->
            alpha = animation.animatedValue as Int
            doneBitmapAlpha = colorMaxValue - alpha
            buttonTextColor = Color.argb(alpha, 25, 204, 149)
            invalidate()
        }
        return valueAnimator
    }

    override fun onDraw(canvas: Canvas?) {

        bitmapCanvas.save()
        bitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // Draw green/gray outside border
        paint.color = ringColor
        buttonRectF.left = (bitmap.width - dpToPx(buttonWidth)) / 2f
        buttonRectF.top = (bitmap.height - dpToPx(buttonHeight)) / 2f
        buttonRectF.right = (bitmap.width + dpToPx(buttonWidth)) / 2f
        buttonRectF.bottom = (bitmap.height + dpToPx(buttonHeight)) / 2f
        bitmapCanvas.drawRoundRect(
            buttonRectF,
            dpToPx(cornerRadius / 2),
            dpToPx(cornerRadius / 2),
            paint
        )

        // Draw rotating circle
        paint.color = ContextCompat.getColor(context, R.color.animColor)
        paint.alpha = if (ringSize == maxRingSize) colorMaxValue else 0
        buttonRectF.left = (bitmap.width - dpToPx(buttonHeight)) / 2f
        buttonRectF.top = (bitmap.height - dpToPx(buttonHeight)) / 2f
        buttonRectF.right = (bitmap.width + dpToPx(buttonHeight)) / 2f
        buttonRectF.bottom = (bitmap.height + dpToPx(buttonHeight)) / 2f
        bitmapCanvas.drawArc(buttonRectF, 270f, angle, true, paint)

        // Draw inner white
        paint.color = Color.WHITE
        paint.alpha = alpha
        buttonRectF.left = (bitmap.width - dpToPx(buttonWidth - 2 * ringSize)) / 2f
        buttonRectF.top = (bitmap.height - dpToPx(buttonHeight - 2 * ringSize)) / 2f
        buttonRectF.right = (bitmap.width + dpToPx(buttonWidth - 2 * ringSize)) / 2f
        buttonRectF.bottom = (bitmap.height + dpToPx(buttonHeight - 2 * ringSize)) / 2f
        bitmapCanvas.drawRoundRect(
            buttonRectF,
            dpToPx(cornerRadius / 2 - ringSize),
            dpToPx(cornerRadius / 2 - ringSize),
            paint
        )

        bitmapCanvas.restore()

        paint.color = Color.TRANSPARENT
        paint.alpha = colorMaxValue
        canvas?.drawBitmap(bitmap, (width - bitmap.width) / 2f, (height - bitmap.height) / 2f, paint)
        paint.alpha = doneBitmapAlpha
        canvas?.drawBitmap(doneBitmap, (width - doneBitmap.width) / 2f, (height - doneBitmap.height) / 2f, paint)

        // Draw text
        setTextColor(buttonTextColor)
        super.onDraw(canvas)
    }

    private fun dpToPx(dp: Int): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics)
}