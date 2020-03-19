package com.develop.vadim.english.Basic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.develop.vadim.english.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ArchiveActivity extends AppCompatActivity {

    public String TAG = "myLogs";
    public List<String> words = new ArrayList<String>();
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        OldMainActivity.myRef.child("words")
                .child(OldMainActivity.NEXT_DATE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        index = (int)dataSnapshot.getChildrenCount();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        OldMainActivity.myRef.child("archive")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            Log.d(TAG, snapshot.getValue().toString());
                            words.add(snapshot.getValue().toString());
                            Continue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_archive, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    private void Continue() {
        String[] simpleArray = new String[words.size() ];
        words.toArray( simpleArray );

        final ListView lvMain = (ListView) findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, simpleArray);
        lvMain.setAdapter(adapter);
        /*
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String data_to_return = lvMain.getItemAtPosition(position).toString();
                String[] word = data_to_return.split(" - ");
                DatabaseReference ref = OldMainActivity.myRef.child("words").child(OldMainActivity.NEXT_DATE).child(String.valueOf(index));
                ref.child("Russian").setValue(word[0]);
                ref.child("English").setValue(word[1]);
                ref.child("category").setValue("every_day");

                Query queryRef = OldMainActivity.myRef.child("archive").orderByKey().equalTo(data_to_return);
                queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, String.valueOf(dataSnapshot.getValue()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                index ++;
                //lvMain.removeViewAt(position);
            }});
            */
    }
}
