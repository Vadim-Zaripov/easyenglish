package com.develop.vadim.english.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import bg.devlabs.transitioner.Transitioner;

public class AddNewWordFragment extends Fragment {

    private EditText englishWordEditText;
    private EditText russianWordEditText;
    private EditText categoryEditText;
    private Button addWordToServiceButton;
    private MaterialCardView categoryMaterialCardView;
    private MaterialCardView categoriesChoosingMAterialMaterialCardView;
    private TextView categoryTextView;
    private ProgressBar wordSendingProgressBar;

    private Transitioner transitioner;

    private long ind;

    private DatabaseReference reference;

    @SuppressLint("HandlerLeak")
    private Handler wordSendingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            wordSendingProgressBar.setVisibility(View.INVISIBLE);

            startAppearAnimation();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        reference = MainActivity.reference.child("words");
        reference.keepSynced(true);

        return inflater.inflate(R.layout.add_new_word_layout, container, false);
    }

    @Override
     public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        englishWordEditText = view.findViewById(R.id.editTextEnglish);
        russianWordEditText = view.findViewById(R.id.editTextRussian);
        categoryMaterialCardView = view.findViewById(R.id.categoryChooseCardView);
        categoryTextView = categoryMaterialCardView.findViewById(R.id.addNewWordCategoryTextView);
        categoriesChoosingMAterialMaterialCardView = view.findViewById(R.id.categoriesMaterialCardView);
        addWordToServiceButton = view.findViewById(R.id.button7);
        wordSendingProgressBar = view.findViewById(R.id.spinKit);
        wordSendingProgressBar.setIndeterminateDrawable(new DoubleBounce());

        categoryMaterialCardView.setOnClickListener(new View.OnClickListener() {

            private View newView;
            private RecyclerView choosingCategoryRecyclerView;
            private ArrayList<String> categories = WordsUserCheckFragment.getCategoryNames();

            @Override
            public void onClick(View v) {
                categories.remove(0);

                categoriesChoosingMAterialMaterialCardView.setVisibility(View.VISIBLE);

                choosingCategoryRecyclerView = categoriesChoosingMAterialMaterialCardView.findViewById(R.id.categoriesWhileAddingWordRecyclerView);
                choosingCategoryRecyclerView.setAdapter(new CategoriesRecyclerViewAdapter(categories));
                choosingCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        });
       // categoryEditText = view.findViewById(R.id.editTextCategory);

        addWordToServiceButton.setOnClickListener(new View.OnClickListener() {
            private Word newWord;

            @Override
            public void onClick(View v) {
                if(!englishWordEditText.getText().toString().equals("") && !russianWordEditText.getText().toString().equals("")) {
                   reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ind = dataSnapshot.getChildrenCount();

                            newWord = new Word(ind);
                            newWord.setWordInEnglish(englishWordEditText.getText().toString());
                            newWord.setWordInRussian(russianWordEditText.getText().toString());

                            //if(!categoryEditText.getText().toString().equals("")) {
                               // newWord.setWordCategory(categoryEditText.getText().toString());

                                ////Check and create categories
                                //CategoriesCheck categoriesCheck = new CategoriesCheck(categoryEditText.getText().toString());
                               // new Thread(categoriesCheck).start();
                            //}

                            startDissapearingAnimation(newWord);

                            englishWordEditText.setText("");
                            russianWordEditText.setText("");
                            //categoryEditText.setText("");


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
                else {
                    Toast.makeText(v.getContext(), "Заполни все поля", Toast.LENGTH_LONG).show();
                }


            }

            private void startDissapearingAnimation(final Word word) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.dissapear);
                categoryMaterialCardView.startAnimation(animation);
                russianWordEditText.startAnimation(animation);
                englishWordEditText.startAnimation(animation);
                addWordToServiceButton.startAnimation(animation);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }

                    @SuppressLint("HandlerLeak")
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        categoryMaterialCardView.setVisibility(View.INVISIBLE);
                        russianWordEditText.setVisibility(View.INVISIBLE);
                        englishWordEditText.setVisibility(View.INVISIBLE);
                        addWordToServiceButton.setVisibility(View.INVISIBLE);

                        wordSendingProgressBar.setVisibility(View.VISIBLE);

                        new Thread(new StartWordSendingThread(word)).start();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
            }


        });
    }

    private void startAppearAnimation() {
        categoryMaterialCardView.setVisibility(View.VISIBLE);
        englishWordEditText.setVisibility(View.VISIBLE);
        russianWordEditText.setVisibility(View.VISIBLE);
        addWordToServiceButton.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.appear);
        categoryMaterialCardView.startAnimation(animation);
        russianWordEditText.startAnimation(animation);
        englishWordEditText.startAnimation(animation);
        addWordToServiceButton.startAnimation(animation);
    }


    private class CategoriesCheck implements Runnable {

        String category;

        CategoriesCheck(String category) {
            this.category = category;
        }

        int index;
        boolean isCategoryReal = false;

        @Override
        public void run() {
            MainActivity.reference.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    index = (int) dataSnapshot.getChildrenCount();

                    for(int i = 0; i < dataSnapshot.getChildrenCount(); i++) {
                        if(Objects.equals(dataSnapshot.child(String.valueOf(i)).getValue(), category)) {
                            isCategoryReal = true;
                        }
                    }

                    if(!isCategoryReal) {
                        MainActivity.reference.child("categories").child(String.valueOf(index)).setValue(category);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    private class CategoriesRecyclerViewAdapter extends RecyclerView.Adapter<CategoriesRecyclerViewAdapter.CategoriesRecyclerViewHolder> {
        private ArrayList<String> categories;

        private int[] materialCardsColors = new int[] {
            R.color.LIGHT_GREEN_TRANSPARENT,
            R.color.LIGHT_PURPLE_TRANSPARENT
        };

        private CategoriesRecyclerViewAdapter(ArrayList<String> categories) {
            this.categories = categories;
            categories.add("Добавить новое");
        }

        @NonNull
        @Override
        public CategoriesRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.categories_choosing_cell, parent, false);

            return new CategoriesRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoriesRecyclerViewHolder holder, int position) {
            holder.categoryNameTextView.setText(categories.get(position));
            holder.materialCardView.setCardBackgroundColor(getResources().getColor(R.color.LIGHT_GREEN_TRANSPARENT));
            holder.position = position;
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        private class CategoriesRecyclerViewHolder extends RecyclerView.ViewHolder {
            int position;
            TextView categoryNameTextView;
            MaterialCardView materialCardView;

            CategoriesRecyclerViewHolder(@NonNull View itemView) {
                super(itemView);

                materialCardView = itemView.findViewById(R.id.categoriesChoosingCellCardView);
                categoryNameTextView = itemView.findViewById(R.id.categoriesChoosingCellTextView);

                if(position == getItemCount() - 1) {
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                }
                else {
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            categoriesChoosingMAterialMaterialCardView.setVisibility(View.INVISIBLE);
                            categoryTextView.setText(categoryNameTextView.getText());
                        }
                    });
                }
            }
        }
    }

    private class StartWordSendingThread implements Runnable {
        private Word word;

        StartWordSendingThread(Word word) {
            this.word = word;
        }

        @Override
        public void run() {
            word.sentWordToService();

            try {
                Thread.sleep(2500);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            wordSendingHandler.sendMessage(wordSendingHandler.obtainMessage());
        }
    }
}
