package com.develop.vadim.english.Basic;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.develop.vadim.english.R;
import com.develop.vadim.english.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

import bg.devlabs.transitioner.Transitioner;

public class ChangeWordActivity extends AppCompatActivity {

    private EditText originalWordEditText;
    private EditText translatedWordEditText;
    private MaterialCardView categoriesMaterialCardView;
    private MaterialCardView categoriesMaterialCardViewPlaceHolder;
    private MaterialCardView categoriesMaterialCardViewComeBackPlaceHolder;
    private RecyclerView categoriesRecyclerView;
    private TextView categoriesTextView;
    private TextView addNewCategoryTextView;
    private ImageView deleteWordImageView;
    private ImageView saveChangesImageView;

    private BroadcastReceiver updateHasBeenDoneBroadcastReceiver;

    private Word changingWord;
    private ArrayList<String> categories = new ArrayList<>();

    private String category;

    private boolean isCategoryNew;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_word);
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        Utils.makeStatusBarTransparent(this);

        updateHasBeenDoneBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Close Activity", "Bye!");
                onBackPressed();
            }
        };

        final Handler changingWordHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                intent.putExtra(getString(R.string.changingWord), changingWord);

                if(isCategoryNew) {
                    intent.putExtra(getString(R.string.addNewCategory), true);
                }

                sendBroadcast(intent);
            }
        };

        final Handler removingWordHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                intent.putExtra(getString(R.string.changingWord), changingWord);
                intent.putExtra(getString(R.string.removeWordKey), true);

                sendBroadcast(intent);
            }
        };

        changingWord = getIntent().getParcelableExtra(getString(R.string.changeWord));
        categories = getIntent().getStringArrayListExtra(getString(R.string.categoriesToChangeWordActivity));

        category = changingWord.getWordCategory();

        saveChangesImageView = findViewById(R.id.saveChangesImageView);
        originalWordEditText = findViewById(R.id.editTextRussian);
        translatedWordEditText = findViewById(R.id.editTextEnglish);
        deleteWordImageView = findViewById(R.id.deleteWordImageView);
        categoriesRecyclerView = findViewById(R.id.categoriesWhileAddingWordRecyclerView);
        categoriesMaterialCardView = findViewById(R.id.categoryChooseCardView);
        categoriesMaterialCardViewPlaceHolder = findViewById(R.id.categoriesMaterialCardView);
        categoriesTextView = findViewById(R.id.addNewWordCategoryTextView);
        categoriesMaterialCardViewComeBackPlaceHolder = findViewById(R.id.categoryChooseCardViewHolder);
        addNewCategoryTextView = findViewById(R.id.addNewWordCategoryTextViewButton);

        categoriesTextView.setText(changingWord.getWordCategory());

        categoriesMaterialCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoriesTextView.setVisibility(View.INVISIBLE);
                addNewCategoryTextView.setVisibility(View.VISIBLE);
                addNewCategoryTextView.setClickable(true);

                categoriesMaterialCardView.setClickable(false);

                deleteWordImageView.animate().alphaBy(1).alpha(0).setDuration(200).start();
                deleteWordImageView.setClickable(false);

                saveChangesImageView.animate().alphaBy(1).alpha(0).setDuration(200).start();
                saveChangesImageView.setClickable(false);

                Transitioner transitioner = new Transitioner(categoriesMaterialCardView, categoriesMaterialCardViewPlaceHolder);
                transitioner.animateTo(1f, (long) 400, new AccelerateDecelerateInterpolator());
                categoriesMaterialCardView.setCardBackgroundColor(getResources().getColor(R.color.colorWhite));

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

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        categoriesRecyclerView.setVisibility(View.VISIBLE);
                        categoriesRecyclerView.setAdapter(new CategoriesRecyclerViewAdapter(categories));
                        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(ChangeWordActivity.this, 2));
                    }
                }, 420);
            }
        });

        deleteWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AtheneDialog atheneDialog = new AtheneDialog(ChangeWordActivity.this, AtheneDialog.TWO_OPTIONS_TYPE);
                atheneDialog.setMessageText(getString(R.string.deleteWordMessage));
                atheneDialog.setPositiveText(getString(R.string.yes));
                atheneDialog.setPositiveClickListener(new TextView.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changingWord.removeWordFromService(removingWordHandler);
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
        });

        saveChangesImageView.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.click);
                view.startAnimation(animation);

                category = categoriesTextView.getText().toString();

                saveChanges();
                changingWordHandler.sendMessage(changingWordHandler.obtainMessage());
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

        if(!category.equals(changingWord.getWordCategory())) {
            if(categories.contains(categoriesTextView.getText().toString())) {
                MainActivity.reference.child("words").child(changingWord.getInd()).child(Word.categoryDatabaseKey).setValue(categoriesTextView.getText().toString());
            }
            else {
                MainActivity.reference.child("categories").child(String.valueOf(categories.size() - 1)).setValue(category)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // ToDo: IOS DIALOG'
                                AtheneDialog atheneDialog = new AtheneDialog(ChangeWordActivity.this, AtheneDialog.SIMPLE_MESSAGE_TYPE);
                                atheneDialog.setMessageText(getString(R.string.no_internet_error));
                                atheneDialog.setPositiveClickListener(new TextView.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        atheneDialog.dismiss();
                                    }
                                });
                                atheneDialog.show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                MainActivity.reference.child("words").child(changingWord.getInd()).child(Word.categoryDatabaseKey).setValue(category);
                            }
                        });

                isCategoryNew = true;
            }

            changingWord.setWordCategory(category);
        }
    }

    private class CategoriesRecyclerViewAdapter extends RecyclerView.Adapter<CategoriesRecyclerViewAdapter.CategoriesRecyclerViewHolder> {

        ArrayList<String> categories;

        private Animation animation = AnimationUtils.loadAnimation(ChangeWordActivity.this, R.anim.appear);

        private int[] materialCardsColors = new int[] {
                R.color.purple,
                R.color.lightPurple,
                R.color.lightBlue,
                R.color.blue
        };


        CategoriesRecyclerViewAdapter(ArrayList<String> categories) {
            this.categories = categories;
        }

        @NonNull
        @Override
        public CategoriesRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_category_choose, parent, false);

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
                    closeAnimation(currentPosition, categories.get(currentPosition));
                }
            });

            holder.materialCardView.startAnimation(animation);

            addNewCategoryTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callChooseCategoryDialog();
                    closeAnimation(currentPosition, "Без категории");
                }
            });
        }

        private void closeAnimation(int currentPosition, String c) {


            Transitioner transitioner = new Transitioner(categoriesMaterialCardView, categoriesMaterialCardViewComeBackPlaceHolder);
            transitioner.animateTo(1f, (long) 400, new AccelerateDecelerateInterpolator());
            categoriesMaterialCardView.setCardBackgroundColor(getResources().getColor(R.color.WHITE_TRANSPARENT));

            deleteWordImageView.setClickable(true);
            deleteWordImageView.animate().alphaBy(0).alpha(1).setDuration(200).start();

            saveChangesImageView.setClickable(true);
            saveChangesImageView.animate().alphaBy(0).alpha(1).setDuration(200).start();

            AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
            alphaAnimation.setDuration(410);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    addNewCategoryTextView.setClickable(false);
                    categoriesRecyclerView.setClickable(false);
                    categoriesMaterialCardView.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    categoriesRecyclerView.setClickable(true);

                    categoriesMaterialCardView.setClickable(true);
                    addNewCategoryTextView.setVisibility(View.INVISIBLE);
                    categoriesMaterialCardView.setClickable(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            addNewCategoryTextView.startAnimation(alphaAnimation);

            categoriesRecyclerView.setVisibility(View.INVISIBLE);

            categoriesTextView.setText(c);
            categoriesTextView.setVisibility(View.VISIBLE);

            category = categories.get(currentPosition);
        }

        private void callChooseCategoryDialog() {
            final Dialog dialog = new Dialog(ChangeWordActivity.this);
            dialog.setContentView(R.layout.layout_add_new_category);
            final EditText editText = dialog.findViewById(R.id.addNewCategoryEditText);
            final ImageView continueImageView = dialog.findViewById(R.id.addNewCategoryImageView);

            dialog.show();

            continueImageView.setOnClickListener(new ImageView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!editText.getText().toString().equals("")) {
                        categoriesTextView.setText(editText.getText());
                        category = editText.getText().toString();
                    }

                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) { }
                    });

                    dialog.dismiss();
                }

            });

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    categoriesTextView.setText("Без категории");
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
