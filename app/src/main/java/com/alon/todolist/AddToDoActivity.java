package com.alon.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alon.todolist.utils.MySP;
import com.alon.todolist.utils.models.ToDoUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddToDoActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText add_EDT_title, add_EDT_date, add_EDT_time, add_EDT_description;
    private Button add_BTN_add;
    private FirebaseFirestore db;
    private String id, title, date, time, description;
    private boolean done = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_do);
        db = FirebaseFirestore.getInstance();
        findAll();
        setClickListeners();
    }

    // Method that finds all the views by id.
    private void findAll() {
        add_EDT_title = findViewById(R.id.add_EDT_title);
        add_EDT_date = findViewById(R.id.add_EDT_date);
        add_EDT_time = findViewById(R.id.add_EDT_time);
        add_EDT_description = findViewById(R.id.add_EDT_description);
        add_BTN_add = findViewById(R.id.add_BTN_add);
    }

    // Method that sets click listeners.
    private void setClickListeners(){
        add_BTN_add.setOnClickListener(this);
    }

    // Method that checks all the edit texts.
    private boolean checkInputValidation(){
        if(add_EDT_title.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Please enter to do title", Toast.LENGTH_SHORT).show();
            return false;
        } else if(add_EDT_date.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Please enter to do date", Toast.LENGTH_SHORT).show();
            return false;
        } else if(add_EDT_time.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Please enter to do time", Toast.LENGTH_SHORT).show();
            return false;
        } else if(add_EDT_description.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Please enter to do description", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Method that add to do to the db.
    private void addToDoToDB(String title, String date, String time, String description){
        String phone = MySP.getInstance().getString("Phone", null);
        CollectionReference collectionReference = db.collection("users").document(phone).collection("todos");
        id = collectionReference.document().getId();
        ToDoUtil toDoUtil = new ToDoUtil(id, title, date, time, description, done);
        collectionReference.document(id).set(toDoUtil).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), "You add new ToDo!", Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 3000);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_SHORT).show();
                add_BTN_add.setClickable(false);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_BTN_add:
                add_BTN_add.setClickable(false);
                if(checkInputValidation()){
                    title = add_EDT_title.getText().toString();
                    date = add_EDT_date.getText().toString();
                    time = add_EDT_time.getText().toString();
                    description = add_EDT_description.getText().toString();
                    addToDoToDB(title, date, time, description);
                } else {
                    add_BTN_add.setClickable(true);
                }
                break;
        }
    }
}