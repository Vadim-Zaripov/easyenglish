package com.develop.vadim.english.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WordsArchiveFragment extends Fragment {

    private String ARCHIVE_ACTIVITY_TAG = "Archive activity";

    private List<Word> learntWordsList;

    private DatabaseReference reference = MainActivity.reference.child("words");

    private RecyclerView archivedWordsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        reference.keepSynced(true);

        return inflater.inflate(R.layout.words_archive_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        archivedWordsRecyclerView = view.findViewById(R.id.archivedWordsRecyclerView);

        swipeRefreshLayout = view.findViewById(R.id.archivedWordsSwipeToRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        updateArchive();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

    }

    private List<Word> getLearntWordsFromDatabase() {
        final List<Word> learntWordsFromDatabaseList = new ArrayList<>();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(long childrenInDatabaseCounter = 0; childrenInDatabaseCounter < dataSnapshot.getChildrenCount(); childrenInDatabaseCounter++) {
                        Word word = new Word(childrenInDatabaseCounter);
                        word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                        word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.englishDatabaseKey).getValue()).toString());

                        learntWordsFromDatabaseList.add(word);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(ARCHIVE_ACTIVITY_TAG, "Can not connect to database");
            }
        });

        return learntWordsFromDatabaseList;
    }

    private void updateArchive() {
        ArchiveFragmentRecyclerViewAdapter archiveFragmentRecyclerViewAdapter = new ArchiveFragmentRecyclerViewAdapter(getLearntWordsFromDatabase());
        archivedWordsRecyclerView.setLayoutManager(new LinearLayoutManager(archivedWordsRecyclerView.getContext()));
        archivedWordsRecyclerView.setAdapter(archiveFragmentRecyclerViewAdapter);
    }

    private class ArchiveFragmentRecyclerViewAdapter extends RecyclerView.Adapter<ArchiveFragmentRecyclerViewAdapter.ArchiveFragmentViewHolder> {

        private List<Word> archivedWordsList;

        ArchiveFragmentRecyclerViewAdapter(List<Word> archivedWordsList) {
            this.archivedWordsList = archivedWordsList;
        }

        @Override
        public ArchiveFragmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.archived_word_cell, parent, false);
            return new ArchiveFragmentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ArchiveFragmentViewHolder holder, int position) {
            Log.d(ARCHIVE_ACTIVITY_TAG, "Cell has been created");
            Word word = archivedWordsList.get(position);
            holder.wordInEnglishTextView.setText(word.getWordInEnglish());
        }

        @Override
        public int getItemCount() {
            return archivedWordsList.size();
        }

        class ArchiveFragmentViewHolder extends RecyclerView.ViewHolder {
            TextView wordInEnglishTextView;

            ArchiveFragmentViewHolder(View itemView) {
                super(itemView);

                wordInEnglishTextView = itemView.findViewById(R.id.archiveWordInEnglishTextView);
            }
        }
    }
}
