package com.alon.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.alon.todolist.utils.MySP;
import com.alon.todolist.utils.models.ToDoUtil;
import com.alon.todolist.utils.adapters.ToDosRecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;


public class ToDoListActivity extends AppCompatActivity implements View.OnClickListener {

    private Button todo_BTN_add;
    private RecyclerView todo_RCV_todos;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ToDoUtil> toDosArrayList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);
        db = FirebaseFirestore.getInstance();
        findAll();
        setClickListeners();
        startService();
    }

    private void startService(){
        actionToService(BackgroundService.START_FOREGROUND_SERVICE);
    }

    private void actionToService(String action) {
        Intent startIntent = new Intent(ToDoListActivity.this, BackgroundService.class);
        startIntent.setAction(action);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent);
        } else {
            startService(startIntent);
        }
    }

    // Method that finds all the views by id.
    private void findAll(){
        todo_BTN_add = findViewById(R.id.todo_BTN_add);
        todo_RCV_todos = findViewById(R.id.todo_RCV_todos);
    }

    // Method that sets click listeners.
    private void setClickListeners(){
        todo_BTN_add.setOnClickListener(this);
    }

    // Method that init the recycler view.
    private void initRecyclerView(){
        todo_RCV_todos.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        todo_RCV_todos.setLayoutManager(layoutManager);
        mAdapter = new ToDosRecyclerViewAdapter(toDosArrayList);
        todo_RCV_todos.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.todo_BTN_add:
                startAddToDoActivity();
                break;
        }
    }

    // Method that start new to do activity.
    private void startAddToDoActivity() {
        Intent intent = new Intent(ToDoListActivity.this, AddToDoActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(todo_RCV_todos != null){
            todo_RCV_todos.removeAllViewsInLayout();
        }
        toDosArrayList.clear();
        getDataFromDB();
    }

    // Method that gets all to dos of user from db.
    private void getDataFromDB() {
        String phone = MySP.getInstance().getString("Phone", null);
        db.collection("users").document(phone).collection("todos").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        ToDoUtil toDoUtil = documentSnapshot.toObject(ToDoUtil.class);
                        toDosArrayList.add(toDoUtil);
                    }
                    initRecyclerView();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}