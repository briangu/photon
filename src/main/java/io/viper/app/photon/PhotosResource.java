package io.viper.app.photon;

import io.viper.net.common.HttpJSONClient;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@Path("/photos/")
public class PhotosResource
{
  static HttpJSONClient _queryClient;
  static HttpJSONClient _publishClient;
  static Map<String, String> _headers = new HashMap<String, String>();

  static {
    try
    {
      _queryClient = HttpJSONClient.create("http://bguarrac-md:1340/activityviews");
      _publishClient = HttpJSONClient.create("http://bguarrac-md:1338/activities");
      _headers.put("X-LinkedIn-Auth-Member", "1");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
    }
    catch (KeyManagementException e)
    {
      e.printStackTrace();
    }
  }

  @GET
  @Produces("text/javascript")
  @Path("/feed")
  public String getMemberPhotoFeed(@QueryParam("id") String id) {
    List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
    try
    {
      queryParams.add(new BasicNameValuePair("q", "feed"));
      queryParams.add(new BasicNameValuePair("id", String.format("urn:feed:photon:public:a::memberId=urn:member:%s", id)));
      return _queryClient.doQuery(queryParams).toString(2);
    }
    catch (URISyntaxException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (JSONException e)
    {
      e.printStackTrace();
    }
    return "{success: false}";
  }

  @GET
  @Produces("text/javascript")
  @Path("/photo")
  public String getPhotoFeed(@QueryParam("photoId") String id) {
    List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
    try
    {
      queryParams.add(new BasicNameValuePair("q", "feed"));
      queryParams.add(new BasicNameValuePair("id", String.format("urn:feed:photon:photo:a::photoId=urn:photo:%s", id)));
      return _queryClient.doQuery(queryParams).toString(2);
    }
    catch (URISyntaxException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (JSONException e)
    {
      e.printStackTrace();
    }
    return "{success: false}";
  }

  // curl -d "id=45310686&photoId=urn:photo:123&thumbnail=http://farm5.static.flickr.com/4105/4994478045_61d71e0b46_o.jpg" http://bguarrac-md:3000/photos/add
  @POST
  @Produces("text/javascript")
  @Path("/add")
  public String addPhotoEvent(
    @FormParam("id") String id,
    @FormParam("photoId") String photoId,
    @FormParam("thumbnail") String thumbnail,
    @FormParam("url") String url)
  {
    try
    {
      String member = String.format("urn:member:%s", id);
      JSONObject post = new JSONObject();
      post.put("actor", member);
      post.put("verb", "share");

      JSONObject object = new JSONObject();
      JSONArray links = new JSONArray();
      JSONObject link = new JSONObject();
      link.put("title", "");
      link.put("description", "");
      link.put("thumbnail", thumbnail);
      link.put("url", url);
      links.put(link);
      object.put("id", photoId);
      object.put("links", links);
      object.put("body", "a photo");
      post.put("object", object);

      post.put("attributedApplication", "urn:app:photon");
      post.put("attributedEntity", member);
      post.put("destination", member);

      return _publishClient.doPost(post.toString(2), _headers).toString(2);
    }
    catch (URISyntaxException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (JSONException e)
    {
      e.printStackTrace();
    }
    return "{success: false}";
  }

  @POST
  @Produces("text/javascript")
  @Path("/comment")
  public String addPhotoCommentEvent(
    @QueryParam("id") String id,
    @QueryParam("photoId") String photoId,
    @FormParam("thumbnail") String thumbnail,
    @FormParam("url") String url)
  {
    try
    {
      String member = String.format("urn:member:%s", id);
      JSONObject post = new JSONObject();
      post.put("actor", member);
      post.put("verb", "post");

      JSONObject object = new JSONObject();
      JSONArray links = new JSONArray();
      JSONObject link = new JSONObject();
      link.put("thumbnail", thumbnail);
      link.put("url", url);
      links.put(link);
      object.put("id", photoId);
      object.put("links", links);
      object.put("body", "a photo");
      post.put("object", object);

      post.put("attributedApplication", "urn:app:photon");
      post.put("attributedEntity", member);
      post.put("destination", member);

      return _queryClient.doPost(post.toString(2), _headers).toString(2);
    }
    catch (URISyntaxException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (JSONException e)
    {
      e.printStackTrace();
    }
    return "{success: false}";
  }
}

