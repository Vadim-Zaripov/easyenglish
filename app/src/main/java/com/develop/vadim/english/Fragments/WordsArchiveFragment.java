package com.develop.vadim.english.Fragments;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
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
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.vadim.english.Basic.ChangeWord;
import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.google.android.material.card.MaterialCardView;
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
    private SearchView archivedWordsSearchView;

    private ArchiveFragmentRecyclerViewAdapter archiveFragmentRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        reference.keepSynced(true);

        return inflater.inflate(R.layout.words_archive_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(ARCHIVE_ACTIVITY_TAG, "starts");

        archivedWordsRecyclerView = view.findViewById(R.id.archivedWordsRecyclerView);
        archivedWordsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        archivedWordsSearchView = view.findViewById(R.id.archivedWordsSearchView);
        archivedWordsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                archiveFragmentRecyclerViewAdapter.getFilter().filter(newText);

                return false;
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.archivedWordsSwipeToRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new Thread(new InitArchivedWordsThread()).start();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        swipeRefreshLayout.setRefreshing(true);
        new Thread(new InitArchivedWordsThread()).start();
    }

    private class ArchiveFragmentRecyclerViewAdapter extends RecyclerView.Adapter<ArchiveFragmentRecyclerViewAdapter.ArchiveFragmentViewHolder> implements Filterable {

        private List<Word> archivedWordsList;
        private List<Word> archivedWordsListFull;

        private Filter archivedWordsFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Word> filteredWordsList = new ArrayList<>();

                if(constraint == null || constraint.length() == 0) {
                    filteredWordsList.addAll(archivedWordsListFull);
                }
                else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for(Word item : archivedWordsListFull) {
                        if(item.getWordInEnglish().toLowerCase().contains(filterPattern)) {
                            filteredWordsList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredWordsList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                archivedWordsList.clear();
                archivedWordsList.addAll((List<Word>) results.values);

                notifyDataSetChanged();
            }
        };

        private ArchiveFragmentRecyclerViewAdapter(List<Word> archivedWordsList) {
            this.archivedWordsList = archivedWordsList;
            this.archivedWordsListFull = new ArrayList<>(archivedWordsList);
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
            holder.position = position;

            holder.wordMaterialCardView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.appear));

        }

        @Override
        public int getItemCount() {
            return archivedWordsList.size();
        }

        @Override
        public Filter getFilter() {
            return archivedWordsFilter;
        }

        class ArchiveFragmentViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView wordMaterialCardView;
            TextView wordInEnglishTextView;
            int position;

            ArchiveFragmentViewHolder(View itemView) {
                super(itemView);
                wordInEnglishTextView = itemView.findViewById(R.id.archiveWordInEnglishTextView);
                wordMaterialCardView = itemView.findViewById(R.id.bob);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent wordDetailsIntent = new Intent(v.getContext(), ChangeWord.class);
                        wordDetailsIntent.putExtra(getString(R.string.changeWord), archivedWordsListFull.get(position));

                        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity());

                        startActivity(wordDetailsIntent, activityOptions.toBundle());
                    }
                });
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

                archiveFragmentRecyclerViewAdapter = new ArchiveFragmentRecyclerViewAdapter(learntWordsFromDatabaseList);

                swipeRefreshLayout.setRefreshing(false);
                archivedWordsRecyclerView.setAdapter(archiveFragmentRecyclerViewAdapter);
            }
        };
        
        @Override
        public void run() {
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(long childrenInDatabaseCounter = 0; childrenInDatabaseCounter < dataSnapshot.getChildrenCount(); childrenInDatabaseCounter++) {
                        Word word = new Word(childrenInDatabaseCounter);
                        word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                        word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                        word.setWordCategory(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.categoryDatabaseKey).getValue()).toString());

                        learntWordsFromDatabaseList.add(word);
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
