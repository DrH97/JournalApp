package com.thetechtriad.drh.journalapp;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> implements Filterable {
    private List<Note> notes;
    private List<Note> notesFiltered;
    private Context context;
    private NotesAdapterListener listener;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNotesDatabaseReference;

    private boolean newNote = true, b;
    private String noteId, userId;
    private Note note;

    NotesAdapter(Context context, List<Note> notes, NotesAdapterListener listener, boolean b) {
        this.notes = notes;
        this.context = context;
        this.notesFiltered = notes;
        this.listener = listener;
        this.b = b;

        filterRecyclerData();
    }

    private void filterRecyclerData() {
        for (int i = 0; i < notesFiltered.size(); i++) {
            if (notesFiltered.get(i).getDeleted()) {
                notesFiltered.remove(i);
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView title, content, date, deleteText, deleteTextLeft;
        ImageView favourite, deleteIcon, deleteIconLeft;
        RelativeLayout viewBackground;
        CardView viewForeground;

        ViewHolder(final View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_header);
            content = itemView.findViewById(R.id.note_content);
            date = itemView.findViewById(R.id.note_date);
            favourite = itemView.findViewById(R.id.favourite);
            viewBackground = itemView.findViewById(R.id.note_view_background);
            viewForeground = itemView.findViewById(R.id.note_view_foreground);
            deleteText = itemView.findViewById(R.id.delete_text);
            deleteTextLeft = itemView.findViewById(R.id.delete_text_left);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
            deleteIconLeft = itemView.findViewById(R.id.delete_icon_left);

            if (b) {
                viewBackground.setBackgroundColor(itemView.getResources().getColor(R.color.colorPrimary));
                deleteIcon.setVisibility(View.GONE);
                deleteText.setVisibility(View.GONE);
                deleteTextLeft.setText(R.string.restore);
                deleteIconLeft.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_refresh_24dp));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onNoteSelected(notesFiltered.get(getAdapterPosition()));
                }
            });

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
    public void onBindViewHolder(@NonNull final NotesAdapter.ViewHolder holder, final int position) {
        Note note = notesFiltered.get(position);
        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.date.setText(date(note.getDate()));

//        Glide.with(context)
//                .load(R.drawable.ic_favorite_24dp)
//                .into(holder.favourite);
//        if (note != null)
        if (note.getFavourite())
            holder.favourite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_24dp));

        holder.favourite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    favouriteNote(holder.getAdapterPosition(), v);
                }
            }

        });
    }

    private String date(String date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
        return format.format(new Date(Long.parseLong(date)));
    }

    @Override
    public int getItemCount() {
        return notesFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    notesFiltered = notes;
                } else {
                    List<Note> filteredList = new ArrayList<>();
                    for (Note row : notes) {
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase()) ||
                                row.getContent().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    notesFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = notesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notesFiltered = (List<Note>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void favouriteNote(int position, View v) {
        Log.e("NotesAdapter", "position: " + position);

//        if (position >= 0) {
            Boolean favourite = false;
            ImageView imageView = (ImageView) v;

            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mNotesDatabaseReference = mFirebaseDatabase.getReference().child("notes").child(userId);

            noteId = notesFiltered.get(position).getNoteId();

            Log.e("NotesAdapter", ""+notesFiltered.get(position).getFavourite());
            if (!notesFiltered.get(position).getFavourite()) {
                favourite = true;
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_24dp));

            } else {

                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_border_black_24dp));
            }

            notesFiltered.get(position).setFavourite(favourite);

            DatabaseReference noteRef = mNotesDatabaseReference.child(noteId);
            Map<String, Object> noteUpdates = new HashMap<>();
            noteUpdates.put("favourite", favourite);

            notifyDataSetChanged();
            noteRef.updateChildren(noteUpdates);
//      }

    }

    void removeNote(int position, DatabaseReference mNotesDatabaseReference) {

        this.mNotesDatabaseReference = mNotesDatabaseReference;

        noteId = notesFiltered.get(position).getNoteId();

        notesFiltered.remove(position);

        notifyItemRemoved(position);

        DatabaseReference noteRef = this.mNotesDatabaseReference.child(noteId);
        Map<String, Object> noteUpdates = new HashMap<>();
        noteUpdates.put("deleted", true);

        noteRef.updateChildren(noteUpdates);
    }

    void restoreNote(Note note, int position, String note_id, DatabaseReference notesDatabaseReference) {
        mNotesDatabaseReference = notesDatabaseReference;

        noteId = note_id;

        DatabaseReference noteRef = this.mNotesDatabaseReference.child(noteId);
        Map<String, Object> noteUpdates = new HashMap<>();
        noteUpdates.put("deleted", false);

        noteRef.updateChildren(noteUpdates);

        Log.e("NA", "Note restored");

        notesFiltered.add(position, note);

        notifyItemInserted(position);
    }

    public interface NotesAdapterListener {
        void onNoteSelected(Note note);
    }
}
