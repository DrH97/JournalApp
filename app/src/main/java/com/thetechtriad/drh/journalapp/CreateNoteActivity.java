package com.thetechtriad.drh.journalapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        Bundle note = getIntent().getExtras();
        if (note != null) {
            newNote = false;
            noteId = note.getString("NoteId");
            noteHeader.setText(note.getString("Title"));
            noteContent.setText(note.getString("Content"));
        }

       final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveNote();
            }
        };

        noteHeader.addTextChangedListener(textWatcher);
        noteContent.addTextChangedListener(textWatcher);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_note, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteNote();
        }

        return super.onOptionsItemSelected(item);
    }

    private String getTime() {
//        Date today = Calendar.getInstance().getTime();
//        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
//
//        return dateFormat.format(today);
//        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
//        return s.format(new Date());
//        Long ts = System.currentTimeMillis();
//        return ts.toString();
        Long ts = new Date().getTime();
        return ts.toString();
    }

    public void saveNote() {
        if (!noteHeader.getText().toString().isEmpty() || !noteContent.getText().toString().isEmpty()) {
            if (newNote) {
                noteId = mNotesDatabaseReference.push().getKey();

                note = new Note(noteId, userId, noteHeader.getText().toString(), noteContent.getText().toString(), getTime(), false, false);

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

        } else {
            if (!newNote)
                deleteNote();
        }
    }

    public void saveNote(View view) {
        saveNote();
    }

    private void deleteNote() {
        DatabaseReference noteRef = mNotesDatabaseReference.child(noteId);
        Map<String, Object> noteUpdates = new HashMap<>();
        noteUpdates.put("deleted", true);

        noteRef.updateChildren(noteUpdates);

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
