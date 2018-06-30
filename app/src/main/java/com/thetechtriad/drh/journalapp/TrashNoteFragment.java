package com.thetechtriad.drh.journalapp;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
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

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TrashNoteFragment extends Fragment implements NotesAdapter.NotesAdapterListener, RecyclerNoteTouchHelper.RecyclerNoteTouchHelperListener{


    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 2;
    private OnListFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private NotesAdapter mAdapter;
    private List<Note> noteList = new ArrayList<>();
    private SearchView searchView;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNotesDatabaseReference;
    private ChildEventListener mChildEventListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrashNoteFragment() {

    }

    @SuppressWarnings("unused")
    public static TrashNoteFragment newInstance(int columnCount) {
        TrashNoteFragment fragment = new TrashNoteFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        }

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        if (mFirebaseDatabase == null) {
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mNotesDatabaseReference = mFirebaseDatabase.getReference().child("notes").child(userId);
        }

        mNotesDatabaseReference.keepSynced(true);

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e("TNR", "Child added");
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

    @Override
    public void onResume() {
        super.onResume();

        Log.e("TrashNoteFrag", "Onresume");

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateRecyclerView(Note value) {
        noteList.add(value);

        if (noteList.size() > 0)
            for (int i = 0; i < noteList.size(); i++) {
                if (!noteList.get(i).getDeleted()) {
                    noteList.remove(i);
                }
            }

        sortRecyclerData(noteList);
        mAdapter.notifyDataSetChanged();

        Log.e("TNF", "" + noteList.size());

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trash_note_list, container, false);

//        if (noteList.size() == 0) {
//            view.findViewById(R.id.no_notes).setVisibility(View.VISIBLE);
//        } else {
//            view.findViewById(R.id.no_notes).setVisibility(View.GONE);
//        }

        mAdapter = new NotesAdapter(getActivity(), noteList, this, true);
        // Set the adapter
        if (view.findViewById(R.id.list) instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = view.findViewById(R.id.list);
            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mRecyclerView.setAdapter(mAdapter);
        }
        mAdapter.notifyDataSetChanged();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerNoteTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof NotesAdapter.ViewHolder) {

            final Note restoredNote = noteList.get(viewHolder.getAdapterPosition());
            final int restoredNoteIndex = viewHolder.getAdapterPosition();

            mAdapter.restoreNote(restoredNote, restoredNoteIndex, restoredNote.getNoteId(), mNotesDatabaseReference);
        }
    }

    @Override
    public void onNoteSelected(Note note) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction();
    }
}
