package ru.zemlyanaya.monochrome

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ru.zemlyanaya.monochrome.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProviders.of(
        this,
        ViewModelProvider.AndroidViewModelFactory(application)
    ).get(MainViewModel::class.java) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        binding.textColour.setOnClickListener {
           val clipboard = ContextCompat.getSystemService(
               this,
               ClipboardManager::class.java
           )
           clipboard?.setPrimaryClip(
               ClipData.newPlainText(
                   getHex(viewModel.colour.value!!),
                   getHex(viewModel.colour.value!!)
               )
           )
            it.clearFocus()

           Toast.makeText(this, "Copied to clipboard.", Toast.LENGTH_SHORT).show()
        }
        binding.textColour.filters = arrayOf<InputFilter>(LengthFilter(9))
        binding.textColour.doAfterTextChanged {
            val colour = parseColorString(it.toString())
            if(binding.colourPickerView.color != colour)
                binding.colourPickerView.setColor(colour, true)
        }

        binding.colourPickerView.setColor(viewModel.colour.value!!, true)
        binding.colourPickerView.setOnColorChangedListener {
            viewModel.setNewColour(binding.colourPickerView.color)
            if(binding.textColour.hasFocus()){
                hideKeyboard()
                binding.textColour.clearFocus()
            }
        }

        viewModel.colour.observe(this, Observer<Int> {
            binding.colourPanel.color = it
            binding.textColour.setText(getHex(it))
        })

    }

    private fun hideKeyboard(){
        val imm =
            this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = this.currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}
