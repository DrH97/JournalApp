package com.thetechtriad.drh.journalapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    private List<Note> notes;
    private Context context;

    NotesAdapter(Context context, List<Note> notes) {
        this.notes = notes;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title, content, date;
        public ImageView favourite;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_header);
            content = itemView.findViewById(R.id.note_content);
            date = itemView.findViewById(R.id.note_date);
            favourite = itemView.findViewById(R.id.favourite);
        }
    }

    @NonNull
    @Override
    public NotesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_cardview, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.ViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.date.setText(note.getDate());

        if (note.getFavourite())
            holder.favourite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_24dp));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

}
