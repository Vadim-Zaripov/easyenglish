package com.develop.vadim.english.Fragments;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.develop.vadim.english.Basic.ChangeWord;
import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WordsArchiveFragment extends Fragment {

    private String ARCHIVE_ACTIVITY_TAG = "Archive activity";

    private DatabaseReference reference = MainActivity.reference.child("words");

    private RecyclerView archivedWordsRecyclerView;

    private ArchiveFragmentRecyclerViewAdapter archiveFragmentRecyclerViewAdapter;

    private Handler initArchivedWordsHandler;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        reference.keepSynced(true);

        return inflater.inflate(R.layout.words_archive_fragment, container, false);
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Log.d(ARCHIVE_ACTIVITY_TAG, "starts");

        initArchivedWordsHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                archiveFragmentRecyclerViewAdapter = new ArchiveFragmentRecyclerViewAdapter(((MainActivity) Objects.requireNonNull(getActivity())).getWordArrayList()); //TODO: Replace to ArchivedWordsArrayList
                archivedWordsRecyclerView.setAdapter(archiveFragmentRecyclerViewAdapter);
            }
        };

        initArchivedWordsHandler.sendMessage(initArchivedWordsHandler.obtainMessage());

        archivedWordsRecyclerView = view.findViewById(R.id.archivedWordsRecyclerView);
        archivedWordsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        initArchivedWordsHandler.sendMessage(initArchivedWordsHandler.obtainMessage());
    }

    private class ArchiveFragmentRecyclerViewAdapter extends RecyclerView.Adapter<ArchiveFragmentRecyclerViewAdapter.ArchiveFragmentViewHolder> {

        private List<Word> archivedWordsList;

        private ArchiveFragmentRecyclerViewAdapter(List<Word> archivedWordsList) {
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

            final int currentPosition = position;
            final Word word = archivedWordsList.get(position);
            holder.wordInEnglishTextView.setText(word.getWordInEnglish());
            holder.position = position;

            holder.wordMaterialCardView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.appear));

            holder.wordMaterialCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent wordDetailsIntent = new Intent(view.getContext(), ChangeWord.class);
                    wordDetailsIntent.putStringArrayListExtra(getString(R.string.categoriesToChangeWordActivity), getCategoriesToWordCheckActivity());
                    Log.d("TAG", String.valueOf(word.getIndex()));
                    wordDetailsIntent.putExtra(getString(R.string.changeWord), archivedWordsList.get(currentPosition));

                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity());

                    startActivity(wordDetailsIntent, activityOptions.toBundle());
                }
            });

        }

        @Override
        public int getItemCount() {
            return archivedWordsList.size();
        }

        class ArchiveFragmentViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView wordMaterialCardView;
            TextView wordInEnglishTextView;
            int position;


            ArchiveFragmentViewHolder(View itemView) {
                super(itemView);
                wordInEnglishTextView = itemView.findViewById(R.id.archiveWordInEnglishTextView);
                wordMaterialCardView = itemView.findViewById(R.id.bob);
            }
        }
    }

    private ArrayList<String> getCategoriesToWordCheckActivity() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Без категрии");
        arrayList.addAll(((MainActivity) Objects.requireNonNull(getActivity())).getCategoryNamesList());
        arrayList.add("Добавить");

        return arrayList;
    }
}
