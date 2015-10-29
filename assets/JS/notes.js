// Manages the documents tab
// Depends on jQuery
// Depends on SCEditor

var tzDocuments = {
	docs : []
}

$(function() {
	$('#noteContainer textarea').sceditor({
		plugins: "bbcode",
		style: "JStexteditor/jquery.sceditor.default.min.css",
		width: "100%",
		resizeEnabled: false,
		toolbar: "bold,italic,underline|left,center,right|color,size",
		
		id:"noteContainerSCEditor"
	});
	
	$('#newDocumentButton').bind('click', function() {
		$('#notesTitleInputContainer').fadeIn(200);
	});
	
	$('#notesTitleInputContainer button').bind('click', function() {
		$('#notesTitleInputContainer').fadeOut(200);
		
		var docTitle = $('#notesTitleInputText').val().trim();
		var exists = false;
		
		if (docTitle != '') {
			$(tzDocuments.docs).each(function(index, value) {
				if (value == docTitle) exists = true;
			});
			
			if (!exists) {
				createDocInServer(docTitle, '');
			} else {
			
			}
		}
	});
});

var getAllDocuments = function(bAsync, finished, fail) {
	// Get All documents
	$.ajax({
		url:'Documents',
		method:'POST',
		data:JSON.stringify({action:'GDOC',data:{'get':'all'}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		async: bAsync,
		success:function(data){
			if (data.count != 0) {
				$(data.list).each(function(index, value) {
					if (value.type == 'file') tzDocuments.docs.push(value.title);
				});
				
				presentAllDocuments();
				finished();
			}
		},
		error: fail	
	});
};

var presentAllDocuments = function() {
	$('#notesList').find("li:not(:first)").remove();
	
	$(tzDocuments.docs).each(function(index, value) {
		createDocIcon(value);
	});
};

var openDoc = function(title) {
	castLoading();
	$.ajax({
		url:'Documents',
		method:'POST',
		data:JSON.stringify({action:'RDOC',data:{'file':title}}),
		contentType: "application/json; charset=utf-8",
		dataType: "text",
		success:function(data){
			createDoc(title, data);
		}		
	});		
};

var createDocInServer = function(title) {
	$.ajax({
		url:'Documents',
		method:'POST',
		data:JSON.stringify({action:'CDOC',data:{'file':title}}),
		contentType: "application/text; charset=utf-8",
		dataType: "json",
		success:function(data){
			tzDocuments.docs.push(data.name);
			presentAllDocuments();
		}		
	});	
};

var createDocIcon = function(title) {
	$('#notesList').append('<li id="documentButton"><img src="mediaimg/existingdocument.png" />'+title+'</li>');
	$('#notesList li').last().bind('click', function() {
		openDoc(title);
	});
};

var createDoc = function(title, content) {
	var gUID = guid();
	var strContent = '<div id="doc_'+gUID+'" class="TzDraggable TzDocumentWrapper"><div id="doch_'+gUID+'" class="TzHandle TH[@pref.theme]"><span class="TzHandleTitle">'+title+'</span><img class="TzWindowButton TzWindowMinimize" id="doch_min_'+gUID+'" src="mediaimg/arrow-down-2.png" /><img class="TzWindowButton TzWindowClose" id="doch_close_'+gUID+'" src="mediaimg/dialog-close.png" /><img class="TzWindowButton TzWindowSave" id="doch_save_'+gUID+'" src="mediaimg/save.png" /><img class="TzWindowButton TzWindowDelete" id="doch_delete_'+gUID+'" src="mediaimg/trash.png" /></div><div class="TzDocumentContainer"><textarea>'+content+'</textarea></div></div>';

	$('#TzWindowArea').append(strContent);
	
	$('#doc_' + gUID)
		.draggable({ handle: "#doch_" + gUID, containment: "#TzWindowArea" })
		.resizable({ minHeight: 480, minWidth:640, handles: 'e, w, n, s' })
		.css('top', 100)
		.css('left', 300);
		
	$('#doch_min_' + gUID).bind('click', function() {
		$('#doc_' + gUID).fadeOut(200);
	});
	
	$('#doch_close_' + gUID).bind('click', function() {
		removeNotificationFromID('#note' + gUID); 
		$('#doc_' + gUID).fadeOut(200);
	});	
	
	$('#doch_save_' + gUID).bind('click', function() {
		castLoading();
		var currentContent = $('#doc_' + gUID + ' textarea').sceditor('instance').val();
		
		$.ajax({
			url:'Documents',
			method:'POST',
			data:JSON.stringify({action:'SDOC',data:{'file':title,'content':currentContent}}),
			contentType: "application/text; charset=utf-8",
			dataType: "json",
			success:function(data){
				notify.success("[#notessavesuccess]", title);
				dismissLoading();
			},
			error:function() {
				notify.error("[#notessavefail]", title);
				dismissLoading();
			}
		});
	});	
	
	$('#doch_delete_' + gUID).bind('click', function() {
		
	});		
	
	createNotification('document', false, function() {
		$('#doc_' + gUID).fadeIn(200);
	}, gUID);
	
	createEditor('#doc_' + gUID + ' textarea');
	
	dismissLoading();
	
	return gUID;
};

var createEditor = function(selector) {
	$(selector).sceditor({
		plugins: "bbcode",
		style: "JStexteditor/jquery.sceditor.default.min.css",
		width: "100%",
		height: "100%",
		resizeEnabled: false,
		toolbar: "bold,italic,underline|left,center,right|color,size",
		
		id:"noteContainerSCEditor"
	});
};