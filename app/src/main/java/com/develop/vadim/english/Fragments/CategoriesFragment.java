package com.develop.vadim.english.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
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
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.varunjohn1990.iosdialogs4android.IOSDialog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import kotlin.jvm.internal.Ref;

public class CategoriesFragment extends Fragment implements UpdateDataListener {

    private RecyclerView wordsCategoriesRecyclerView;
    private TextView emptyCategoriesTextView;

    private DatabaseReference databaseReference;

    private WordsCategoriesRecyclerViewAdapter wordsCategoriesRecyclerViewAdapter;

    private Handler initCategoriesHandler;

    private View viewLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        databaseReference = MainActivity.reference.child("words");

        viewLayout = inflater.inflate(R.layout.categories_fragment, container, false);
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
                    wordsCategoriesRecyclerViewAdapter = new WordsCategoriesRecyclerViewAdapter(categories);
                    wordsCategoriesRecyclerView.setAdapter(wordsCategoriesRecyclerViewAdapter);
                }
            }
        };

        initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());

        wordsCategoriesRecyclerView = view.findViewById(R.id.userWordsCheckRecyclerView);
        emptyCategoriesTextView = view.findViewById(R.id.emptyContainerTextView);

        wordsCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    @Override
    public void onDataChange() {
        initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());
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
            return new WordsCategoriesRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.category_cell, parent, false));
        }

        @Override
        public void onBindViewHolder(WordsCategoriesRecyclerViewHolder holder, int position) {

            final int currentPosition = position;
            holder.categoryTextView.setText(categoryNamesList.get(position));
            holder.wordsInThisCategoryArrayList = wordsInCategoriesArrayList.get(position);

            if(wordsInCategoriesArrayList.get(position).size() != 0) {
                for(int wordsInCategoriesCounter = 0; wordsInCategoriesCounter < wordsInCategoriesArrayList.get(position).size(); wordsInCategoriesCounter++) {

                    final int localPosition = position;
                    final int currentWordIndex = wordsInCategoriesCounter;
                    final Word word = wordsInCategoriesArrayList.get(position).get(currentWordIndex);
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

                            return true; //It must return true just because if it returns "true" it will not call OnClick method after dropping view
                        }
                    });

                    wordInCategoryTextView.setText(wordsInCategoriesArrayList.get(position).get(wordsInCategoriesCounter).getWordInEnglish());

                    view.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
                            alphaAnimation.setDuration(200);

                            switch(motionEvent.getAction()) {
                                case MotionEvent.ACTION_DOWN:

                                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {

                                            view.setClickable(false);
                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            AlphaAnimation appearAlphaAnimation = new AlphaAnimation(0f, 1f);
                                            appearAlphaAnimation.setDuration(200);

                                            wordInCategoryTextView.setText(word.getWordInRussian());
                                            wordInCategoryTextView.startAnimation(appearAlphaAnimation);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {

                                        }
                                    });

                                    wordInCategoryTextView.startAnimation(alphaAnimation);

                                    break;
                                case MotionEvent.ACTION_CANCEL  :
                                    alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) { }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            AlphaAnimation appearAlphaAnimation = new AlphaAnimation(0f, 1f);
                                            appearAlphaAnimation.setDuration(200);

                                            wordInCategoryTextView.setText(word.getWordInEnglish());
                                            wordInCategoryTextView.startAnimation(appearAlphaAnimation);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) { }
                                    });

                                    wordInCategoryTextView.startAnimation(alphaAnimation);

                                    break;
                            }

                            return true;
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
                        // ToDo: IOS DIALOG
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
                                        public void onAnimationStart(Animation animation) {
                                        }

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
            LinearLayout wordInCategoryLinearLayout;
            LinearLayout linearLayoutF;
            ExpansionHeader expansionHeader;
            ExpansionLayout expansionLayout;
            TextView learnTextView;
            TextView shareTextView;

            ArrayList<Word> wordsInThisCategoryArrayList;

            int height;

            boolean catchHeightFlag = true;

            WordsCategoriesRecyclerViewHolder(final View itemView) {
                super(itemView);

                wordInCategoryLinearLayout = itemView.findViewById(R.id.expandableRecyclerView);
                materialCardView = itemView.findViewById(R.id.categoryCellMaterialCardView);

                learnTextView = itemView.findViewById(R.id.learnWordsTextView);
                shareTextView = itemView.findViewById(R.id.shareWordsTextView);
                categoryTextView = itemView.findViewById(R.id.archiveWordInEnglishTextView);
                expansionHeader = itemView.findViewById(R.id.categoryCellExpansionHeader);
                expansionLayout = itemView.findViewById(R.id.categoryCellExpansionLayout);
                linearLayoutF = itemView.findViewById(R.id.categoriesMaterialCardView);
                expansionHeader.setExpansionLayout(expansionLayout);

                expansionHeader.setOnClickListener(view -> {
                        expansionLayout.toggle(true);
                        expansionHeader.setClickable(false);

                        if(expansionLayout.isExpanded()) {
                            LinearLayout categoryActionsLinearLayout =
                                    expansionHeader.findViewById(R.id.category_actions_linearlayout);

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
                                public void onAnimationEnd(Animator animation) {
                                    ViewGroup.LayoutParams layoutParams = categoryActionsLinearLayout.getLayoutParams();
                                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                    expansionHeader.setClickable(true);
                                }
                            });
                            anim.start();
                        }
                        else {
                            LinearLayout categoryActionsLinearLayout =
                                    expansionHeader.findViewById(R.id.category_actions_linearlayout);

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
                                public void onAnimationEnd(Animator animation) {
                                    categoryActionsLinearLayout.setVisibility(View.GONE);
                                    expansionHeader.setClickable(true);
                                }
                            });
                            anim.start();
                        }
                });
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
}
