package com.develop.vadim.english.Fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.vadim.english.Basic.AtheneDialog;
import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.github.chengang.library.TickView;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.varunjohn1990.iosdialogs4android.IOSDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import bg.devlabs.transitioner.Transitioner;

import static android.content.Context.ALARM_SERVICE;

public class AddNewWordFragment extends Fragment implements UpdateDataListener {

    private View rootView;

    private boolean isRecyclerAdapted = false;

    private EditText englishWordEditText;
    private EditText russianWordEditText;
    private ImageView addWordToServiceImageView;
    private MaterialCardView categoryMaterialCardView;
    private MaterialCardView categoriesChoosingMaterialMaterialCardView;
    private TextView categoryTextView;
    private TextView headerTextView;
    private TextView addWordToServiceTextView;
    private TextView categoriesTextView;
    private TextView addNewCategoryTextView;
    private ProgressBar wordSendingProgressBar;
    private RecyclerView choosingCategoryRecyclerView;
    private MaterialCardView categoryMaterialCardViewHolder;
    private TickView tickView;

    public final static int NEW_CATEGORY_HAS_BEEN_ADDED = 5;

    private Transitioner transitioner;

    private long ind;

    private DatabaseReference reference;

    @SuppressLint("HandlerLeak")
    private Handler wordSendingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what) {
                case NEW_CATEGORY_HAS_BEEN_ADDED:
                    Log.d("BOB", "not new word");
                    break;
                case 12:
                    Log.d("BOB", "new category");
                    break;
            }

            wordSendingProgressBar.setVisibility(View.INVISIBLE);
            tickView.setVisibility(View.VISIBLE);
            tickView.toggle();
            tickView.addAnimatorListener(new TickView.TickAnimatorListener() {
                @Override
                public void onAnimationStart(TickView tickView) { }

                @Override
                public void onAnimationEnd(final TickView tickView) {
                    AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
                    alphaAnimation.setDuration(200);
                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tickView.setVisibility(View.INVISIBLE);
                            tickView.toggle();
                            startAppearAnimation();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });

                    tickView.startAnimation(alphaAnimation);
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        reference = MainActivity.reference.child("words");
        rootView = inflater.inflate(R.layout.add_new_word_layout, container, false);

        return rootView;
    }

    @Override
     public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        englishWordEditText = view.findViewById(R.id.editTextEnglish);
        russianWordEditText = view.findViewById(R.id.editTextRussian);
        categoryMaterialCardView = view.findViewById(R.id.categoryChooseCardView);
        categoryTextView = categoryMaterialCardView.findViewById(R.id.addNewWordCategoryTextView);
        headerTextView = view.findViewById(R.id.headerTextView);
        categoriesTextView = view.findViewById(R.id.categoriesTextViewTitle);
        addWordToServiceTextView = view.findViewById(R.id.addWordToServiceTextView);
        categoriesChoosingMaterialMaterialCardView = view.findViewById(R.id.categoriesMaterialCardView);
        addWordToServiceImageView = view.findViewById(R.id.addWordToServiceImageView);
        wordSendingProgressBar = view.findViewById(R.id.spinKit);
        addNewCategoryTextView = view.findViewById(R.id.addNewWordCategoryTextViewButton);
        categoryMaterialCardViewHolder = view.findViewById(R.id.categoryChooseCardViewHolder);
        tickView = view.findViewById(R.id.tickViewAccent);


        wordSendingProgressBar.setIndeterminateDrawable(new DoubleBounce());

        categoryTextView.setText("Без категории");

        categoryMaterialCardView.setOnClickListener(new View.OnClickListener() {
            private Handler handler;

            @SuppressLint("HandlerLeak")
            @Override
            public void onClick(View v) {

                getView().setClickable(true);

                if(!isRecyclerAdapted) {
                    isRecyclerAdapted = true;
                    adaptRecyclerView();
                }

                transitioner = new Transitioner(categoryMaterialCardView, categoriesChoosingMaterialMaterialCardView);
                transitioner.animateTo(1f, (long) 400, new AccelerateDecelerateInterpolator());

                categoryMaterialCardView.setCardBackgroundColor(Color.WHITE);
                categoryTextView.setVisibility(View.INVISIBLE);
                addNewCategoryTextView.setVisibility(View.VISIBLE);
                categoryMaterialCardView.setClickable(false);

                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
                alphaAnimation.setDuration(600);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        addNewCategoryTextView.setClickable(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });

                addNewCategoryTextView.startAnimation(alphaAnimation);

                addWordToServiceImageView.animate().alphaBy(1).alpha(0).setDuration(300).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(420);
                        }
                        catch(InterruptedException e) {
                            e.printStackTrace();
                        }

                        handler.sendMessage(handler.obtainMessage());
                    }
                }).start();

                handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        choosingCategoryRecyclerView = categoryMaterialCardView.findViewById(R.id.categoriesWhileAddingWordRecyclerView);
                        choosingCategoryRecyclerView.setVisibility(View.VISIBLE);
                        choosingCategoryRecyclerView.setAdapter(new CategoriesRecyclerViewAdapter(getCategories()));
                        choosingCategoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    }
                };
            }
        });

        addWordToServiceImageView.setOnClickListener(new View.OnClickListener() {
            Word newWord;

            @Override
            public void onClick(View v) {
                if(!englishWordEditText.getText().toString().equals("") && !russianWordEditText.getText().toString().equals("")) {
                   reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ind = dataSnapshot.getChildrenCount();

                            newWord = new Word(ind);
                            newWord.setWordInEnglish(englishWordEditText.getText().toString().trim().replace("\n", ""));
                            newWord.setWordInRussian(russianWordEditText.getText().toString().trim().replace("\n", ""));

                            startDisappearingAnimation(newWord, categoryTextView.getText().toString().trim().toLowerCase());


                            englishWordEditText.setText("");
                            russianWordEditText.setText("");
                            categoryTextView.setText("Без категории");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            if(databaseError.getCode() == DatabaseError.DISCONNECTED ||
                                    databaseError.getCode() == DatabaseError.NETWORK_ERROR) {
                                AtheneDialog atheneDialog = new AtheneDialog(getContext(), AtheneDialog.SIMPLE_MESSAGE_TYPE);
                                atheneDialog.setMessageText(getString(R.string.no_internet_error));
                                atheneDialog.setPositiveClickListener(new TextView.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        atheneDialog.dismiss();
                                    }
                                });
                                atheneDialog.show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(v.getContext(), "Заполните все поля!", Toast.LENGTH_LONG).show();
                }
            }

            private void startDisappearingAnimation(final Word word, final String category) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.dissapear);
                categoryMaterialCardView.startAnimation(animation);
                russianWordEditText.startAnimation(animation);
                englishWordEditText.startAnimation(animation);
                addWordToServiceImageView.startAnimation(animation);
                headerTextView.startAnimation(animation);
                categoriesTextView.startAnimation(animation);
                addWordToServiceTextView.startAnimation(animation);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }

                    @SuppressLint("HandlerLeak")
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        categoryMaterialCardView.setVisibility(View.INVISIBLE);
                        russianWordEditText.setVisibility(View.INVISIBLE);
                        englishWordEditText.setVisibility(View.INVISIBLE);
                        addWordToServiceImageView.setVisibility(View.INVISIBLE);
                        headerTextView.setVisibility(View.INVISIBLE);
                        categoriesTextView.setVisibility(View.INVISIBLE);
                        addWordToServiceTextView.setVisibility(View.INVISIBLE);

                        wordSendingProgressBar.setVisibility(View.VISIBLE);

                        Log.d("BOB", category + "1");
                        new Thread(new StartWordSendingThread(word, category)).start();

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
            }
        });


    }

    private void adaptRecyclerView() {

        int categoriesMaterialCardView = rootView
                .findViewById(R.id.add_new_word_recycler)
                .getMeasuredHeight();

        int createCategoryTextViewHeight = rootView
                .findViewById(R.id.add_new_word_create_category_textview)
                .getMeasuredHeight();

        ViewGroup.LayoutParams layoutParams = rootView
                .findViewById(R.id.add_new_word_recycler)
                .getLayoutParams();
        layoutParams.height = categoriesMaterialCardView - createCategoryTextViewHeight;

        rootView
                .findViewById(R.id.add_new_word_recycler)
                .setLayoutParams(layoutParams);
    }

    private ArrayList getCategories() {
        ArrayList<String> categoriesList = new ArrayList<>();
        categoriesList.add("Без категории");
        categoriesList.addAll(((MainActivity)getActivity()).getCategoryNamesList());

        return categoriesList;
    }


    private void startAppearAnimation() {
        categoryMaterialCardView.setVisibility(View.VISIBLE);
        englishWordEditText.setVisibility(View.VISIBLE);
        russianWordEditText.setVisibility(View.VISIBLE);
        addWordToServiceImageView.setVisibility(View.VISIBLE);
        headerTextView.setVisibility(View.VISIBLE);
        categoriesTextView.setVisibility(View.VISIBLE);
        addWordToServiceTextView.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.appear);
        categoryMaterialCardView.startAnimation(animation);
        russianWordEditText.startAnimation(animation);
        englishWordEditText.startAnimation(animation);
        addWordToServiceImageView.startAnimation(animation);
        headerTextView.startAnimation(animation);
        categoriesTextView.startAnimation(animation);
        addWordToServiceTextView.startAnimation(animation);
    }


    @Override
    public void onDataChange() {

    }

    private class CategoriesRecyclerViewAdapter extends RecyclerView.Adapter<CategoriesRecyclerViewAdapter.CategoriesRecyclerViewHolder> {
        private ArrayList<String> categories;

        private Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.appear);

        private int[] materialCardsColors = new int[] {
            R.color.blue,
            R.color.lightBlue,
            R.color.lightPurple,
            R.color.middlePurple
        };

        private CategoriesRecyclerViewAdapter(ArrayList<String> categories) {
            this.categories = categories;
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
            holder.materialCardView.setCardBackgroundColor(getResources().getColor(materialCardsColors[new Random().nextInt(materialCardsColors.length)]));
            holder.position = position;

            holder.materialCardView.startAnimation(animation);
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

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startClosingAnimation(categoryNameTextView.getText().toString());
                    }
                });

                addNewCategoryTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startClosingAnimation("Без категории");
                        callChooseCategoryDialog();
                    }
                });

                getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startClosingAnimation("Без категории");
                    }
                });
            }

            private void startClosingAnimation(String category) {
                getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { }
                });
                Transitioner transitioner = new Transitioner(categoryMaterialCardView, categoryMaterialCardViewHolder);
                transitioner.animateTo(1f, (long) 400, new AccelerateDecelerateInterpolator());
                categoryMaterialCardView.setClickable(true);
                categoryMaterialCardView.setCardBackgroundColor(getResources().getColor(R.color.WHITE_TRANSPARENT));
                choosingCategoryRecyclerView.setVisibility(View.INVISIBLE);

                categoryTextView.setText(category);

                categoryTextView.setVisibility(View.VISIBLE);
                categoryTextView.startAnimation(animation);

                addWordToServiceImageView.animate().alphaBy(0).alpha(1).setDuration(420).start();

                AlphaAnimation alphaAnimation  = new AlphaAnimation(1f, 0f);
                alphaAnimation.setDuration(400);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        addNewCategoryTextView.setClickable(false);
                        choosingCategoryRecyclerView.setClickable(false);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        addNewCategoryTextView.setVisibility(View.INVISIBLE);
                        choosingCategoryRecyclerView.setClickable(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });

                addNewCategoryTextView.startAnimation(alphaAnimation);
            }

            private void callChooseCategoryDialog() {
                AtheneDialog atheneDialog = new AtheneDialog(getContext(), AtheneDialog.EDIT_TEXT_TWO_OPTIONS_TYPE);
                atheneDialog.setPositiveClickListener(new TextView.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!atheneDialog.getUserAnswerEditText().getText().toString().equals("")) {
                            String category = atheneDialog.getUserAnswerEditText().getText().toString().trim().toLowerCase();
                            if(!categories.contains(category)) {
                                ((MainActivity)getActivity()).categoryNames.add(category);
                            }

                            categoryTextView.setText(atheneDialog.getUserAnswerEditText().getText());
                        }

                        atheneDialog.dismiss();
                    }
                });

                atheneDialog.setNegativeClickListener(new TextView.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        atheneDialog.dismiss();
                    }
                });

                atheneDialog.show();
            }
        }
    }

    private class StartWordSendingThread implements Runnable {
        Word word;
        String category;

        StartWordSendingThread(Word word, String category) {
            this.word = word;
            this.category = category;
        }

        @Override
        public void run() {
            word.setWordCategory(category);

            ((MainActivity)getActivity()).wordArrayList.add(word);

            if(getCategories().contains(category.toLowerCase()) || category.toLowerCase().equals("без категории") || category.toLowerCase().equals("добавить")) {
               word.sentWordToService();
               wordSendingHandler.sendEmptyMessage(MainActivity.CATEGORIES_LOAD_END);
            }
            else {
                MainActivity.reference.child("categories").child(String.valueOf(((MainActivity)getActivity()).categoryNames.size())).setValue(category);

                ((MainActivity)getActivity()).categoryNames.add(category);

                word.setWordCategory(category);
                word.sentWordToService();
                wordSendingHandler.sendEmptyMessage(NEW_CATEGORY_HAS_BEEN_ADDED);
            }

            ((MainActivity)getActivity()).callFragmentContentUpdate(MainActivity.CATEGORIES_FRAGMENT_KEY);
        }
    }
}
