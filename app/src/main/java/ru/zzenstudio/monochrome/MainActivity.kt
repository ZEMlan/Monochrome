/*
 * Created by Evgeniya Zemlyanaya (@zzemlyanaya), ZZen Studio
 *  * Copyright (c) 2021 . All rights reserved.
 */

package ru.zzenstudio.monochrome

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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.favourites_sheet.view.*
import ru.zzenstudio.monochrome.database.Colour
import ru.zzenstudio.monochrome.databinding.ActivityMainBinding


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

           Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show()
        }
        binding.textColour.filters = arrayOf<InputFilter>(LengthFilter(9))
        binding.textColour.setOnEditorActionListener { v, _, _ ->
            val colour = parseColorString(v.text.toString())
            if(binding.colourPickerView.color != colour)
                binding.colourPickerView.setColor(colour, true)

            return@setOnEditorActionListener true
        }

        binding.colourPickerView.apply {
            setColor(viewModel.colour.value!!, true)
            setOnColorChangedListener {
                viewModel.setNewColour(binding.colourPickerView.color)
            }
        }

        val sheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
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


        viewModel.colour.observe(this, {
            binding.colourPanel.color = it
            binding.textColour.setText(getHex(it))
        })
        viewModel.colours.observe(this, {
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
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                try {
                    val colourName = name.text.toString()
                    val colourHex = hex.text.toString()

                    if(colourName == "" || colourName == "")
                        throw Exception(getString(R.string.fill_fields))

                    val colour = Colour(code, colourHex, colourName)
                    viewModel.insert(colour)
                } catch (e: Exception) {
                    showError(e.message.orEmpty())
                }

            }
            .setNegativeButton(getString(R.string.cancel)) {_, _ -> }
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
            .setPositiveButton(getString(R.string.edit)
            ) { _, _ ->
                try {
                    val colourName = name.text.toString()

                    if(colourName == "")
                        throw Exception(getString(R.string.fill_fields))

                    viewModel.edit(colour.code, colourName)
                } catch (e: Exception) {
                    showError(e.message.orEmpty())
                }
            }
            .setNeutralButton(
                getString(R.string.cancel)
            ) { _, _ ->
                viewModel.insert(colour)
                (recyclerView.adapter as FavColoursRecyclerAdapter).restoreItem(colour, position)
                recyclerView.scrollToPosition(position)
            }
            .setNegativeButton(
                getString(R.string.delete)
            ) {_, _ ->
                viewModel.delete(colour)
                Snackbar
                    .make(recyclerView, getString(R.string.colour_deleted), Snackbar.LENGTH_SHORT)
                    .setAction(
                        getString(R.string.cancel)
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
