package ru.zemlyanaya.monochrome

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import ru.zemlyanaya.monochrome.database.Colour
import ru.zemlyanaya.monochrome.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

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

        val sheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED)
                    sheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                if(newState == BottomSheetBehavior.STATE_HIDDEN
                    || newState == BottomSheetBehavior.STATE_COLLAPSED)
                    binding.fab.setImageResource(R.drawable.ic_star)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        binding.fab.setOnClickListener {
            if (sheetBehavior.state != BottomSheetBehavior.STATE_HALF_EXPANDED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                binding.fab.setImageResource(R.drawable.ic_plus)
            }
            else {
               createAddDialog().show()
            }
        }

        recyclerView = binding.bottomSheet.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        val itemAnimator = DefaultItemAnimator()
        itemAnimator.addDuration = 400
        itemAnimator.removeDuration = 400
        itemAnimator.moveDuration = 400

        recyclerView.itemAnimator = itemAnimator
        enableSwipeToEditAndUndo()


        viewModel.colour.observe(this, Observer<Int> {
            binding.colourPanel.color = it
            binding.textColour.setText(getHex(it))
        })
        viewModel.colours.observe(this, Observer {
                list -> binding.bottomSheet.recyclerView.adapter =
            FavColoursRecyclerAdapter({onColourClick(it)}, list.orEmpty())
        })

    }

    private fun enableSwipeToEditAndUndo() {
        val swipeToDeleteCallback: SwipeToEditeAndDeleteCallback = object : SwipeToEditeAndDeleteCallback(recyclerView.context) {
            override fun onSwiped(@NonNull viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                val item: Colour = (recyclerView.adapter as FavColoursRecyclerAdapter).getData()[position]
                (recyclerView.adapter as FavColoursRecyclerAdapter).removeItem(position)
                recyclerView.rootView.let { createEditDialog(item, position).show() }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun onColourClick(colour: Colour){
        viewModel.setNewColour(colour.code)
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

    private fun createAddDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this, R.style.AppTheme_DialogStyle)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.fav_colour_dialog, null)

        val name: TextInputEditText = view.findViewById(R.id.nameText)
        val hex: TextInputEditText = view.findViewById(R.id.hexText)
        hex.setText(getHex(viewModel.colour.value!!))
        hex.isEnabled = false
        val code = viewModel.colour.value!!

        builder.setView(view)
            .setPositiveButton("Сохранить") { _, _ ->
                try {
                    val colourName = name.text.toString()
                    val colourHex = hex.text.toString()

                    if(colourName == "" || colourName == "")
                        throw Exception("Заполните все поля!")

                    val colour = Colour(code, colourHex, colourName)
                    viewModel.insert(colour)
                } catch (e: Exception) {
                    showError(e.message.orEmpty())
                }

            }
            .setNegativeButton("Отмена") {_, _ -> }
        return builder.create()
    }

    private fun createEditDialog(colour: Colour, position: Int): AlertDialog{
        val builder = AlertDialog.Builder(this, R.style.AppTheme_DialogStyle)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.fav_colour_dialog, null)

        val name: TextInputEditText = view.findViewById(R.id.nameText)
        val hex: TextInputEditText = view.findViewById(R.id.hexText)
        name.setText(colour.name)
        hex.setText(colour.hex)
        hex.isEnabled = false

        builder.setView(view)
            .setPositiveButton("Изменить"
            ) { _, _ ->
                try {
                    val colourName = name.text.toString()

                    if(colourName == "")
                        throw Exception("Заполните все поля!")

                    viewModel.edit(colour.code, colourName)
                } catch (e: Exception) {
                    showError(e.message.orEmpty())
                }
            }
            .setNeutralButton(
                "Отмена"
            ) { _, _ ->
                viewModel.insert(colour)
                (recyclerView.adapter as FavColoursRecyclerAdapter).restoreItem(colour, position)
                recyclerView.scrollToPosition(position)
            }
            .setNegativeButton(
                "Удалить"
            ) {_, _ ->
                viewModel.delete(colour)
                Snackbar
                    .make(recyclerView, "Цвет удалён из избранного.", Snackbar.LENGTH_SHORT)
                    .setAction(
                        "ВЕРНУТЬ"
                    )  {
                        viewModel.insert(colour)
                        (recyclerView.adapter as FavColoursRecyclerAdapter).restoreItem(colour, position)
                        recyclerView.scrollToPosition(position)
                    }
                    .setActionTextColor(resources.getColor(R.color.white))
                    .show()
            }
        return builder.create()
    }

    private fun showError(e: String){
        Toast.makeText(recyclerView.context, e, Toast.LENGTH_SHORT)
            .show()
    }

}
