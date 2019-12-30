package com.example.notes;

import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;

public class NotesRecyclerAdapter extends FirestoreRecyclerAdapter<Note, NotesRecyclerAdapter.NoteViewHolder> {

    private static final String TAG = "NotesRecyclerAdapter";
    NoteListener noteListener;

    public NotesRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Note> options,NoteListener noteListener) {
        super(options);
        this.noteListener = noteListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note note) {

        holder.noteTextView.setText(note.getText());
        CharSequence dateCharSeq = DateFormat.format("EEEE, MMM d, yyyy h:mm:ss a", note.getCreated().toDate());
        holder.dateTextView.setText(dateCharSeq);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.staggered, parent, false);
        return new NoteViewHolder(view);
    }


    class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView noteTextView, dateTextView;        ConstraintLayout parentl;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
          parentl = itemView.findViewById(R.id.cons) ;

            Random random = new Random();   //generating random nos for generating random colours
           int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
            parentl.setBackgroundColor(color);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DocumentSnapshot snapshot = getSnapshots().getSnapshot(getAdapterPosition());
                    noteListener.EditNote(snapshot);

                }
            });
        }

        public void deleteItem() {
            noteListener.DeleteItem(getSnapshots().getSnapshot(getAdapterPosition()));
        }
    }

    interface NoteListener {
        public void EditNote(DocumentSnapshot snapshot);
        public void DeleteItem(DocumentSnapshot snapshot);
    }
}


