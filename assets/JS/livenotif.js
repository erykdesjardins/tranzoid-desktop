// Live notification system
// Never ending AJAX requests
// Requires jQuery
// Requires notify

var notifications = {
	retries: 0,
	title: '[#title]',
	titlenotif: false,
	interval: null,
	notificationCount: 0,
	hasFocus: true
};

var lastMessage = {
	author: '',
	message: ''
};

var batteryLevel = 0;
var batteryState = 'battery';

// Set focus to window 
$(window).blur(function(){
	notifications.hasFocus = false;
});

$(window).focus(function(){
	notifications.hasFocus = true;
	
	if (menuProperties.currentPane == 'messages' && notifications.notificationCount != 0 && messaging.currentContact != '') {
		markAsRead(messaging.currentContact);
		removeTag(messaging.currentContact.replace('+1', ''))
		resetTitle();
	}
});

// Recursive function
var waitForNotification = function() {
	$.ajax({
		url: 'NOTIF',
		method: 'POST', 	
		data:JSON.stringify({action:'NOTF',data:{'listenTo':'everything'}}),		
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		
		success: function(data) {
			notifications.retries = 0;
			receiveNotif(data);
		}, 
		error: function() {
			dismissLoading();
			$('#crashOverlay').fadeIn();
			
			recoverConnection();
		}
	});
};

var recoverConnection = function() {
	setTimeout(function() {
		$.ajax({
			url: 'AUTH',
			method: 'POST', 	
			data:JSON.stringify({action:'ECHO',data:{'text':'echo'}}),		
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			
			success: function(data) {
				if (data.error != null && data.error == 'auth') {
					$('#loginOverlay').fadeIn();
					restart();
				}
				
				$('#crashOverlay').fadeOut();
			}, 
			error: function() {
				recoverConnection();
			}
		});		
	}, 1000);
}

var setNotificationTitle = function(title) {
	notifications.titlenotif = true;
	var switchedTitle = false;
	
	if (notifications.interval != null) clearInterval(notifications.interval);
	
	notifications.notificationCount++;
	
	notifications.interval = setInterval(function() {
		switchedTitle = !switchedTitle;
		
		if (switchedTitle) document.title = title + ' (' + notifications.notificationCount + ')';
		else document.title = notifications.title;
	}, 1000);
};

var resetTitle = function() {
	notifications.titlenotif = false;
	clearInterval(notifications.interval);
	document.title = notifications.title;
	notifications.notificationCount = 0;
};

var receiveNotif = function(data) {
	switch(data.type) {
		case 'rebound':
			break;
			
		case 'sms':
			var address = data.address;
			var originalAddress = address;
			var found = false;
			
			$(currentContacts.contacts).each(function(index, ct) {
				if (!found) {
					for (var i = 0; i < ct.number.length; i++) {
						if (address.indexOf(ct.number[i]) != -1) {
							address = ct.name;
							found = true;
						}
					}
				}
			}); 
			
			if (lastMessage.message != data.message) {			
				lastMessage.author = address;
				lastMessage.message = data.message;
				
				var parsedMessage = parseSms(data.message);		
				
				notify.smsreceived(parsedMessage, address);
				messaging.sms.unshift({writer:originalAddress,body:parsedMessage,time:data.time,type:'in'});
				incrementSmsCount();
				
				if (messaging.currentContact == originalAddress && menuProperties.currentPane == 'messages') {
					var li = generateConversationMessage(data.id, originalAddress, parsedMessage, data.time, 'in');
					$('#currentMessagesList').prepend(li).scrollTop();
					$('#currentMessagesList li').first().hide().show('bounce');
					
					if (!notifications.hasFocus) 
						setNotificationTitle("SMS Received");
					else 
						markAsRead(messaging.currentContact);
				} else if (menuProperties.currentPane != 'messages') {
					createNotification('sms', true, function() {
						showBlackHover();
						presentPaneMessages();
						showConversation(originalAddress);
						resetTitle();
					});
					
					// Unread count notif
					incrementUnreadTag(originalAddress);					
					
					setNotificationTitle("SMS Received");
				} else {
					// Unread count notif
					incrementUnreadTag(originalAddress);							
				}
				
				presentConversations();
				
				playNotif();
			}
			
			break;
			
		case 'battery':
			$('#batteryLevel').html(data.level + (data.level == 100 ? '%' : ' %'));
			$('#batteryLogo').css('right', ((100-data.level)/2).toString() + 'px');
			
			if (batteryLevel != data.level) {
				if (batteryLevel > data.level)
					$('#batteryLogoWrapper').effect('highlight', {color:'#F00'});
				else
					$('#batteryLogoWrapper').effect('highlight', {color:'#0F0'});
					
				if (data.level == 100 && data.charge == 'plugged') notify.battery('[#batterycharged]', '[#battery]'); 	
					
				batteryLevel = data.level;
			}
			
			var color = '#FFF';
			
			if (data.level <= 15) color = '#FF0000';
			else if (data.level <= 50) color = '#f1da36';
			else if (data.level <= 75) color = '#76FF00';
			else if (data.level <= 99) color = '#2EFF00';
			else color = '#00FF00';
			
			$('#batteryLogo').css('background-color', color);
			
			if (batteryState != data.charge) {
				batteryState = data.charge;
				
				if (data.charge == 'plugged') {
					$('#batteryLogoWrapper').animate({boxShadow: '0 0 30px ' + color});
				} 
			}			
			
			break;
	}
	
	waitForNotification();
};

var removeNotificationType = function(type) {
	switch (type) {
		case 'sms':
		    $('.noteSMS').remove();
			break;
		case 'media':
			$('.noteMedia').remove();
			break;
		case 'video':
			$('.noteVideo').remove();
			break;
		break;
	}
}

var removeNotificationFromID = function(id) {
	$(id).remove();
}

var createNotification = function(type, removeonclick, onclick, forcedid) {
	var id = 'note' + guid();
	var li = null;
  
	switch (type) {
		case 'sms':
			var li = $('<li id="'+id+'" class="noteSMS"><img src="mediaimg/mail-message.png" /></li>');
			break;
		case 'media':
			if ($('.noteMedia').length == 0) {
				li = $('<li id="'+id+'" class="noteMedia"><img src="mediaimg/media-playback.png" /></li>');
				$('#notificationPanel').append(li);		
			}			
			break;
		case 'document':
			if ($('#note' + forcedid).length == 0) {
				li = $('<li id="note'+forcedid+'" class="noteDocument"><img src="mediaimg/document_icon.png" /></li>');
			}
			break;
		case 'video':
			if ($('.noteVideo').length == 0) {
				li = $('<li id="'+id+'" class="noteVideo"><img src="mediaimg/videocut.png" /></li>');	
			}			
			break;		
	}
	
	if (li != null) {
		if (onclick != null && onclick != undefined) li.bind('click', onclick);
		if (removeonclick != null && removeonclick != undefined && removeonclick) {
			li.bind('click', function() { $('#' + id).remove(); });
		}
				
		$('#notificationPanel').append(li);	
		$('#' + id).effect('bounce', 800);
	}
}
