// Depends on jQuery
// Depends on livenotif
// Graphics events and functions called from HTML

var menuProperties = {menuShown:false,currentPane:''};
var menuBubbleMargin = ['250px','130px','50px','20px','50px','130px','250px'];
var menuTransitioning = false;

$(function() {	
	$('#liFiles').click(function() {
		validateMainlistClick();
		presentPaneFiles();
	});
	
	$('#liContacts').click(function() {
		validateMainlistClick();
		presentPaneContacts();
	});	
	
	$('#liMessages').click(function() {
		validateMainlistClick();
		presentPaneMessages();
	});		
	
	$('#liGallery').click(function() {
		validateMainlistClick();
		presentPanePhotos();
	});		
	
	$('#liNotes').click(function() {
		validateMainlistClick();
		presentPaneNotes();
	});		
	
	$('#liDevice').click(function() {
		validateMainlistClick();
		presentPaneDevice();
	});		

	$('#liSettings').click(function() {
		validateMainlistClick();
		presentPaneSettings();
	});			
	
	$('#tzLogo').click(toggleMenu);
});

var validateMainlistClick = function() {
	playCursor(); 
	showBlackHover();
	
	$('#rightMainMenu li').removeClass('selectedmenuitem');
	$(this).addClass('selectedmenuitem');
	$('.mainBackgroundBlack').fadeIn(500);
	
	toggleMenu();
};

var presentPaneFiles = function() {
	if (menuProperties.currentPane != 'filebrowser') {
		hideAllContent();
		getSessionCurrentFolder();
		$('#fileBrowserWrapper').fadeIn(500);
		menuProperties.currentPane = 'filebrowser';
	} 
};

var presentPanePhotos = function() {
	if (menuProperties.currentPane != 'gallery') {
		hideAllContent();
		$('#galleryWrapper').fadeIn(500);
		menuProperties.currentPane = 'gallery';
	} 
};

var presentPaneContacts = function() {
	if (menuProperties.currentPane != 'contacts') {
		hideAllContent();
		$('#contactsWrapper').fadeIn(500);
		menuProperties.currentPane = 'contacts';
	} 
};

var presentPaneMessages = function() {
	if (menuProperties.currentPane != 'messages') {
		hideAllContent();
		presentConversations();
		removeNotificationType('sms');
		resetTitle();
		$('#messagesWrapper').fadeIn(500);
		menuProperties.currentPane = 'messages';
	} 
};

var presentPaneNotes = function() {
	if (menuProperties.currentPane != 'notes') {
		hideAllContent();
		$('#notesWrapper').fadeIn(500);
		menuProperties.currentPane = 'notes';
	} 
};

var presentPaneDevice = function() {
	if (menuProperties.currentPane != 'device') {
		hideAllContent();
		castLoading();
		$('#deviceWrapper').fadeIn(500);
		computeStatsValues();
		dismissLoading();
		
		menuProperties.currentPane = 'device';
	} 
};

var presentPaneSettings = function() {
	if (menuProperties.currentPane != 'settings') {
		hideAllContent();
		$('#settingsWrapper').fadeIn(500);
		menuProperties.currentPane = 'settings';
	}
};

var presentNextOf = function(subject) {
	switch (subject) {
		case 'filebrowser': presentPanePhotos(); break;
		case 'gallery': presentPaneContacts(); break;
		case 'contacts': presentPaneMessages(); break;
		case 'messages': presentPaneNotes(); break;
		case 'notes': presentPaneDevice(); break;
		case 'device': presentPaneSettings(); break;
		case 'settings': presentPaneFiles(); break;
	} 
};

var presentPreviousOf = function(subject) {
	switch (subject) {
		case 'contacts': presentPanePhotos(); break;
		case 'messages': presentPaneContacts(); break;
		case 'notes': presentPaneMessages(); break;
		case 'device': presentPaneNotes(); break;
		case 'settings': presentPaneDevice(); break;
		case 'filebrowser': presentPaneSettings(); break;
		case 'gallery': presentPaneFiles(); break;
	} 
};

var showBlackHover = function() {
	$('#blackHover').fadeIn(500);
}

var hideBlackHover = function() {
	$('#blackHover').fadeOut(500);
}

var hideAllContent = function() {
	$('#fileBrowserWrapper').fadeOut(500).removeClass('selected');
	$('#contactsWrapper').fadeOut(500).removeClass('selected');
	$('#messagesWrapper').fadeOut(500).removeClass('selected');
	$('#galleryWrapper').fadeOut(500).removeClass('selected');
	$('#deviceWrapper').fadeOut(500).removeClass('selected');
	$('#settingsWrapper').fadeOut(500).removeClass('selected');
	$('#notesWrapper').fadeOut(500).removeClass('selected');
}

var castLoading = function() {
	$('#loadingWrapper').fadeIn(200);
	$('.loadingBlackOverlay').fadeIn(200);
}
	
var dismissLoading = function() {
	$('#loadingWrapper').fadeOut(200);
	$('.loadingBlackOverlay').fadeOut(200);
}

var toggleMenu = function(forced) {
	if (!menuTransitioning) {
		menuTransitioning = true;
		
		$('#contactEditDialogWrapper, #galleryViewerWrapper').fadeOut(200);
		
		if (menuProperties.menuShown) {
			$('#mainMenuWrapper').fadeOut(500, function() {
				menuProperties.menuShown=false;
				menuTransitioning = false;
			});
			
			$('#rightMainMenu li').animate({'margin-top':'-220px'}, 500, 'easeInQuart');
			
			$('#blackHover').removeClass('mainPageFar');
		} else {
			spinTzLogo(360, 300);
			$('#mainMenuWrapper').fadeIn(500, function() {
				menuProperties.menuShown=true;
			});
			
			for (var i = 0; i <= 6; i++) {
				spinItem(
				$('#rightMainMenu li')
					.eq(i)
					.delay(i*50)
					.fadeIn(100)
					.animate({
						'margin-top':menuBubbleMargin[i]
					}, 800, 'easeOutElastic', function() {menuTransitioning = false;}), 0, i%2==0?360:-360, 500);
			}
			
			$('#blackHover').addClass('mainPageFar'); 
		}
	}
}

var spinTzLogo = function(angle, dur){
    var $elem = $('#tzLogo');

    $({deg: 0}).animate({deg: angle}, {
        duration: dur,
        step: function(now) {
            $elem.css({
                transform: 'rotate(' + now + 'deg)'
            });
        }
    });
}

var spinItem = function(id, angle, dur){
	spinItem(id, 0, angle, dur);
}

var spinItem = function(id, start, angle, dur){
    var $elem = $(id);

    $({deg: start}).animate({deg: angle}, {
        duration: dur,
        step: function(now) {
            $elem.css({
				'-webkit-transform': 'rotate(' + now + 'deg)',
				'-moz-transform': 'rotate(' + now + 'deg)',
				'-ms-transform': 'rotate(' + now + 'deg)',
				'-o-transform': 'rotate(' + now + 'deg)',
                'transform': 'rotate(' + now + 'deg)'
            });
        }
    });
}
