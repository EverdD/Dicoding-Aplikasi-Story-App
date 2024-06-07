package com.firmannurcahyo.submission.frontend.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.firmannurcahyo.submission.R
import com.firmannurcahyo.submission.frontend.model.isValidEmail

class CustomView : AppCompatEditText, View.OnTouchListener {

    private lateinit var clearButtonImage: Drawable
    private lateinit var usernameImage: Drawable
    private var buttonDrawable: Drawable? = null
    private var buttonPosition: ButtonPosition? = null
    private var inputType: InputType? = null

    enum class ButtonPosition {
        START, END
    }

    enum class InputType {
        USERNAME, EMAIL, PASSWORD
    }

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int = 0) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomView, defStyleAttr, 0)

        inputType = when {
            a.getBoolean(R.styleable.CustomView_email, false) -> InputType.EMAIL
            a.getBoolean(R.styleable.CustomView_password, false) -> InputType.PASSWORD
            else -> InputType.USERNAME
        }

        clearButtonImage =
            ContextCompat.getDrawable(context, R.drawable.ic_baseline_close) as Drawable

        setOnTouchListener(this)
        addTextChangedListener(textChangeListener)

        a.recycle()
    }

    private val textChangeListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // Do nothing.
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            updateButtonVisibility(s.toString())
            updateErrorText(s.toString())
        }

        override fun afterTextChanged(s: Editable) {
            // Do nothing.
        }
    }

    private fun updateButtonVisibility(input: String) {
        val hasText = input.isNotEmpty()
        val shouldShowButton = when (inputType) {
            InputType.USERNAME -> hasText
            InputType.EMAIL -> !input.isValidEmail()
            InputType.PASSWORD -> input.length < 8 && hasText
            else -> false
        }

        buttonPosition = if (shouldShowButton) ButtonPosition.END else null
        updateButtonDrawable()
    }

    private fun updateErrorText(input: String) {
        error = when (inputType) {
            InputType.EMAIL -> if (!input.isValidEmail()) resources.getString(R.string.invalid_email) else null
            InputType.PASSWORD -> if (input.length < 8) resources.getString(R.string.invalid_password) else null
            else -> null
        }
    }

    private fun updateButtonDrawable() {
        buttonDrawable = when (buttonPosition) {
            ButtonPosition.START -> usernameImage
            ButtonPosition.END -> clearButtonImage
            else -> null
        }
        setButtonDrawable()
    }

    private fun setButtonDrawable() {
        setCompoundDrawablesRelativeWithIntrinsicBounds(buttonDrawable, null, null, null)
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        val x = event.x.toInt()
        if (buttonDrawable != null && event.action == MotionEvent.ACTION_UP) {
            val buttonStart: Int
            val buttonEnd: Int
            val isButtonClicked: Boolean

            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                buttonEnd = paddingStart
                isButtonClicked = x < buttonEnd
            } else {
                buttonStart = width - paddingEnd
                isButtonClicked = x > buttonStart
            }

            if (isButtonClicked) {
                text?.clear()
                return true
            }
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        gravity = when (buttonPosition) {
            ButtonPosition.START -> Gravity.START or Gravity.CENTER_VERTICAL
            ButtonPosition.END -> Gravity.END or Gravity.CENTER_VERTICAL
            else -> Gravity.START or Gravity.CENTER_VERTICAL
        }
    }
}