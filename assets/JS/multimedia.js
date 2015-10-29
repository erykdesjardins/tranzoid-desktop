// Manages the gallery tab
// Depends on jQuery

var gallery = {
	galleryFiles : [], 
	iterator : 0, 
	defaultIncrement : 50, 
	currentRotation : 0, 
	title : "[#gallerymenu]",
	subtitle : "[#gallerysub]"
};

var audioLibrary = {
	audioFiles : [], 
	albums : [], 
	artists : [], 
	songs : [], 
	title : "[#audiomenu]", 
	subtitle : "[#audiosub]", 
	currentAlbum:"", 
	currentArtist:""
};

var videoLibrary = {
	videoFiles : [], 
	title : "[#videomenu]",
	subtitle : "[#videosub]"
};

var musicPlayer = {
	playlist : [], 
	currentIndex : -1, 
	currentTime : 0,
	refreshRate : 60,
	musicBars : 100,
	playerState : 'stop', 
	repeat : false, 
	shuffle : false, 
	resetTime : true,
	elementId : 'mainMusicPlayer', 
	interval : undefined,
	ctx : undefined
}

var hasAudioContext = function() {return typeof musicPlayer.ctx !== 'undefined';};

$(function() {
	// Photo gallery section
	$('#galleryBtnMore').bind('click', nextSet);
	
	$('#galleryViewerWrapper .photoViewerCloseBtn').click(function() {
		$('#galleryViewerWrapper').fadeOut(500);
	});
	
	$('#galleryViewerWrapper .photoViewerRotateBtn').click(function() {
		spinItem('#galleryViewer', gallery.currentRotation, gallery.currentRotation+90, 200);
		gallery.currentRotation += 90;
		
		gallery.currentRotation = gallery.currentRotation == 360 ? 0 : gallery.currentRotation;
	});	
	
	$('#galleryViewerWrapper .photoViewerDerotateBtn').click(function() {
		spinItem('#galleryViewer', gallery.currentRotation, gallery.currentRotation-90, 200);
		gallery.currentRotation -= 90;
		
		gallery.currentRotation = gallery.currentRotation == -360 ? 0 : gallery.currentRotation;
	});	
	
	$('.mediaMainMenuBtn').bind('click', function() {
		if (!$(this).hasClass('selected')) {
			$('.mediaMainMenuBtn').removeClass('selected');
			$(this).addClass('selected');
			
			var id = $(this).attr('id');
			var selector = '#' + id.substr(0, id.length-3) + 'Wrapper';
			
			$('.mediaContentWrapper').fadeOut(500);
			$(selector).fadeIn(500);		
			
			switch (id) {
				case 'galleryPhotosBtn':
					$('#galleryWrapper > h2').html(gallery.title);
					$('#galleryWrapper > h3').html(gallery.subtitle);
					break;
				case 'musicLibraryBtn':
					$('#galleryWrapper > h2').html(audioLibrary.title);
					$('#galleryWrapper > h3').html(audioLibrary.subtitle);
					break;
				case 'videoLibraryBtn':
					$('#galleryWrapper > h2').html(videoLibrary.title);
					$('#galleryWrapper > h3').html(videoLibrary.subtitle);
					break;
			}	
		}
	});
	
	// Music player section
	musicPlayer.ctx = new AudioCtx();
	
	$('#musicArtistType').bind('click', function() {
		$('#musicAlbumsWrapper, #musicSongsWrapper, #addToPlaylistBtn, #musicPlaylistWrapper').fadeOut(200);
		$('#musicArtistsWrapper').fadeIn(200);
			
		changeMusicTitle('[#musicartists]');
		
		$('#musicTypeSelection li').removeClass('selected');
		$(this).addClass('selected');
	});
	
	$('#musicAlbumType').bind('click', function() {
		if (audioLibrary.currentArtist != "") {
			$('#musicArtistsWrapper, #musicSongsWrapper, #addToPlaylistBtn, #musicPlaylistWrapper').fadeOut(200);
			$('#musicAlbumsWrapper').fadeIn(200);	
			
			changeMusicTitle(audioLibrary.currentArtist);
			
			$('#musicTypeSelection li').removeClass('selected');
			$(this).addClass('selected');
		}
	});
	
	$('#musicSongType').bind('click', function() {
		if (audioLibrary.currentAlbum != "") {
			$('#musicArtistsWrapper, #musicAlbumsWrapper, #musicPlaylistWrapper').fadeOut(200);
			$('#musicSongsWrapper, #addToPlaylistBtn').fadeIn(200);	
			
			changeMusicTitle(audioLibrary.currentAlbum);
			
			$('#musicTypeSelection li').removeClass('selected');
			$(this).addClass('selected');
		}	
	});
	
	$('#musicNowPalyingType').bind('click', function() {
		$('#musicArtistsWrapper, #musicAlbumsWrapper, #musicSongsWrapper, #addToPlaylistBtn').fadeOut(200);
		$('#musicPlaylistWrapper').fadeIn(200);	
		
		changeMusicTitle("[#nowplaying]");
		
		$('#musicTypeSelection li').removeClass('selected');
		$(this).addClass('selected');
	});	
	
	$('#addToPlaylistBtn').bind('click', function() {
		$(audioLibrary.songs).each(function(index, song) {
			addToPlayList(song, false);
		});
		
		presentPlaylist();
	});
	
	$('#btnMediaTogglePlay').bind('click', function() {
		switch (musicPlayer.playerState) {
			case 'stop': musicPlayerPlay(); break;
			case 'pause': musicPlayerPause(); break;
			case 'play': musicPlayerPause(); break;
		}
	});
	
	var mp = document.getElementById(musicPlayer.elementId);
	mp.addEventListener('loadedmetadata', function() {
		if (musicPlayer.resetTime) {
			mp.currentTime = 0;
			musicPlayer.resetTime = false;
		} else {
			mp.currentTime = musicPlayer.currentTime;
		}
	});
	mp.addEventListener('error', function() {
		$('#musicPlaylistWrapper table tbody tr')
			.eq(musicPlayer.currentIndex)
			.find('td')
			.css('background-color', '#444')
			.css('color', '#CCC');
		musicPlayerNext();
	});	
	mp.addEventListener('ended', musicPlayerNext);
	
	$('#btnMediaNext').bind('click', musicPlayerNext);
	$('#btnMediaBack').bind('click', musicPlayerPrevious);
	$('#btnMediaStop').bind('click', musicPlayerStop);
	
	$('.mediaPlayingTimelineWrapper').bind('click', function(evt) {
		var x = evt.pageX - $(this).offset().left;
		var percent = x / $(this).width();
			
		if (musicPlayer.playerState != 'stop') {
			var duration = musicPlayer.playlist[musicPlayer.currentIndex].duration;
			document.getElementById(musicPlayer.elementId).currentTime = duration / 1000 * percent;
		}
	});
	
	musicPlayer.ctx.setAudio(mp);
	
	for (var i = 0; i < musicPlayer.musicBars; i++) {
		$('#mediaWaveContainer').append('<div id="mediaWave'+i+'" style="left:'+i+'%" class="mediaWave"></div>');
	}
});

var getAllMedia = function(bAsync, finished, fail) {	
	$.ajax({
		url: 'GAMD/MEDIA.tz',
		method: 'GET', 	
		dataType: "json",
		async: bAsync,
		success: function(data) { 
			gallery.galleryFiles = data.photos;
			audioLibrary.audioFiles = data.songs;
			audioLibrary.albums = data.albums;
			audioLibrary.artists = data.artists;
			videoLibrary.videoFiles = data.videos;
			
			presentArtists();
			
			finished();
		}, 
		error: fail
	});	
	
	$.get('mediaimg/rloading.gif');
}

var updateMusicTime = function() {
	if (menuProperties.currentPane == 'gallery') {
		var bar = $('#mediaPlayingTimeline');
		var audio = document.getElementById(musicPlayer.elementId);
		var duration = musicPlayer.playlist[musicPlayer.currentIndex].duration;
		
		var percent = musicPlayer.currentTime / duration * 100;
		
		// Progressbar
		// bar.css('width', percent.toString() + '%');
		
		var time = new Date(musicPlayer.currentTime);
		var formattedTime = time.getMinutes() + ":" + (time.getSeconds() < 10 ? "0" : "") + time.getSeconds();
		
		// musicPlayer.currentTime += musicPlayer.refreshRate;
		$('.currentMusicTime').html(formattedTime);
	}
	
	animateFreqBars();
};

var animateFreqBars = function() {
	var arr = musicPlayer.ctx.getFreqArray();
	var percent = 0;
	
	for (var i = 0; i < musicPlayer.musicBars; i++) {
		percent = arr[i] / 255;
		$('#mediaWave' + i).css('height', (percent * 100).toString() + '%').css('opacity', (percent+0.2).toString());
	}
}

var dismissFreqBars = function() {
	$('.mediaWave').animate({'opacity' : '0'}, 2000);
}

var handleMusicTimeUpdate = function(start) {
	if (start && musicPlayer.interval == undefined) {
		musicPlayer.interval = setInterval(updateMusicTime, musicPlayer.refreshRate);
	} else if (!start && musicPlayer != undefined) {
		clearInterval(musicPlayer.interval);
		musicPlayer.interval = undefined;
	}
};

var musicPlayerNext = function() {
	if (musicPlayer.currentIndex < musicPlayer.playlist.length-1) {
		musicPlayer.currentIndex++;
		musicPlayer.resetTime = true;
		musicPlayer.currentTime = 0;
		musicPlayerPlay();
	}
};

var musicPlayerPrevious = function() {
	if (musicPlayer.currentIndex > 0) {
		musicPlayer.currentIndex--;
		musicPlayer.resetTime = true;
		musicPlayer.currentTime = 0;
		musicPlayerPlay();
	}
};

var musicPlayerPlay = function() {
	if (musicPlayer.currentIndex < 0) musicPlayer.currentIndex = 0;

	if (musicPlayer.currentIndex >= 0 && musicPlayer.currentIndex < musicPlayer.playlist.length) {
		var song = musicPlayer.playlist[musicPlayer.currentIndex];
		
		var mp = document.getElementById(musicPlayer.elementId);
		$('#' + musicPlayer.elementId).attr('src', song.path + '?tz=DOWNLOAD');
		
		mp.play();
		
		$('#musicPlaylistWrapper table tbody tr')
			.removeClass('nowPlaying')
			.eq(musicPlayer.currentIndex)
			.addClass('nowPlaying');
			
		var time = new Date(song.duration);
		var formattedTime = time.getMinutes() + ":" + (time.getSeconds() < 10 ? "0" : "") + time.getSeconds();
		
		$('.totalMusicTime').html(formattedTime);
		updateMusicTime();
		
		musicPlayer.playerState = 'play';
		handleMusicTimeUpdate(true);
	} else {
		musicPlayer.currentIndex = -1;
	}
};

var musicPlayerPause = function() {
	var mp = document.getElementById(musicPlayer.elementId);
	musicPlayer.resetTime = false;

	if (musicPlayer.playerState == 'pause') {
		musicPlayerPlay();
	} else if (musicPlayer.playerState == 'play') {
		mp.pause();
		musicPlayer.playerState = 'pause';
		
		handleMusicTimeUpdate(false);
		dismissFreqBars();
	}
};

var musicPlayerStop = function() {
	var mp = document.getElementById(musicPlayer.elementId);
	mp.pause();
	musicPlayer.currentTime = 0;
	
	musicPlayer.currentIndex = 0;
	musicPlayer.playerState = 'stop';
	musicPlayer.resetTime = true;
	
	handleMusicTimeUpdate(false);
	updateMusicTime();
	dismissFreqBars();
};

var presentArtists = function() {
	$('#musicTypeSelection li').removeClass('selected');
	$('#musicArtistType').addClass('selected');
	
	$('#musicArtistsWrapper table tbody').empty();
	
	var tr = "<tr id=\"artist0\"><td>[#all]</td><td>" + audioLibrary.albums.length + "</td><td>" + audioLibrary.audioFiles.length + "</td></tr>"
	$('#musicArtistsWrapper table tbody').append(tr);
	
	$(audioLibrary.artists).each(function(index, value) {
		if (value.id == 1) value.artist = '[#others]';
		
		tr = "<tr id=\"artist" + value.id + "\"><td>" + value.artist + "</td><td>" + value.albums + "</td><td>" + value.tracks + "</td></tr>"
		$('#musicArtistsWrapper table tbody').append(tr);
	});
	
	$('#musicArtistsWrapper table tbody tr').bind('click', function() {
		presentAlbums($(this).find('td').first().html(), Number($(this).attr('id').substr(6)));
	});	
	
	changeMusicTitle('[#musicartists]');
	$('#musicArtistsWrapper').fadeIn(200);
	$('#musicAlbumsWrapper, #musicSongsWrapper, #addToPlaylistBtn, #musicPlaylistWrapper').fadeOut(200);	
};

var presentAlbums = function(artistName, artistID) {
	$('#musicTypeSelection li').removeClass('selected');
	$('#musicAlbumType').addClass('selected');

	var albums = [];
	if (artistID == 0) {
		albums = audioLibrary.albums;
	} else {
		$(audioLibrary.albums).each(function(index, value) {
			if (value.artist == artistName) {
				albums.push(value);
			}
		});
	}
	
	$('#musicAlbumsWrapper ul').empty();
	var li = '<li id="album0" style="background-image:url(\'mediaimg/mediacd.png\')"><span>[#all]</span></li>';
	$('#musicAlbumsWrapper ul').append(li);
	
	$(albums).each(function(index, value) {
		li = '<li id="album'+value.id+'" style="background-image:url(\'' + (value.art == "null" ? 'mediaimg/mediacd.png' : value.art + '?tz=ABSOLUTE') + '\')"><span>' + value.album + '</span></li>';
		$('#musicAlbumsWrapper ul').append(li);
	});
	
	$('#musicAlbumsWrapper ul li').bind('click', function() {
		presentSongsFromAlbum(Number($(this).attr('id').substr(5)));
		
		audioLibrary.currentAlbum = $(this).find('span').html();
		changeMusicTitle(audioLibrary.currentAlbum);
	});
	
	$('#musicAlbumsWrapper ul li').first().unbind('click').bind('click', function() {
		presentSongsFromArtist(artistID);
	});
	
	audioLibrary.currentArtist = artistName;
	changeMusicTitle(artistName);
	
	$('#musicArtistsWrapper, #musicSongsWrapper, #addToPlaylistBtn, #musicPlaylistWrapper').fadeOut(200);
	$('#musicAlbumsWrapper').fadeIn(200);	
};

var presentSongsFromAlbum = function(albumID) {
	$('#musicTypeSelection li').removeClass('selected');
	$('#musicSongType').addClass('selected');

	var songs = [];
	$(audioLibrary.audioFiles).each(function(index, value) {
		if (value.album == albumID) {
			songs.push(value);
		}
	});	
	
	songs.sort(function(a, b) {
		return a.track > b.track;
	});	
	
	audioLibrary.songs = songs;
	$('#musicSongsWrapper table tbody').empty();
	$(songs).each(function(index, value) {
		var time = new Date(value.duration);
		var formattedTime = time.getMinutes() + ":" + (time.getSeconds() < 10 ? "0" : "") + time.getSeconds();
		
		var row = "<tr id=\"song"+value.id+"\"><td>"+value.title+"</td><td>"+(value.track==0?"-":(value.track>1000?value.track-1000:value.track))+"</td><td>"+formattedTime+"</td><td>"+(value.year==0?"-":value.year)+"</td><td>"+value.path.substr(value.path.lastIndexOf("/") + 1)+"</td><td><img class=\"mediaPlayBtn\" src=\"mediaimg/mediaplay.png\" /><img class=\"mediaListBtn\" src=\"mediaimg/medialist.png\" /></td></tr>";
		$('#musicSongsWrapper table tbody').append(row);
		
		$('#song'+value.id+' .mediaListBtn').bind('click', function() {
			addToPlayList(value, true);
		});			
	});

	$('#musicArtistsWrapper, #musicAlbumsWrapper, #musicPlaylistWrapper').fadeOut(200);
	$('#musicSongsWrapper, #addToPlaylistBtn').fadeIn(200);		
};

var presentSongsFromArtist = function(artistID) {
	$('#musicTypeSelection li').removeClass('selected');
	$('#musicSongType').addClass('selected');

	var songs = [];
	if (artistID == 0) {
		songs = audioLibrary.audioFiles;
	} else {
		$(audioLibrary.audioFiles).each(function(index, value) {
			if (value.artist == artistID) {
				songs.push(value);
			}
		});			
	}
	
	songs.sort(function(a, b) {
		return a.title > b.title;
	});	
	
	audioLibrary.songs = songs;
	$('#musicSongsWrapper table tbody').empty();
	$(songs).each(function(index, value) {
		var time = new Date(value.duration);
		var formattedTime = time.getMinutes() + ":" + (time.getSeconds() < 10 ? "0" : "") + time.getSeconds();
		
		var row = "<tr id=\"song"+value.id+"\"><td>"+value.title+"</td><td>"+(value.track==0?"-":(value.track>1000?value.track-1000:value.track))+"</td><td>"+formattedTime+"</td><td>"+(value.year==0?"-":value.year)+"</td><td>"+value.path.substr(value.path.lastIndexOf("/") + 1)+"</td><td><img class=\"mediaPlayBtn\" src=\"mediaimg/mediaplay.png\" /><img class=\"mediaListBtn\" src=\"mediaimg/medialist.png\" /></td></tr>";
		$('#musicSongsWrapper table tbody').append(row);
		
		$('#song'+value.id+' .mediaListBtn').bind('click', function() {
			addToPlayList(value, true);
		});		
	});	
	
	$('#musicArtistsWrapper, #musicAlbumsWrapper, #musicPlaylistWrapper').fadeOut(200);
	$('#musicSongsWrapper, #addToPlaylistBtn').fadeIn(200);	
};

var presentPlaylist = function() {
	$('#musicTypeSelection li').removeClass('selected');
	$('#musicNowPalyingType').addClass('selected');	
	
	$('#musicPlaylistWrapper table tbody').empty();
	$(musicPlayer.playlist).each(function(index, value) {
		var time = new Date(value.duration);
		var formattedTime = time.getMinutes() + ":" + (time.getSeconds() < 10 ? "0" : "") + time.getSeconds();
		
		var row = "<tr id=\"song"+value.id+"\"><td>"+value.title+"</td><td>"+(value.track==0?"-":(value.track>1000?value.track-1000:value.track))+"</td><td>"+formattedTime+"</td><td>"+value.path.substr(value.path.lastIndexOf("/") + 1)+"</td><td><img class=\"mediaRemoveBtn\" src=\"mediaimg/dialog-close.png\" /></td></tr>";
		$('#musicPlaylistWrapper table tbody').append(row);
		
		$('#song'+value.id+' .mediaRemoveBtn').bind('click', function(evt) {
			musicPlayer.playlist.splice(index, 1);
			if (index < musicPlayer.currentIndex) {
				musicPlayer.currentIndex--;
			} else if (index == musicPlayer.currentIndex) {
				musicPlayerStop();
			}
			
			$('#musicPlaylistWrapper table tbody tr').eq(index).remove();
			evt.preventDefault();
		});	
		
		if (musicPlayer.playerState != 'stop') {
			$('#musicPlaylistWrapper table tbody tr')
				.removeClass('nowPlaying')
				.eq(musicPlayer.currentIndex)
				.addClass('nowPlaying');			
		}
		
		$('#song'+value.id+' td').bind('click', function(evt) {
			var index = $(this).parent()[0].sectionRowIndex;
			musicPlayer.currentIndex = index;
			
			musicPlayerPlay();
			evt.preventDefault();
		});	
	});		

	changeMusicTitle("[#nowplaying]");
	$('#musicArtistsWrapper, #musicAlbumsWrapper, #musicSongsWrapper, #addToPlaylistBtn').fadeOut(200);
	$('#musicPlaylistWrapper').fadeIn(200);	
};

var addToPlayList = function(song, showPlaylist) {
	musicPlayer.playlist.push(song);
	
	if (showPlaylist) presentPlaylist();
}

var changeMusicTitle = function(title) {
	$('#musicSectionHeaderWrapper h3').html(title);
};

var nextSet = function() {

	for (var i = gallery.iterator; i < gallery.iterator + gallery.defaultIncrement; i++) {
		if (gallery.galleryFiles[i] != null) {
			$('#GalleryList').append(generateListItem(i));
		}
	}

	getImage(gallery.iterator);
	
	gallery.iterator += gallery.defaultIncrement;
}

var generateListItem = function(index) {
	return '<div class="galleryThumbnail" id="gal'+index+'"><span class="galleryDate"></span></div>';
}

var getImage = function(index) {
	$('#galleryBtnMore').hide();

	if (gallery.galleryFiles[index] != undefined && gallery.galleryFiles[index].id != undefined) {
		$.ajax({
			url: '/GALPH?' + gallery.galleryFiles[index].id + '.jpg',
			success: function(data) {
				$('#gal' + index)
					.css('background-image', 'url(\'/GALPH?' + gallery.galleryFiles[index].id + '.jpg\')')
					.css('background-size', 'cover')
					.bind('click', function() {
						playCursor();
						displayImageFullSize(gallery.galleryFiles[index].path, gallery.galleryFiles[index].date);
					})
					.find('.galleryDate')
					.html(gallery.galleryFiles[index].date);
					
				if (index == 0 || gallery.galleryFiles[index].date != gallery.galleryFiles[index-1].date)  {
					// $('#gal' + index).before('<h3>'+gallery.galleryFiles[index].date+'</h3>');
				}	
					
				if (index < gallery.iterator) {
					getImage(index + 1);
				} else {
					$('#GalleryList').append($('#galleryBtnMore'));
					$('#galleryBtnMore').show();
				}
			},
			error: function(data) {
				$('#gal' + index).remove();
			}
		});
	} else {
		// Remove trailing loadings
		$('#gal' + index).remove();
	}
}

var displayImageFullSize = function(path, date) {
	hideContextMenu();
	castLoading();

	$.get(path + "?tz=ABSOLUTE", null, function() {
		$('#galleryViewer').css('background-image', "url('" + path + "?tz=ABSOLUTE" + "')");
		$('#galleryViewerWrapper').show('slide', {direction:'up'}, 500);
		$('#galleryViewerTitle').html(date);	
		
		dismissLoading();
	});
};