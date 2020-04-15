package com.develop.vadim.english.Fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.Broadcasts.NotificationBroadcast;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import bg.devlabs.transitioner.Transitioner;

import static android.content.Context.ALARM_SERVICE;

public class AddNewWordFragment extends Fragment implements UpdateDataListener {

    private EditText englishWordEditText;
    private EditText russianWordEditText;
    private ImageView addWordToServiceImageView;
    private MaterialCardView categoryMaterialCardView;
    private MaterialCardView categoriesChoosingMaterialMaterialCardView;
    private TextView categoryTextView;
    private TextView headerTextView;
    private TextView addWordToServiceTextView;
    private LinearLayout categoriesTextViewsLinearLayout;
    private ProgressBar wordSendingProgressBar;
    private RecyclerView choosingCategoryRecyclerView;
    private MaterialCardView categoryMaterialCardViewHolder;

    public final static int NEW_CATEGORY_HAS_BEEN_ADDED = 5;

    private Transitioner transitioner;

    private Calendar timePickerCalendar = Calendar.getInstance();

    private long ind;

    private DatabaseReference reference;

    private SharedPreferences timeSharedPreferences;

    private boolean isCategoryNew = false;

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

            startAppearAnimation();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        reference = MainActivity.reference.child("words");
        reference.keepSynced(true);

        timeSharedPreferences = getActivity().getSharedPreferences("Time Picker", Context.MODE_PRIVATE);

        return inflater.inflate(R.layout.add_new_word_layout, container, false);
    }

    @Override
     public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        englishWordEditText = view.findViewById(R.id.editTextEnglish);
        russianWordEditText = view.findViewById(R.id.editTextRussian);
        categoryMaterialCardView = view.findViewById(R.id.categoryChooseCardView);
        categoryTextView = categoryMaterialCardView.findViewById(R.id.addNewWordCategoryTextView);
        headerTextView = view.findViewById(R.id.headerTextView);
        categoriesTextViewsLinearLayout = view.findViewById(R.id.categoriesTextViewLinearLayout);
        addWordToServiceTextView = view.findViewById(R.id.addWordToServiceTextView);
        categoriesChoosingMaterialMaterialCardView = view.findViewById(R.id.categoriesMaterialCardView);
        addWordToServiceImageView = view.findViewById(R.id.addWordToServiceImageView);
        wordSendingProgressBar = view.findViewById(R.id.spinKit);
        wordSendingProgressBar.setIndeterminateDrawable(new DoubleBounce());
        categoryMaterialCardViewHolder = view.findViewById(R.id.categoryChooseCardViewHolder);

        categoryTextView.setText("Без категории");

        categoryMaterialCardView.setOnClickListener(new View.OnClickListener() {
            private Handler handler;

            @SuppressLint("HandlerLeak")
            @Override
            public void onClick(View v) {
                transitioner = new Transitioner(categoryMaterialCardView, categoriesChoosingMaterialMaterialCardView);
                transitioner.animateTo(1f, (long) 400, new AccelerateDecelerateInterpolator());

                isCategoryNew = false;

                categoryMaterialCardView.setCardBackgroundColor(Color.WHITE);
                categoryTextView.setVisibility(View.INVISIBLE);
                categoryMaterialCardView.setClickable(false);

                addWordToServiceImageView.animate().alphaBy(1).alpha(0).setDuration(300).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(410);
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
       // categoryEditText = view.findViewById(R.id.editTextCategory);

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
                            newWord.setWordInEnglish(englishWordEditText.getText().toString());
                            newWord.setWordInRussian(russianWordEditText.getText().toString());

                            startDisappearingAnimation(newWord, categoryTextView.getText().toString());


                            englishWordEditText.setText("");
                            russianWordEditText.setText("");
                            categoryTextView.setText("Без категории");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });

                   if(timeSharedPreferences.getBoolean(getString(R.string.firstRun), true)) {
                       setUpService();
                       timeSharedPreferences.edit().putBoolean(getString(R.string.firstRun), false).apply();
                   }
                }
                else {
                    Toast.makeText(v.getContext(), "Заполни все поля", Toast.LENGTH_LONG).show();
                }
            }

            private void startDisappearingAnimation(final Word word, final String category) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.dissapear);
                categoryMaterialCardView.startAnimation(animation);
                russianWordEditText.startAnimation(animation);
                englishWordEditText.startAnimation(animation);
                addWordToServiceImageView.startAnimation(animation);
                headerTextView.startAnimation(animation);
                categoriesTextViewsLinearLayout.startAnimation(animation);
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
                        categoriesTextViewsLinearLayout.setVisibility(View.INVISIBLE);
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

    private ArrayList getCategories() {
        ArrayList<String> categoriesList = new ArrayList<>();
        categoriesList.add("Без категории");
        categoriesList.addAll(((MainActivity)getActivity()).getCategoryNamesList());
        categoriesList.add("Добавить");

        return categoriesList;
    }

    private void setUpService() {
        Intent intent = new Intent(getContext(), NotificationBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);

        Calendar wakeUpTimeCalendar = Calendar.getInstance();
        wakeUpTimeCalendar.set(Calendar.HOUR_OF_DAY, timeSharedPreferences.getInt(getString(R.string.hourOfDay), 12));
        wakeUpTimeCalendar.set(Calendar.MINUTE, timeSharedPreferences.getInt(getString(R.string.minute), 0));
        wakeUpTimeCalendar.set(Calendar.SECOND, 0);

        alarmManager.cancel(pendingIntent);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTimeCalendar.getTimeInMillis(), pendingIntent);
    }

    private void startAppearAnimation() {
        categoryMaterialCardView.setVisibility(View.VISIBLE);
        englishWordEditText.setVisibility(View.VISIBLE);
        russianWordEditText.setVisibility(View.VISIBLE);
        addWordToServiceImageView.setVisibility(View.VISIBLE);
        headerTextView.setVisibility(View.VISIBLE);
        categoriesTextViewsLinearLayout.setVisibility(View.VISIBLE);
        addWordToServiceTextView.setVisibility(View.VISIBLE);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.appear);
        categoryMaterialCardView.startAnimation(animation);
        russianWordEditText.startAnimation(animation);
        englishWordEditText.startAnimation(animation);
        addWordToServiceImageView.startAnimation(animation);
        headerTextView.startAnimation(animation);
        categoriesTextViewsLinearLayout.startAnimation(animation);
        addWordToServiceTextView.startAnimation(animation);
    }


    @Override
    public void onDataChange() {

    }

    private class CategoriesRecyclerViewAdapter extends RecyclerView.Adapter<CategoriesRecyclerViewAdapter.CategoriesRecyclerViewHolder> {
        private ArrayList<String> categories;

        private Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.appear);

        private int[] materialCardsColors = new int[] {
            R.color.LIGHT_GREEN_TRANSPARENT,
            R.color.LIGHT_PURPLE_TRANSPARENT,
            R.color.CASSANDORA_YELLOW,
            R.color.JADE_DUST_TRANSPARENT,
            R.color.JELLYFISH,
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

            if(position == getItemCount() - 1) {
                holder.materialCardView.setCardBackgroundColor(getResources().getColor(R.color.DOUBLE_DRAGON_SKIN));
            }
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
                        Transitioner transitioner = new Transitioner(categoryMaterialCardView, categoryMaterialCardViewHolder);
                        transitioner.animateTo(1f, (long) 400, new AccelerateDecelerateInterpolator());
                        categoryMaterialCardView.setClickable(true);
                        categoryMaterialCardView.setCardBackgroundColor(getResources().getColor(R.color.WHITE_TRANSPARENT));
                        choosingCategoryRecyclerView.setVisibility(View.INVISIBLE);

                        if(position == getItemCount() - 1) {
                            callChooseCategoryDialog();
                        }
                        else {
                            categoryTextView.setText(categoryNameTextView.getText());
                            isCategoryNew = false;
                        }

                        categoryTextView.setVisibility(View.VISIBLE);
                        categoryTextView.setText(categoryNameTextView.getText());
                        categoryTextView.startAnimation(animation);

                        addWordToServiceImageView.animate().alphaBy(0).alpha(1).setDuration(420).start();
                    }
                });
            }

            private void callChooseCategoryDialog() {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.add_new_category_layout);
                final EditText editText = dialog.findViewById(R.id.addNewCategoryEditText);
                final ImageView continueEditText = dialog.findViewById(R.id.addNewCategoryImageView);
                dialog.show();

                categoryTextView.setText(categories.get(0));

                continueEditText.setOnClickListener(new ImageView.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!editText.getText().toString().equals("")) {
                            categoryTextView.setText(editText.getText());
                            isCategoryNew = true;
                        }

                        dialog.dismiss();
                    }

                });
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


            //Imitate loading
            try {
                Thread.sleep(2000);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }

            word.setWordCategory(category);
            ((MainActivity)getActivity()).wordArrayList.add(word);

            if(getCategories().contains(category) || category.equals("default")) {
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
