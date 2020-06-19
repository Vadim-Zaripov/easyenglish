package com.develop.vadim.english.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.develop.vadim.english.Basic.AtheneDialog;
import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.Basic.Word;
import com.develop.vadim.english.R;
import com.develop.vadim.english.utils.Utils;
import com.github.florent37.expansionpanel.ExpansionHeader;
import com.github.florent37.expansionpanel.ExpansionLayout;
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class CategoriesFragment extends Fragment implements UpdateDataListener {

    private LinearLayout wordsCategoriesRecyclerView;
    private TextView emptyCategoriesTextView;

    private DatabaseReference databaseReference;

    private Handler initCategoriesHandler;

    private View viewLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        databaseReference = MainActivity.reference.child("words");

        viewLayout = inflater.inflate(R.layout.fragment_categories_list, container, false);
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

                ArrayList<String> categories = ((MainActivity)getActivity()).getCategoryNamesList();

                if(categories.size() == 0) {
                    emptyCategoriesTextView.setVisibility(View.VISIBLE);
                }
                else {
                    emptyCategoriesTextView.setVisibility(View.INVISIBLE);

                    viewLayout.setClickable(true);
                    CategoryListView categoryListView = new CategoryListView(wordsCategoriesRecyclerView, categories);
                    categoryListView.create();
                }
            }
        };

        initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());

        wordsCategoriesRecyclerView = view.findViewById(R.id.userWordsCheckRecyclerView);
        emptyCategoriesTextView = view.findViewById(R.id.emptyContainerTextView);

//        wordsCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    @Override
    public void onDataChange() {
        initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());
    }

    private class CategoryListView {
        private LinearLayout linearLayout;

        private ArrayList<String> categoriesArrayList;
        private ArrayList<Word> wordArrayList;
        private ArrayList<ArrayList<Word>> wordsInCategoriesArrayList = new ArrayList<>();

        private ExpansionLayoutCollection expansionLayoutCollection = new ExpansionLayoutCollection();

        public CategoryListView(LinearLayout linearLayout, ArrayList<String> categoriesArrayList) {
            this.linearLayout = linearLayout;
            this.categoriesArrayList = categoriesArrayList;
            wordArrayList = ((MainActivity)getActivity()).getWordArrayList();

            for(int categoriesCounter = 0; categoriesCounter < categoriesArrayList.size(); categoriesCounter++) {
                ArrayList<Word> wordsInThisCategoryList = new ArrayList<>();
                for(int wordsCounter = 0; wordsCounter < wordArrayList.size(); wordsCounter++) {
                    if(wordArrayList.get(wordsCounter).getWordCategory().equals(categoriesArrayList.get(categoriesCounter))) {
                        wordsInThisCategoryList.add(wordArrayList.get(wordsCounter));
                    }
                }
                wordsInCategoriesArrayList.add(wordsInThisCategoryList);
            }

            // expansionLayoutCollection.openOnlyOne(true); //TODO: Remove if it is necessary
        }

        void create() {
            linearLayout.removeAllViews();
            for(int categoryViewCounter = 0; categoryViewCounter < categoriesArrayList.size(); categoryViewCounter++) {
                final int currentPosition = categoryViewCounter;

               View categoryView = inflateCategoryView();

               RecyclerView wordsInCurrentCategoryRecyclerView = categoryView.findViewById(R.id.expandableRecyclerView);
               wordsInCurrentCategoryRecyclerView.setAdapter(new WordsInCategoriesRecyclerViewAdapter(wordsInCategoriesArrayList.get(currentPosition)));
               wordsInCurrentCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(linearLayout.getContext()));

               TextView textView = categoryView.findViewById(R.id.archiveWordInEnglishTextView);
               textView.setText(categoriesArrayList.get(currentPosition));

               categoryView.findViewById(R.id.learnWordsTextView).setOnTouchListener(Utils.loginTouchListener);
               categoryView.findViewById(R.id.learnWordsTextView).setOnClickListener(getLearnTextViewOnClickListener(currentPosition));

               categoryView.findViewById(R.id.shareWordsTextView).setOnTouchListener(Utils.loginTouchListener);
               categoryView.findViewById(R.id.shareWordsTextView).setOnClickListener(getShareTextViewOnClickListener(currentPosition));

               categoryView.findViewById(R.id.categoryCellMaterialCardView).setOnClickListener(getExpansionHeaderOnClickListener(categoryView.findViewById(R.id.categoryCellExpansionLayout)));
               categoryView.findViewById(R.id.categoryCellMaterialCardView).setOnLongClickListener(getExpansionHeaderOnLongClickListener(currentPosition));

               expansionLayoutCollection.add(categoryView.findViewById(R.id.categoryCellExpansionLayout));

               linearLayout.addView(categoryView);
           }
        }

        private View inflateCategoryView() {
            return getLayoutInflater().inflate(R.layout.cell_category, linearLayout, false);
        }

        private View.OnLongClickListener getExpansionHeaderOnLongClickListener(int currentPosition) {
            return new View.OnLongClickListener() {
                    @SuppressLint("HandlerLeak")
                    @Override
                    public boolean onLongClick(View view) {
                        AtheneDialog atheneDialog = new AtheneDialog(linearLayout.getContext(), AtheneDialog.TWO_OPTIONS_TYPE);
                        atheneDialog.setMessageText(getString(R.string.deleteCategoryMessage));
                        atheneDialog.setPositiveText(getString(R.string.yes));
                        atheneDialog.setPositiveClickListener(new TextView.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                view.setClickable(false);

                                Handler handler = new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);

                                        initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());
                                        linearLayout.removeViewAt(currentPosition);
                                        atheneDialog.dismiss();
                                    }
                                };

                                DatabaseReference reference = MainActivity.reference.child("categories");
                                reference.child(String.valueOf(currentPosition))
                                        .setValue(categoriesArrayList.get(categoriesArrayList.size() - 1))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                reference.child(String.valueOf(categoriesArrayList.size() - 1)).removeValue();

                                                for (int wordsCounter = 0; wordsCounter < wordArrayList.size(); wordsCounter++) {
                                                    if (wordArrayList.get(wordsCounter).getWordCategory().equals(categoriesArrayList.get(currentPosition))) {
                                                        wordArrayList.get(wordsCounter).setWordCategory("Без категории");

                                                        databaseReference.child(String.valueOf(wordArrayList.get(wordsCounter).getInd())).child(Word.categoryDatabaseKey).setValue("Без категории");
                                                    }
                                                }

                                                ((MainActivity) getActivity()).categoryNames.remove(currentPosition);


                                                handler.sendMessage(handler.obtainMessage());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                atheneDialog.dismiss();

                                                AtheneDialog exceptionDialog = new AtheneDialog(getContext(), AtheneDialog.SIMPLE_MESSAGE_TYPE);
                                                exceptionDialog.setMessageText(getString(R.string.no_internet_error));
                                                exceptionDialog.setPositiveClickListener(new TextView.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        atheneDialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                            }
                        });
                        atheneDialog.setNegativeText(getString(R.string.no));
                        atheneDialog.setNegativeClickListener(new TextView.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                atheneDialog.dismiss();
                            }
                        });

                        atheneDialog.show();

                        return true;
                    }
            };
        }

        private View.OnClickListener getExpansionHeaderOnClickListener(ExpansionLayout expansionLayout) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: Fix Animation
                    
                    expansionLayout.toggle(true);
                    if(expansionLayout.isExpanded()) {
                        LinearLayout categoryActionsLinearLayout =
                                view.findViewById(R.id.category_actions_linearlayout);

                        categoryActionsLinearLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        final int targetHeight = categoryActionsLinearLayout.getMeasuredHeight();
                        categoryActionsLinearLayout.getLayoutParams().height = 0;
                        categoryActionsLinearLayout.setVisibility(View.VISIBLE);

                        ValueAnimator anim =
                                ValueAnimator.ofInt(categoryActionsLinearLayout.getMeasuredHeight(), targetHeight);
                        anim.setInterpolator(new AccelerateInterpolator());
                        anim.setDuration(250);
                        anim.addUpdateListener(animation -> {
                            ViewGroup.LayoutParams layoutParams = categoryActionsLinearLayout.getLayoutParams();
                            layoutParams.height = (int) (targetHeight * animation.getAnimatedFraction());
                            categoryActionsLinearLayout.setLayoutParams(layoutParams);
                        });
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);

                                view.setClickable(false);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                ViewGroup.LayoutParams layoutParams = categoryActionsLinearLayout.getLayoutParams();
                                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                view.setClickable(true);
                            }
                        });
                        anim.start();
                    }
                    else {
                        LinearLayout categoryActionsLinearLayout =
                                view.findViewById(R.id.category_actions_linearlayout);

                        final int targetHeight = categoryActionsLinearLayout.getMeasuredHeight();
                        categoryActionsLinearLayout.getLayoutParams().height = categoryActionsLinearLayout.getMeasuredHeight();

                        ValueAnimator anim =
                                ValueAnimator.ofInt(categoryActionsLinearLayout.getMeasuredHeight(), targetHeight);
                        anim.setInterpolator(new AccelerateInterpolator());
                        anim.setDuration(250);
                        anim.addUpdateListener(animation -> {
                            ViewGroup.LayoutParams layoutParams = categoryActionsLinearLayout.getLayoutParams();
                            layoutParams.height = (int) (targetHeight * (1 - animation.getAnimatedFraction()));
                            categoryActionsLinearLayout.setLayoutParams(layoutParams);
                        });
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);

                                view.setClickable(false);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                categoryActionsLinearLayout.setVisibility(View.GONE);
                                view.setClickable(true);
                            }
                        });
                        anim.start();
                    }
                }
            };
        }

        private View.OnClickListener getShareTextViewOnClickListener(int currentPosition) {
            return new View.OnClickListener() {
                URI currentUri;
                @Override
                public void onClick(View view) {

                    getView().setClickable(false);

                    final AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
                    alphaAnimation.setDuration(250);

                    viewLayout.setClickable(false);
                    ((MainActivity) getActivity()).spinKitView.setVisibility(View.VISIBLE);
                    try {
                        currentUri = appendQueryParameters("https://athene.page.link", "user", MainActivity.user.getUid());
                        currentUri = appendQueryParameters(currentUri.toString(), "category", categoriesArrayList.get(currentPosition));

                        Log.d("URL", MainActivity.user.getUid());
                        Log.d("URL", categoriesArrayList.get(currentPosition));
                        Log.d("URL", currentUri.toString());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    Log.d("URI", currentUri.toString());
                    FirebaseDynamicLinks
                            .getInstance()
                            .createDynamicLink()
                            .setLink(Uri.parse(currentUri.toString()))
                            .setDomainUriPrefix("https://athene.page.link")
                            .setAndroidParameters(
                                    new DynamicLink.AndroidParameters.Builder("com.develop.vadim.english").build()
                            )
                            .buildShortDynamicLink()
                            .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                                @Override
                                public void onSuccess(ShortDynamicLink shortDynamicLink) {
                                    getView().setClickable(true);

                                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    String shareBody = shortDynamicLink.getShortLink().toString();
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {
                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            ((MainActivity) getActivity()).spinKitView.setVisibility(View.INVISIBLE);

                                            startActivity(Intent.createChooser(shareIntent, "Поделиться категорией"));
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {
                                        }
                                    });

                                    ((MainActivity) getActivity()).spinKitView.startAnimation(alphaAnimation);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG", e.toString());

                                    getView().setClickable(true);

                                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {
                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            ((MainActivity) getActivity()).spinKitView.setVisibility(View.INVISIBLE);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {
                                        }
                                    });

                                    ((MainActivity) getActivity()).spinKitView.startAnimation(alphaAnimation);
                                }
                            });
                }

                private URI appendQueryParameters(String uri, String key, String parameter) throws URISyntaxException {
                    URI oldUri = new URI(uri);

                    StringBuilder buildQueryBuilder = new StringBuilder();
                    buildQueryBuilder.append(key);
                    buildQueryBuilder.append("=");
                    buildQueryBuilder.append(parameter);

                    String newQuery = oldUri.getQuery();
                    if (newQuery == null) {
                        newQuery = buildQueryBuilder.toString();
                    }
                    else {
                        newQuery += "&" + buildQueryBuilder.toString();
                    }

                    URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), newQuery, oldUri.getFragment());


                    return newUri;
                }
            };
        }

        private View.OnClickListener getLearnTextViewOnClickListener(int currentPosition) {
            return new View.OnClickListener() {
                ArrayList<Word> changingWordsArrayList = new ArrayList<>();

                @Override
                public void onClick(View view) {

                    for(int wordsInThisCategoryCounter = 0; wordsInThisCategoryCounter < wordsInCategoriesArrayList.get(currentPosition).size(); wordsInThisCategoryCounter++) {
                        Word word = wordsInCategoriesArrayList.get(currentPosition).get(wordsInThisCategoryCounter);

                        if(word.getLevel() == Word.LEVEL_ADDED) {
                            changingWordsArrayList.add(word);
                        }
                    }

                    if(changingWordsArrayList.size() == 0) {
                        Toast.makeText(getContext(), "В данной категории все слова уже в вашем цикле изучения", Toast.LENGTH_LONG).show();
                    }
                    else {
                        AtheneDialog atheneDialog = new AtheneDialog(getContext(), AtheneDialog.TWO_OPTIONS_TYPE);
                        atheneDialog.setMessageText("В данной категории " + changingWordsArrayList.size() + " слов для добавления. Добавить?");
                        atheneDialog.setPositiveText(getString(R.string.yes));
                        atheneDialog.setPositiveClickListener(new TextView.OnClickListener() {
                            @Override
                            public void onClick(View view) {
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

                                atheneDialog.dismiss();
                            }
                        });
                        atheneDialog.setNegativeText(getString(R.string.no));
                        atheneDialog.setNegativeClickListener(new TextView.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                atheneDialog.dismiss();
                            }
                        });
                        atheneDialog.show();
                    }
                }
            };
        }
    }

    private class WordsCategoriesRecyclerViewAdapter extends RecyclerView.Adapter<WordsCategoriesRecyclerViewAdapter.WordsCategoriesRecyclerViewHolder> {

        ArrayList<String> categoryNamesList;
        ArrayList<Word> wordArrayList;
        ArrayList<ArrayList<Word>> wordsInCategoriesArrayList = new ArrayList<>();

        ArrayList<ExpansionLayout> expansionLayoutArrayList = new ArrayList<>();

        private ExpansionLayoutCollection expansionLayoutCollection = new ExpansionLayoutCollection();

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

            this.expansionLayoutCollection.openOnlyOne(true);
        }

        @NonNull
        @Override
        public WordsCategoriesRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WordsCategoriesRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_category, parent, false));
        }

        @Override
        public void onBindViewHolder(WordsCategoriesRecyclerViewHolder holder, int position) {
            expansionLayoutCollection.add(holder.expansionLayout);
            expansionLayoutArrayList.add(holder.expansionLayout);

            final int currentPosition = position;
            holder.categoryTextView.setText(categoryNamesList.get(position));

            holder.wordsInCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            holder.wordsInCategoryRecyclerView.setAdapter(new WordsInCategoriesRecyclerViewAdapter(wordsInCategoriesArrayList.get(position)));
            if(wordsInCategoriesArrayList.get(position).size() != 0) {

            }
            else {
                TextView thisCategoryIsEmpty = new TextView(getContext());
                thisCategoryIsEmpty.setText(getString(R.string.thisCategoryIsEmpty));
                thisCategoryIsEmpty.setTextColor(getResources().getColor(R.color.colorWhite));
                thisCategoryIsEmpty.setGravity(Gravity.CENTER);
                thisCategoryIsEmpty.setTextSize(18f);
            }

            holder.expansionHeader.setToggleOnClick(false);
            holder.expansionHeader.setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("HandlerLeak")
                @Override
                public boolean onLongClick(View view) {
                    AtheneDialog atheneDialog = new AtheneDialog(getContext(), AtheneDialog.TWO_OPTIONS_TYPE);
                    atheneDialog.setMessageText(getString(R.string.deleteCategoryMessage));
                    atheneDialog.setPositiveText(getString(R.string.yes));
                    atheneDialog.setPositiveClickListener(new TextView.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            view.setClickable(false);

                            Handler handler = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);

                                    initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());
                                    notifyDataSetChanged();
                                    atheneDialog.dismiss();
                                }
                            };

                            DatabaseReference reference = MainActivity.reference.child("categories");
                            reference.child(String.valueOf(position))
                                    .setValue(categoryNamesList.get(categoryNamesList.size() - 1))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            reference.child(String.valueOf(categoryNamesList.size() - 1)).removeValue();

                                            for(int wordsCounter = 0; wordsCounter < wordArrayList.size(); wordsCounter++) {
                                                if(wordArrayList.get(wordsCounter).getWordCategory().equals(categoryNamesList.get(position))) {
                                                    wordArrayList.get(wordsCounter).setWordCategory("Без категории");

                                                    databaseReference.child(String.valueOf(wordArrayList.get(wordsCounter).getInd())).child(Word.categoryDatabaseKey).setValue("Без категории");
                                                }
                                            }

                                            ((MainActivity)getActivity()).categoryNames.remove(position);


                                            handler.sendMessage(handler.obtainMessage());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            atheneDialog.dismiss();

                                            AtheneDialog exceptionDialog = new AtheneDialog(getContext(), AtheneDialog.SIMPLE_MESSAGE_TYPE);
                                            exceptionDialog.setMessageText(getString(R.string.no_internet_error));
                                            exceptionDialog.setPositiveClickListener(new TextView.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    atheneDialog.dismiss();
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                    atheneDialog.setNegativeText(getString(R.string.no));
                    atheneDialog.setNegativeClickListener(new TextView.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            atheneDialog.dismiss();
                        }
                    });

                    atheneDialog.show();

                    return true;
                }
            });
            holder.expansionHeader.setOnClickListener(view -> {
                Log.d("BIB", String.valueOf(currentPosition));
               //TODO: Fix Animation
                holder.expansionLayout.toggle(false);
                { if(holder.expansionLayout.isExpanded()) {
                    LinearLayout categoryActionsLinearLayout =
                            holder.expansionHeader.findViewById(R.id.category_actions_linearlayout);

                    categoryActionsLinearLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    final int targetHeight = categoryActionsLinearLayout.getMeasuredHeight();
                    categoryActionsLinearLayout.getLayoutParams().height = 0;
                    categoryActionsLinearLayout.setVisibility(View.VISIBLE);

                    ValueAnimator anim =
                            ValueAnimator.ofInt(categoryActionsLinearLayout.getMeasuredHeight(), targetHeight);
                    anim.setInterpolator(new AccelerateInterpolator());
                    anim.setDuration(250);
                    anim.addUpdateListener(animation -> {
                        ViewGroup.LayoutParams layoutParams = categoryActionsLinearLayout.getLayoutParams();
                        layoutParams.height = (int) (targetHeight * animation.getAnimatedFraction());
                        categoryActionsLinearLayout.setLayoutParams(layoutParams);
                    });
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);

                            holder.expansionHeader.setClickable(false);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ViewGroup.LayoutParams layoutParams = categoryActionsLinearLayout.getLayoutParams();
                            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            holder.expansionHeader.setClickable(true);
                        }
                    });
                    anim.start();
                }
                else {
                    LinearLayout categoryActionsLinearLayout =
                            holder.expansionHeader.findViewById(R.id.category_actions_linearlayout);

                    final int targetHeight = categoryActionsLinearLayout.getMeasuredHeight();
                    categoryActionsLinearLayout.getLayoutParams().height = categoryActionsLinearLayout.getMeasuredHeight();

                    ValueAnimator anim =
                            ValueAnimator.ofInt(categoryActionsLinearLayout.getMeasuredHeight(), targetHeight);
                    anim.setInterpolator(new AccelerateInterpolator());
                    anim.setDuration(250);
                    anim.addUpdateListener(animation -> {
                        ViewGroup.LayoutParams layoutParams = categoryActionsLinearLayout.getLayoutParams();
                        layoutParams.height = (int) (targetHeight * (1 - animation.getAnimatedFraction()));
                        categoryActionsLinearLayout.setLayoutParams(layoutParams);
                    });
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);

                            holder.expansionHeader.setClickable(false);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            categoryActionsLinearLayout.setVisibility(View.GONE);
                            holder.expansionHeader.setClickable(true);
                        }
                    });
                    anim.start();
                    }
                }
            });

            holder.learnTextView.setOnTouchListener(Utils.loginTouchListener);
            holder.learnTextView.setOnClickListener(new TextView.OnClickListener() {
                ArrayList<Word> changingWordsArrayList = new ArrayList<>();

                @Override
                public void onClick(View view) {

                    for(int wordsInThisCategoryCounter = 0; wordsInThisCategoryCounter < wordsInCategoriesArrayList.get(currentPosition).size(); wordsInThisCategoryCounter++) {
                        Word word = wordsInCategoriesArrayList.get(currentPosition).get(wordsInThisCategoryCounter);

                        if(word.getLevel() == Word.LEVEL_ADDED) {
                            changingWordsArrayList.add(word);
                        }
                    }

                    if(changingWordsArrayList.size() == 0) {
                        Toast.makeText(getContext(), "В данной категории все слова уже в вашем цикле изучения", Toast.LENGTH_LONG).show();
                    }
                    else {
                        AtheneDialog atheneDialog = new AtheneDialog(getContext(), AtheneDialog.TWO_OPTIONS_TYPE);
                        atheneDialog.setMessageText("В данной категории " + changingWordsArrayList.size() + " слов для добавления. Добавить?");
                        atheneDialog.setPositiveText(getString(R.string.yes));
                        atheneDialog.setPositiveClickListener(new TextView.OnClickListener() {
                            @Override
                            public void onClick(View view) {
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

                                atheneDialog.dismiss();
                            }
                        });
                        atheneDialog.setNegativeText(getString(R.string.no));
                        atheneDialog.setNegativeClickListener(new TextView.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                atheneDialog.dismiss();
                            }
                        });
                        atheneDialog.show();
                    }
                }
            });

            holder.shareTextView.setOnTouchListener(Utils.loginTouchListener);
            holder.shareTextView.setOnClickListener(new TextView.OnClickListener() {
                URI currentUri;
                @Override
                public void onClick(View view){

                    getView().setClickable(false);

                    final AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
                    alphaAnimation.setDuration(250);

                    viewLayout.setClickable(false);
                    ((MainActivity)getActivity()).spinKitView.setVisibility(View.VISIBLE);
                    try {
                        currentUri = appendQueryParameters("https://athene.page.link", "user", MainActivity.user.getUid());
                        currentUri = appendQueryParameters(currentUri.toString(), "category", categoryNamesList.get(currentPosition));

                        Log.d("URL", MainActivity.user.getUid());
                        Log.d("URL", categoryNamesList.get(currentPosition));
                        Log.d("URL", currentUri.toString());
                    }
                    catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    Log.d("URI", currentUri.toString());
                    FirebaseDynamicLinks
                            .getInstance()
                            .createDynamicLink()
                            .setLink(Uri.parse(currentUri.toString()))
                            .setDomainUriPrefix("https://athene.page.link")
                            .setAndroidParameters(
                                    new DynamicLink.AndroidParameters.Builder("com.develop.vadim.english").build()
                            )
                            .buildShortDynamicLink()
                            .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                                @Override
                                public void onSuccess(ShortDynamicLink shortDynamicLink) {
                                    getView().setClickable(true);

                                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    String shareBody = shortDynamicLink.getShortLink().toString();
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) { }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            ((MainActivity)getActivity()).spinKitView.setVisibility(View.INVISIBLE);

                                            startActivity(Intent.createChooser(shareIntent, "Поделиться категорией"));
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) { }
                                    });

                                    ((MainActivity)getActivity()).spinKitView.startAnimation(alphaAnimation);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG", e.toString());

                                    getView().setClickable(true);

                                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) { }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            ((MainActivity)getActivity()).spinKitView.setVisibility(View.INVISIBLE);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) { }
                                    });

                                    ((MainActivity)getActivity()).spinKitView.startAnimation(alphaAnimation);
                                }
                            });
                }

                private URI appendQueryParameters(String uri, String key, String parameter) throws URISyntaxException {
                    URI oldUri = new URI(uri);

                    StringBuilder buildQueryBuilder = new StringBuilder();
                    buildQueryBuilder.append(key);
                    buildQueryBuilder.append("=");
                    buildQueryBuilder.append(parameter);

                    String newQuery = oldUri.getQuery();
                    if (newQuery == null) {
                        newQuery = buildQueryBuilder.toString();
                    }
                    else {
                        newQuery += "&" + buildQueryBuilder.toString();
                    }

                    URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(), newQuery, oldUri.getFragment());


                    return newUri;
                }
            });
        }

        @Override
        public int getItemCount() {
            return categoryNamesList.size();
        }

        class WordsCategoriesRecyclerViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView materialCardView;
            TextView categoryTextView;
            RecyclerView wordsInCategoryRecyclerView;
            LinearLayout linearLayoutF;
            ExpansionHeader expansionHeader;
            ExpansionLayout expansionLayout;
            TextView learnTextView;
            TextView shareTextView;

            WordsCategoriesRecyclerViewHolder(View itemView) {
                super(itemView);

                wordsInCategoryRecyclerView = itemView.findViewById(R.id.expandableRecyclerView);
                materialCardView = itemView.findViewById(R.id.categoryCellMaterialCardView);

                learnTextView = itemView.findViewById(R.id.learnWordsTextView);
                shareTextView = itemView.findViewById(R.id.shareWordsTextView);
                categoryTextView = itemView.findViewById(R.id.archiveWordInEnglishTextView);
                expansionHeader = itemView.findViewById(R.id.categoryCellExpansionHeader);
                expansionLayout = itemView.findViewById(R.id.categoryCellExpansionLayout);
                linearLayoutF = itemView.findViewById(R.id.categoriesMaterialCardView);

                /*expansionHeader.setOnClickListener(new View.OnClickListener() {
                    boolean isFirstOpen = true;
                    int notOpenedHeight;
                    int openedHeight;

                    @Override
                    public void onClick(View view) {

                        if(expansionLayout.isExpanded()) {
                            expansionLayout.toggle(true);

                            ValueAnimator valueAnimator = ValueAnimator.ofInt(openedHeight, notOpenedHeight);
                            valueAnimator.addUpdateListener(animation -> {
                                expansionLayout.setClickable(false);
                                int val = (Integer) animation.getAnimatedValue();
                                ViewGroup.LayoutParams layoutParams = materialCardView.getLayoutParams();
                                layoutParams.height = val;
                                materialCardView.setLayoutParams(layoutParams);
                            });

                            valueAnimator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation, boolean isReverse) {
                                    expansionHeader.setClickable(false);
                                }

                                @Override
                                public void onAnimationEnd(Animator animation, boolean isReverse) {
                                    expansionHeader.setClickable(true);
                                }
                            });

                            valueAnimator.setDuration(300);
                            valueAnimator.start();
                        }
                        else {
                            expansionLayout.expand(true);
                            ValueAnimator valueAnimator = ValueAnimator.ofInt(notOpenedHeight,openedHeight);
                            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    ViewGroup.LayoutParams layoutParams = materialCardView.getLayoutParams();
                                    layoutParams.height = (int) (Integer) animation.getAnimatedValue();
                                    materialCardView.setLayoutParams(layoutParams);
                                }
                            });

                            valueAnimator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation, boolean isReverse) {
                                    expansionHeader.setClickable(false);
                                }

                                @Override
                                public void onAnimationEnd(Animator animation, boolean isReverse) {
                                    expansionHeader.setClickable(true);
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
                });*/
            }

        }
    }

    private class WordsInCategoriesRecyclerViewAdapter extends RecyclerView.Adapter<WordsInCategoriesRecyclerViewAdapter.WordsInCategoriesRecyclerViewHolder> {
        private ArrayList<Word> wordsInCurrentCategoryArrayList;


        public WordsInCategoriesRecyclerViewAdapter(ArrayList<Word> wordsInCurrentCategoryArrayList) {
            this.wordsInCurrentCategoryArrayList = wordsInCurrentCategoryArrayList;
        }

        @NonNull
        @Override
        public WordsInCategoriesRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new WordsInCategoriesRecyclerViewHolder(getLayoutInflater().inflate(R.layout.cell_word_in_category, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull WordsInCategoriesRecyclerViewHolder holder, int position) {
            final int currentPosition = position;

            holder.wordTextView.setText(wordsInCurrentCategoryArrayList.get(currentPosition).getWordInEnglish());
            holder.itemView.findViewById(R.id.wordInCategoryMaterialCardView).setOnClickListener(new View.OnClickListener() {
                boolean isInEnglish = true;
                @Override
                public void onClick(View view) {
                    view.setClickable(false);

                    AlphaAnimation disappearAnimation = new AlphaAnimation(1f, 0f);
                    disappearAnimation.setDuration(200);
                    disappearAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(isInEnglish) {
                                holder.wordTextView.setText(wordsInCurrentCategoryArrayList.get(currentPosition).getWordInRussian());

                                isInEnglish = false;
                            }
                            else {
                                holder.wordTextView.setText(wordsInCurrentCategoryArrayList.get(currentPosition).getWordInEnglish());

                                isInEnglish = true;
                            }
                            AlphaAnimation appearAnimation = new AlphaAnimation(0f, 1f);
                            appearAnimation.setDuration(200);
                            appearAnimation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) { }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    view.setClickable(true);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) { }
                            });

                            holder.wordTextView.startAnimation(appearAnimation);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });

                    holder.wordTextView.startAnimation(disappearAnimation);
                }
            });
            holder.itemView.findViewById(R.id.wordInCategoryMaterialCardView).setOnLongClickListener(new View.OnLongClickListener() {
                Word word;
                @Override
                public boolean onLongClick(View view) {
                    word =  wordsInCurrentCategoryArrayList.get(currentPosition);
                    switch((int) word.getLevel()) {

                        case -2: {
                            AtheneDialog atheneDialog = new AtheneDialog(getContext(), AtheneDialog.TWO_OPTIONS_TYPE);
                            atheneDialog.setMessageText(getString(R.string.add_word_from_categoty));
                            atheneDialog.setPositiveText(getString(R.string.yes));
                            atheneDialog.setPositiveClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    word.setLevel(0);
                                    MainActivity.reference.child("words")
                                            .child(wordsInCurrentCategoryArrayList.get(currentPosition).getInd()).child(Word.levelDatabaseKey).setValue(0)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getContext(), "Слово успешно добавлено в цикл изучения", Toast.LENGTH_LONG).show();

                                                        ((MainActivity) getActivity()).wordArrayList.set((int) word.getIndex(), word);
                                                        initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());
                                                    }
                                                    else if (task.isCanceled()) {
                                                        //TODO: make Athene Dialog
                                                        Toast.makeText(getContext(), "Произошла неизвестная ошибка, провнрьте подключение к сети", Toast.LENGTH_LONG).show();
                                                    }

                                                    atheneDialog.dismiss();
                                                }
                                            });
                                }
                            });
                            atheneDialog.setNegativeText(getString(R.string.no));
                            atheneDialog.setNegativeClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    atheneDialog.dismiss();
                                }
                            });
                            atheneDialog.show();
                        }

                        break;
                        case -1:
                            Toast.makeText(getContext(), "Вы уже изучили это слово", Toast.LENGTH_LONG).show();

                            break;
                        default:
                            Toast.makeText(getContext(), "Вы уже изучаете это слово на данный момент", Toast.LENGTH_LONG).show();

                            break;
                    }

                    return true; //It must return true just because if it returns "true" it will not call OnClick method after dropping view
                }
            });
        }

        @Override
        public int getItemCount() {
            return wordsInCurrentCategoryArrayList.size();
        }

        private class WordsInCategoriesRecyclerViewHolder extends RecyclerView.ViewHolder {

            TextView wordTextView;

            public WordsInCategoriesRecyclerViewHolder(@NonNull View itemView) {
                super(itemView);

                wordTextView = itemView.findViewById(R.id.wordInCategoryTextView);
            }
        }

    }
}
