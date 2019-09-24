package com.example.bussiness_card_scanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
Button Button_image;
TextView text_apper,result_data;
ImageView image_apper;
Bitmap image_photo;
FirebaseVisionImage firebase_obj;
String[] data_string;
HashMap<String,Integer> map_list;
boolean number_check;
String number_text,email_text,name_text,before_email_text;
StringBuffer buffer;
ArrayList<Integer> Count_array;
ArrayList<String> name_text_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button_image=findViewById(R.id.Button_image);
        image_apper=findViewById(R.id.image_view);
        text_apper=findViewById(R.id.text_view);
        result_data=findViewById(R.id.result_data);
        buffer=new StringBuffer();
        map_list=new HashMap<>();
        Count_array=new ArrayList<>();
        name_text_list=new ArrayList<>();

//        data_string=new ArrayList<>();

        Button_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this).setTitle("Take Image from").setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA}, 101);
                        }
                        else
                        {
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, 102);
                        }
                    }
                }).setNegativeButton("File Storage", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 104);
                        }
                        else
                        {
                            Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(cameraIntent, 103);
                        }

                    }
                });
                dialog.show();
                dialog.create();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==102)
        {
            if (data != null) {
                image_photo = (Bitmap) data.getExtras().get("data");
                image_apper.setImageBitmap(image_photo);
                choose_image(image_photo);
            }
        }
        else  if(resultCode==RESULT_OK && requestCode==103){
            Uri selectedImage = data.getData();
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();
            image_photo = (BitmapFactory.decodeFile(picturePath));
            image_apper.setImageBitmap(image_photo);

             choose_image(image_photo);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101)
        {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 102);
        }else if(requestCode==104)
        {
            Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(cameraIntent, 103);
        }
    }

    public void choose_image(Bitmap picture)
    {
        firebase_obj=FirebaseVisionImage.fromBitmap(picture);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> result =
                detector.processImage(firebase_obj)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...
                                buffer.setLength(0);
                                name_text="";
                                email_text="";
                                number_text="";
                                before_email_text="";
                                int count_match=0;
                                String text_field=firebaseVisionText.getText();

                                String[] data_arr=text_field.split("\n");

                                for(int bb=0;bb<data_arr.length;bb++)
                                {
                                   buffer.append(data_arr[bb]).append(" ");
                                   if(data_arr[bb].toLowerCase().contains("pvt. ltd.")||
                                           data_arr[bb].toLowerCase().contains("pvt.ltd.")||
                                           data_arr[bb].toLowerCase().contains("private limited")||
                                           data_arr[bb].toLowerCase().contains("dr.")||
                                           data_arr[bb].toLowerCase().contains("dr ")||
                                           data_arr[bb].toLowerCase().contains("mr.")||
                                           data_arr[bb].toLowerCase().contains("mr ")||
                                           data_arr[bb].toLowerCase().contains("ms.")||
                                           data_arr[bb].toLowerCase().contains("ms ")||
                                           data_arr[bb].toLowerCase().contains("er.")||
                                           data_arr[bb].toLowerCase().contains("er ")||
                                           data_arr[bb].toLowerCase().contains("advt.")||
                                           data_arr[bb].toLowerCase().contains("adv.")||
                                           data_arr[bb].toLowerCase().contains("services"))
                                   {
                                       name_text_list.add(data_arr[bb]);
                                       //name_text=data_arr[bb];
                                       Log.e("match_pattern",name_text);
                                   }

                                    String p1="((\\+)?(0)?\\d{1,3})?(\\s|-)?(\\()?[1-9](\\s|-|\\.)?\\d(\\s|-|\\.)?\\d(\\))?(\\s|-|\\.)?\\d(\\s|-|\\.)?\\d(\\s|-|\\.)?\\d(\\s|-|\\.)?\\d(\\s|-|\\.)?\\d(\\s|-|\\.)?\\d(\\s|-|\\.)?\\d";
                                    Matcher m= Pattern.compile(p1).matcher(data_arr[bb]);
                                    if(m.find()){
                                        number_text=m.group();
                                    }
                                }

                                if(number_text!=null && !number_text.equals(""))
                                {
                                    StringBuffer bh=new StringBuffer();
                                    for(int c=0;c<number_text.length();c++)
                                    {
                                        if(number_text.charAt(c)>=48 && number_text.charAt(c)<=57)
                                        {
                                            bh.append(number_text.charAt(c));
                                        }
                                    }
                                    if(bh.length()>10)
                                    {
                                        String tem=bh.reverse().substring(0,10);
                                        bh.setLength(0);
                                        bh.append(tem);
                                        number_text=bh.reverse().toString();
                                    }
                                    else {
                                        number_text=bh.toString();
                                    }

                                }

                                Log.e("buffer_string",buffer.toString());

                                data_string=buffer.toString().split(" ");

                                text_apper.setText(text_field);
                                Integer[] count_array=new Integer[data_string.length];

                                Count_array.clear();
                                for(int k=0;k<data_string.length;k++)
                                {
                                    int count=0;

                                    for(int hh=0;hh<data_string.length;hh++)
                                    {
                                        if(data_string[hh].toLowerCase().contains(data_string[k].toLowerCase()))
                                        {
                                            count=count+1;
                                            //map_list.put(data_string[k],count);
                                            Log.e("map_values",data_string[k]);
                                        }
                                        Count_array.add(k,count);
//                                        count_array[k]=count;
                                    }


                                    Log.e("string_data",data_string[k]);
                                    if(data_string[k].contains("@") && data_string[k].length()>5)
                                    {
                                        email_text=data_string[k];
                                        String[] email_before_string=data_string[k].split("@");
                                        before_email_text=email_before_string[0];
                                    }
                                }

                                String greater_temp="";
                                StringBuffer greater_string=new StringBuffer();
                                int greaterno=0,no_pos=0,temp=0,lar=0;

                                for(int cc=0;cc<data_string.length-1;cc++)
                                {
                                    for(int x=cc+1;x<data_string.length;x++)
                                    {
                                        if(Count_array.get(cc)<Count_array.get(x))
                                        {
                                            greater_temp=data_string[x];
                                            data_string[x]=data_string[cc];
                                            data_string[cc]=greater_temp;

                                            temp=Count_array.get(x);
                                            Count_array.set(x,Count_array.get(cc));
                                            Count_array.set(cc,temp);
                                        }
                                    }
                                }

                                ArrayList<String> possible_coucomes=new ArrayList<>();
                                for(int ss=0;ss<data_string.length;ss++)
                                {
                                    System.out.println("arrray--"+data_string[ss]);
                                    if(data_string[ss].length()>2 && before_email_text.toLowerCase().contains(data_string[ss].toLowerCase()) && !data_string[ss].contains("@"))
                                    {
                                        possible_coucomes.add(data_string[ss]);
                                    }
                                }

                                int temp_len=0;
                                for(int v=0;v<possible_coucomes.size();v++)
                                {
                                    if(temp_len<possible_coucomes.get(v).length())
                                    {
                                        temp_len=possible_coucomes.get(v).length();
                                        no_pos=v;
                                    }
                                }

                                String gg="";
                                if(name_text_list.size()>0)
                                {
                                    for(int nn=0;nn<name_text_list.size();nn++)
                                    {
                                        name_text=name_text_list.get(nn);
                                        if(name_text.equals(""))
                                        {
                                            if(possible_coucomes.size()>0)
                                            {
                                                gg="email:"+email_text+"\nphone:"+number_text+"\nname:"+possible_coucomes.get(no_pos);
                                            }
                                            else {
                                                gg="email:"+email_text+"\nphone:"+number_text+"\nname:"+"";
                                            }

                                        }
                                        else{
                                            String[] bb=name_text.split(" ");
                                            boolean bb_check=false;
                                            for(int b=0;b<bb.length;b++)
                                            {
                                                if(before_email_text.toLowerCase().contains(bb[b].toLowerCase()))
                                                {
                                                    bb_check=true;
                                                }
                                            }
                                            if(bb_check)
                                            {
                                                gg="email:"+email_text+"\nphone:"+number_text+"\nname:"+name_text;
                                                break;
                                            }
                                            else {
                                                if(possible_coucomes.size()>0)
                                                {
                                                    gg="email:"+email_text+"\nphone:"+number_text+"\nname:"+possible_coucomes.get(no_pos);
                                                }
                                                else {
                                                    gg="email:"+email_text+"\nphone:"+number_text+"\nname:"+"";
                                                }
                                                break;
                                            }

                                        }
                                    }
                                }
                                else {
                                    if(possible_coucomes.size()>0)
                                    {
                                        gg="email:"+email_text+"\nphone:"+number_text+"\nname:"+possible_coucomes.get(no_pos);
                                    }
                                    else {
                                        gg="email:"+email_text+"\nphone:"+number_text+"\nname:"+"";
                                    }

                                }

                                result_data.setText(gg);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
    }

}
