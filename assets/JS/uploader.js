var classUploader = function() {
	this.currentType = 'none';
	this.autoStart = 1;
	this.currentCount = 0;
	this.maximum = 0;
	this.autoClose = false;
	this.successCB = undefined;
	this.errorCB = undefined;
	this.async = false;
	
	this.typeassoc = {
		'wallpaper' : '[#wallpaper]',
		'file' : '[#files]',
		'none' : ''
	};
	
	this.clear = function() {
		$('#uploaderFileList, #uploadInputContainer').empty();
	};

	this.show = function() {
		$('#globalUploader h3').html(this.typeassoc[this.currentType]);
	
		$('#globalUploader').fadeIn(500);
		$('#uploaderContainer').removeClass('hidden');
	};
	
	this.dismiss = function() {
		$('#globalUploader').fadeOut(500);
		$('#uploaderContainer').addClass('hidden');
	};	
	
	this.addEmptyField = function() {
		var listItem = '<li><button><img src="mediaimg/bigplus.png" width="32px" />[#addfile]</button><span></span>' + '</li>';
		var inputFile = '<input type="file" />';
		var currentIndex = this.currentCount;
		
		$('#uploaderFileList').append(listItem);
		$('#uploadInputContainer').append(inputFile);
		
		$('#uploaderFileList li').eq(currentIndex).find('button').bind('click', function() {
			$('#uploadInputContainer input').eq(currentIndex).click();
		});
		
		$('#uploadInputContainer input').eq(currentIndex).bind('change', this.fileSelected);
		
		this.currentCount++;
	};
	
	this.removeField = function(index) {
		$('#uploaderFileList li').eq(index).remove();
		this.currentCount--;
	};	
	
	this.prevalidate = function() {
		
		
		return true;
	};
	

	
	this.success = function() {
		$('#uploadProgress').addClass('hidden');
		if (this.autoClose) this.dismiss();
		if (!isUndef(this.successCB)) this.successCB();
	};
	
	this.error = function(code) {
		$('#uploadProgress').addClass('hidden');
		if (!isUndef(this.errorCB)) this.errorCB();
	};
	
	this.upload = function(index) {
		var f = $('#uploadInputContainer input')[index].files[0]; 
	
		var xhr = XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject('Microsoft.XMLHttp');
		xhr.open("PUT", 'UPLOAD', true);
		xhr.setRequestHeader("X-File-Name", f.name);
		xhr.setRequestHeader("X-File-Size", f.size);
		
		if(xhr.upload) {
	        xhr.upload.onprogress = function(evt) {
	    		var percentComplete = (evt.loaded / evt.total) * 100;	
	    		$('#uploadProgressBar').css('width', percentComplete + '%');
	        }; 	
        }	
		
		xhr.send(f);
	
		/*
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
			  		dataType: "json",
			  		success:function(data) {
			  			notify.success('UPLOADED!! YESSS!!!!!!!!!!! <3');
			  			
			  			if (index == maximum) this.success();
			  			else this.upload(index+1);
			  		},
			  		
		            xhr: function() {  
		            	// custom xhr
		                var myXhr = $.ajaxSettings.xhr();
		                
		                if(myXhr.upload){ 
		                	// check if upload property exists
		                    myXhr.upload.addEventListener('progress', function(evt) {
					    		var percentComplete = evt.loaded / evt.total * 100;	
					    		$('#uploadProgressBar').css('width', percentComplete + '%');
		                    }, false); 
		                }
		                
		                return myXhr;
		            }		  			  		
				});
	      	}
	      
	      	r.readAsArrayBuffer(f);
	    } else { 
	    	error();
	    	alert("Failed to load file");
	    }	
	    */
	};	
	
	this.begin = function() {
		$('#uploadProgress').removeClass('hidden');
		this.upload(0);
	};	
	
	this.fileSelected = function(fileinput) {
		if (tzUploader.autoStart && tzUploader.maximum == 1 && tzUploader.prevalidate()) {
			tzUploader.begin();
		}
	};	
	
	this.cast = function(opt) {
		this.currentType = defOr(opt.type, 'none');
		this.maximum = defOr(opt.maxFile, 1);
		this.autoStart = defOr(opt.autoStart, false);
		this.autoClose = defOr(opt.autoClose, false);
		this.successCB = opt.success;
		this.errorCB = opt.error;
		this.async = defOr(opt.async, false);
		this.currentCount = 0;
		
		this.clear();
		this.addEmptyField();
		this.show();
	};	
};

var tzUploader = new classUploader();

$(function() {
	$('#uploadCloseBtn').bind('click', function() {
		tzUploader.dismiss();
	});
});	
