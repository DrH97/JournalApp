package com.thetechtriad.drh.journalapp;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateNoteActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNotesDatabaseReference;

    private EditText noteHeader, noteContent;

    private boolean newNote = true;
    String noteId, userId;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (mFirebaseDatabase == null) {
            mFirebaseDatabase = FirebaseDatabase.getInstance();
        }

        mNotesDatabaseReference = mFirebaseDatabase.getReference().child("notes").child(userId);

        noteHeader = findViewById(R.id.edit_note_header);
        noteContent = findViewById(R.id.edit_note_content);

    }

    private String getTime() {
        Date today = Calendar.getInstance().getTime();
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();

        return dateFormat.format(today);
    }

    public void saveNote(View view) {
        if (!noteHeader.getText().toString().isEmpty() && !noteContent.getText().toString().isEmpty()) {
            if (newNote) {
                note = new Note(userId, noteHeader.getText().toString(), noteContent.getText().toString(), getTime(), false);

                noteId = mNotesDatabaseReference.push().getKey();

                assert noteId != null;
                mNotesDatabaseReference.child(noteId).setValue(note);
                newNote = false;
            } else {
                DatabaseReference noteRef = mNotesDatabaseReference.child(noteId);
                Map<String, Object> noteUpdates = new HashMap<>();
                noteUpdates.put("title", noteHeader.getText().toString());
                noteUpdates.put("content", noteContent.getText().toString());

                noteRef.updateChildren(noteUpdates);
            }

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
    }
}
