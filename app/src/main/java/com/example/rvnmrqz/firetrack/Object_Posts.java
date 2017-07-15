package com.example.rvnmrqz.firetrack;

/**
 * Created by arvin on 6/22/2017.
 */

public class Object_Posts {
    String _id;
 //   String encoded_poster_image;
    String poster_name;
    String datetime;
    String message;
    String encoded_post_picture;

    //   public Object_Posts(String _id, String encoded_poster_image, String poster_name, String datetime, String message, String encoded_post_picture)
    public Object_Posts(String _id, String poster_name, String datetime, String message, String encoded_post_picture){
        super();
        this._id=_id;
      //  this.encoded_poster_image = encoded_poster_image;
        this.poster_name = poster_name;
        this.datetime = datetime;
        this.message = message;
        this.encoded_post_picture = encoded_post_picture;
    }

    public String get_id(){
        return  _id;
    }
    public void set_id(String _id){
        this._id=_id;
    }

  /*  public String getEncoded_poster_image(){
        return encoded_poster_image;
    }
    public void setEncoded_poster_image(String encoded_poster_image){
        this.encoded_poster_image = encoded_poster_image;
    }*/

    public String getPoster_name(){
        return poster_name;
    }
    public void setPoster_name(String poster_name){
        this.poster_name=poster_name;
    }

    public String getDatetime(){
        return  datetime;
    }
    public void setDatetime(String datetime){
        this.datetime = datetime;
    }

    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message=message;
    }

    public String getEncoded_post_picture(){
        return encoded_post_picture;
    }
    public void setEncoded_post_picture(String encoded_post_picture){
        this.encoded_post_picture=encoded_post_picture;
    }


}
