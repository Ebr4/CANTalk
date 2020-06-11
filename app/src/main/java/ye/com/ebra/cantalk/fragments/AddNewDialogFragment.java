package ye.com.ebra.cantalk.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.support.v7.app.AlertDialog;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ye.com.ebra.cantalk.R;
import ye.com.ebra.cantalk.adapter.Item;
import ye.com.ebra.cantalk.database.DBConnection;
import ye.com.ebra.cantalk.database.SharedPref;

import static android.app.Activity.RESULT_OK;
import static android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK;
import static android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
import static android.app.AlertDialog.THEME_HOLO_DARK;
import static android.app.AlertDialog.THEME_HOLO_LIGHT;
import static android.app.AlertDialog.THEME_TRADITIONAL;


public class AddNewDialogFragment extends DialogFragment implements View.OnClickListener {

    // Permissions that the program will need to achieve its job in correct way
    private final String[] permissions =
            {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            };
    private static final int MULTIPLE_PERMISSIONS = 123;
    private static final int REQUEST_IMAGE_CAPTURE=1;
    private String PhotoPath;

    View view;
    // options to choose if the user wants to add category or image
    private RadioButton Image_option,Category_option;
    // store the users' option as text !
    private String type="Image";
    // choose image
    private ImageButton choose_image;
    // save the content into the database
    private Button done;
    // label image or category
    private EditText Label;
    // id of the category required for image to be added in the correct category!
    public static int Cat_id;
    // information that will be stored in the database ;)
    private byte[] Image;
    private String StringLabel;
    private Bitmap DecodeImage_stream;
    private DBConnection dbConnection;

    // for theme
    SharedPref sharedpref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        // Initialize variables and set the views into JAVA variables ..
        view    =   inflater.inflate(R.layout.add_new_activity,container,false);
        choose_image        = view.findViewById(R.id.choose_image);
        Label               = view.findViewById(R.id.image_name_editText);
        done                = view.findViewById(R.id.add_done);
        dbConnection        = new DBConnection(getActivity().getApplicationContext());

        Image_option        = view.findViewById(R.id.image_option);
        Category_option     = view.findViewById(R.id.category_option);


        Image_option.setChecked(true);
        Image_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Category_option.setChecked(false);
                setType("Image");

            }
        });
        Category_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Image_option.setChecked(false);
                setType("Category");

            }
        });
        choose_image.setOnClickListener(this);
        done.setOnClickListener(this);

        // Dialog Title
        getDialog().setTitle("Add new");

        //Dialog title color
        sharedpref=new SharedPref(getActivity().getApplicationContext());
        if(sharedpref.loadNightModeState()){
            this.getDialog().getWindow().setBackgroundDrawableResource(R.color.tintcolordark);

            Label.setHintTextColor(Color.WHITE);
            Label.setTextColor(Color.WHITE);
            // set the dialog title to the night mode ..
            getDialog().setTitle(Html.fromHtml("<font color='#FFFFFF'>Add new</font>"));
            }
        else { this.getDialog().getWindow().setBackgroundDrawableResource(R.color.tintcolor);
            Label.setHintTextColor(Color.BLACK);
            Label.setTextColor(Color.BLACK);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_image:
                AddPhoto();
                //openGallery();
                break;
            case R.id.add_done:
                Save();
                dismiss();
                break;
        }
    }
    // sets the type image or category
    public void setType(String type) {
        this.type = type;
    }
    // sets the Category image and its name
    public void getInfo() {
        // get label and image information
        StringLabel = Label.getText().toString();
        Image       = getImageByteArray(DecodeImage_stream);
    }

    // Save into the database
    public void Save() {
        if(type.equals("Category")){

            // insert into categories table
            try {
                getInfo();
                dbConnection.InsertItemCategory(new Item(StringLabel,Image,Cat_id,type));
                Toast.makeText(getActivity().getApplicationContext(), "Category ADDED SUCCESSFULLY", Toast.LENGTH_LONG).show();
            } catch (NullPointerException e) {
                // set the default image if the user doesn't choose one !
                Bitmap defaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.image_24dp);
                dbConnection.InsertItemCategory(new Item(StringLabel,getImageByteArray(defaultImage),Cat_id,type));
                Toast.makeText(getActivity().getApplicationContext(), "Category ADDED SUCCESSFULLY", Toast.LENGTH_LONG).show();
            }
            finally {
                // to get back to the fragment ;) (refresh the fragment)
                changeFragment(Cat_id);
            }
        }else {
            // insert into images table
            try {
                getInfo();
                dbConnection.InsertItemImage(new Item(StringLabel,Image,Cat_id,type));
                Toast.makeText(getActivity().getApplicationContext(), "PICTURE ADDED SUCCESSFULLY", Toast.LENGTH_LONG).show();
            } catch (NullPointerException e) {
                // set the default image if the user doesn't choose one !
                Bitmap defaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.image_24dp);
                dbConnection.InsertItemImage(new Item(StringLabel,getImageByteArray(defaultImage),Cat_id,type));
                Toast.makeText(getActivity().getApplicationContext(), "PICTURE ADDED SUCCESSFULLY", Toast.LENGTH_LONG).show();
            } finally {
                // to get back to the fragment ;) (refresh the fragment)
                changeFragment(Cat_id);
            }
        }
    }
    /////////// add photo methods :) be happy Omar ///////
    private void AddPhoto() {
        final  AlertDialog.Builder builder;
        if (sharedpref.loadNightModeState()){
            builder = new AlertDialog.Builder(view.getContext(), THEME_HOLO_DARK);

        }else{
            builder = new AlertDialog.Builder(view.getContext(), THEME_HOLO_LIGHT);
        }
        builder.setTitle("Add Photo");

        // 1 select
        builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                openGallery();
                dialog.dismiss();
                //contentPadding: EdgeInsets.all(0.0);
            }
        });

        // 2 Camera
        builder.setNeutralButton("Camera", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // check permissions for in case user denied them before
                        if(checkPermissions()) {
                            takeAPhotoIntent();
                        }
                        dialog.dismiss();
                    }
                });

        // 3 clear
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    // opens gallery and select image..
    public void openGallery(){
        Intent intentImg=new Intent(Intent.ACTION_GET_CONTENT);
        intentImg.setType("image/*");
        if (intentImg.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intentImg,200);
        }
    }
    // take a photo using camera program
    private void takeAPhotoIntent(){
        // or  takePictureIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE)
        Intent takePictureIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    // controls the image from OpenGallery and takeAPhotoIntent intents..
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == 200) {
            Uri uri_img = data.getData();
            try {
                assert uri_img != null;
                InputStream input_img = view.getContext().getContentResolver().openInputStream(uri_img);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                // when the selected image size is more than 1 MB
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;
                String imageType = options.outMimeType;

                options.inSampleSize = calculateInSampleSize(options, 500,500);
                DecodeImage_stream = BitmapFactory.decodeStream(input_img,null,options);

                Toast.makeText(getActivity(),"Image size "+DecodeImage_stream.getByteCount(),Toast.LENGTH_LONG).show();
                choose_image.setImageBitmap(DecodeImage_stream);

            } catch (FileNotFoundException f) {
                Log.d("add new file not found:", f.getMessage());
        }
        }
        else if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            Bundle extra = data.getExtras();
            // image saved into this Bitmap variable
            DecodeImage_stream = (Bitmap) extra.get("data");
            choose_image.setImageBitmap(DecodeImage_stream);

            try{
                PhotoPath=createImageName();
                onCaptureImageResult(data);
            } catch (Exception e) {
                Log.e("Take photo error: ",e.getMessage());
            }
        }
    }
    ////////////////////////////////////////////////// control the image size when selected by the user ..

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /////////////////////////////////////////////////////////////////////
    // Create a file name and path for the image ..
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assert thumbnail != null;
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File destination = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),PhotoPath);
        FileOutputStream fo=null;
        try {
            // Create a private image file in PICTURES folder for the program
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(fo != null)
                    fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String createImageName(){
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "JPEG_" + timeStamp + "_" +".jpg";
    }

    // transform Bitmap image into Byte[]
    public byte[] getImageByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    //Request permission
    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getActivity().getBaseContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity()
                    , listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()])
                    , MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    //////////////////////////////// update View //////////////////////
    public void changeFragment(int CAT_ID){
        ViewContentFragment f_c_s=new ViewContentFragment();
        f_c_s.setCat_id(CAT_ID);
        // replace the fragment with the new fragment and add it to BackStack,
        // then pop it from BackStack to get to the old fragment without overlapping the old fragments...
        getFragmentManager().beginTransaction().replace(R.id.content_Layout,f_c_s).addToBackStack(String.valueOf(CAT_ID)).commit();
        getFragmentManager().popBackStack();
    }
}