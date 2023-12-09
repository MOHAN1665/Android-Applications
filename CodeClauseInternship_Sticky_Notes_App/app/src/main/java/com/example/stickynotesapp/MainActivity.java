package com.example.stickynotesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.view.View;


public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteDeleteListener,NotesAdapter.OnItemClickListener{

    private NoteDao noteDao;
    private final List<Note> notesList = new ArrayList<>();
    private NotesAdapter mAdapter;
    private TextView textNoResults;
    private String searchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the TextView by its ID
        textNoResults = findViewById(R.id.textNoResults);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        mAdapter = new NotesAdapter(notesList,this);
        mAdapter.setOnItemClickListener(this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        NoteDatabase db = NoteDatabase.getInstance(this);
        noteDao = db.noteDao();

        loadNotes();

        Button btnAddNote = findViewById(R.id.btnAddNote);
        EditText editTextNoteInput = findViewById(R.id.editTextNoteInput);

        btnAddNote.setOnClickListener(view -> {
            String noteContent = editTextNoteInput.getText().toString().trim();
            if (!noteContent.isEmpty()) {
                addNoteToDatabase(noteContent);
                editTextNoteInput.setText(""); // Clear input field after adding the note
            } else {
                Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show();
            }
        });

        EditText editTextSearch = findViewById(R.id.editTextSearch);
        
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchText = charSequence.toString(); // Update searchText variable in MainActivity
                mAdapter.filter(searchText); // Set the search text in the adapter
                // Show/hide the TextView based on filtered results
                // Show/hide the TextView based on filtered results
                if (mAdapter.getItemCount() == 0) {
                    textNoResults.setVisibility(View.VISIBLE);
                } else {
                    textNoResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void loadNotes() {
        new Thread(() -> {
            List<Note> fetchedNotes = noteDao.getAllNotes();
            runOnUiThread(() -> {
                notesList.clear();
                notesList.addAll(fetchedNotes);
                mAdapter.setNotes(notesList);
                mAdapter.filter(searchText);
                mAdapter.notifyDataSetChanged(); // Notify only when the entire dataset changes
            });
        }).start();
    }

    private List<Note> filterList(List<Note> notesList, String searchText) {
        // Implement filtering logic here and return the filtered list
        // This method should filter the notesList based on the searchText
        // Example implementation:

        List<Note> filteredList = new ArrayList<>();
        for (Note note : notesList) {
            if (note.getContent().toLowerCase(Locale.getDefault()).contains(searchText.toLowerCase(Locale.getDefault()))) {
                filteredList.add(note);
            }
        }
        return filteredList;
    }

    @Override
    public void onItemClick(int position) {
        Note clickedNote = mAdapter.getFilteredNotesList().get(position);
        clickedNote.setPinned(!clickedNote.isPinned()); // Toggle the pin status
        updateNoteInDatabase(clickedNote);
    }


    private void addNoteToDatabase(String content) {
        new Thread(() -> {
            Note newNote = new Note();
            newNote.setContent(content);
            newNote.setPinned(false); // Set pin status
            noteDao.insert(newNote);
            loadNotes();
        }).start();
    }

    private void updateNoteInDatabase(Note noteToUpdate) {
        new Thread(() -> {
            noteDao.update(noteToUpdate); // Update the note in the database
            // Refresh the notes list after the update
            runOnUiThread(this::loadNotes);
        }).start();
    }

    @Override
    public void onDeleteClick(int position) {
        // Get the note to delete based on its position
        Note noteToDelete = notesList.get(position);

        // Delete the note from the database and update UI
        deleteNoteFromDatabase(noteToDelete);
    }

    private void deleteNoteFromDatabase(Note noteToDelete) {
        new Thread(() -> {
            noteDao.delete(noteToDelete);
            runOnUiThread(() -> {
                int position = mAdapter.getFilteredNotesList().indexOf(noteToDelete);
                if (position != -1) {
                    mAdapter.getFilteredNotesList().remove(noteToDelete);
                    mAdapter.notifyItemRemoved(position);
                    mAdapter.notifyItemRangeChanged(position, mAdapter.getFilteredNotesList().size()); // Update the remaining items
                }
            });
        }).start();
    }
}