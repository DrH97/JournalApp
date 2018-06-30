package com.thetechtriad.drh.journalapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        RecyclerNoteTouchHelper.RecyclerNoteTouchHelperListener,
        NotesAdapter.NotesAdapterListener, TrashNoteFragment.OnListFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    View header;
    GoogleSignInClient mGoogleSignInClient;

    private RecyclerView mRecyclerView;
    private NotesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SearchView searchView;

    private List<Note> noteList = new ArrayList<>();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNotesDatabaseReference;
    private ChildEventListener mChildEventListener;

    private DrawerLayout drawerLayout;

    public static int navItemIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String userId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        }

        if (mFirebaseDatabase == null) {
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mNotesDatabaseReference = mFirebaseDatabase.getReference().child("notes").child(userId);
        }

        mNotesDatabaseReference.keepSynced(true);

        findViewById(R.id.no_notes).setVisibility(View.VISIBLE);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateNoteActivity.class));
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);

        mRecyclerView = findViewById(R.id.note_recycler);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mAdapter = new NotesAdapter(this, noteList, this, false);
        mRecyclerView.setAdapter(mAdapter);

//        whiteNotificationBar(mRecyclerView);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerNoteTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateRecyclerView(dataSnapshot.getValue(Note.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateRecyclerViewChild(dataSnapshot.getValue(Note.class));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                updateRecyclerView(dataSnapshot.getValue(Note.class));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                updateRecyclerView(dataSnapshot.getValue(Note.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mNotesDatabaseReference.addChildEventListener(mChildEventListener);
    }

    private void updateRecyclerView(Note value) {
        noteList.add(value);

        if (noteList.size() > 0)
        for (int i = 0; i < noteList.size(); i++) {
            if (noteList.get(i).getDeleted()) {
                noteList.remove(i);
            }
        }

        sortRecyclerData(noteList);
        mAdapter.notifyDataSetChanged();

        if (noteList.size() == 0) {
            findViewById(R.id.no_notes).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.no_notes).setVisibility(View.GONE);
        }
    }

    private void updateRecyclerViewChild(Note value) {
        if (noteList.size() > 0)
            for (int i = 0; i < noteList.size(); i++) {
                if (noteList.get(i).getNoteId().equals(value.getNoteId())) {
                    noteList.remove(i);
                    noteList.add(i, value);
                }
            }

        mAdapter.notifyDataSetChanged();
    }

    private void sortRecyclerData(List<Note> noteList) {
        Collections.sort(noteList, new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                return Long.parseLong(o1.getDate()) > Long.parseLong(o2.getDate()) ? -1 : Long.parseLong(o1.getDate()) < Long.parseLong(o2.getDate()) ? 1 : 0;
            }
        });
    }

    private void whiteNotificationBar(RecyclerView mRecyclerView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = mRecyclerView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            mRecyclerView.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (noteList.size() == 0) {
            findViewById(R.id.no_notes).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.no_notes).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setUserData(getUserData());
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

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            mAdapter.notifyDataSetChanged();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            // Handle the camera action

            FragmentManager fm = getFragmentManager();
            fm.popBackStackImmediate();
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.remove(fr);
//            ft.commit();
            drawerLayout.closeDrawers();
            return true;
        } else if (id == R.id.nav_trash) {
            Fragment fr = new TrashNoteFragment();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.main_content, fr);
            ft.addToBackStack("trashFrag");
            ft.commit();

//        } else if (id == R.]id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_sign_out) {
            signOut();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
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

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof NotesAdapter.ViewHolder) {
            String title = noteList.get(viewHolder.getAdapterPosition()).getTitle();

            final Note deletedNote = noteList.get(viewHolder.getAdapterPosition());
            final int deletedNoteIndex = viewHolder.getAdapterPosition();

            mAdapter.removeNote(viewHolder.getAdapterPosition(), mNotesDatabaseReference);

            Snackbar.make(drawerLayout, title + " removed from Journal!", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAdapter.restoreNote(deletedNote, deletedNoteIndex, deletedNote.getNoteId(), mNotesDatabaseReference);
                            mAdapter.notifyDataSetChanged();
                        }
                    }).setActionTextColor(Color.BLUE).show();
        }
    }

    @Override
    public void onNoteSelected(Note note) {
        Intent intent = new Intent(this, CreateNoteActivity.class);

        intent.putExtra("NoteId", note.getNoteId());
        intent.putExtra("Title", note.getTitle());
        intent.putExtra("Content", note.getContent());

        startActivity(intent);
    }

    @Override
    public void onListFragmentInteraction() {

    }

}
