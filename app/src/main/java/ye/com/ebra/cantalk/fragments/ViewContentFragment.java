package ye.com.ebra.cantalk.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import ye.com.ebra.cantalk.R;
import ye.com.ebra.cantalk.adapter.ContentAdapter;
import ye.com.ebra.cantalk.adapter.Item;
import ye.com.ebra.cantalk.database.DBConnection;
import ye.com.ebra.cantalk.interfaces.sendItemToSentenceBar;

public class ViewContentFragment extends Fragment {
    private int Cat_id;
    private Item item;
    static ViewContentFragment f_c_s;

    // send data to sentence bar
     sendItemToSentenceBar connector;
    // text to speech
     TextToSpeech toSpeech;
     int result;

    public void setCat_id(int CAT_ID){
        this.Cat_id=CAT_ID;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.activity_view_images,container,false);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DBConnection dbConnection       =new DBConnection(getActivity());
        GridView gridView               =getActivity().findViewById(R.id.Images_gridView);

        final ArrayList<Item> items2 = new ArrayList<>(dbConnection.getAll(Cat_id));

        ContentAdapter dataAdapter=new ContentAdapter(getActivity(), items2);
        gridView.setAdapter(dataAdapter);
        // communicate fragments with each other
        connector=(sendItemToSentenceBar) getActivity();
        toSpeech=new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS)
                {
                    result=toSpeech.setLanguage(Locale.US);
                }
                else
                {
                    Toast.makeText(getActivity().getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //when an item in gridView is selected do something
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                item=items2.get(position);
                //if the item is category, show its contents
                if(item.getType().equals("Category"))
                {
                    // add the category id to back list ..
                    // to use it when user click back button .. find the right category id
                    ContentFragment.back.add(Cat_id);
                    changeFragment(item.getID());
                    AddNewDialogFragment.Cat_id=item.getID();

                }else {
                    //else if the item is image, read its label vocally
                    //and send it to the sentence bar fragment
                    speak(item.getName());
                    connector.sendData(item);
                }
            }
        });
        //if an item in gridView is long pressed, do something
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Item itemInfo=items2.get(position);

                LongClickPress longPress=new LongClickPress();
                //control the items
                longPress.setID(itemInfo.getID());
                longPress.setLabel(itemInfo.getName());
                longPress.setType(itemInfo.getType());
                longPress.setCAT_id(Cat_id);
                //show a dialog for the long pressed item
                FragmentManager fragmentManager=getFragmentManager();
                longPress.show(fragmentManager,"long press");


                return true;
            }
        });
    }

    //check if the TextToSpeech is supported in the device
    public void speak(String label){
        //if not supported show message to the user
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
        {
            Toast.makeText(getActivity().getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show();
        }
        else
        {
            //if supported read the label vocally
            toSpeech.speak(label,TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    //update the fragment
    public  void changeFragment(int CAT_ID) {
        f_c_s = new ViewContentFragment();
        f_c_s.setCat_id(CAT_ID);
        getFragmentManager().beginTransaction().replace(R.id.content_Layout, f_c_s).addToBackStack(String.valueOf(CAT_ID)).commit();
    }
}
