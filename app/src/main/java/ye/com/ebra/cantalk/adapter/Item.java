package ye.com.ebra.cantalk.adapter;

public class Item {

    /*
    *
    *
    *  Item class to create images or categories with required attributes
    *
    */
    private int id;
    private String name;
    private byte[] image;
    private int cat_id;
    private String type;

    public Item(int ID, String Name, byte[] Image, int Cat_id, String Type) {
        this.id = ID;
        this.name = Name;
        this.image = Image;
        this.cat_id=Cat_id;
        this.type=Type;
    }
    public Item() {

    }
    public Item(String Name, byte[] Image, int Cat_id, String Type) {
        this.name = Name;
        this.image = Image;
        this.cat_id=Cat_id;
        this.type=Type;
    }


    // getting ID
    public int getID(){
        return this.id;
    }
    // setting id
    public void setID(int id){
        this.id = id;
    }

    public int getCat_id() {
        return cat_id;
    }

    public void setCat_id(int cat_id) {
        this.cat_id = cat_id;
    }

    // getting first name
    public String getName(){
        return this.name;
    }

    // setting first name
    public void setName(String fname){
        this.name = fname;
    }

    //getting profile pic
    public byte[] getImage(){
        return this.image;
    }

    //setting profile pic
    public void setImage(byte[] b){
        this.image=b;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
