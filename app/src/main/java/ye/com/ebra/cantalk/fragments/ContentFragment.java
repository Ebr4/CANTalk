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

public class ContentFragment extends Fragment {

    static int Cat_id;

    private Item item;
    // to store Category id for Add new
    static ArrayList<Integer> back;

    // send data to sentence bar
    sendItemToSentenceBar connector;

    // text to speech
    TextToSpeech toSpeech;
    int result;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_fragment_layout,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // initialize back Arraylist
        back=new ArrayList<>();

        DBConnection dbConnection= new DBConnection(getActivity());
        GridView gridView=getActivity().findViewById(R.id.items_id_gridView);

        final ArrayList<Item> items2=new ArrayList<>(dbConnection.getAll(Cat_id));

        ContentAdapter dataAdapter=new ContentAdapter(getActivity(),items2);

        gridView.setAdapter(dataAdapter);

        // communicate fragments with each other
        connector=(sendItemToSentenceBar) getActivity();
        //Use TextToSpeech to read images labels vocally
        toSpeech=new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS) {
                    result=toSpeech.setLanguage(Locale.US);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show();
                }}});



        //when an item in the gridView selected do something
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // to control the clicked item ..
                item=items2.get(position);

                //if the selected item is category, view its contents
                if(item.getType().equals("Category"))
                {
                    back.add(Cat_id);
                    changeFragment(item.getID());
                    AddNewDialogFragment.Cat_id=item.getID();

                }else {
                    //else if the selected item is image send it to sentence bar fragment
                    //and read its label vocally
                    speak(item.getName());
                    connector.sendData(item);
                }
            }
        });

        //when an item in the gridView is long pressed, do something
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                Item itemInfo=items2.get(position);

                LongClickPress longPress=new LongClickPress();
                //control the items according to its type
                longPress.setID(itemInfo.getID());
                longPress.setLabel(itemInfo.getName());
                longPress.setType(itemInfo.getType());
                longPress.setCAT_id(Cat_id);

                //shows the dialog
                FragmentManager fragmentManager=getFragmentManager();
                longPress.show(fragmentManager,"long press");


                return true;
            }
        });

    }

    //check if the TextToSpeech is supported in the device
    public void speak(String label){
        //if not supported show message
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

    // keep tracing the category id when user goes inside another category and creates another category
    // then goes inside it and so on
    public static int getBackID(){
        if(back.size()>0){
            int b=back.get(back.size()-1);
            back.remove(back.size() - 1);
            return b;
        }else {
            return 0;
        }
    }

    //update fragment
    public void changeFragment(int CAT_ID){

        ViewContentFragment f_c_s=new ViewContentFragment();
        if(CAT_ID>0){
            f_c_s.setCat_id(CAT_ID);
        }else{
            f_c_s.setCat_id(1);
        }
        getFragmentManager().beginTransaction().replace(R.id.content_Layout,f_c_s).addToBackStack(String.valueOf(CAT_ID)).commit();
    }


}
