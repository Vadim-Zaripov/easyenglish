package com.develop.vadim.english.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.develop.vadim.english.Basic.MainActivity;
import com.develop.vadim.english.R;
import com.develop.vadim.english.Basic.Word;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class AddNewWordFragment extends Fragment {

    private EditText englishWordEditText;
    private EditText russianWordEditText;
    private EditText categoryEditText;
    private Button addWordToServiceButton;

    private long ind;

    private DatabaseReference reference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        reference = MainActivity.reference.child("words");
        reference.keepSynced(true);

        return inflater.inflate(R.layout.add_new_word_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        englishWordEditText = view.findViewById(R.id.main_word_first_edittext);
        russianWordEditText = view.findViewById(R.id.main_word_second_edittext);
        addWordToServiceButton = view.findViewById(R.id.main_add_word_button);
        categoryEditText = view.findViewById(R.id.main_word_category_edittext);

        addWordToServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!englishWordEditText.getText().toString().equals("") && !russianWordEditText.getText().toString().equals("")) {
                   reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ind = dataSnapshot.getChildrenCount();

                            Word newWord = new Word(ind);
                            newWord.setWordInEnglish(englishWordEditText.getText().toString());
                            newWord.setWordInRussian(russianWordEditText.getText().toString());

                            if(!categoryEditText.getText().toString().equals("")) {
                                newWord.setWordCategory(categoryEditText.getText().toString());

                                //Check and create categories
                                CategoriesCheck categoriesCheck = new CategoriesCheck(categoryEditText.getText().toString());
                                new Thread(categoriesCheck).start();
                            }
                            newWord.sentWordToService();

                            englishWordEditText.setText("");
                            russianWordEditText.setText("");
                            categoryEditText.setText("");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(v.getContext(), "Заполни все поля", Toast.LENGTH_LONG).show();
                }
            }
        });
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

                    Log.d("BABKA", "BOB");
                    for(int i = 0; i < dataSnapshot.getChildrenCount(); i++) {
                        if(dataSnapshot.child(String.valueOf(i)).getValue().equals(category)) {
                            Log.d("BABKA", "BOB1");
                            isCategoryReal = true;
                        }
                    }

                    if(!isCategoryReal) {
                        MainActivity.reference.child("categories").child(String.valueOf(ind)).setValue(category);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
