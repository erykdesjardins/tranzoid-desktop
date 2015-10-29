var notify = {
	success: function(message, title) {
		notify.popupNotif(message, title, "diagon");
	},
	
	warning: function(message, title) {
		notify.popupNotif(message, title, "warning");
	},
	
	error: function(message, title) {
		notify.popupNotif(message, title, "error");
	},	
	
	smssent: function(message, title) {
		notify.popupNotif(message, title, "mailsent");
	},
	
	smsreceived: function(message, title) {
		notify.popupNotif(message, title, "mailreceived");
	},	

	sync: function(message, title) {
		notify.popupNotif(message, title, "sync");
	},	
	
	battery: function(message, title) {
		notify.popupNotif(message, title, "battery");
	},
	
	popupNotif: function(message, title, image) {
		var item = "<li><img src=\"mediaimg/"+image+".png\" /><div class=\"notificationTextContainer\">"+ (title == undefined ? "" : "<h2>"+title+"</h2>") +"<span class=\"notificationContent\">"+message+"</span></div></li>"	
		$('.notificationArea').prepend(item);
		
		item = $('.notificationArea li').first();	
		item.show('blind');
		
		setTimeout(function() {
			item.hide('blind', {}, 300, function() {
				item.remove();
			});
		}, 5000);	
	}
};