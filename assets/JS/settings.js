// Settings panel management
// Depends on jQuery

var gPreferences = {};
var existingWallpapers = [];

$(function() {
	$('#wallpaperFileInput').bind('change', uploadWallpaper);
	$('#wallpaperUploadButton').bind('click', function() {
		tzUploader.cast({
			maxFile : 1,
			type : 'wallpaper',
			autoStart : true,
			autoClose : true
		});
	});
	
	$('#settingsList li').bind('click', function() {
		if (!$(this).hasClass('selectedpref')) {
			$('#settingsList li').removeClass('selectedpref');
			$(this).addClass('selectedpref');
			
			$('.settingTab').hide('blind', {direction:'right'}, 300);
			$('#' + $(this).attr('id') + 'Tab').show('blind', {direction:'left'}, 300);
			
			playCursor(); 
		}
	});
	
	$('#settingLangList li').bind('click', function() {
		playTick();
		
		$.ajax({
			url: 'PREF',
			method: 'POST', 	
			data:JSON.stringify({action:'CLNG',data:{'lang':$(this).attr('name')}}),		
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			success: function(data) {
				restart();
			}
		});			
	});
	
	$('#themeList li').bind('click', function() {
		playTick();
		var name = $(this).attr('name');
		
		$.ajax({
			url: 'PREF',
			method: 'POST', 	
			data:JSON.stringify({action:'CPRF',data:{'pref':'theme','value':name}}),		
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			success: function(data) {
				restart();
			}
		});			
	});
	
	$('#settingDeviceRingtone li').bind('click', function() {
		castLoading();
		playTick();
		var name = $(this).attr('name');
		
		$.ajax({
			url: 'PREF',
			method: 'POST', 	
			data:JSON.stringify({action:'CRNG',data:{'type':name}}),		
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			success: function(data) {
				dismissLoading();
			}
		});			
	});	
});

var updatePrefInterface = function() {
	$('.toggleButton').each(function() {
		var name = $(this).attr('name');
		var active = getPreference(name);
			
		$(this).bind('click', function() {
			if ($(this).hasClass('on')) {
				$(this).removeClass('on');
				$(this).addClass('off');
				setPreference(name, false);
			} else {
				$(this).removeClass('off');
				$(this).addClass('on');
				setPreference(name, true);
			}
			
			playTick();
		});
		
		$(this).addClass(active ? 'on' : 'off');
	});
	
	$('.TzColorpicker').each(function() {
		var name = $(this).attr('name');
		var color = getPreference(name);
		$(this).css('background-color', color);
		
		$(this).bind('click', function() {
			var btn = $(this);
			
			pickColor(color, function() {
				var rgb = colorPickerValue;
				var rgbStr = 'rgba(' + rgb.r + ', ' + rgb.g + ', ' + rgb.b + ', 0.7)';
				
				btn.css('background-color', rgbStr);
				setPreference(name, rgbStr);
			});
		});
	});
	
	for (var i = 0; i < existingWallpapers.length; i++) {
		var listItem = '<li style="background-image:url(TZWP/'+existingWallpapers[i]+');"></li>';
		$('#settingWallpaperList').append(listItem);
	}
}

var loadPreferences = function(bAsync, finished, fail) {
	$.ajax({
		url: 'PREF',
		method: 'POST', 	
		data:JSON.stringify({action:'GPRF',data:{'get':'all'}}),		
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		async: bAsync,
		success: function(data) {
			gPreferences = data.prefs.prefs;
			existingWallpapers = data.wallpapers;
			updatePrefInterface();
			finished();
		},
		error: fail
	});	
};

var uploadWallpaper = function(file) {
	var f = file.target.files[0]; 

    if (f) {
    	var r = new FileReader();
    	
    	r.onload = function(event) { 
			var contents = event.target.result;
		  	
			var arrBuff = new ArrayBuffer(contents.length);
			var writer = new Uint8Array(arrBuff);
			for (var i = 0, len = contents.length; i < len; i++) {
			    writer[i] = contents.charCodeAt(i);
			}	 	
		  	
			$.ajax({
		  		url:"UPLOAD",
		  		method:"POST",
		  		contentType:"application/octet-stream; charset=x-user-defined",
		  		data:r.result,
		  		processData: false,
		  		success:function(data) {
		  			notify.success('UPLOADED!! YESSS!!!!!!!!!!! <3');
		  		},
		  		
	            xhr: function() {  
	            	// custom xhr
	                var myXhr = $.ajaxSettings.xhr();
	                
	                if(myXhr.upload){ 
	                	// check if upload property exists
	                    myXhr.upload.addEventListener('progress', function(evt) {
				    		var percentComplete = evt.loaded / evt.total;	
				    		$('#uploadProgress').html((Math.floor(percentComplete * 100)).toString() + "%" + "  (" + (evt.total - evt.loaded).toString() + " bytes remaining)");
	                    
	                    }, false); 
	                }
	                
	                return myXhr;
	            }		  			  		
			});
      	}
      
      	r.readAsArrayBuffer(f);
    } else { 
    	alert("Failed to load file");
    }	
}