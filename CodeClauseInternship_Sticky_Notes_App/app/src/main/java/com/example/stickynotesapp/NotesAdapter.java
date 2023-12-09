package com.example.stickynotesapp;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notesList;
    private List<Note> filteredNotesList;
    private final OnNoteDeleteListener onNoteDeleteListener;
    private OnItemClickListener itemClickListener;


    public interface OnNoteDeleteListener {
        void onDeleteClick(int position);
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }


    public class NoteViewHolder extends RecyclerView.ViewHolder {
        EditText editTextNote;
        ImageButton btnDeleteNote;
        ImageButton btnPinNote;

        public NoteViewHolder(View view) {
            super(view);
            editTextNote = view.findViewById(R.id.editTextNote);
            btnDeleteNote = view.findViewById(R.id.btnDeleteNote);
            btnPinNote = view.findViewById(R.id.btnPinNote);

            // Implement click listener here
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            });

            // Set a click listener for the pin icon button
            btnPinNote.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            });

        }
    }

    public NotesAdapter(List<Note> notesList, OnNoteDeleteListener listener) {
        this.notesList = notesList;
        this.filteredNotesList = new ArrayList<>(notesList);
        this.onNoteDeleteListener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sticky_note_item, parent, false);

        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        Note note = filteredNotesList.get(position); // Use filteredNotesList
        holder.editTextNote.setText(note.getContent());

        if (note.isPinned()) {
            // Set a different background color or indicator for pinned notes
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            // Set default background color or remove indicator for unpinned notes
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(view -> {
            if (itemClickListener != null) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(clickedPosition);
                }
            }
        });

        holder.btnDeleteNote.setOnClickListener(view -> {
            if (onNoteDeleteListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onNoteDeleteListener.onDeleteClick(adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredNotesList.size();
    }

    // Method to set the notes list and initialize the filtered list
    public void setNotes(List<Note> notesList) {
        this.notesList = notesList;
        this.filteredNotesList = new ArrayList<>(notesList);
        notifyDataSetChanged();
    }

    public List<Note> getFilteredNotesList() {
        return filteredNotesList;
    }

    public void setSearchText(String searchText) {
        filter(searchText); // Call filter method when search text changes
    }

    // Filter method to update the displayed list based on search query
    public void filter(String searchText) {
        searchText = searchText.toLowerCase(Locale.getDefault());
        filteredNotesList.clear();

        if (searchText.isEmpty()) {
            filteredNotesList.addAll(notesList);
        } else {
            for (Note note : notesList) {
                if (note.getContent().toLowerCase(Locale.getDefault()).contains(searchText)) {
                    filteredNotesList.add(note);
                }
            }
        }
        notifyDataSetChanged();

        // Print a message if no matches found
        if (filteredNotesList.isEmpty()) {
            Log.d("NotesAdapter", "No matching results found");
        }
    }

}
