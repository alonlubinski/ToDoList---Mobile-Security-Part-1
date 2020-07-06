package com.alon.todolist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

public class ToDoDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private String id, title, date, time, description;
    private TextView details_TXT_title, details_TXT_date, details_TXT_time, details_TXT_description;
    private Button details_BTN_finish, details_BTN_delete;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_details);
        db = FirebaseFirestore.getInstance();
        findAll();
        setClickListeners();
        if(getIntent().getExtras() != null){
            id = getIntent().getStringExtra("id");
            title = getIntent().getStringExtra("title");
            date = getIntent().getStringExtra("date");
            time = getIntent().getStringExtra("time");
            description = getIntent().getStringExtra("description");
            initDetails();
        }
    }

    // Method that finds all the views by id.
    private void findAll() {
        details_TXT_title = findViewById(R.id.details_EDT_title);
        details_TXT_date = findViewById(R.id.details_EDT_date);
        details_TXT_time = findViewById(R.id.details_EDT_time);
        details_TXT_description = findViewById(R.id.details_EDT_description);
        details_BTN_finish = findViewById(R.id.details_BTN_finish);
        details_BTN_delete = findViewById(R.id.details_BTN_delete);
    }

    // Method that sets click listeners.
    private void setClickListeners(){
        details_BTN_finish.setOnClickListener(this);
        details_BTN_delete.setOnClickListener(this);
    }

    // Method that init all the details to ui.
    private void initDetails() {
        details_TXT_title.setText(title);
        details_TXT_date.setText(date);
        details_TXT_time.setText(time);
        details_TXT_description.setText(description);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.details_BTN_finish:

                break;

            case R.id.details_BTN_delete:

                break;
        }
    }
}