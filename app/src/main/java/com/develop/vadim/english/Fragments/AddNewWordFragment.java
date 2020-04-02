package com.develop.vadim.english.Fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.Broadcasts.WordCheckBroadcast;
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
import java.util.Objects;
import java.util.Random;

import bg.devlabs.transitioner.Transitioner;

import static android.content.Context.ALARM_SERVICE;

public class AddNewWordFragment extends Fragment {

    private EditText englishWordEditText;
    private EditText russianWordEditText;
    private EditText categoryEditText;
    private ImageView addWordToServiceImageView;
    private MaterialCardView categoryMaterialCardView;
    private MaterialCardView categoriesChoosingMaterialMaterialCardView;
    private TextView categoryTextView;
    private ProgressBar wordSendingProgressBar;
    private RecyclerView choosingCategoryRecyclerView;
    private MaterialCardView categoryMaterialCardViewHolder;
    private ImageView timePickerImageView;

    private boolean isCategoryNew;

    private Transitioner transitioner;

    private Calendar timePickerCalendar = Calendar.getInstance();

    private long ind;

    private DatabaseReference reference;

    private SharedPreferences timeSharedPreferences;

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
        categoriesChoosingMaterialMaterialCardView = view.findViewById(R.id.categoriesMaterialCardView);
        addWordToServiceImageView = view.findViewById(R.id.addWordToServiceImageView);
        wordSendingProgressBar = view.findViewById(R.id.spinKit);
        wordSendingProgressBar.setIndeterminateDrawable(new DoubleBounce());
        categoryMaterialCardViewHolder = view.findViewById(R.id.categoryChooseCardViewHolder);
        categoryEditText = view.findViewById(R.id.categoryEditText);
        timePickerImageView = view.findViewById(R.id.timeImageView);

        timePickerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(view.getContext(), "Выберите время, в которое вас удобно повторить  слова", Toast.LENGTH_LONG).show();
                TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), timePickerDialogTimeSetListener,
                        timePickerCalendar.get(Calendar.HOUR_OF_DAY),
                        timePickerCalendar.get(Calendar.MINUTE), true);

                timePickerDialog.show();
            }
        });

        categoryMaterialCardView.setOnClickListener(new View.OnClickListener() {
            private Handler handler;

            private ArrayList<String> categories = new ArrayList<>();

            @SuppressLint("HandlerLeak")
            @Override
            public void onClick(View v) {
                transitioner = new Transitioner(categoryMaterialCardView, categoriesChoosingMaterialMaterialCardView);
                transitioner.animateTo(1f, (long) 400, new AccelerateDecelerateInterpolator());

                isCategoryNew = false;

                categoryMaterialCardView.setCardBackgroundColor(Color.WHITE);
                categoryTextView.setText("XENOUS");
                categoryTextView.setVisibility(View.INVISIBLE);
                categoryEditText.setVisibility(View.INVISIBLE);
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
                        choosingCategoryRecyclerView.setAdapter(new CategoriesRecyclerViewAdapter(categories));
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

                            startDisappearingAnimation(newWord);

                            englishWordEditText.setText("");
                            russianWordEditText.setText("");
                            categoryTextView.setText("");
                            categoryEditText.setVisibility(View.INVISIBLE);
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

            private void startDisappearingAnimation(final Word word) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.dissapear);
                categoryMaterialCardView.startAnimation(animation);
                russianWordEditText.startAnimation(animation);
                englishWordEditText.startAnimation(animation);
                addWordToServiceImageView.startAnimation(animation);

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

                        wordSendingProgressBar.setVisibility(View.VISIBLE);

                        new Thread(new StartWordSendingThread(word)).start();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
            }
        });
    }

    private void setUpService() {
        Intent intent = new Intent(getContext(), WordCheckBroadcast.class);
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

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.appear);
        categoryMaterialCardView.startAnimation(animation);
        russianWordEditText.startAnimation(animation);
        englishWordEditText.startAnimation(animation);
        addWordToServiceImageView.startAnimation(animation);
    }

    private TimePickerDialog.OnTimeSetListener timePickerDialogTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeSharedPreferences.edit().putInt(getString(R.string.hourOfDay), hourOfDay).putInt(getString(R.string.minute), minute).apply();
        }
    };

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
            categories.add("Добавить");
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
                            categoryEditText.setVisibility(View.VISIBLE);

                            isCategoryNew = true;
                        }
                        else {
                            categoryTextView.setVisibility(View.VISIBLE);
                            categoryTextView.setText(categoryNameTextView.getText());
                            categoryTextView.startAnimation(animation);
                        }

                        addWordToServiceImageView.animate().alphaBy(0).alpha(1).setDuration(420).start();
                    }
                });
            }
        }
    }

    private class StartWordSendingThread implements Runnable {
        Word word;

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
