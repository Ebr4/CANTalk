package ye.com.ebra.cantalk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import ye.com.ebra.cantalk.R;

public class ContentAdapter extends ArrayAdapter<Item> {

    private Context context;
    // fill items in arraylist
    private ArrayList<Item> items;
    // constructor
    public ContentAdapter(@NonNull Context context, ArrayList<Item> items) {
        super(context,R.layout.activity_item ,items);
        this.context=context;
        this.items=items;
    }
    // hold the contents of contents (images and labels)
    public class Holder{
        ImageView image;
        TextView label;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Item item=getItem(position);
        Holder VHolder;
        // if the template view is empty then fill it or make it empty
        if(convertView == null){
            VHolder = new Holder();
            // inflate the item layout
            LayoutInflater ItemInflater=LayoutInflater.from(getContext());
            // get the item template to fill
            convertView=ItemInflater.inflate(R.layout.activity_item,parent,false);
            // get Holder views to fill
            VHolder.image=convertView.findViewById(R.id.image_id);
            VHolder.label=convertView.findViewById(R.id.name_id);
            convertView.setTag(VHolder);
        }else {
            VHolder=(Holder) convertView.getTag();
        }
        // set the values to image and label views
        VHolder.image.setImageBitmap(convertByteToBitmap(item.getImage()));
        VHolder.label.setText(item.getName());
        return convertView;
    }

    // convert the byte[] to bitmap image ..
    private Bitmap convertByteToBitmap(byte[] bytes){
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

}
