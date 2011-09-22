$().ready(function() {

  function createUploader()
  {
    var pub_uploader = new qq.FileUploader({
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

      var my_uploader = new qq.FileUploader({
        element: document.getElementById('my-file-uploader-demo1'),
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

    $.getJSON('/photos/feed?id='+params.id, function(data) {
       var r = 0;
       var c = 0;

       $.each(data.elements, function(key, activity) {
         var imageHTML = renderActivity(activity, "#template-photo-image");
         var ownerHTML = renderActivity(activity, "#template-photo-owner");
         $('#ir'+r+'c'+c).html(imageHTML);
         $('#or'+r+'c'+c).html(ownerHTML);

         activityIdMap[activity.object.id] = activity.id;

         c++;
         if (c % 3 == 0) r++;
         c %= 3;
       });

       fixBrokenImages();
       showTimeAgoDates();
       activateLightbox("lb_pub");
     });

      $.getJSON('/photos/myfeed?id='+params.id, function(data) {
         var r = 0;
         var c = 0;

         $.each(data.elements, function(key, activity) {
           var imageHTML = renderActivity(activity, "#template-photo-image-mine");
           var ownerHTML = renderActivity(activity, "#template-photo-owner");
           $('#my_ir'+r+'c'+c).html(imageHTML);
           $('#my_or'+r+'c'+c).html(ownerHTML);

           activityIdMap[activity.object.id] = activity.id;

           c++;
           if (c % 3 == 0) r++;
           c %= 3;
         });

         fixBrokenImages();
         showTimeAgoDates();
         activateLightbox("lb_mine");

         // add inline comment boxes
         $(".add-comment").click(function () {
           m = this.id.match("add-comment-(.*)");
           activityUrn = m[1];
           $("#comment-box-" + activityUrn).show(500);
         });

         $(".comment-button").click(function () {
           commentBox = $(this).parents(".comment-box")
           commentTextArea = commentBox.find(".comment-textarea")
           commentText = commentTextArea.val();
           if (commentText.length > 0) {
             m = commentBox.attr('id').match("comment-box-urn_activity_(.*)");
             activityUrn = m[1]
             $.ajax({
               type: 'POST',
               url: '/activities/' + activityUrn + '/comments',
               data: "message=" +escape(commentText),
               success: function(data) {
                 commentTextArea.val("");
                 commentBox.hide(500);
                 li = $('<li style="display: none">' + commentText + "</li>")
                 commentBox.siblings('ol').append(li);
                 li.fadeIn(500);
               },
               error: function(data, textStatus, errorThrown) {
                 alert("Error posting comment: " + errorThrown);
               }
             });
           } else {
             commentBox.effect('shake', { times: 2 }, 200);
           }
         });

         $(".cancel-comment").click(function () {
           $(this).parents(".comment-box").hide(500);
         });
       });
  }

  function activateLightbox(clazz) {
   $('a.'+clazz).lightBox({
     fixedNavigation:true,
     imageDataFn: imageData,
     containerResizeSpeed: 350
   });
  }

  function imageData(imgArr, actimg) {
    var url = imgArr[actimg][0];
    var photoId = url.substring(url.lastIndexOf("/")+1);
    var threadId = activityIdMap["urn:photo:"+photoId];

    var html = "";
    $.ajax({
        async: false,
        dataType: "json",
        url: '/photos/photocomments/?threadId='+threadId,
        success: function(data) {
            var i = 0;
            $.each(data.elements, function(key, activity) {
                if (i++ >= 3) return;
                html += renderActivity(activity, "#template-photo-comment");
            })

            if (i >= 3) {
                html += "<div><a href='#'>Show all comments</a></div>";
            }
        }
    });

    html += "<br/><a href='"+url+"' target='_blank'>"+url+"</a><br/>";

    return html;
  }

  function showTimeAgoDates() {
    // Show timeago
    $(".easydate").each(function() {
      var previousDate = parseInt($(this).attr("rel"), 10);
      var date = new Date(previousDate);
      $(this).html($.easydate.format_date(date));
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