// Depends on jQuery
// Main utilities used by other modules

var startTime = new Date();

var currentTime = {
	cHours : startTime.getHours(),
	cMinutes : startTime.getMinutes(),
	cSeconds : startTime.getSeconds()
};

var colorPickerValue = '';
var colorCallback = undefined;

$(function() {
	// Color pickers
	$('#colorpickerHolder').ColorPicker({
		flat: true, 	
		onSubmit: function(hsb, hex, rgb, el) {
			playTick();
			
			colorPickerValue = rgb;
			$('#colorPickerWrapper').fadeOut(300);
			if (colorCallback != undefined) colorCallback();
		}
	});
	
	$('#colorPickerCloseButton').bind('click', function() {
		$('#colorPickerWrapper').fadeOut(300);
		
		playTick();
	});
});

var pickColor = function(color, callback) {
	playTick();
	
	color = color.substr(5).split(/,\s*/)
	colorCallback = callback;
	
	$('#colorpickerHolder').ColorPickerSetColor({r:color[0],g:color[1],b:color[2]});
	$('#colorPickerWrapper').fadeIn(300);
}

var hashSize = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};

var restart = function() {
	$('#rebootOverlay').fadeIn(1000, function() {
		location.reload(true); 
	});
}

var getPreference = function(pref) {
	return gPreferences[pref] == undefined ? "" : gPreferences[pref];
};

var executeAsync = function(func) {
    setTimeout(func, 0);
}

var setPreference = function(pref, value) {
	gPreferences[pref] = value;
	
	$.ajax({
		url: 'PREF',
		method: 'POST', 	
		data:JSON.stringify({action:'CPRF',data:{'pref':pref,'value':value}}),		
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		
		success: function(data) {

		}, 
		error: function() {
			notify.error();
		}
	});	
};

var playCursor = function() {
	if (getPreference('interfaceAudio')) playSound('cursorAudio');
};

var playNotif = function() {
	if (getPreference('notificationAudio')) playSound('notificationAudio');
};

var playAdd = function() {
	if (getPreference('addAudio')) playSound('addAudio');
};

var playPopup = function() {
	if (getPreference('popupAudio')) playSound('popupAudio');
};

var playTick = function() {
	if (getPreference('interfaceAudio')) playSound('tickAudio');
};

var playClosure = function() {
	if (getPreference('interfaceAudio')) playSound('closurePlayer');
};

var playSound = function(audio) {
	var cursor = document.getElementById(audio);
	
	cursor.onloadeddata = function() {
		cursor.pause();
		cursor.currentTime = 0;
		cursor.play();
	};	

	cursor.load();
};

var sortByKey = function(h, k) {
	return h.sort(function(a, b) {
		var x = a[k]; var y = b[k];
		return ((x < y) ? -1 : ((x > y) ? 1 : 0));
	});
};

var sortAssoc = function(aInput){
	
}

var checkFileAPI = function() {
    return (window.File && window.FileReader && window.FileList && window.Blob) 
}

// http://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid-in-javascript
function s4() {
  return Math.floor((1 + Math.random()) * 0x10000)
             .toString(16)
             .substring(1);
};

function guid() {
  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
         s4() + '-' + s4() + s4() + s4();
};

var getStartTimeStringFormat = function() {
	return currentTime.cHours.toString() + ":" +
	       formatTimeBlock(currentTime.cMinutes) + ":" + 
	       formatTimeBlock(currentTime.cSeconds);	
};

var formatTimeBlock = function(block) {
	if (block < 10) return '0' + block;
	else return block;
};

var addOneSecondToCurrentTime = function() {
	currentTime.cSeconds++;
	
	if (currentTime.cSeconds == 60) currentTime.cMinutes++;
	if (currentTime.cMinutes == 60) currentTime.cHours++;
	if (currentTime.cHours == 24) currentTime.cHours = 0;
};

var defOr = function(obj, def) {
	return typeof obj === 'undefined' ? def : obj;
};

var isUndef = function(obj) {
	return typeof obj === 'undefined';
}

var emptyNullStr = function(str) {
	return str == 'null' ? '' : str;
}