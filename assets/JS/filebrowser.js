// Depends on jQuery
// File browsing and transfers

var fileBrowserVars = {
	currentElement : '',
	currentClipMethod : '',
	folderCur : 1
};

$(function() {
	getSessionCurrentFolder();
	$('#photoViewerWrapper .photoViewerCloseBtn').click(function() {
		$('#photoViewerWrapper').fadeOut(500);
	});
	
	$('#bfctView').click(function() {
		displayImageDownload(fileBrowserVars.currentElement);
	});
	
	$('#bfctPlaymedia').click(function() {
		$('#mediaPreviewPlayer').attr('src', fileBrowserVars.currentElement + '?tz=DOWNLOAD');
		document.getElementById('mediaPreviewPlayer').play();
		
		castMediaPreview(fileBrowserVars.currentElement);
	});
	
	$('#bfctCopy').click(function() {
		castLoading();
		fileBrowserVars.currentClipMethod = 'copy';
		
		sendClipboardPath(fileBrowserVars.currentElement);
	});
	
	$('#bfctCut').click(function() {
		castLoading();
		fileBrowserVars.currentClipMethod = 'cut';
		
		sendClipboardPath(fileBrowserVars.currentElement);
	});	
	
	$('#bfctDelete').click(function() {
		if (confirm('[#deleteconfirm]')) {
			castLoading();
			deleteFile(fileBrowserVars.currentElement);
		}
	});		
	
	$('#browserPasteButton').click(function() {
		castLoading();
		hidePasteButton();
		
		if (fileBrowserVars.currentClipMethod == 'cut') cutFile();
		else if (fileBrowserVars.currentClipMethod == 'copy') copyFile();
	});
	
	$('#bfctPlayvideo').click(function() {
		$('#videoPreviewPlayer').attr('src', fileBrowserVars.currentElement + '?tz=DOWNLOAD');
		document.getElementById('videoPreviewPlayer').play();
		
		castVideoPreview(fileBrowserVars.currentElement);
	});
	
	$('#mediaPreviewContainer')
		.draggable({ handle: "#mediaPreviewHandle", containment: "#TzWindowArea" })
		.resizable({ maxHeight: 30, minWidth:300, maxWidth:800, handles: 'e, w' });

	$('#videoPreviewContainer')
		.draggable({ handle: "#videoPreviewHandle", containment: "#TzWindowArea" })
		.resizable({ minHeight: 240, minWidth:360 });
		
		
	$('#mediaPreviewMinimize').bind('click', function() { $('#mediaPreviewContainer').fadeOut(200); });
	$('#mediaPreviewClose').bind('click', function() { 
		removeNotificationType('media'); 
		$('#mediaPreviewContainer').fadeOut(200); 
		document.getElementById('mediaPreviewPlayer').pause();
	});
	
	$('#videoPreviewMinimize').bind('click', function() { $('#videoPreviewContainer').fadeOut(200); });
	$('#videoPreviewClose').bind('click', function() { 
		removeNotificationType('video'); 
		$('#videoPreviewContainer').fadeOut(200); 
		document.getElementById('videoPreviewPlayer').pause();
	});	
});

var sendClipboardPath = function(filename) {
	$.ajax({
		url:'File',
		method:'POST',
		data:JSON.stringify({action:'COPY',data:{'file':filename}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success:function(data){
			if (data.response == 'ok') {
				dismissLoading();
				$('#browserPasteFilename').html(filename);
				showPasteButton();
			}
		}		
	});	
}

var showPasteButton = function() {
	$('#browserPasteButton').show('clip');
}

var hidePasteButton = function() {
	$('#browserPasteButton').hide('clip');
}

var cutFile = function() {
	$.ajax({
		url:'File',
		method:'POST',
		data:JSON.stringify({action:'MOVE',data:{'path':''}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success:function(data){
			if (data.response == 'ok') {
				getSessionCurrentFolder();
				notify.success('[#clipboardmove]', '[#clipboard]');
			} else {
				notify.error('[#clipboardfail]', '[#clipboard]');
			}
			
			dismissLoading();
		}		
	});		
}

var copyFile = function() {
	$.ajax({
		url:'File',
		method:'POST',
		data:JSON.stringify({action:'CPST',data:{'path':''}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success:function(data){
			if (data.response == 'ok') {
				getSessionCurrentFolder();
				notify.success('[#clipboardpaste]', '[#clipboard]');
			}else {
				notify.error('[#clipboardfail]', '[#clipboard]');
			}
			
			dismissLoading();
		}		
	});	
}

var deleteFile = function(filename) {
	$.ajax({
		url:'File',
		method:'POST',
		data:JSON.stringify({action:'DELF',data:{'file':filename}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success:function(data){
			if (data.response == 'ok') {
				getSessionCurrentFolder();
				notify.success('[#filedeleted]', '[#filemenu]');
			}else {
				notify.error('[#clipboardfail]', '[#filemenu]');
			}
			
			dismissLoading();
		}		
	});	
}

var castMediaPreview = function(filename) {
	$('#mediaPreviewContainer').fadeIn(200);
	createNotification('media', false, function() {
		$('#mediaPreviewContainer').fadeIn(200);
	});
	
	$('#TzMediaTitle').html(filename);
}

var castVideoPreview = function(filename) {
	$('#videoPreviewContainer').fadeIn(200);
	createNotification('video', false, function() {
		$('#videoPreviewContainer').fadeIn(200);
	});
	
	$('#TzVideoTitle').html(filename);
}

var getSessionCurrentFolder = function() {
	$.ajax({
		url:'File',
		method:'POST',
		data:JSON.stringify({action:'FOLD',data:{'path':'/'}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success:function(data){
			if (data.error != 'auth') presentFolder(data);
		}		
	});
}

var getFolderContent = function(folderPath) {
	castLoading();
	
	$.ajax({
		url:'File',
		method:'POST',
		data:JSON.stringify({action:'BRWS',data:{'path':folderPath}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success:function(data){
			if (data.error != 'auth') {
				var back = folderPath == '../';
				if (back) {
					$('#fileListingWrapper .fileListing').last().remove();
					$('#fileListingWrapper .fileListing').last().remove();
				}
				
				presentFolder(data);
				
				if (back) { 
					fileBrowserVars.folderCur--;
				} else {
					fileBrowserVars.folderCur++;
				}
			}
			if (data.message == 'auth') notify.warning('[#browseautherror]', '[#filemenu]');
			
			dismissLoading();
		}		
	});
};

var presentFolder = function(json) {
	$('#blackHover').eq(0).scrollTop(0);

	$('.fileListing').each(function(index) {
		var multi = fileBrowserVars.folderCur - index;
		var transX = "translateX(" + (-100-10*multi).toString() + "px) ";
		var transZ = "translateZ(" + (-300-10*multi).toString() + "px) ";
		var rotatY = "rotateY(" + (-40-5*multi).toString() + "deg)";
		
		var transformation = transX + transZ + rotatY;
	
		$(this)
			.css("-webkit-transform",transformation)
			.css("-moz-transform",transformation)
			.css("-ms-transform",transformation)
			.css("-o-transform",transformation)
			.css("transform",transformation)
			.css("opacity",(1-(multi*0.1)-0.6));
	});
	
	$('#fileBrowserCurrentFolder').html(json.absolute);	
	
	json.list = sortByKey(json.list, 'title');
	
	$('#fileListingWrapper').append('<ul class="fileListing"></ul>');
	var unorderedList = $('#fileListingWrapper .fileListing').last();
	
	$(json.list).each(function(index, value) {
		var ext = value.filetype.toLowerCase();
		var item = $(gerenateListElement(value.type, value.title, ext));
		
		if (value.type == 'dir') {
			item.click(function() {
				playCursor();
				getFolderContent(value.title);
			});
		} else if (ext == 'png' || ext == 'jpg' || ext == 'jpeg') {
			item.click(function() {
				displayImageDownload(value.title);
			});			
			
			item.bind('contextmenu', function(ev) {
				$('#bfctView').show();
				$('#bfctPlaymedia').hide();
				$('#bfctPlayvideo').hide();
				
				fileBrowserVars.currentElement = value.title;
			  
				castContextMenu(ev.pageX, ev.pageY);
					
				return false;
			});
		} else if (ext == 'mp3' || ext == 'wav' || ext == 'm4a' || ext == 'flac' || ext == 'aac') {
			item.bind('contextmenu', function(ev) {
				$('#bfctView').hide();
				$('#bfctPlaymedia').show();
				$('#bfctPlayvideo').hide();
				
				fileBrowserVars.currentElement = value.title;
			  
				castContextMenu(ev.pageX, ev.pageY);
					
				return false;
			});			
		} else if (ext == 'mp4' || ext == 'mpg' || ext == 'avi' || ext == 'ogv') {
			item.bind('contextmenu', function(ev) {
				$('#bfctView').hide();
				$('#bfctPlaymedia').hide();
				$('#bfctPlayvideo').show();
				
				fileBrowserVars.currentElement = value.title;
			  
				castContextMenu(ev.pageX, ev.pageY);
					
				return false;
			});		
		} else {
			item.bind('contextmenu', function(ev) {
				$('#bfctView').hide();
				$('#bfctPlaymedia').hide();
				
				fileBrowserVars.currentElement = value.title;
			  
				castContextMenu(ev.pageX, ev.pageY);
					
				return false;
			});			
		}
		
		unorderedList.append(item);
	});
	
	if (json.absolute == '/') {
		unorderedList.find('li').first().remove();
		$('#fileBrowserCurrentFolder').html('[ [#root] ]');
	}
};

var bindContextMenuClosingEvent = function()  {
	$(document).bind('click', function(ev) {
		if (ev.which == 1) {
			hideContextMenu();
		}
	});
};

var castContextMenu = function(x, y) {
	$('#browserFileContextMenu')
		.hide()
		.css('top',  y)
		.css('left', x)
		.fadeIn(300);	
		
	bindContextMenuClosingEvent();	
}

var hideContextMenu = function() {
 	$('#browserFileContextMenu').fadeOut(300);
	$(document).unbind('click'); 
};

var gerenateListElement = function(type, title, ext) {
	switch (type) {
		case 'dir':  return '<li class="dirItem"><img src="mediaimg/folder.png" />'   + title + '</li>';
		case 'file':
			if (ext == 'png' || ext == 'jpg' || ext == 'jpeg')
				return '<li class="fileItem"><img src="mediaimg/imagefile.png" />' + title + '</li>';
			else if (ext == 'mp3' || ext == 'wav' || ext == 'm4a' || ext == 'flac' || ext == 'aac')
				return '<li class="fileItem"><img src="mediaimg/audio.png" /><a target="_blank" href="' + title + '?tz=DOWNLOAD">' + title + '</a></li>';
			else if (ext == 'mp4' || ext == 'mpg' || ext == 'avi' || ext == 'ogv')
				return '<li class="fileItem"><img src="mediaimg/video.png" /><a target="_blank" href="' + title + '?tz=DOWNLOAD">' + title + '</a></li>';
			else 
				return '<li class="fileItem"><img src="mediaimg/file-doc.png" /><a target="_blank" href="' + title + '?tz=DOWNLOAD">' + title + '</a></li>';
		default: return '';
	}
};

var displayImageDownload = function(filename) {
	hideContextMenu();
	castLoading();

	$.get(filename + "?tz=DOWNLOAD", null, function() {
		$('#photoViewer').css('background-image', "url('" + filename + "?tz=DOWNLOAD" + "')");
		$('#photoViewerWrapper').show('slide', {direction:'up'}, 500);
		$('#photoViewerTitle').html(filename);	
		
		dismissLoading();
	});
};
