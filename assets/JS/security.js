// Depends on jQuery
// Depends on livenotif
// Depends on contacts
// Depends on sms
// Depends on gallery
// Login activities are managed here

var completedLoading = 0;
var loadingsToComplete = 6;

$(function() {
	$('#loginPasswordInput').bind("enterKey",function(e){
		$.ajax({
			url:'Security',
			method:'POST',
			data:JSON.stringify({action:'PSW',data:{password:$('#loginPasswordInput').val()}}),
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			success:function(data){
				if (data.response == "ok") {
					cookies.set('session', data.session, 7);
					notify.success("[#loginsuccessmsg]");
					initializeTranzoid();
				} else {
					notify.error("[#loginfailedmsg]", "[#loginfailedtitle]");
				}
			}
		});
	
	});
	
	$('#loginPasswordInput').keyup(function(e){
		if(e.keyCode == 13) {
			$(this).trigger("enterKey");
		}
	});	
	
	$.get("mediaimg/buttonred.png");
	$.get("mediaimg/buttongreen.png");
	
	bypassLogin();
});

var bypassLogin = function() {
	$.ajax({
		url:'Security',
		method:'POST',
		data:JSON.stringify({action:'SESH',data:{method:'bypassIntro'}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success:function(data){
			if (data.response == 'ok') {
				notify.success('[#welcomeback]');
				initializeTranzoid();
			}	
		}
	});
};

var initializeTranzoid = function() {
	$('#loginLoading').fadeIn(300, function() {
		requestContactList(true, function() {completeLoadingBlock(0);}, function() {failLoadingBlock(0);} );
		getAllMedia(true, function() {completeLoadingBlock(1);}, function() {failLoadingBlock(1);});
		getAllSms(true, function() {completeLoadingBlock(2);}, function() {failLoadingBlock(2);});
		getAllDocuments(true, function() {completeLoadingBlock(3);}, function() {failLoadingBlock(3);});
		loadPreferences(true, function() {completeLoadingBlock(4);}, function() {failLoadingBlock(4);});
		requestDeviceInfo(true, function() {completeLoadingBlock(5);}, function() {failLoadingBlock(5);});
	});
};

var completeLoadingBlock = function(index) {
	completedLoading++;
	$('#loading' + index.toString()).css('color', '#3F6').find('.colorNotif').css('background-color', '#3F6');
	
	if (completedLoading == loadingsToComplete) {
		$('#loginOverlay').fadeOut(500, function() {
			$('#loginLoading').hide('slide', {direction:'up'}, 1000);
		});
		
		onLogin();	
	}
};

var failLoadingBlock = function(index) {
	$('#loading' + index.toString()).css('color', '#C44').find('.colorNotif').css('background-color', '#C44');
};

var onLogin = function() {	
	playClosure();
	waitForNotification();
		
	computeStatsValues();	
	
	$(document).bind('keydown', function(event) {
		if (event.which == 32 && event.ctrlKey) toggleMenu(true);
		
		if (event.which == 39 && event.ctrlKey && event.altKey) {
			presentNextOf(menuProperties.currentPane);
		}
		
		if (event.which == 37 && event.ctrlKey && event.altKey) {
			presentPreviousOf(menuProperties.currentPane);
		}
	});	
};