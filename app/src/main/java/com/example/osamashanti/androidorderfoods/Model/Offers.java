package com.example.osamashanti.androidorderfoods.Model;

public class Offers {

    private String Image;
    private String Title;
    private String Type;
    private String Min;
    private String Delivery;

    public Offers() {
    }

    public Offers(String image, String title, String type, String min, String delivery) {
        Image = image;
        Title = title;
        Type = type;
        Min = min;
        Delivery = delivery;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getMin() {
        return Min;
    }

    public void setMin(String min) {
        Min = min;
    }

    public String getDelivery() {
        return Delivery;
    }

    public void setDelivery(String delivery) {
        Delivery = delivery;
    }
}
