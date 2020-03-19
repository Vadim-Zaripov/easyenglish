package com.develop.vadim.english.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Message;
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
        archivedWordsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        new Thread(new InitArchivedWordsThread()).start();

        swipeRefreshLayout = view.findViewById(R.id.archivedWordsSwipeToRefreshLayout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new Thread(new InitArchivedWordsThread()).start();
            }
        });
    }

    private class ArchiveFragmentRecyclerViewAdapter extends RecyclerView.Adapter<ArchiveFragmentRecyclerViewAdapter.ArchiveFragmentViewHolder> {

        private List<Word> archivedWordsList;

        ArchiveFragmentRecyclerViewAdapter(List<Word> archivedWordsList) {
            this.archivedWordsList = archivedWordsList;
        }

        @NonNull
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

    private class InitArchivedWordsThread implements Runnable {
        ArrayList<Word> learntWordsFromDatabaseList = new ArrayList<>();

        @SuppressLint("HandlerLeak")
        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                swipeRefreshLayout.setRefreshing(false);
                archivedWordsRecyclerView.setAdapter(new ArchiveFragmentRecyclerViewAdapter(learntWordsFromDatabaseList));
            }
        };
        
        @Override
        public void run() {
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(long childrenInDatabaseCounter = 0; childrenInDatabaseCounter < dataSnapshot.getChildrenCount(); childrenInDatabaseCounter++) {
                        if(Objects.equals(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.levelDatabaseKey).getValue(), 0)) {
                            Word word = new Word(childrenInDatabaseCounter);
                            word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                            word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.englishDatabaseKey).getValue()).toString());

                            learntWordsFromDatabaseList.add(word);
                        }
                    }

                    handler.sendMessage(handler.obtainMessage());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(ARCHIVE_ACTIVITY_TAG, "Can not connect to database");
                }
            });
        }
    }
}
