package com.example.saish.charvisionmarkiv;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Block;
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


public class GoogleCloudVision {

    public static String run_OCR(Context c, Bitmap picture) {
        String data = "", textword = "";


        //IMPORTANT_CORE_SECTION:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


        //creating vision object
        Vision.Builder visionbuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null
        );

        String key = c.getString(R.string.mykey);
        visionbuilder.setVisionRequestInitializer(new VisionRequestInitializer(key));
        final Vision vision = visionbuilder.build();

        //Encoding the Image.
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        //initially picture is to be compressed
        picture.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
        // String base64Data = Base64.encodeToString(byteStream.toByteArray(), Base64.URL_SAFE);


        Image inputImage = new Image();
        inputImage.encodeContent(byteStream.toByteArray());

        Feature desiredFeatures = new Feature();
        desiredFeatures.setType("TEXT_DETECTION");


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

                                data = data + "\n\n";
                                data = data + textword;
                                data = data + "\n";
                                textword = "";
                            }
                        }
                    }
                }

                return data;

            } catch (IOException e) {
                e.printStackTrace();
                return "ERROR";
            }
        }
        return "ERROR";
    }
}
