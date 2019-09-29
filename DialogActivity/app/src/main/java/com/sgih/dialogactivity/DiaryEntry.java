package com.sgih.dialogactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DiaryEntry extends AppCompatActivity {

    String diaryText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_entry);
        final EditText dEntry = findViewById(R.id.Diarytext);
        Button saveBtn = findViewById(R.id.Save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaryText = dEntry.getText().toString();
                Log.i("Diary Entry: ", diaryText);
            }
        });
    }
}
