package com.develop.vadim.english.Fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.Basic.WordCheckActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WordsUserCheckFragment extends Fragment {

    private RecyclerView wordsCategoriesRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView categorySearchView;
    private MaterialCardView shareMaterialCardView;
    private MaterialCardView deleteMaterialCardView;

    private DatabaseReference databaseReference;
    private DatabaseReference categoryReference;

    private static ArrayList<String> categoryNames;

    private WordsCategoriesRecyclerViewAdapter wordsCategoriesRecyclerViewAdapter;

    private Handler initCategoriesHandler;
    private Handler loadingCategoriesHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        databaseReference = MainActivity.reference.child("words");
        categoryReference = MainActivity.reference.child("categories");
        databaseReference.keepSynced(true);
        categoryNames = getCategories();
        Log.d("TAG", categoryNames.toString());

        return inflater.inflate(R.layout.user_words_check_fragment, container, false);
    }


    @SuppressLint("HandlerLeak")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initCategoriesHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                wordsCategoriesRecyclerViewAdapter = new WordsCategoriesRecyclerViewAdapter(categoryNames);
                swipeRefreshLayout.setRefreshing(false);
                wordsCategoriesRecyclerView.setAdapter(wordsCategoriesRecyclerViewAdapter);
            }
        };

        loadingCategoriesHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                categoryNames = getCategories();
                initCategoriesHandler.sendEmptyMessage(1);
            }
        };

        initCategoriesHandler.sendMessage(initCategoriesHandler.obtainMessage());

        wordsCategoriesRecyclerView = view.findViewById(R.id.userWordsCheckRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.userWordsCheckSwipeToRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity)getActivity()).updateCategories(loadingCategoriesHandler);
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
            }
        });

        categorySearchView = view.findViewById(R.id.categorySearchView);
        categorySearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        categorySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                wordsCategoriesRecyclerViewAdapter.getFilter().filter(newText);

                return false;
            }
        });

        wordsCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    private ArrayList<String> getCategories() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Все слова");
        categories.addAll(((MainActivity)getActivity()).getCategoryNamesList());

        return categories;
    }

    private class WordsCategoriesRecyclerViewAdapter extends RecyclerView.Adapter<WordsCategoriesRecyclerViewAdapter.WordsCategoriesRecyclerViewHolder> implements Filterable {

        ArrayList<String> categoryNamesListFull;
        ArrayList<String> categoryNamesList;

        Handler removeCategoryHandler;

        Filter categoryFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<String> filteredCategoriesList = new ArrayList<>();

                if(constraint == null || constraint.length() == 0) {
                    filteredCategoriesList.addAll(categoryNamesListFull);
                }
                else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for(String categoryName : categoryNamesListFull) {
                        if(categoryName.toLowerCase().contains(filterPattern)) {
                            filteredCategoriesList.add(categoryName);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredCategoriesList;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                categoryNamesList.clear();
                categoryNamesList.addAll((ArrayList) results.values);

                notifyDataSetChanged();
            }
        };

        WordsCategoriesRecyclerViewAdapter(ArrayList<String> categoryNamesList) {
            this.categoryNamesList = categoryNamesList;
            this.categoryNamesListFull = new ArrayList<>(categoryNamesList);
        }

        @NonNull
        @Override
        public WordsCategoriesRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WordsCategoriesRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.archived_word_cell, parent, false));
        }

        @Override
        public void onBindViewHolder(WordsCategoriesRecyclerViewHolder holder, int position) {
            holder.category = categoryNamesList.get(position);
            holder.categoryTextView.setText(categoryNamesList.get(position));
            holder.position = position;

            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.appear);
            holder.materialCardView.startAnimation(animation);
        }

        @Override
        public int getItemCount() {
            return categoryNamesList.size();
        }

        @Override
        public Filter getFilter() {
            return categoryFilter;
        }

        class WordsCategoriesRecyclerViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView materialCardView;
            TextView categoryTextView;
            String category;
            int position;

            WordsCategoriesRecyclerViewHolder(final View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swipeRefreshLayout.setRefreshing(true);
                        StartCheckingThread startCheckingThread = new StartCheckingThread(category);
                        new Thread(startCheckingThread).start();
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(v.getContext());
                        bottomSheetDialog.setContentView(R.layout.categories_bottom_sheet_dialog);
                        bottomSheetDialog.show();

                        shareMaterialCardView = bottomSheetDialog.findViewById(R.id.bottomSheetShareCardView);
                        deleteMaterialCardView  = bottomSheetDialog.findViewById(R.id.bottomSheetDeleteCardView);

                        shareMaterialCardView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FirebaseDynamicLinks
                                        .getInstance()
                                        .createDynamicLink()
                                        .setLink(Uri.parse("https://xenous.ru/lett" + "/" + FirebaseAuth.getInstance().getUid() + "/" + category))
                                        .setDomainUriPrefix("https://easyenglish.page.link")
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

                        deleteMaterialCardView.setOnClickListener(new View.OnClickListener() {
                            @SuppressLint("HandlerLeak")
                            @Override
                            public void onClick(View v) {
                                if(position != 0) {
                                    removeCategoryHandler = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            super.handleMessage(msg);

                                            swipeRefreshLayout.setRefreshing(false);
                                            categoryNamesList.remove(position);
                                            notifyDataSetChanged();
                                        }
                                    };

                                    new Thread(new RemoveCategoryThread(categoryTextView.getText().toString())).start();
                                }
                                else {
                                    Toast.makeText(v.getContext(), "Данная категория не может быть удалена", Toast.LENGTH_SHORT).show();
                                }

                                bottomSheetDialog.cancel();
                            }
                        });

                        return false;
                    }
                });
                materialCardView = itemView.findViewById(R.id.bob);
                categoryTextView = itemView.findViewById(R.id.archiveWordInEnglishTextView);
            }
        }
    }

    private class StartCheckingThread implements Runnable {

        private String category;
        private ArrayList<Word> neededWordList = new ArrayList<>();

        StartCheckingThread(String category) {
            this.category = category;
        }

        @SuppressLint("HandlerLeak")
        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Log.d("HANDLE", "Work");

                swipeRefreshLayout.setRefreshing(false);
                callCheckService();
            }
        };

        @Override
        public void run() {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(long childrenInDatabaseCounter = 0; childrenInDatabaseCounter < dataSnapshot.getChildrenCount(); childrenInDatabaseCounter++) {
                        Word word = new Word(childrenInDatabaseCounter);
                        if(Objects.equals(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.categoryDatabaseKey).getValue(), category)) {

                            //TODO: Create normal check
                            word.setWordInEnglish(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.englishDatabaseKey).getValue()).toString());
                            word.setWordInRussian(Objects.requireNonNull(dataSnapshot.child(String.valueOf(childrenInDatabaseCounter)).child(Word.russianDatabaseKey).getValue()).toString());

                            neededWordList.add(word);
                        }
                    }

                    try {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    handler.sendMessage(handler.obtainMessage());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }

        private void callCheckService() {
            Intent wordCheckIntent = new Intent(getContext(), WordCheckActivity.class);

            wordCheckIntent.putExtra(getString(R.string.word_check_flag), true);
            wordCheckIntent.putExtra(getString(R.string.parcelableWordKey), neededWordList);

            startActivity(wordCheckIntent);
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

                    wordsCategoriesRecyclerViewAdapter.removeCategoryHandler.sendMessage(wordsCategoriesRecyclerViewAdapter.removeCategoryHandler.obtainMessage());
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }
}
