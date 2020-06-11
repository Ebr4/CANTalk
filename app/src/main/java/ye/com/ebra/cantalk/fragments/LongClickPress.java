package ye.com.ebra.cantalk.fragments;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import ye.com.ebra.cantalk.R;
import ye.com.ebra.cantalk.database.DBConnection;

import static android.app.Activity.RESULT_OK;

public class LongClickPress extends DialogFragment implements View.OnClickListener {
    View LongPressDialog;
    private int ID,CAT_id;
    private String Label,type;

    FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        LongPressDialog =inflater.inflate(R.layout.activity_long_click_press_dialog,container,false);

        // try to remove Dialog Title then if the api throws any exception the catch statement will fix it.
        try{
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        }catch (NullPointerException n){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
            }
        }
        TextView renameBtn,updatePhotoBtn,removeBtn;

        renameBtn       =LongPressDialog.findViewById(R.id.rename_id);
        updatePhotoBtn  =LongPressDialog.findViewById(R.id.updatePhoto_id);
        removeBtn       =LongPressDialog.findViewById(R.id.remove_id);

        renameBtn.setOnClickListener(this);
        updatePhotoBtn.setOnClickListener(this);
        removeBtn.setOnClickListener(this);

        fragmentManager=getFragmentManager();

        return LongPressDialog;
    }

    //if one of the options in the long press menu is selected, control the item
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rename_id:
                getReName();
                dismiss();
                break;
            case R.id.updatePhoto_id:
                getUpdate();
                dismiss();
                break;
            case R.id.remove_id:
                getRemove();
                dismiss();
                break;
        }
    }

    public void getReName(){
        Rename rename=new Rename();
        rename.id=ID;
        rename.name=Label;
        rename.type=type;
        rename.CAT_ID=CAT_id;
        rename.show(fragmentManager,"rename tag");
    }

    public void getUpdate(){
        UpdatePhoto updatePhoto=new UpdatePhoto();
        updatePhoto.id=ID;
        updatePhoto.type=type;
        updatePhoto.CAT_ID=CAT_id;
        updatePhoto.show(fragmentManager,"update tag");
    }

    public void getRemove(){
        Remove remove=new Remove();
        remove.type=type;
        remove.id=ID;
        remove.CAT_ID=CAT_id;
        remove.show(fragmentManager,"remove tag");
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public  void setLabel(String label) {
        this.Label = label;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCAT_id(int CAT_id) {
        this.CAT_id = CAT_id;
    }

    public static class Rename extends DialogFragment implements View.OnClickListener{
        View renameView;
        private EditText renameEditText;
        //private Button renameButton,cancel;
        private TextView renameButton,cancel;
        private int id,CAT_ID;
        private String name,type;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            renameView      =   inflater.inflate(R.layout.activity_rename,container,false);
            // try to remove Dialog Title then if the api throws any exception the catch statement will fix it.
            try{
                getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            }catch (NullPointerException n){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
                }
            }
            renameEditText  =   renameView.findViewById(R.id.rename_editText_id);
            renameButton    =   renameView.findViewById(R.id.rename_button_id);
            cancel          =   renameView.findViewById(R.id.rename_cancel_button_id);

            renameEditText.setHint(name);

            renameButton.setOnClickListener(this);
            cancel.setOnClickListener(this);

            return renameView;
        }

        //if the rename option is selected for the long press menu
        //get the item's label and update the fragment if changed
        //if cancelled, dismiss the dialog
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rename_button_id:
                    DBConnection dbConnection=new DBConnection(renameView.getContext());

                    if(dbConnection.rename(id,type,renameEditText.getText().toString()) == 1){
                        updateFragment(CAT_ID);
                        dismiss();
                        Toast.makeText(getActivity(),type+" has been renamed successfully",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getActivity(),"Rename Error !!",Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                    break;
                case R.id.rename_cancel_button_id:
                    dismiss();
                    break;
            }
        }

        public void updateFragment(int CAT_ID){
            // Content fragment second ((f_c_s) Omar had forgot to edit the name )
            ViewContentFragment f_c_s=new ViewContentFragment();
            if(CAT_ID>0){
                f_c_s.setCat_id(CAT_ID);
            }else{
                f_c_s.setCat_id(1);
            }
            // replace the fragment with the new fragment and add it to BackStack,
            // then pop it from BackStack to get to the old fragment without overlapping the
            // old fragments...

            getFragmentManager().beginTransaction().replace(R.id.content_Layout,f_c_s).
                    addToBackStack(String.valueOf(CAT_ID)).commit();
            getFragmentManager().popBackStack();
        }
    }

    public static class UpdatePhoto extends DialogFragment implements View.OnClickListener{
        View updateView;
        private ImageButton chooseImage;
        private byte[] newImage;
        private Bitmap DecodeImage_stream;
        private TextView update,cancel;
        private int id,CAT_ID;
        private String type;


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            updateView      =   inflater.inflate(R.layout.activity_update_photo,container,false);
            // try to remove Dialog Title then if the api throws any exception the catch statement will fix it.
            try{
                getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            }catch (NullPointerException n){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
                }
            }            chooseImage              =   updateView.findViewById(R.id.choose_image);
            update                      =   updateView.findViewById(R.id.update_ok_id);
            cancel                      =   updateView.findViewById(R.id.update_cancel_id);

            chooseImage.setOnClickListener(this);
            update.setOnClickListener(this);
            cancel.setOnClickListener(this);

            return updateView;
        }

        //if the update photo option is selected from the long press menu
        //let the user open the gallery
        //when an image is selected from the gallery update the item's image
        //if cancelled, dismiss the dialog
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.choose_image:
                    openGallery();
                    break;
                case R.id.update_ok_id:

                    try{
                        DBConnection dbConnection=new DBConnection(updateView.getContext());
                        // byte[] image
                        newImage=getImageByteArray(DecodeImage_stream);
                        dbConnection.updatePhoto(id,type,newImage);
                        updateFragment(CAT_ID);
                        Toast.makeText(getActivity(),type+" photo has been updated successfully",Toast.LENGTH_LONG).show();
                        dismiss();
                    }catch (NullPointerException n){
                        Toast.makeText(getActivity(),"you haven't chose any photo :)",Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                    break;
                case R.id.update_cancel_id:
                    dismiss();
                    break;
            }
        }

        // opens gallery and select image..
        public void openGallery(){
            Intent intentImg=new Intent(Intent.ACTION_GET_CONTENT);
            intentImg.setType("image/*");
            startActivityForResult(intentImg,200);
        }
        // controls the image from OpenGallery method..
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(resultCode == RESULT_OK && requestCode ==200){
                Uri uri_img=data.getData();
                try{
                    assert uri_img != null;
                    InputStream input_img   = updateView.getContext().getContentResolver().openInputStream(uri_img);
                    DecodeImage_stream      = BitmapFactory.decodeStream(input_img);
                    chooseImage.setImageBitmap(DecodeImage_stream);
                }catch (FileNotFoundException f){
                    Log.d("add new file not found:",f.getMessage());
                }
            }
        }

        // transform Bitmap image into Byte[]
        public byte[] getImageByteArray(Bitmap bitmap) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }

        public void updateFragment(int CAT_ID){

            ViewContentFragment f_c_s=new ViewContentFragment();
            if(CAT_ID>0){
                f_c_s.setCat_id(CAT_ID);
            }else{
                f_c_s.setCat_id(1);
            }
            // replace the fragment with the new fragment and add it to BackStack,
            // then pop it from BackStack to get to the old fragment without overlapping the old fragments...
            getFragmentManager().beginTransaction().replace(R.id.content_Layout,f_c_s).addToBackStack(String.valueOf(CAT_ID)).commit();
            getFragmentManager().popBackStack();
        }

    }

    public static class Remove extends DialogFragment implements View.OnClickListener{
        View removeView;
        private TextView removeTextView,remove,cancel;
        private int id,CAT_ID;
        private String type;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            removeView      =   inflater.inflate(R.layout.activity_long_click_ensure_remove,container,false);
            // try to remove Dialog Title then if the api throws any exception the catch statement will fix it.
            try{
                getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            }catch (NullPointerException n){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Objects.requireNonNull(getDialog().getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
                }
            }
            remove              =   removeView.findViewById(R.id.remove_yes_id);
            removeTextView      =   removeView.findViewById(R.id.remove_textView_id);
            cancel              =   removeView.findViewById(R.id.remove_cancel_id);

            if(type.equals("Category")) {
                removeTextView.setText("Are you sure you want to remove this category and all of its contents");
            }else{
                removeTextView.setText("Are you sure you want to remove this item");
            }

            remove.setOnClickListener(this);
            cancel.setOnClickListener(this);

            return removeView;
        }

        //if the remove option is selected from the long press menu
        //remove the item from the fragment and the database
        //if cancelled, dismiss the dialog
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.remove_yes_id:
                    DBConnection dbConnection=new DBConnection(removeView.getContext());
                    if(dbConnection.remove(id,type)==1){
                        Toast.makeText(getActivity(),type+" has been removed successfully",Toast.LENGTH_LONG).show();
                        updateFragment(CAT_ID);
                        dismiss();
                    }else{
                        Toast.makeText(getActivity(),"Remove Error ",Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                    break;
                case R.id.remove_cancel_id:
                    dismiss();
                    break;
            }
        }
        public void updateFragment(int CAT_ID){

            ViewContentFragment f_c_s=new ViewContentFragment();
            if(CAT_ID>0){
                f_c_s.setCat_id(CAT_ID);
            }else{
                f_c_s.setCat_id(1);
            }
            // replace the fragment with the new fragment and add it to BackStack,
            // then pop it from BackStack to get to the old fragment without overlapping the old fragments...
            getFragmentManager().beginTransaction().replace(R.id.content_Layout,f_c_s).addToBackStack(String.valueOf(CAT_ID)).commit();
            getFragmentManager().popBackStack();
        }
    }


}