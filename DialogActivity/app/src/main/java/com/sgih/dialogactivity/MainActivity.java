package com.sgih.dialogactivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraCharacteristics;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.AudioEncoding;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.InputAudioConfig;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.gson.JsonElement;
import com.google.protobuf.ByteString;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.contract.Scores;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.yalantis.guillotine.animation.GuillotineAnimation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.api.AIListener;
import ai.api.AIServiceContext;
import ai.api.AIServiceContextBuilder;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener {
    Intent diaryIntent;
    AIService aiService;
    String currentPhotoPath;
    AIDataService aiDataService;
    AIServiceContext customAIServiceContext;
    AIRequest aiRequest;
    TextToSpeech ttobj;
    ScrollView sv;
    EditText et;
    TextView tv;
    RecyclerView recyclerView;
    List<Message> messageList;
    Button toD, listen;
    final int REQUEST_IMAGE_CAPTURE = 1;
    EmotionServiceClient restClient;
    MediaRecorder recorder;
    InputAudioConfig inputAudioConfig;
    ByteArrayOutputStream byteArrayOutputStream;
    int clicked = 0;
    Toolbar toolbar;
    FrameLayout root;
    View contentHamburger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        restClient = new EmotionServiceRestClient("","https://westcentralus.api.cognitive.microsoft.com/face/v1.0");
        final String subscriptionKey = System.getenv("FACE_SUBSCRIPTION_KEY");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sv = findViewById(R.id.scroll);
        toolbar = findViewById(R.id.toolbar);
        root = findViewById(R.id.root);
        et = findViewById(R.id.et);

        contentHamburger = findViewById(R.id.content_hamburger);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(null);
        }
        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine, null);
        root.addView(guillotineMenu);
        new GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.guillotine_hamburger), contentHamburger)
                .setStartDelay(1)
                .setActionBarViewForAnimation(toolbar)
                .setClosedOnStart(true)
                .build();
        TextView personalBtn = findViewById(R.id.Personal);
        TextView dashBtn = findViewById(R.id.dashboard);
        TextView diaryBtn = findViewById(R.id.diarybtn);
        TextView recBtn = findViewById(R.id.recommendations);
        TextView rewBtn = findViewById(R.id.rewards);
        TextView logout = findViewById(R.id.logout);
        personalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Personal.class);
                startActivity(intent);
            }
        });
        dashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Dashboard.class);
                startActivity(intent);
            }
        });
        diaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DiaryEntry.class);
                startActivity(intent);
            }
        });
        recBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Recommendations.class);
                startActivity(intent);
            }
        });
        rewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Rewards.class);
                startActivity(intent);
            }
        });
        Button sendBtn = findViewById(R.id.Send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });



        messageList = new ArrayList<Message>();
        listen = findViewById(R.id.listen);
        recyclerView = (RecyclerView) findViewById(R.id.chatRV);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        final AIConfiguration config = new AIConfiguration("f6ca2cfe8ae24e728ac525cfd2a37bc9", AIConfiguration.SupportedLanguages.EnglishUS, AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        aiDataService = new AIDataService(this, config);
        customAIServiceContext = AIServiceContextBuilder.buildFromSessionId("17");
        aiRequest = new AIRequest();

        diaryIntent = new Intent(MainActivity.this, DiaryEntry.class);
        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Started Listening", Toast.LENGTH_SHORT).show();
                aiService.startListening();
            }
        });

    }
    private void sendMessage(View view) {
        String msg = et.getText().toString();
        et.setText("");
        if(msg.trim().isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter Query!", Toast.LENGTH_LONG).show();
        }
        else {
            aiRequest.setQuery(msg);
            MyAsyncTask requestTask = new MyAsyncTask(MainActivity.this, aiDataService, customAIServiceContext);
            requestTask.execute(aiRequest);
        }
    }
public void callback(AIResponse aiResponse) {
    if (aiResponse != null) {
        // process aiResponse here
        String botReply = aiResponse.getResult().getFulfillment().getSpeech();
        messageList.add(new Message(aiResponse.getResult().getFulfillment().getSpeech(), aiResponse.getResult().getResolvedQuery()));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter (MainActivity.this, messageList);
        recyclerView.setAdapter (adapter);
        Log.d("Callback", "Bot Reply: " + botReply);

    } else {
        Log.d("Callback", "Bot Reply: Null");
        tv.setText("There was some communication issue. Please Try again!");
    }
}
    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        // Show results in TextView.
        messageList.add(new Message(result.getFulfillment().getSpeech(), result.getResolvedQuery()));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter (MainActivity.this, messageList);
        recyclerView.setAdapter (adapter);

        sv.fullScroll(View.FOCUS_DOWN);

        final String res = result.getFulfillment().getSpeech();
        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS)
                {
                    ttobj.setLanguage(Locale.UK);
                    ttobj.speak(res, TextToSpeech.QUEUE_FLUSH, null, null);


                }
            }
        });
        if(result.getFulfillment().getSpeech().contains("yes")  && result.getFulfillment().getSpeech().contains("photo")) {
            dispatchTakePictureIntent();
        }
            else if(result.getFulfillment().getSpeech().contains("yes")){
                Intent intent = new Intent(MainActivity.this, DiaryEntry.class);
                startActivity(intent);
        }
            else if(result.getFulfillment().getSpeech().contains("diary"))
        {
            Intent intent = new Intent(MainActivity.this, DiaryEntry.class);
            startActivity(intent);
        }
    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                "face",  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", CameraCharacteristics.LENS_FACING_FRONT);  // Tested on API 24 Android version 7.0(Samsung S6)
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", CameraCharacteristics.LENS_FACING_FRONT); // Tested on API 27 Android version 8.0(Nexus 6P)
                takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            }
     else {takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1); // Tested API 21 Android version 5.0.1(Samsung S4)
        }
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.sgih.dialogactivity.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(1, 1, null);
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }
        String photoPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/" + files[0].getName();
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        if(bitmap == null)
        {
            Toast.makeText(getApplicationContext(), "Null Bitmap", Toast.LENGTH_SHORT).show();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
        AsyncTask<InputStream, String, List<RecognizeResult>> processAsync = new AsyncTask<InputStream, String, List<RecognizeResult>>() {
            @Override
            protected void onPostExecute(List<RecognizeResult> recognizeResults) {
                Toast.makeText(getApplicationContext(), "Finished!", Toast.LENGTH_SHORT).show();
                for (RecognizeResult res : recognizeResults) {
                    List<Double> list = new ArrayList<>();
                    String emotion = "None";
                    Scores scores = res.scores;
                    list.add(scores.surprise);
                    list.add(scores.anger);
                    list.add(scores.contempt);
                    list.add(scores.disgust);
                    list.add(scores.fear);
                    list.add(scores.neutral);
                    list.add(scores.happiness);
                    list.add(scores.sadness);
                    Collections.sort(list);
                    double maxNum = list.get(list.size() - 1);
                    if (maxNum == scores.surprise)
                        emotion = "Surprise";
                    if (maxNum == scores.anger)
                        emotion = "Anger";
                    if (maxNum == scores.contempt)
                        emotion = "Contempt";
                    if (maxNum == scores.disgust)
                        emotion = "Disgust";
                    if (maxNum == scores.fear)
                        emotion = "Fear";
                    if (maxNum == scores.neutral)
                        emotion = "Neutral";
                    if (maxNum == scores.happiness)
                        emotion = "Happiness";
                    if (maxNum == scores.sadness)
                        emotion = "Sadness";
                    Log.i("Emotion: ", emotion);

                }
            }

            @Override
            protected List<RecognizeResult> doInBackground(InputStream... inputStreams) {

                List<RecognizeResult> results = null;
                try{
                    results = restClient.recognizeImage(inputStreams[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (EmotionServiceException e) {
                    e.printStackTrace();
                }
                return results;
            }
        };
//        processAsync.execute(bs);
    }
}

