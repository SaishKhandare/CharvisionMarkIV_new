package com.example.saish.charvisionmarkiv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Block;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Page;
import com.google.api.services.vision.v1.model.Paragraph;
import com.google.api.services.vision.v1.model.Symbol;
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.google.api.services.vision.v1.model.Word;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //reference website https://code.tutsplus.com/tutorials/how-to-use-google-cloud-machine-learning-services-for-android--cms-28630

    ImageView imgview;
    Button analyze;
    Button capture;
    TextView textview;
    Bitmap picture;
    Bitmap cropped;

    String msg, data, textword = "";

    int nametrap = 0;
    int totaltrap = 0;
    int istrapedname = 0;
    int istrapedtotal = 0;
    int result = 0; // used in condition check function

    String totalv = "";
    String namev = "";



    String[] namearr = new String[]{"name","m/s.","m/s","name:"};
    String[] totalarr = new String[]{"total"};


    BatchAnnotateImagesRequest batchRequest;
    BatchAnnotateImagesResponse batchResponse;
    Vision vision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgview = (ImageView) findViewById(R.id.imageView);
        analyze = (Button) findViewById(R.id.button);
        capture = (Button) findViewById(R.id.button2);
        textview = (TextView) findViewById(R.id.textView);


        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable mydraw = getResources().getDrawable(R.drawable.bill2);
                picture = ((BitmapDrawable) mydraw).getBitmap();
                imgview.setImageBitmap(picture);

                int height = ((int) (picture.getHeight() * 0.05));
                int width = ((int) (picture.getWidth() * 0.5));
                cropped  = Bitmap.createBitmap(picture,38,216,width,height);
                imgview.setImageBitmap(cropped);
            }
        });

        analyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textview.setText("done");

                //checking internet permission
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                    textview.setText("got internet connection!");
                }

                //creating vision object
                Vision.Builder visionbuilder = new Vision.Builder(
                        new NetHttpTransport(),
                        new AndroidJsonFactory(),
                        null
                );
                String key = getResources().getString(R.string.mykey);
                visionbuilder.setVisionRequestInitializer(new VisionRequestInitializer(key));
                final Vision vision = visionbuilder.build();

                //Encoding the Image.
                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                //initially picture is to be compressed
                cropped.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
                String base64Data = Base64.encodeToString(byteStream.toByteArray(), Base64.URL_SAFE);


                Image inputImage = new Image();
                inputImage.encodeContent(byteStream.toByteArray());

                Feature desiredFeatures = new Feature();
                desiredFeatures.setType("DOCUMENT_TEXT_DETECTION");


                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);
                request.setFeatures(Arrays.asList(desiredFeatures));

                BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));

                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    //your codes here

                    try {
                        textview.setText("Analysis in progress..");
                        BatchAnnotateImagesResponse batchResponse = vision.images().annotate(batchRequest).execute();

                        //using Textannotation
                        final TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();
                        for (Page page : text.getPages()) {
                            for (Block block : page.getBlocks()) {
                                data = data + "\n new_block_start";

                                for (Paragraph para : block.getParagraphs()) {
                                    data = data + "\n new_para_start";
                                    for (Word word : para.getWords()) {

                                        for (Symbol symbol : word.getSymbols()) {
                                            textword = textword + symbol.getText().toString();
                                        }

                                        //trap for name:-------------------------------------------
                                        if (nametrap >= 1) {
                                            namev = namev + " | " + textword;
                                            nametrap = nametrap - 1;
                                        }
                                        //sets the trap for name:
                                        name_trap(textword);
                                        // trap for name ends:-------------------------------------

                                        // trap for total:-----------------------------------------
                                        if (totaltrap >= 1) {
                                            totalv = totalv + "|" + textword;
                                            totaltrap = totaltrap - 1;
                                        }
                                        //sets trap for total
                                        total_trap(textword);
                                        //Trap for total ends here---------------------------------

                                        data = data + "\n\n";
                                        data = data + textword + word.getBoundingBox();
                                        data = data + "\n";
                                        textword = "";
                                    }
                                }
                            }
                        }

                        //finalyse data:----------------------------------------------------------------
                        finalise_data();
                        //-------------------------------------------------------------------------------

                    } catch (IOException e) {
                        e.printStackTrace();
                        textview.setText("ERROR IN BatchAnnotateImagesResponse batchResponse check line 102 // creating vision object");
                    }
                }

            }
        });
    }


    //FUNCTION TO TRAP NAME uses name_condition
    public void name_trap(String textword) {
        if(istrapedname == 0)
        {
            if (name_condition(textword)) {
                nametrap = 3;
                data = data + "\ntrapped name here \n";
                istrapedname = 1;
            }
        }
    }

    //FUNCTION TO TRAP TOTAL uses total_condition
    public void total_trap(String textword) {

        if(istrapedtotal==0)
        {
            if (total_condition(textword)) {
                totaltrap = 2;
                data = data + "\ntrapped total here \n";
                istrapedtotal = 1;
            }
        }
    }


    //FUNCTION FOR NAME CONDITION to Iterate through name_arr
    public boolean name_condition(String textword)
    {
        for(int h=0;h < namearr.length;h++)
        {
            if(textword.trim().toLowerCase().equals(namearr[h]))
            {
                result=1;
            }
        }
        if(result==1)
        {
            result=0;
            return true;
        }
        else
        {
            result=0;
            return false;
        }
    }

    //FUNCTION FOR TOTAL CONDITION to Iterate through total_arr
    public boolean total_condition(String textword)
    {
        for(int h=0;h < totalarr.length;h++)
        {
            if(textword.trim().toLowerCase().equals(totalarr[h]))
            {
                result=1;
            }
        }
        if(result==1)
        {
            result=0;
            return true;
        }
        else
        {
            result=0;
            return false;
        }
    }

    //FUNCTION TO finalisedata
    public void finalise_data() {
        data = data + "ABOVE ARE THE RESULTS";
        data = data + "\n Recipient:" + namev;
        data = data + "\n TOTAL" + totalv;
        data = (data + "\n NAME TRAP : " + istrapedname);
        data = (data + "\n TOTAL TRAP : " + istrapedtotal);
        textview.setText(data);
    }

}







































































//using listing responses.
/**
 List<AnnotateImageResponse> responses =batchResponse.getResponses();
 for(AnnotateImageResponse res : responses)
 {
 for(EntityAnnotation annotate : res.getTextAnnotations() )
 {
 data = data + "DESCRIPTION: \n\n";
 data = data + annotate.getDescription();
 data = data + "BOUNDINGS: \n\n";
 data = data + annotate.getBoundingPoly();
 }
 }

 **/