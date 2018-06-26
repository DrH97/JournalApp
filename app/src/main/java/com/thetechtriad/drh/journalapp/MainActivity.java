package com.thetechtriad.drh.journalapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    View header;
    GoogleSignInClient mGoogleSignInClient;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<Note> noteList = new ArrayList<>();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNotesDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.e(TAG, userId);
        if (mFirebaseDatabase == null) {
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mNotesDatabaseReference = mFirebaseDatabase.getReference().child("notes").child(userId);
        }

        mNotesDatabaseReference.keepSynced(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateNoteActivity.class));
            }
        });

        mRecyclerView = findViewById(R.id.note_recycler);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NotesAdapter(this, noteList);
        mRecyclerView.setAdapter(mAdapter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);

        setUserData(getUserData());
        setNotesData();

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                findViewById(R.id.no_notes).setVisibility(View.GONE);
                Note note = dataSnapshot.getValue(Note.class);
                noteList.add(note);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                noteList = new ArrayList<>();
                setNotesData();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                noteList = new ArrayList<>();
                setNotesData();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                noteList = new ArrayList<>();
                setNotesData();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mNotesDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (noteList.size() < 1) {
            findViewById(R.id.no_notes).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.no_notes).setVisibility(View.GONE);
        }
    }

    private void setNotesData() {
//        Note note = new Note("First Note", "This is the first note of the app", getDate(), false);
//        noteList.add(note);
//
//        note = new Note("Welcome", "This is the welcome note of the app", getDate(), true);
//        noteList.add(note);
//
//        note = new Note("2nd Note",  "This is the 2nd note of the app", getDate(), false);
//        noteList.add(note);
//
//        note = new Note("Another Note",  "This is another note of the app", getDate(), false);
//        noteList.add(note);
//
//        note = new Note("Ala! Note",  "What is going on???!", getDate(), true);
//        noteList.add(note);
//
//        note = new Note("Long Trial Note, with long nammeeeee",  "This is the longest note ever, i'm trying to make it long and see if i can still see all this content or nah, if possible then yess we made: otherwise, crap crap crapppp!", getDate(), true);
//        noteList.add(note);

        mAdapter.notifyDataSetChanged();
    }

    private String getDate() {
        Date today = Calendar.getInstance().getTime();
        DateFormat dateFormat = SimpleDateFormat.getDateInstance();
        dateFormat.format(today);

        return dateFormat.format(today);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private FirebaseUser getUserData() {

//        return GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setUserData(FirebaseUser acct) {
        String personName = null, personGivenName = null, personFamilyName = null, personEmail = null, providerId = null, personProviderId = null, personId = null;
        Uri personPhoto = null;
        boolean emailVerified;

        if (acct != null) {
            personName = acct.getDisplayName();
//
            personEmail = acct.getEmail();
//
            personPhoto = acct.getPhotoUrl();
            emailVerified = acct.isEmailVerified();
            personId = acct.getUid();

//            for (UserInfo profile : acct.getProviderData()) {
//                providerId = profile.getProviderId();
//                personGivenName = profile.getGivenName();
//                personFamilyName = acct.getFamilyName();
//                personProviderId = acct.getId();
//            }
        }

        ImageView navHeaderImage = header.findViewById(R.id.nav_header_image);
        TextView navHeaderTitle = header.findViewById(R.id.nav_header_title);
        TextView navHeaderSubtitle = header.findViewById(R.id.nav_header_subtitle);

        if (personName != null)
            navHeaderTitle.setText(personName);
        if (personEmail != null)
         navHeaderSubtitle.setText(personEmail);
        if (personPhoto != null) {
            Glide.with(this).load(personPhoto)
                    .into(navHeaderImage);

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_sign_out) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        revokeAccess();
                        showLoginView();
                    }
                });
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    private void showLoginView() {
        startActivity(new Intent(this, SplashScreenActivity.class));
        finish();
    }
}
