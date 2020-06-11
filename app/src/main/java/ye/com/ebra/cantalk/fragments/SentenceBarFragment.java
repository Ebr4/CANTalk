package ye.com.ebra.cantalk.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

import ye.com.ebra.cantalk.R;
import ye.com.ebra.cantalk.adapter.Item;

import static java.lang.Thread.sleep;


public class SentenceBarFragment extends Fragment {
    // store items in arraylist
    private ArrayList<Item> items;
    // to store sentence bar layout
    private LinearLayout sentenceLayout;
    // store items view in arraylist of Views
    private ArrayList<View> itemsViews;
    // to read the sentence vocally
    private TextToSpeech toSpeech;
    private int result;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View sentenceBarFragment=inflater.inflate(R.layout.activity_sentence_bar_fragment,container,false);
        sentenceLayout  =   sentenceBarFragment.findViewById(R.id.sentence_bar);
    return  sentenceBarFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        items=new ArrayList<>();
        itemsViews=new ArrayList<>();
        // trace views in the sentence bar layout
        // if user long pressed the view then last item will be removed
        sentenceLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removeLastItem();
                return true;
            }
        });
        // play button
        ImageButton play = getActivity().findViewById(R.id.play_button);

        // when the play button is clicked
        // if the items array list is not empty read the sentence vocally
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(items.size()<1){
                        Toast.makeText(getActivity().getApplicationContext(),"PLease select an item first :)",Toast.LENGTH_LONG).show();
                    }else {
                        play_sentence();
                    }
                } catch (InterruptedException e) {
                    Toast.makeText(getActivity().getApplicationContext(),"Error Play sentence"+e.toString(),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        // read the Strings vocally with UK language
    toSpeech=new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
             if(status==TextToSpeech.SUCCESS) {
        result=toSpeech.setLanguage(Locale.UK);
        }else{
            Toast.makeText(getActivity().getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show();
            }}});
    }

    // add an item to the sentence bar
    // first create new View and fill it with data of the clicked item
    // second add the view to the arraylist of views in the layout
    public void AddItem(Item item){
        View itemView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_item,sentenceLayout,false);
        ImageView image = itemView.findViewById(R.id.image_id);
        image.setImageBitmap(convertByteToBitmap(item.getImage()));
        items.add(item);
        itemsViews.add(itemView);
        sentenceLayout.addView(itemView);
    }

    // convert the byte[] to bitmap image..
    private Bitmap convertByteToBitmap(byte[] bytes){
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    // check if the device supports text to speech
    // if supported play the labels vocally with 1.5 second space between words
    public void play_sentence() throws InterruptedException {
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
        {
            Toast.makeText(getActivity().getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show();
        }
        else
        {
            //if the play button is pressed read all items' labels vocally
            for(int i=0;i<items.size();i++){
                toSpeech.speak(items.get(i).getName(),TextToSpeech.QUEUE_FLUSH,null);
                sleep(1500);
            }
        }
    }

    //remove the last item in the sentence bar
    public void removeLastItem(){
        if(itemsViews.size()>0){
            sentenceLayout.removeViewAt(itemsViews.size()-1);
            itemsViews.remove(itemsViews.size()-1);
            items.remove(items.size()-1);
        }
    }
}