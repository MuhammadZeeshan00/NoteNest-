package com.example.notenest

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notenest.adapter.NoteAdapter
import com.example.notenest.data.Note
import com.example.notenest.data.NoteState
import com.example.notenest.data.QuoteState
import com.example.notenest.databinding.ActivityMainBinding
import com.example.notenest.firebase.FirebaseAuthManager
import com.example.notenest.firebase.FirebaseDatabaseManager
import com.example.notenest.util.NetworkUtils
import com.example.notenest.vm.NoteViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: NoteViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    @Inject
    lateinit var authManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Show swipe hint after initial load
        viewModel.noteState.observe(this) { state ->
            when (state) {
                is NoteState.Success -> {
                    if (state.notes.isNotEmpty()) {
                        binding.rvNotes.postDelayed({
                            showSwipeHint()
                        }, 1000) // Show after 1 second delay
                    }
                }

                else -> {}
            }
        }


        // Set up RecyclerView
        noteAdapter = NoteAdapter(
            onNoteClick = { note ->
                // On note click, open NoteDetailActivity for editing
                val intent = Intent(this, NoteDetailActivity::class.java).apply {
                    putExtra(NoteDetailActivity.EXTRA_NOTE_ID, note.id)
                }
                startActivity(intent)
            },
            onNoteDelete = { note ->
                viewModel.deleteNote(note)
            }
        )

        binding.rvNotes.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            // Add item decoration for spacing if needed
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
        }

        // Setup swipe to delete
        setupSwipeToDelete()

        // Observe note state
        viewModel.noteState.observe(this) { state ->
            binding.progressNotes.visibility = View.GONE
            binding.tvNotesError.visibility = View.GONE
            binding.rvNotes.visibility = View.GONE

            when (state) {
                is NoteState.Loading -> {
                    binding.progressNotes.visibility = View.VISIBLE
                }

                is NoteState.Success -> {
                    noteAdapter.submitList(state.notes)
                    binding.rvNotes.visibility = View.VISIBLE
                }

                is NoteState.Error -> {
                    binding.tvNotesError.text = state.message
                    binding.tvNotesError.visibility = View.VISIBLE
                }
            }
        }

        // Observe quote state
        viewModel.quoteState.observe(this) { state ->
            binding.progressQuote.visibility = View.GONE
            binding.tvQuote.visibility = View.GONE
            binding.errorContainer.visibility = View.GONE

            when (state) {
                is QuoteState.Loading -> {
                    binding.progressQuote.visibility = View.VISIBLE
                }

                is QuoteState.Success -> {
                    if (state.quotes.isNotEmpty()) {
                        val randomQuote =
                            state.quotes[kotlin.random.Random.nextInt(state.quotes.size)]
                        binding.tvQuote.text = "\"${randomQuote.text}\" - ${randomQuote.author}"
                        binding.tvQuote.visibility = View.VISIBLE
                    } else {
                        binding.tvQuote.text = "\"Keep going, you're doing great!\" - Unknown"
                        binding.tvQuote.visibility = View.VISIBLE
                    }
                }

                is QuoteState.Error -> {
                    // Do not show the error container, just display the fallback quote
                    binding.tvQuote.text = "\"Keep going, you're doing great!\" - Unknown"
                    binding.tvQuote.visibility = View.VISIBLE
                }
            }
        }

        // Retry button click
        binding.btnRetryQuote.setOnClickListener {
            viewModel.fetchQuotes()
        }

        // FAB click to add new note
        binding.fabAddNote.setOnClickListener {
            val intent = Intent(this, NoteDetailActivity::class.java)
            startActivity(intent)
        }

        // Logout button click
        binding.btnLogout.setOnClickListener {
            if (!NetworkUtils.isOnline(this)) {
                Toast.makeText(
                    this,
                    "Cannot sign out while offline. Please connect to the internet.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            showSignOutConfirmationDialog()
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,  // No drag-and-drop
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT  // Swipe directions
        ) {
            private val background = ColorDrawable(Color.RED)
            private val deleteIcon = ContextCompat.getDrawable(
                this@MainActivity,
                android.R.drawable.ic_menu_delete
            )?.apply {
                setTint(Color.WHITE)
            }
            private val intrinsicWidth = deleteIcon?.intrinsicWidth ?: 0
            private val intrinsicHeight = deleteIcon?.intrinsicHeight ?: 0

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = noteAdapter.currentList[position]
                viewModel.deleteNote(note)
                showUndoSnackbar(note)
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    canvas,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                val itemView = viewHolder.itemView
                val backgroundCornerOffset = 20

                when {
                    dX > 0 -> { // Swiping to the right
                        background.setBounds(
                            itemView.left,
                            itemView.top,
                            itemView.left + dX.toInt() + backgroundCornerOffset,
                            itemView.bottom
                        )
                    }

                    dX < 0 -> { // Swiping to the left
                        background.setBounds(
                            itemView.right + dX.toInt() - backgroundCornerOffset,
                            itemView.top,
                            itemView.right,
                            itemView.bottom
                        )
                    }

                    else -> { // View is unswiped
                        background.setBounds(0, 0, 0, 0)
                    }
                }
                background.draw(canvas)

                // Draw the delete icon
                deleteIcon?.let { icon ->
                    val iconMargin = (itemView.height - intrinsicHeight) / 2
                    val iconTop = itemView.top + (itemView.height - intrinsicHeight) / 2
                    val iconBottom = iconTop + intrinsicHeight

                    when {
                        dX > 0 -> { // Swiping to the right
                            val iconLeft = itemView.left + iconMargin
                            val iconRight = itemView.left + iconMargin + intrinsicWidth
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        }

                        dX < 0 -> { // Swiping to the left
                            val iconLeft = itemView.right - iconMargin - intrinsicWidth
                            val iconRight = itemView.right - iconMargin
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        }
                    }
                    icon.draw(canvas)
                }
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvNotes)
    }


    private fun showSwipeHint() {
        if (noteAdapter.itemCount > 0) {
            val firstChild = binding.rvNotes.getChildAt(0) ?: return

            // Create a swipe animation
            val animator = ObjectAnimator.ofFloat(
                firstChild,
                "translationX",
                0f,
                100f,
                -100f,
                0f
            ).apply {
                duration = 1000
                interpolator = AccelerateDecelerateInterpolator()
                startDelay = 500 // Wait half a second before starting
            }

            // Show a message explaining the swipe action
            Snackbar.make(
                binding.root,
                "Swipe left or right to delete notes",
                Snackbar.LENGTH_LONG
            ).show()

            animator.start()
        }
    }

    private fun showUndoSnackbar(deletedNote: Note) {
        Snackbar.make(binding.root, "Note deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                viewModel.insertNote(deletedNote)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION) {
                        // Optional: Permanently delete from database if not undone
                    }
                }
            })
            .show()
    }

    private fun showSignOutConfirmationDialog() {

        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                // User confirmed, proceed with sign-out
                authManager.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                // User canceled, dismiss the dialog
                dialog.dismiss()
            }
            .setCancelable(false) // Prevent dismissing by tapping outside or pressing back
            .show()
    }
}


