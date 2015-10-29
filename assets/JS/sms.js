// Depends on jQuery
// Depends on contacts
// Depends on livenotif
// Text messaging

var messaging = {sms:[], currentContact:'', unread:{}, conversationShown:0};

var emotsRel = {
	code:[/:\)/g, /:\(/g, /:\//g, /:\|/g, /:@/g, /:D/g, /;\)/g, /:P/g, /:p/g, /<3/g, /\^\^/g, /-_-/g, />_</g, /:'\(/g, /:O/g, /:o/g],
	name:["smile", "sad", "ermm", "pouty", "angry", "grin", "wink", "tongue", "tongue", "heart", "happy", "getlost", "pinch", "cwy", "shocked", "shocked"],
	format:".png"
}; 

$(function() {
	$('#btnSendSMS').bind('click', function() {
		sendFunction();
	});
	
	$('#closeNewSMS').bind('click', function() {
		$('#newSMSDialog').hide('slide', {direction:'up'}, 300);
	});
	
	$('#sendNewSMS').bind('click', function() {
		var number = $('#newSMSNumber').val();
		var content = $('#newSMSContent').val();
		
		if (number.trim() != '' && content.trim() != '') {
			sendSMS(number, content);
			$('#newSMSDialog').hide('drop', {direction:'left'}, 300);
			
			$('#newSMSNumber').val('');
			$('#newSMSContent').val('');	
		}
	});
	
	$('#smsTextArea').keydown(function (e) {
  		if (e.ctrlKey && e.keyCode == 13) {
    		sendFunction();
  		}
	});
	
	$('#sms_btn_sync').bind('click', syncSms);
	$('#sms_btn_delc').bind('click', deleteConversation);
	$('#sms_btn_save').bind('click', saveAllSms);
	$('#sms_btn_load').bind('click', reloadSMS);
	
	$('#uploadSmsJson').bind('change', readSmsJsonFile);
});

var sendFunction = function() {
	if ($('#smsTextArea').val().trim() != '') {
		sendSMS(messaging.currentContact, $('#smsTextArea').val());
		$('#smsTextArea').val('');
		
		spinItem('#btnSendSMS', 0, -360, 300);
		playAdd();
	}
}

var syncSms = function() {
	$('#sms_btn_sync').unbind('click');
	notify.sync('[#smssyncing]');
	
	getAllSms(true, function() {
		$('#sms_btn_sync').bind('click', syncSms);
		
		presentConversations();
		showConversation(messaging.currentContact, true);
		
		notify.sync('[#smssyncdone]');
	}, function() {});
}

var reloadSMS = function() {
	$('#uploadSmsJson').click();
}

var readSmsJsonFile = function(evt) {
	var f = evt.target.files[0]; 
	
    if (f) {
		var r = new FileReader();
		r.onload = function(e) { 
	    	var contents = e.target.result;
			var smsDatabase = JSON.parse(contents);
		

		}
		
		r.readAsText(f);
	} else { 
		alert("Failed to load file");
    }	
};

var displayRestoreDialog = function(smsDatabase) {

};

var doSmsRestoration = function(smsDatabase, iterator, max) {
	if (max != 0) {
		restoreSingleSms(smsDatabase[iterator], function() {
			iterator++;
			
			if (iterator < max) {
				doSmsRestoration(smsDatabase, iterator, max);
			}
		});
	}
};

var restoreSingleSms = function(sms, callback) {
	$.ajax({
		url:'SMS',
		method:'POST',
		data:JSON.stringify({'action':'ISMS','data':sms}),		
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success:callback
	});
}

var saveAllSms = function() {
	window.open('ASMS/SMS' + getFormattedDateForFile() + '.tzsms');
}

var getFormattedDateForFile = function() {
    var date = new Date();
    var str = date.getFullYear().toString() + date.getMonth() + date.getDate() + date.getHours() + date.getMinutes() + date.getSeconds();

    return str;
}

var getAllSms = function(bAsync, finished, fail) {
	$.ajax({
		url:'ASMS/SMS.tz',
		method:'GET',
		dataType: "json",
		async: bAsync,
		success:function(texts){
			messaging.sms = texts.messages;
			executeAsync(function() {
				$(messaging.sms).each(function(index, value) {
					value.body = parseSms(value.body);
					
					if (value.read == "0") {
						incrementUnreadTag(value.writer);
					}
				});
			});
			
			finished();
		},
		error: fail	
	});
};

var presentConversations = function() {
	$('#conversationsList').empty();
	
	var addresses = [];

	$(messaging.sms).each(function(index, sms) {
		if ($.inArray(sms.writer, addresses) == -1) addresses.push(sms.writer);
	});
	
	var newSMSItem = $(addNewSMSListItem());
	
	newSMSItem.bind('click', popupNewSMS);	
	
	$('#conversationsList').append(newSMSItem);
	
	$(addresses).each(function(index, writer) {
		var li = $(generateConversationBlock(writer));
		
		if (li != undefined) {
			li.bind('click', function() {
				playCursor();
				showConversation(writer);
				
				removeTag(li.attr('id'));
			});
			
			$('#conversationsList').append(li);
		}
	});
	
	for (var key in messaging.unread) {
		var count = messaging.unread[key];
		
		if (count != undefined && count != 0) $('#conv' + key).prepend('<div class="unreadTag" id="unreadTag'+key+'">'+count.toString()+'</div>');
	};
};

var incrementUnreadTag = function(number) {
	var writerTrimed = number.replace("+1", "");
	
	if (messaging.unread[writerTrimed] == undefined) messaging.unread[writerTrimed] = 1;
	else messaging.unread[writerTrimed]++;	
};

var removeTag = function(number) {
	messaging.unread[number] = 0;
	$('#unreadTag' + number).remove();
};

var generateConversationBlock = function(number) {
	var name = currentContacts.numberassoc[number];
	var ctID = currentContacts.idassoc[number];
	var numbertrim = number == undefined ? undefined : number.replace("+1", "");
	
	if (number == undefined) return undefined;
	else if (name == undefined) return '<li id="conv'+numbertrim+'" class="conversationListItem"><img src="CTIMG?'+ctID+'" /><div class="infoWrapper"><span class="name">'+numbertrim+'</span></div></li>';
	else return '<li id="conv'+numbertrim+'" class="conversationListItem"><img src="CTIMG?'+ctID+'" /><div class="infoWrapper"><span class="name">'+name+'</span><span class="number">'+numbertrim+'</span></div></li>';
};

var popupNewSMS = function() {
	$('#newSMSDialog').show('slide', {direction:'up'}, 300);
};

var addNewSMSListItem = function() {
	return '<li id="newSMSListItem" class="conversationListItem newSMSListItem"><span class="name">[#newsmsbtn]</span></li>';
};

var showConversation = function(number, reset) {
	castLoading();
	$('#currentConversation').fadeOut(200, function() {
		$('#currentMessagesList').empty();
	
		if (reset == undefined || reset) messaging.conversationShown = 0; 
	
		var count = 0;
		$(messaging.sms).each(function(index, message) {
			if (count < messaging.conversationShown+100 && message.writer == number) {
				var jMes = generateConversationMessage(message.id, message.writer, message.body, message.time, message.type);
				$('#currentMessagesList').append(jMes);
				
				$('#sms'+message.id+' .btnDeleteSMS').bind('click', function() {
					if (confirm('[#deletesmsconfirm]')) {
						deleteMessage(message.id);
						$('#sms'+message.id).hide('blind', {}, 300);
						
						messaging.sms.splice(index, 1);
					}
				});		
				count++;	
			}
		});
		
		if (count == messaging.conversationShown+100) {
			var btn = '<li id="btnAppendMoreSMS"><img src="mediaimg/bigplus.png" /></li>'
			$('#currentMessagesList').append(btn);
			
			$('#btnAppendMoreSMS').bind('click', function() {
				$(this).remove();
				showConversation(number, false);
			});
		}
		
		messaging.conversationShown = count;
		
		$('#currentConversation').fadeIn(200);
		messaging.currentContact = number;
		
		markAsRead(number);
		
		dismissLoading(); 
	});
};

var deleteMessage = function(id) {
	$.ajax({
		url:'SMS',
		method:'POST',
		data:JSON.stringify({action:'DSMS',data:{'id':id}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json"
	});
};

var deleteConversation = function() {
	if (confirm('[#deleteconvconfirm]')) {
		castLoading();
		$.ajax({
			url:'SMS',
			method:'POST',
			data:JSON.stringify({action:'DCNV',data:{'number':messaging.currentContact}}),
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			success:function() {
				var deleted = 0;
				var parsedNumber = messaging.currentContact.replace('+1', '');
				
				$(messaging.sms).each(function(index, value) {
					if (value.writer != undefined && value.writer.replace('+1', '') == parsedNumber) {
						messaging.sms.splice(index-deleted, 1);
						deleted++;
					}
				});
				
				messaging.currentContact = '';
				$('#currentConversation').fadeOut(300);
				
				dismissLoading();
			}
		});	
	}
}	

var parseSms = function(message) {
	return message == undefined ? message : parseEmots(parseURL(message));
}

var parseEmots = function(message) {
	$(emotsRel.code).each(function(index, value) {
		message = message.replace(value, '<span class="smsEmot ' + emotsRel.name[index] + '"></span>');
	});
	
	return message;
};

var parseURL = function(message) {
	return  message.replace(/(\b(https?|ftp|file):\/\/[\-A-Z0-9+&@#\/%?=~_|!:,.;]*[\-A-Z0-9+&@#\/%=~_|])/ig, '<a href="$1">$1</a>');
};

var generateConversationMessage = function(id, number, message, time, type) {
	var name = type == 'in' ? currentContacts.numberassoc[number] : '[#me]';
	
	return '<li id="sms'+id+'" class="' + (type == 'in' ? "messageIn" : "messageOut") + '"><h2>'+(name==undefined?number:name)+'</h2>'+message+'<span class="date">' + time + '</span><img src="mediaimg/dialog-close.png" class="btnDeleteSMS" /></li>';
};

var markAsRead = function(number) {
	removeTag(number.replace("+1", ""));

	$.ajax({
		url:'SMS',
		method:'POST',
		data:JSON.stringify({action:'READ',data:{'number':number}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json"
	});	
}

var sendSMS = function(number, text) {
	var jsonString = '{"action":"SSMS","data":{"number":"'+number+'","content":"'+text.replace(/\"/g, "\\\"")+'"}}';

	$.ajax({
		url:'SMS',
		method:'POST',
		data:jsonString,
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success:function(data){
			var pText = parseSms(text);
			messaging.sms.unshift({writer:number,body:pText,time:data.time,type:'out','id':data.id});
			presentConversations();
			incrementSmsCount();
			
			if (messaging.currentContact == number) { 
				var li = generateConversationMessage(data.id, number, pText, data.time, 'out');
				$('#currentMessagesList').prepend(li).scrollTop();
				$('#currentMessagesList li').first().hide().show('blind');
			}
			
			notify.smssent('[#successsendsms]');
		}		
	});	
};