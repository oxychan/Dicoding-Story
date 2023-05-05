package com.dicoding.dicodingstory.ui.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import com.dicoding.dicodingstory.R

class AuthEditText : AppCompatEditText {

    private var isPassword = false
    private var isEmail = false

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val styledAttr = context.obtainStyledAttributes(attrs, R.styleable.AuthEditText)
            isPassword = styledAttr.getBoolean(R.styleable.AuthEditText_isPassword, false)
            isEmail = styledAttr.getBoolean(R.styleable.AuthEditText_isEmail, false)

            styledAttr.recycle()
        }

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when {
                    isPassword && s.toString().length < 8 -> {
                        showValidationError(resources.getString(R.string.error_password))
                    }
                    isEmail && !isValidEmail(s.toString()) -> {
                        showValidationError(resources.getString(R.string.error_email))
                    }
                    else -> {
                        showValidationError("")
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showValidationError(errorMessage: String) {
        if (errorMessage == "") {
            this@AuthEditText.error = null
        } else {
            this@AuthEditText.error = errorMessage
        }
    }
}