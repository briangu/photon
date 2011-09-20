$().ready(function() {

  function createUploader()
  {
    var uploader = new qq.FileUploader({
      element: document.getElementById('file-uploader-demo1'),
      action: '/u/',
      debug: false,
      params: params,
      allowedExtensions: ["jpg", "jpeg", "gif", "png", "tiff", "bmp"],
      onComplete: function(id, filename, responseJSON) {
        if (!responseJSON.success) return;
        $.post('/photos/add',
               {
                 id: params.id,
                 thumbnail: responseJSON.url,
                 url: responseJSON.url,
                 photoId: "urn:photo:"+responseJSON.key
               },
               function(response) {
                 getFeed();
               });
      }
      });
  }

  function fixBrokenImages() {
    $("img").each(function() {
      this.onerror = function () {
        this.src = "/images/no-image.png";
        console.log("An error occurred loading the image.");
      };
    });
  }

  function showSpinner() {
    var spinner = new Spinner().spin();
    spinner.el.className = "spinner";
    $('#stream-updates').html(spinner.el);
  }

  function getFeed() {
    showSpinner();

    /**
     * Get Feed
     */
     $.getJSON('/photos/feed?id='+params.id, function(data) {
       var r = 0;
       var c = 0;

       $.each(data.elements, function(key, activity) {
         var imageHTML = renderActivity(activity, "#template-photo-image");
         var ownerHTML = renderActivity(activity, "#template-photo-owner");
         $('#ir'+r+'c'+c).html(imageHTML);
         $('#or'+r+'c'+c).html(ownerHTML);

         c++;
         if (c % 3 == 0) r++;
         c %= 3;
       });

       fixBrokenImages();
//       showTimeAgoDates();
     });
  }

  function showTimeAgoDates() {
    // Show timeago
    $(".easydate").each(function() {
      var previousDate = parseInt($(this).attr("rel"), 10);
      var date = new Date(previousDate);
      $(this).html($.easydate.format_date(new Date(previousDate)));
    });
  }

   /**
    * Just takes an activity and template id, and adds the resulting html to the stream
    */
   function renderActivity(activity, templateId) {
     var template = $(templateId).html();
     return Mustache.to_html(template, activity);
   }

   createUploader();
   getFeed();
});