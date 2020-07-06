package com.alon.todolist.utils.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.alon.todolist.R;
import com.alon.todolist.ToDoDetailsActivity;
import com.alon.todolist.utils.models.ToDoUtil;

import java.util.ArrayList;

public class ToDosRecyclerViewAdapter extends RecyclerView.Adapter<ToDosRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<ToDoUtil> mDataset;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Context context;
        private TextView todo_TXT_num, todo_TXT_title, todo_TXT_date, todo_TXT_time;
        private ImageView todo_IMG_done;
        private ToDoUtil toDoUtil;

        public MyViewHolder(View v) {
            super(v);
            context = v.getContext();
            todo_TXT_num = v.findViewById(R.id.todo_TXT_num);
            todo_TXT_title = v.findViewById(R.id.todo_TXT_title);
            todo_TXT_date = v.findViewById(R.id.todo_TXT_date);
            todo_TXT_time = v.findViewById(R.id.todo_TXT_time);
            todo_IMG_done = v.findViewById(R.id.todo_IMG_done);
            v.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            startToDoDetailsActivity();
        }

        // Method that start the to do details activity.
        private void startToDoDetailsActivity() {
            Intent intent = new Intent(context, ToDoDetailsActivity.class);
            intent.putExtra("id", toDoUtil.getId());
            intent.putExtra("title", toDoUtil.getTitle());
            intent.putExtra("date", toDoUtil.getDate());
            intent.putExtra("time", toDoUtil.getTime());
            intent.putExtra("description", toDoUtil.getDescription());
            context.startActivity(intent);
        }
    }

    // Constructor.
    public ToDosRecyclerViewAdapter(ArrayList<ToDoUtil> myDataset) {
        mDataset = myDataset;
    }

    // Create new views.
    @Override
    public ToDosRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_row, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    // Method that replaces the contents of a view.
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.todo_TXT_num.setText(String.valueOf(position + 1));
        holder.todo_TXT_title.setText(mDataset.get(position).getTitle());
        holder.todo_TXT_date.setText(mDataset.get(position).getDate());
        holder.todo_TXT_time.setText(mDataset.get(position).getTime());
        holder.toDoUtil = mDataset.get(position);

    }

    // Method that returns the size of the dataset.
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}
