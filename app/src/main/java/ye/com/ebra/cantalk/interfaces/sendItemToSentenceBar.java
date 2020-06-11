package ye.com.ebra.cantalk.interfaces;
import ye.com.ebra.cantalk.adapter.Item;

public interface sendItemToSentenceBar {
    // send item to the sentence bar fragment :)
    void sendData(Item item);
    // remove the last item from sentence bar :)
    void removeData();
}
