package com.develop.vadim.english.Basic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.develop.vadim.english.R;
import com.google.android.material.card.MaterialCardView;
import com.varunjohn1990.iosdialogs4android.IOSDialog;

import java.util.ArrayList;
import java.util.Random;

import bg.devlabs.transitioner.Transitioner;

public class ChangeWord extends AppCompatActivity {

    private EditText originalWordEditText;
    private EditText translatedWordEditText;
    private ImageView deleteWordImageView;
    private ImageView saveChangesImageView;

    private MaterialCardView categoriesMaterialCardView;
    private MaterialCardView categoriesMaterialCardViewPlaceHolder;
    private MaterialCardView categoriesMaterialCardViewComeBackPlaceHolder;
    private RecyclerView categoriesRecyclerView;
    private TextView categoriesTextView;

    private BroadcastReceiver updateHasBeenDoneBroadcastReceiver;

    private Word changingWord;
    private ArrayList<String> categories = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_word);

        updateHasBeenDoneBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Close Activity", "Bye!");
                onBackPressed();
            }
        };

        final Handler removingWordHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                sendBroadcast(new Intent(MainActivity.BROADCAST_ACTION).putExtra(getString(R.string.changingWord), changingWord));
            }
        };

        changingWord = getIntent().getParcelableExtra(getString(R.string.changeWord));
        categories = getIntent().getStringArrayListExtra("BOB");
        Log.d("BOB", categories.toString());

        //categories.add("Добавить");

        saveChangesImageView = findViewById(R.id.saveChangesImageView);
        originalWordEditText = findViewById(R.id.editTextRussian);
        translatedWordEditText = findViewById(R.id.editTextEnglish);
        deleteWordImageView = findViewById(R.id.deleteWordImageView);
        categoriesRecyclerView = findViewById(R.id.categoriesWhileAddingWordRecyclerView);
        categoriesMaterialCardView = findViewById(R.id.categoryChooseCardView);
        categoriesMaterialCardViewPlaceHolder = findViewById(R.id.categoriesMaterialCardView);
        categoriesTextView = findViewById(R.id.addNewWordCategoryTextView);
        categoriesMaterialCardViewComeBackPlaceHolder = findViewById(R.id.categoryChooseCardViewHolder);

        categoriesMaterialCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoriesTextView.setVisibility(View.INVISIBLE);


                Transitioner transitioner = new Transitioner(categoriesMaterialCardView, categoriesMaterialCardViewPlaceHolder);
                transitioner.animateTo(1f, (long) 400, new AccelerateDecelerateInterpolator());
                categoriesMaterialCardView.setCardBackgroundColor(getResources().getColor(R.color.colorWhite));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        categoriesRecyclerView.setVisibility(View.VISIBLE);
                        categoriesRecyclerView.setAdapter(new CategoriesRecyclerViewAdapter(categories));
                        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(ChangeWord.this, 2));
                    }
                }, 420);
            }
        });

        deleteWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IOSDialog.Builder(v.getContext())
                        .negativeButtonText("Нет")
                        .positiveButtonText("Да")
                        .message(getString(R.string.deleteWordMessage))
                        .negativeClickListener(new IOSDialog.Listener() {
                            @Override
                            public void onClick(IOSDialog iosDialog) {
                                startActivity(new Intent(ChangeWord.this, WordCheckActivity.class));
                            }
                        })
                        .positiveClickListener(new IOSDialog.Listener() {
                            @Override
                            public void onClick(IOSDialog iosDialog) {
                                changingWord.removeWordFromService(removingWordHandler);
                                iosDialog.dismiss();

                            }
                        })
                        .build()
                        .show();
            }
        });

        saveChangesImageView.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges();
                removingWordHandler.sendMessage(removingWordHandler.obtainMessage());
                onBackPressed();
            }
        });

        originalWordEditText.setText(changingWord.getWordInEnglish());
        translatedWordEditText.setText(changingWord.getWordInRussian());
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(updateHasBeenDoneBroadcastReceiver, new IntentFilter(MainActivity.BROADCAST_UPDATE_HAS_BEEN_DONE_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(updateHasBeenDoneBroadcastReceiver);
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try {
            unregisterReceiver(updateHasBeenDoneBroadcastReceiver);
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void saveChanges() {
        if(!originalWordEditText.getText().toString().equals(changingWord.getWordInEnglish())) {
            MainActivity.reference.child("words").child(changingWord.getInd()).child(Word.englishDatabaseKey).setValue(originalWordEditText.getText().toString()) ;
            changingWord.setWordInEnglish(originalWordEditText.getText().toString());
        }

        if(!translatedWordEditText.getText().toString().equals(changingWord.getWordInEnglish())) {
            MainActivity.reference.child("words").child(changingWord.getInd()).child(Word.russianDatabaseKey).setValue(translatedWordEditText.getText().toString());
            changingWord.setWordInRussian(translatedWordEditText.getText().toString());
        }
    }

    private class CategoriesRecyclerViewAdapter extends RecyclerView.Adapter<CategoriesRecyclerViewAdapter.CategoriesRecyclerViewHolder> {

        ArrayList<String> categories = new ArrayList<>();

        final int lastPosition = getItemCount() - 1;

        private Animation animation = AnimationUtils.loadAnimation(ChangeWord.this, R.anim.appear);

        private int[] materialCardsColors = new int[] {
                R.color.LIGHT_GREEN_TRANSPARENT,
                R.color.LIGHT_PURPLE_TRANSPARENT,
                R.color.CASSANDORA_YELLOW,
                R.color.JADE_DUST_TRANSPARENT,
                R.color.JELLYFISH,
        };


        CategoriesRecyclerViewAdapter(ArrayList<String> categories) {
            this.categories = categories;
        }

        @NonNull
        @Override
        public CategoriesRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_choosing_cell, parent, false);

            return new CategoriesRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoriesRecyclerViewHolder holder, int position) {
            final int currentPosition = position;

            holder.categoryTextView.setText(categories.get(position));
            holder.materialCardView.setCardBackgroundColor(getResources().getColor(materialCardsColors[new Random().nextInt(materialCardsColors.length)]));

            holder.materialCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Transitioner transitioner = new Transitioner(categoriesMaterialCardView, categoriesMaterialCardViewComeBackPlaceHolder);
                    transitioner.animateTo(1f, (long) 400, new AccelerateDecelerateInterpolator());
                    categoriesMaterialCardView.setCardBackgroundColor(getResources().getColor(R.color.WHITE_TRANSPARENT));

                    categoriesRecyclerView.setVisibility(View.INVISIBLE);

                    categoriesTextView.setText(categories.get(currentPosition));
                    categoriesTextView.setVisibility(View.VISIBLE);

                    if(currentPosition == 0) {
                        changingWord.setWordCategory("default");
                    }
                    else {
                        changingWord.setWordCategory(categories.get(currentPosition));
                    }

                    if(currentPosition == getItemCount() - 1) {
                        //
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        class CategoriesRecyclerViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView materialCardView;
            TextView categoryTextView;

            CategoriesRecyclerViewHolder(@NonNull View itemView) {
                super(itemView);

                materialCardView = itemView.findViewById(R.id.categoriesChoosingCellCardView);
                categoryTextView = itemView.findViewById(R.id.categoriesChoosingCellTextView);
            }
        }
    }
}
