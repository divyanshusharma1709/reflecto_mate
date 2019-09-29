package com.sgih.dialogactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.divyanshu.draw.widget.DrawView;

public class DiaryEntry extends AppCompatActivity {
    DrawView draw_view;

    String diaryText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_entry);
        final EditText dEntry = findViewById(R.id.Diarytext);
        draw_view = findViewById(R.id.draw_view);
        Button saveBtn = findViewById(R.id.Save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaryText = dEntry.getText().toString();
                Log.i("Diary Entry: ", diaryText);
                UploadAsyncTask upload = new UploadAsyncTask(diaryText);
                upload.execute(diaryText);
            }
        });
        Button undo = findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_view.undo();
            }
        });
        Button redo = findViewById(R.id.redo);
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_view.redo();
            }
        });
        Button clear = findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_view.clearCanvas();
            }
        });


    }
}
