package com.develop.vadim.english.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.gson.Gson;

import java.lang.invoke.LambdaConversionException;
import java.util.ArrayList;
import java.util.Objects;

public class WordsUserCheckFragment extends Fragment {

    RecyclerView wordsCategoriesRecyclerView;
    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<String> categoryNames = new ArrayList<>();

    private DatabaseReference databaseReference;
    private DatabaseReference categoryReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        databaseReference = MainActivity.reference.child("words");
        categoryReference = MainActivity.reference.child("categories");
        categoryNames = initCategoriesNames();
        databaseReference.keepSynced(true);
        
        return inflater.inflate(R.layout.user_words_check_fragment, container, false);
    }

    private ArrayList<String> initCategoriesNames() {
        final ArrayList<String> categoryNames = new ArrayList<>();

        categoryNames.add("Все слова");

        categoryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(int i = 0 ; i < dataSnapshot.getChildrenCount(); i++) {
                    categoryNames.add(String.valueOf(dataSnapshot.child(String.valueOf(i)).getValue()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return categoryNames;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        wordsCategoriesRecyclerView = view.findViewById(R.id.userWordsCheckRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.userWordsCheckSwipeToRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        updateRecyclerView();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        wordsCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        updateRecyclerView();

        super.onViewCreated(view, savedInstanceState);
    }

    private void updateRecyclerView() {
        wordsCategoriesRecyclerView.setAdapter(new WordsCategoriesRecyclerViewAdapter(categoryNames));
    }

    class WordsCategoriesRecyclerViewAdapter extends RecyclerView.Adapter<WordsCategoriesRecyclerViewAdapter.WordsCategoriesRecyclerViewHolder> {

        ArrayList<String> categoryNamesList;

        WordsCategoriesRecyclerViewAdapter(ArrayList<String> categoryNamesList) {
            this.categoryNamesList = categoryNamesList;
        }

        @Override
        public WordsCategoriesRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WordsCategoriesRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.archived_word_cell, parent, false));
        }

        @Override
        public void onBindViewHolder(WordsCategoriesRecyclerViewHolder holder, int position) {
            holder.categoryName.setText(categoryNames.get(position));
        }

        @Override
        public int getItemCount() {
            return categoryNamesList.size();
        }

        class WordsCategoriesRecyclerViewHolder extends RecyclerView.ViewHolder {
            TextView categoryName;

            WordsCategoriesRecyclerViewHolder(final View itemView) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callCheckService((byte) 0, categoryName.getText().toString());
                    }
                });
                categoryName = itemView.findViewById(R.id.archiveWordInEnglishTextView);
            }
        }
    }

    private void callCheckService(byte stage, String categoryName) {
        WordCheckFragment wordCheckFragment = new WordCheckFragment();

        Bundle stageBundle = new Bundle();
        stageBundle.putByte(getString(R.string.words_check_stage), stage);
        Bundle categoryNameBundle = new Bundle();
        categoryNameBundle.putString(getString(R.string.words_check_category_name), categoryName);

        wordCheckFragment.setArguments(stageBundle);
        wordCheckFragment.setArguments(categoryNameBundle);

        saveData(createNeededWordsList(categoryName));
        wordCheckFragment.show(Objects.requireNonNull(getActivity()).getFragmentManager(), "WordCheckFragment Tag");
    }

    private void saveData(ArrayList<Word> neededWordsList) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("Shared preferences for User Words Check", Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(neededWordsList);
        editor.putString(getString(R.string.service_saved_indexes_key), json);
        editor.apply();
    }

    private ArrayList<Word> createNeededWordsList(final String categoryName) {
        final ArrayList<Word> words = new ArrayList<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for(long childrenInDatabaseCounter = 0; childrenInDatabaseCounter < dataSnapshot.getChildrenCount(); childrenInDatabaseCounter++) {
                   Word word = new Word(childrenInDatabaseCounter);

                   if(categoryName.equals("no category")) {
                       word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                       word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                   }
                   else if(String.valueOf(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).getValue()).equals(categoryName)) {
                       word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                       word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.russianDatabaseKey).getValue()).toString());
                   }

                   words.add(word);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return words;
    }
}
