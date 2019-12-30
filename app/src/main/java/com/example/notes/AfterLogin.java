package com.example.notes;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Date;
import java.util.Random;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class AfterLogin extends AppCompatActivity implements FirebaseAuth.AuthStateListener,NotesRecyclerAdapter.NoteListener{
    private static final String TAG = "MainActivity";
    RecyclerView recyclerView;
    NotesRecyclerAdapter notesRecyclerAdapter;      //instance of NotesRecyclerAdapter
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));        //for creating lines between the items in recycle views
        Toolbar toolbar = (Toolbar) findViewById(R.id.mytoolbar);       //setting the toolbar in this activity
        setSupportActionBar(toolbar);
        initRecyclerView(FirebaseAuth.getInstance().getCurrentUser());      //Initializing the recycler View
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });
        FloatingActionButton logOut = findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent toMainActivity = new Intent(AfterLogin.this,MainActivity.class);
                startActivity(toMainActivity);
                Toast.makeText(AfterLogin.this,"Succesfully logged out",Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(AfterLogin.this);
        if (notesRecyclerAdapter != null) {
            notesRecyclerAdapter.stopListening();
        }
    }
    //Initialising Recycler view using query firebase
    public  void initRecyclerView(FirebaseUser user) {
        Query query = FirebaseFirestore.getInstance()
                .collection("notes")
                .whereEqualTo("userId", user.getUid())
                .orderBy("created", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();
        notesRecyclerAdapter = new NotesRecyclerAdapter(options,this);
        recyclerView.setAdapter(notesRecyclerAdapter);
        notesRecyclerAdapter.startListening();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }
    private void showAlertDialog() {
        Random random = new Random();

        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

        final EditText noteEditText = new EditText(this);   noteEditText.setBackgroundColor(color);noteEditText.setMinLines(3);
        new AlertDialog.Builder(this)
                .setTitle("Add Note")
                .setView(noteEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: " + noteEditText.getText());
                        if (!noteEditText.getText().toString().isEmpty())
                            addNote(noteEditText.getText().toString());
                        else
                            Toast.makeText(AfterLogin.this,"Fields Empty !! Failed to add",Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void addNote(final String text) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Note note = new Note(text, false, new Timestamp(new Date()), userId);
        FirebaseFirestore.getInstance()
                .collection("notes")
                .add(note)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "onSuccess: Succesfully added the note...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: " + e.getLocalizedMessage());
                        Toast.makeText(AfterLogin.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
    }
    @Override   // creating menu options in the toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.navigation_drawer,menu);
        return true;
    }
    //setting click listeners for the menu options
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                final TextView tv = new TextView(this); tv.setText("Are you sure you want to Log out ?" +"");
                tv.setTextSize(20);
                new  AlertDialog.Builder(this).setTitle("Log out").setView(tv)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                Intent toMainActivity = new Intent(AfterLogin.this,MainActivity.class);
                                startActivity(toMainActivity);
                                Toast.makeText(AfterLogin.this,"Succesfully logged out",Toast.LENGTH_LONG).show();
                            }
                        }).setNegativeButton("No",null).show();
            case R.id.nav_acc:


            default:
                return super.onOptionsItemSelected(item);
        } }//end of onOptionsItemSelected

                // Handling Changes

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.RIGHT) {
                Toast.makeText(AfterLogin.this, "Deleting", Toast.LENGTH_SHORT).show();

                NotesRecyclerAdapter.NoteViewHolder noteViewHolder = (NotesRecyclerAdapter.NoteViewHolder) viewHolder;
                noteViewHolder.deleteItem();

            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(AfterLogin.this, R.color.colorAccent))
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };


    @Override
    public void EditNote(final DocumentSnapshot snapshot) {
        final Note note = snapshot.toObject(Note.class);
        final EditText editText = new EditText(this);
        editText.setText(note.getText().toString());
        editText.setSelection(note.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Edit Note")
                .setView(editText)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newText = editText.getText().toString();
                        note.setText(newText);
                        snapshot.getReference().set(note)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: ");
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void DeleteItem(DocumentSnapshot snapshot) {

        final DocumentReference documentReference = snapshot.getReference();
        final Note note = snapshot.toObject(Note.class);

        documentReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Item deleted");
                    }
                });

        Snackbar.make(recyclerView, "Deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        documentReference.set(note);
                    }
                })
                .show();

    }

} // end of class