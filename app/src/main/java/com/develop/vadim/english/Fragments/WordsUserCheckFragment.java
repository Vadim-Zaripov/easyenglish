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
import java.util.Objects;

public class WordsUserCheckFragment extends Fragment {

    private RecyclerView wordsCategoriesRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private DatabaseReference databaseReference;
    private DatabaseReference categoryReference;

    private static ArrayList<String> categoryNames;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        databaseReference = MainActivity.reference.child("words");
        categoryReference = MainActivity.reference.child("categories");
        databaseReference.keepSynced(true);

        return inflater.inflate(R.layout.user_words_check_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        new Thread(new InitCategoriesThread()).start();

        wordsCategoriesRecyclerView = view.findViewById(R.id.userWordsCheckRecyclerView);

        swipeRefreshLayout = view.findViewById(R.id.userWordsCheckSwipeToRefreshLayout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        new Thread(new InitCategoriesThread()).start();
                    }
                });
            }
        });

        wordsCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        super.onViewCreated(view, savedInstanceState);
    }

    private class WordsCategoriesRecyclerViewAdapter extends RecyclerView.Adapter<WordsCategoriesRecyclerViewAdapter.WordsCategoriesRecyclerViewHolder> {

        ArrayList<String> categoryNamesList;

        WordsCategoriesRecyclerViewAdapter(ArrayList<String> categoryNamesList) {
            this.categoryNamesList = categoryNamesList;
        }

        @NonNull
        @Override
        public WordsCategoriesRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WordsCategoriesRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.archived_word_cell, parent, false));
        }

        @Override
        public void onBindViewHolder(WordsCategoriesRecyclerViewHolder holder, int position) {
            holder.category = categoryNamesList.get(position);
            holder.categoryName.setText(categoryNamesList.get(position));
        }

        @Override
        public int getItemCount() {
            return categoryNamesList.size();
        }

        class WordsCategoriesRecyclerViewHolder extends RecyclerView.ViewHolder {
            TextView categoryName;
            String category;

            WordsCategoriesRecyclerViewHolder(final View itemView) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swipeRefreshLayout.setRefreshing(true);
                        StartCheckingThread startCheckingThread = new StartCheckingThread(category);
                        new Thread(startCheckingThread).start();
                    }
                });
                categoryName = itemView.findViewById(R.id.archiveWordInEnglishTextView);
            }
        }
    }

    private class StartCheckingThread implements Runnable {

        private String category;
        private ArrayList<Word> neededWordList = new ArrayList<>();

        StartCheckingThread(String category) {
            this.category = category;
        }

        @SuppressLint("HandlerLeak")
        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Log.d("HANDLE", "Work");

                swipeRefreshLayout.setRefreshing(false);
                callCheckService();
            }
        };

        @Override
        public void run() {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(long childrenInDatabaseCounter = 0; childrenInDatabaseCounter < dataSnapshot.getChildrenCount(); childrenInDatabaseCounter++) {
                        Word word = new Word(childrenInDatabaseCounter);
                        if(Objects.equals(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.categoryDatabaseKey).getValue(), category)) {

                            //TODO: Create normal check
                            word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                            word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.russianDatabaseKey).getValue()).toString());

                            neededWordList.add(word);
                        }
                    }

                    try {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.sendMessage(handler.obtainMessage());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }

        private void callCheckService() {
            WordCheckDialogFragment wordCheckDialogFragment = new WordCheckDialogFragment();

            wordCheckDialogFragment.setArguments(createBundle());
            wordCheckDialogFragment.show(Objects.requireNonNull(getActivity()).getFragmentManager(), "WordCheckDialogFragment Tag");

        }

        private Bundle createBundle() {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(getString(R.string.parcelableWordKey), neededWordList);
            bundle.putBoolean(getString(R.string.word_check_flag), true);

            return bundle;
        }
    }

    private class InitCategoriesThread implements Runnable {
        InitCategoriesThread() {
            categoryNames = new ArrayList<>();
            categoryNames.add("Все слова");
        }

        @SuppressLint("HandlerLeak")
        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                swipeRefreshLayout.setRefreshing(false);
                updateRecyclerView();
            }
        };

        @Override
        public void run() {

            categoryReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(int categoryReferenceChildrenCounter = 0 ; categoryReferenceChildrenCounter < dataSnapshot.getChildrenCount(); categoryReferenceChildrenCounter++) {
                        categoryNames.add(String.valueOf(dataSnapshot.child(String.valueOf(categoryReferenceChildrenCounter)).getValue()));
                    }

                    handler.sendMessage(handler.obtainMessage());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }

        private void updateRecyclerView() {
            wordsCategoriesRecyclerView.setAdapter(new WordsCategoriesRecyclerViewAdapter(categoryNames));
        }
    }

    public static ArrayList<String> getCategoryNames() {

        return categoryNames;
    }
}
