package com.sabadac.submitbuttonanimation

import android.animation.*
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import kotlin.reflect.KMutableProperty0

class SubmitButtonAnimation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    TextView(context, attrs, defStyleAttr), View.OnClickListener {

    val alphaProperty = "alpha"
    val widthProperty = "width"
    val ringProperty = "ring"
    val angleProperty = "angle"

    private val minRingSize = 2
    private val maxRingSize = 4
    private val maxButtonWidth = 191
    private val minButtonWidth = 63
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

    init {
        this.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (!isRunning) {
            isRunning = true

            val firstAnimator = fillButtonBefore()
            val secondAnimator = shrinkButton()
            val thirdAnimator = ringFill()
            val fourthAnimator = scaleButtonAfter()
            val fifthAnimator = fillButtonAfter()

            val animatorSet = AnimatorSet()
            animatorSet.playSequentially(firstAnimator, secondAnimator, thirdAnimator, fourthAnimator, fifthAnimator)
            animatorSet.start()
        }
    }

    private fun fillButtonBefore(): AnimatorSet {
        val alphaPropertyHolder = PropertyValuesHolder.ofInt(alphaProperty, colorMaxValue, 0)
        val alfaAnimator = ValueAnimator()
        alfaAnimator.duration = animationDuration
        alfaAnimator.setValues(alphaPropertyHolder)
        alfaAnimator.interpolator = LinearInterpolator()
        alfaAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                alpha = animation?.getAnimatedValue(alphaProperty) as Int
                invalidate()
            }
        })
        val colorAnimator = getColorAnimator(
            animationDuration,
            ContextCompat.getColor(context, R.color.animColor),
            Color.WHITE,
            ::buttonTextColor
        )
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(alfaAnimator, colorAnimator)

        return animatorSet
    }

    private fun getColorAnimator(
        duration: Long,
        fromColor: Int,
        toColor: Int,
        affectedProperty: KMutableProperty0<Int>
    ): ValueAnimator {
        val valueAnimator = ValueAnimator()
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.setIntValues(fromColor, toColor)
        val argbEvaluator = ArgbEvaluator()
        valueAnimator.setEvaluator(argbEvaluator)
        valueAnimator.duration = duration
        valueAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                affectedProperty.set(animation?.animatedValue as Int)
                invalidate()
            }
        })

        return valueAnimator
    }

    private fun shrinkButton(): AnimatorSet {
        val alphaPropertyHolder = PropertyValuesHolder.ofInt(alphaProperty, 0, colorMaxValue)
        val widthPropertyHolder = PropertyValuesHolder.ofInt(widthProperty, maxButtonWidth, minButtonWidth)
        val ringPropertyHolder = PropertyValuesHolder.ofInt(ringProperty, minRingSize, maxRingSize)
        val valueAnimator = ValueAnimator()
        valueAnimator.duration = animationDuration
        valueAnimator.setValues(
            widthPropertyHolder,
            ringPropertyHolder,
            alphaPropertyHolder
        )
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                alpha = animation?.getAnimatedValue(alphaProperty) as Int
                buttonTextColor = Color.argb(colorMaxValue - alpha, colorMaxValue, colorMaxValue, colorMaxValue)
                buttonWidth = animation?.getAnimatedValue(widthProperty) as Int
                ringSize = animation?.getAnimatedValue(ringProperty) as Int
                invalidate()
            }
        })
        val colorAnimator = getColorAnimator(
            animationDuration,
            ContextCompat.getColor(context, R.color.animColor),
            ContextCompat.getColor(context, R.color.ringBackground),
            ::ringColor
        )
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(valueAnimator, colorAnimator)

        return animatorSet
    }

    private fun ringFill(): ValueAnimator {
        val anglePropertyHolder = PropertyValuesHolder.ofFloat(angleProperty, 0f, 360f)
        val valueAnimator = ValueAnimator()
        valueAnimator.duration = animationDuration * 2
        valueAnimator.setValues(anglePropertyHolder)
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                ringColor = ContextCompat.getColor(context, R.color.animColor)
            }
        })
        valueAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                angle = animation?.getAnimatedValue(angleProperty) as Float
                invalidate()
            }
        })
        return valueAnimator
    }

    private fun scaleButtonAfter(): ValueAnimator {
        val alphaPropertyHolder = PropertyValuesHolder.ofInt(alphaProperty, colorMaxValue, 0)
        val widthPropertyHolder = PropertyValuesHolder.ofInt(widthProperty, minButtonWidth, maxButtonWidth)
        val ringPropertyHolder = PropertyValuesHolder.ofInt(ringProperty, maxRingSize, minRingSize)
        val valueAnimator = ValueAnimator()
        valueAnimator.duration = animationDuration
        valueAnimator.setValues(widthPropertyHolder, ringPropertyHolder, alphaPropertyHolder)
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                buttonTextColor = Color.argb(0, colorMaxValue, colorMaxValue, colorMaxValue)
            }
        })
        valueAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                buttonWidth = animation?.getAnimatedValue(widthProperty) as Int
                ringSize = animation?.getAnimatedValue(ringProperty) as Int
                alpha = animation?.getAnimatedValue(alphaProperty) as Int
//                innerAlpha = alpha
                doneBitmapAlpha = colorMaxValue - alpha
                invalidate()
            }
        })
        return valueAnimator
    }

    private fun fillButtonAfter(): ValueAnimator {
        val alphaPropertyHolder = PropertyValuesHolder.ofInt(alphaProperty, 0, colorMaxValue)
        val valueAnimator = ValueAnimator()
        valueAnimator.duration = animationDuration
        valueAnimator.startDelay = 2 * animationDuration
        valueAnimator.setValues(alphaPropertyHolder)
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                isRunning = false

            }
        })
        valueAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                alpha = animation?.getAnimatedValue(alphaProperty) as Int
                doneBitmapAlpha = colorMaxValue - alpha
                buttonTextColor = Color.argb(alpha, 25, 204, 149)
                invalidate()
            }
        })
        return valueAnimator
    }

    override fun onDraw(canvas: Canvas?) {

        bitmapCanvas.save()
        bitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // Draw green/gray outside border
        paint.color = ringColor
        buttonRectF.left = (bitmap.width - dpToPx(buttonWidth, context)) / 2f
        buttonRectF.top = (bitmap.height - dpToPx(buttonHeight, context)) / 2f
        buttonRectF.right = (bitmap.width + dpToPx(buttonWidth, context)) / 2f
        buttonRectF.bottom = (bitmap.height + dpToPx(buttonHeight, context)) / 2f
        bitmapCanvas.drawRoundRect(
            buttonRectF,
            dpToPx(buttonHeight / 2, context),
            dpToPx(buttonHeight / 2, context),
            paint
        )

        // Draw rotating circle
        paint.color = ContextCompat.getColor(context, R.color.animColor)
        paint.alpha = if (ringSize == maxRingSize) colorMaxValue else 0
        buttonRectF.left = (bitmap.width - dpToPx(buttonHeight, context)) / 2f
        buttonRectF.top = (bitmap.height - dpToPx(buttonHeight, context)) / 2f
        buttonRectF.right = (bitmap.width + dpToPx(buttonHeight, context)) / 2f
        buttonRectF.bottom = (bitmap.height + dpToPx(buttonHeight, context)) / 2f
        bitmapCanvas.drawArc(buttonRectF, 270f, angle, true, paint)

        // Draw inner white
        paint.color = Color.WHITE
        paint.alpha = alpha
        buttonRectF.left = (bitmap.width - dpToPx(buttonWidth - 2 * ringSize, context)) / 2f
        buttonRectF.top = (bitmap.height - dpToPx(buttonHeight - 2 * ringSize, context)) / 2f
        buttonRectF.right = (bitmap.width + dpToPx(buttonWidth - 2 * ringSize, context)) / 2f
        buttonRectF.bottom = (bitmap.height + dpToPx(buttonHeight - 2 * ringSize, context)) / 2f
        bitmapCanvas.drawRoundRect(
            buttonRectF,
            dpToPx(buttonHeight / 2 - ringSize, context),
            dpToPx(buttonHeight / 2 - ringSize, context),
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

    private fun dpToPx(dp: Int, context: Context): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics)

    private fun spToPx(sp: Int, context: Context): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics)
}