package com.develop.vadim.english.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.github.florent37.expansionpanel.ExpansionHeader;
import com.github.florent37.expansionpanel.ExpansionLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.view.QueryParams;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.pitt.library.fresh.FreshDownloadView;
import com.varunjohn1990.iosdialogs4android.IOSDialog;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import bg.devlabs.transitioner.Transitioner;

public class WordsUserCheckFragment extends Fragment {

    private RecyclerView wordsCategoriesRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialCardView shareMaterialCardView;
    private MaterialCardView deleteMaterialCardView;

    private DatabaseReference databaseReference;
    private DatabaseReference categoryReference;

    private static ArrayList<String> categoryNames;

    private WordsCategoriesRecyclerViewAdapter wordsCategoriesRecyclerViewAdapter;

    private Handler initCategoriesHandler;
    private Handler loadingCategoriesHandler;

    private View viewLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        databaseReference = MainActivity.reference.child("words");
        categoryReference = MainActivity.reference.child("categories");
        databaseReference.keepSynced(true);

        viewLayout = inflater.inflate(R.layout.user_words_check_fragment, container, false);
        return viewLayout;
    }


    @SuppressLint("HandlerLeak")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)   {
        super.onViewCreated(view, savedInstanceState);

        initCategoriesHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                viewLayout.setClickable(true);
                swipeRefreshLayout.setRefreshing(true);
                wordsCategoriesRecyclerViewAdapter = new WordsCategoriesRecyclerViewAdapter(getCategories());
                swipeRefreshLayout.setRefreshing(false);
                wordsCategoriesRecyclerView.setAdapter(wordsCategoriesRecyclerViewAdapter);
            }
        };

        initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());

        wordsCategoriesRecyclerView = view.findViewById(R.id.userWordsCheckRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.userWordsCheckSwipeToRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                viewLayout.setClickable(false);
                initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());
            }
        });



        wordsCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    private ArrayList<String> getCategories() {
        ArrayList<String> categories = new ArrayList<>();
        categories.addAll(((MainActivity)getActivity()).getCategoryNamesList());

        return categories;
    }

    private ArrayList<Word> getAllWords() {
        return ((MainActivity)getActivity()).getWordArrayList();
    }

    private class WordsCategoriesRecyclerViewAdapter extends RecyclerView.Adapter<WordsCategoriesRecyclerViewAdapter.WordsCategoriesRecyclerViewHolder> {

        ArrayList<String> categoryNamesList;
        ArrayList<Word> wordArrayList;
        ArrayList<ArrayList<Word>> wordsInCategoriesArrayList = new ArrayList<>();

        WordsCategoriesRecyclerViewAdapter(ArrayList<String> categoryNamesList) {
            this.categoryNamesList = categoryNamesList;
            wordArrayList = ((MainActivity)getActivity()).getWordArrayList();

            for(int categoriesCounter = 0; categoriesCounter < categoryNamesList.size(); categoriesCounter++) {
                ArrayList<Word> wordsInThisCategoryList = new ArrayList<>();
                for(int wordsCounter = 0; wordsCounter < wordArrayList.size(); wordsCounter++) {
                    if(wordArrayList.get(wordsCounter).getWordCategory().equals(categoryNamesList.get(categoriesCounter))) {
                        wordsInThisCategoryList.add(wordArrayList.get(wordsCounter));
                    }
                }
                wordsInCategoriesArrayList.add(wordsInThisCategoryList);
            }
        }

        @NonNull
        @Override
        public WordsCategoriesRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new WordsCategoriesRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.archived_word_cell, parent, false));
        }

        @Override
        public void onBindViewHolder(WordsCategoriesRecyclerViewHolder holder, final int position) {

            holder.category = categoryNamesList.get(position);
            holder.categoryTextView.setText(categoryNamesList.get(position));
            holder.wordsInThisCategoryArrayList = wordsInCategoriesArrayList.get(position);
            holder.setPosition(position);

            if(wordsInCategoriesArrayList.get(position).size() != 0) {
                for (int wordsInCategoriesCounter = 0; wordsInCategoriesCounter < wordsInCategoriesArrayList.get(position).size(); wordsInCategoriesCounter++) {

                    final int localPosition = position;
                    final int currentWordIndex = wordsInCategoriesCounter;
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.word_in_category_cell, null, false);
                    final TextView wordInCategoryTextView = view.findViewById(R.id.wordInCategoryTextView);

                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        Word word;
                        @Override
                        public boolean onLongClick(View view) {
                            word =  wordsInCategoriesArrayList.get(localPosition).get(currentWordIndex);
                            switch((int) word.getLevel()) {

                                case -2:
                                    {
                                    new IOSDialog.Builder(getContext())
                                            .message(getString(R.string.add_word_from_categoty))
                                            .positiveButtonText(getString(R.string.yes))
                                            .positiveClickListener(new IOSDialog.Listener() {
                                                @Override
                                                public void onClick(final IOSDialog iosDialog) {
                                                    word.setLevel(0);
                                                    MainActivity.reference.child("words")
                                                            .child(wordsInCategoriesArrayList.get(localPosition).get(currentWordIndex).getInd()).child(Word.levelDatabaseKey).setValue(0)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        Toast.makeText(getContext(), "Слово успешно добавлено в цикл изучения", Toast.LENGTH_LONG).show();

                                                                        ((MainActivity)getActivity()).wordArrayList.set((int) word.getIndex(), word);
                                                                        initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());
                                                                    }
                                                                    else if(task.isCanceled()) {
                                                                        Toast.makeText(getContext(), "Произошла неизвестная ошибка, провнрьте подключение к сети", Toast.LENGTH_LONG).show();
                                                                    }

                                                                    iosDialog.dismiss();
                                                                }
                                                            });
                                                }
                                            })
                                            .negativeButtonText(getString(R.string.no))
                                            .negativeClickListener(new IOSDialog.Listener() {
                                                @Override
                                                public void onClick(IOSDialog iosDialog) {
                                                    iosDialog.dismiss();
                                                }
                                            })
                                            .build()
                                            .show();
                                    }
                                    break;
                                case -1:
                                    Toast.makeText(getContext(), "Вы уже изучили это слово", Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Log.d("BIB", String.valueOf(wordsInCategoriesArrayList.get(localPosition).get(currentWordIndex).getLevel()));
                                    Toast.makeText(getContext(), "Вы уже изучаете это слово на данный момент", Toast.LENGTH_LONG).show();
                                    break;
                            }

                            return true; //It must return true just because if it returns true it will not call OnClick method after dropping view
                        }
                    });

                    wordInCategoryTextView.setText(wordsInCategoriesArrayList.get(position).get(wordsInCategoriesCounter).getWordInEnglish());


                    view.setOnClickListener(new View.OnClickListener() {
                        boolean inEnglish = true;

                        @Override
                        public void onClick(final View view) {
                            if (true) { //TODO: remove this fucking if, please
                                Animation animationFrom = new AlphaAnimation(1f, 0f);
                                animationFrom.setDuration(300);
                                animationFrom.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        view.setClickable(false);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        if(inEnglish) {
                                            wordInCategoryTextView.setText(wordsInCategoriesArrayList.get(localPosition).get(currentWordIndex).getWordInRussian());

                                            inEnglish = false;
                                        }
                                        else {
                                            wordInCategoryTextView.setText(wordsInCategoriesArrayList.get(localPosition).get(currentWordIndex).getWordInEnglish());

                                            inEnglish = true;
                                        }

                                        Animation animationTo = new AlphaAnimation(0f, 1f);
                                        animationTo.setDuration(300);

                                        animationTo.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {
                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                view.setClickable(true);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {
                                            }
                                        });

                                        wordInCategoryTextView.startAnimation(animationTo);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }
                                });

                                wordInCategoryTextView.startAnimation(animationFrom);
                            }
                        }
                    });

                    holder.wordInCategoryLinearLayout.addView(view);
                }
            }
            else {
                TextView thisCategoryIsEmpty = new TextView(getContext());
                thisCategoryIsEmpty.setText(getString(R.string.thisCategoryIsEmpty));
                thisCategoryIsEmpty.setTextColor(getResources().getColor(R.color.colorWhite));
                thisCategoryIsEmpty.setGravity(Gravity.CENTER);
                thisCategoryIsEmpty.setTextSize(18f);

                holder.wordInCategoryLinearLayout.addView(thisCategoryIsEmpty);

            }

            holder.learnTextView.setOnClickListener(new TextView.OnClickListener() {
                ArrayList<Word> changingWordsArrayList = new ArrayList<>();

                @Override
                public void onClick(View view) {

                    for(int wordsInThisCategoryCounter = 0; wordsInThisCategoryCounter < wordsInCategoriesArrayList.get(position).size(); wordsInThisCategoryCounter++) {
                        Word word = wordsInCategoriesArrayList.get(position).get(wordsInThisCategoryCounter);

                        if(word.getLevel() == Word.LEVEL_ADDED) {
                            changingWordsArrayList.add(word);
                        }
                    }

                    if(changingWordsArrayList.size() == 0) {
                        Toast.makeText(getContext(), "В данной категории все слова уже в вашем цикле изучения", Toast.LENGTH_LONG).show();
                    }
                    else {
                        new IOSDialog.Builder(getContext())
                                .message("В данной категории " + String.valueOf(changingWordsArrayList.size()) + " слов для добавления. Добавить?")
                                .positiveButtonText(getString(R.string.yes))
                                .positiveClickListener(new IOSDialog.Listener() {
                                    @Override
                                    public void onClick(IOSDialog iosDialog) {
                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            boolean isSuccessful = true;

                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                for(final Word word : changingWordsArrayList) {
                                                    word.setLevel(0);

                                                    if(!isSuccessful) {
                                                        break;
                                                    }

                                                    databaseReference.child(word.getInd()).child(Word.levelDatabaseKey).setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()) {
                                                                ((MainActivity)getActivity()).wordArrayList.set((int) word.getIndex(), word);
                                                            }
                                                            if(task.isCanceled()) {
                                                                Toast.makeText(getContext(), "Произошла ошибка, проверьте поделючение к сети", Toast.LENGTH_LONG).show();
                                                                isSuccessful = false;
                                                            }
                                                        }
                                                    });
                                                }

                                                if(isSuccessful) {
                                                    Toast.makeText(getContext(), "Слова добавлены успешно", Toast.LENGTH_LONG).show();
                                                    initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(getContext(), "Произошла ошибка, проверьте поделючение к сети", Toast.LENGTH_LONG).show();
                                            }
                                        });

                                        iosDialog.dismiss();
                                    }
                                })
                                .negativeButtonText(getString(R.string.no))
                                .negativeClickListener(new IOSDialog.Listener() {
                                    @Override
                                    public void onClick(IOSDialog iosDialog) {
                                        iosDialog.dismiss();
                                    }
                                })
                                .build()
                                .show();
                    }
                }
            });


            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.appear);
            holder.materialCardView.startAnimation(animation);
        }

        @Override
        public int getItemCount() {
            return categoryNamesList.size();
        }

        class WordsCategoriesRecyclerViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView materialCardView;
            MaterialCardView materialCardViewPlaceHolder;
            TextView categoryTextView;
            LinearLayout wordInCategoryLinearLayout;
            LinearLayout linearLayoutF;
            ExpansionHeader expansionHeader;
            ExpansionLayout expansionLayout;
            TextView learnTextView;
            TextView shareTextView;

            ArrayList<Word> wordsInThisCategoryArrayList;

            String category = "u";
            int position;
            int height;

            boolean catchHeightFlag = true;

            WordsCategoriesRecyclerViewHolder(final View itemView) {
                super(itemView);

                wordInCategoryLinearLayout = itemView.findViewById(R.id.expandableRecyclerView);
                materialCardView = itemView.findViewById(R.id.bob);

                learnTextView = itemView.findViewById(R.id.learnWordsTextView);
                shareTextView = itemView.findViewById(R.id.shareWordsTextView);
                materialCardViewPlaceHolder = itemView.findViewById(R.id.categoryCellMaterialCardViewPlaceHolder);
                categoryTextView = itemView.findViewById(R.id.archiveWordInEnglishTextView);
                expansionHeader = itemView.findViewById(R.id.categoryCellExpansionHeader);
                expansionLayout = itemView.findViewById(R.id.categoryCellExpansionLayout);
                linearLayoutF = itemView.findViewById(R.id.bob2);
                expansionHeader.setExpansionLayout(expansionLayout);

                Log.d("POLLY", category);



                learnTextView.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("HandlerLeak")
                    @Override
                    public void onClick(View view) {
                        Handler handler = new Handler() {

                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                            }
                        };
                        learnCategory(handler);
                    }
                });

                shareTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseDynamicLinks
                                .getInstance()
                                .createDynamicLink()

                                .setDomainUriPrefix("https://sharelett.page.link")
                                .setAndroidParameters(
                                        new DynamicLink.AndroidParameters.Builder("com.develop.vadim.english").build()
                                )
                                .buildShortDynamicLink()
                                .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                                    @Override
                                    public void onSuccess(ShortDynamicLink shortDynamicLink) {
                                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                        shareIntent.setType("text/plain");
                                        String shareBody = shortDynamicLink.getShortLink().toString();
                                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                                        startActivity(Intent.createChooser(shareIntent, "Поделиться"));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("TAG", e.toString());
                                    }
                                });
                    }
                });

                expansionHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(expansionLayout.isExpanded()) {
                            expansionLayout.toggle(true);
                            ValueAnimator valueAnimator = ValueAnimator.ofInt(275, height);
                            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int val = (Integer) animation.getAnimatedValue();
                                    ViewGroup.LayoutParams layoutParams = materialCardView.getLayoutParams();
                                    layoutParams.height = val;
                                    materialCardView.setLayoutParams(layoutParams);
                                }
                            });
                            valueAnimator.setDuration(300);
                            valueAnimator.start();
                        }
                        else {
                            expansionLayout.expand(true);
                            ValueAnimator valueAnimator = ValueAnimator.ofInt(materialCardView.getMeasuredHeight(), 275);
                            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    ViewGroup.LayoutParams layoutParams = materialCardView.getLayoutParams();
                                    int val = (Integer) animation.getAnimatedValue();
                                    layoutParams.height = val;
                                    materialCardView.setLayoutParams(layoutParams);
                                }
                            });
                            valueAnimator.setDuration(300);
                            valueAnimator.start();
                        }

                        if(catchHeightFlag) {
                            height = materialCardView.getMeasuredHeight();

                            catchHeightFlag = false;
                        }
                    }
                });

            }

            private void learnCategory(Handler handler) {

            }

            private void setPosition(int position) {
                this.position = position;
            }

        }
    }

    private class RemoveCategoryThread implements Runnable {
        String category;
        boolean isCategoryDeleted = false;

        @SuppressLint("HandlerLeak")
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                swipeRefreshLayout.setRefreshing(false);
            }
        };

        RemoveCategoryThread(String categoryName) {
            this.category = categoryName;

            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        public void run() {
            categoryReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(int categoriesCounter = 0; categoriesCounter < dataSnapshot.getChildrenCount(); categoriesCounter++) {

                        //Removing category from categories list
                        if(Objects.equals(category, dataSnapshot.child(String.valueOf(categoriesCounter)).getValue())) {
                            categoryReference.child(String.valueOf(categoriesCounter)).setValue(dataSnapshot.child(String.valueOf(dataSnapshot.getChildrenCount() - 1)).getValue());
                            categoryReference.child(String.valueOf(dataSnapshot.getChildrenCount() - 1)).removeValue();

                            isCategoryDeleted = true;
                        }
                    }

                    //Replacing removing category
                    if(isCategoryDeleted) {
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(int wordsCounter = 0; wordsCounter < dataSnapshot.getChildrenCount(); wordsCounter++) {
                                    if(Objects.requireNonNull(dataSnapshot.child(String.valueOf(wordsCounter)).child(Word.categoryDatabaseKey).getValue()).toString().equals(category)) {
                                        databaseReference.child(String.valueOf(wordsCounter)).child(Word.categoryDatabaseKey).setValue("default");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }

                    ((MainActivity)getActivity()).updateData(initCategoriesHandler);
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    } //
}
