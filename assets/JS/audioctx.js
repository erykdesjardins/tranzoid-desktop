var AudioCtx = function(src) {
	this.ctx = undefined;
	this.element = undefined;
	this.audioSource = undefined;
	this.analyser = undefined;
	
	this.compCheck = function() {
		return typeof this.getContext() !== 'undefined';
	}
	
	this.getContext = function() {
		return this.ctx;
	};
	
	this.setAudio = function(audio) {
		this.element = audio;
		
		if (this.compCheck()) {
			this.audioSource = this.getContext().createMediaElementSource(this.element);
			this.audioSource.connect(this.analyser);
			this.analyser.connect(this.getContext().destination);
		}	
	};
	
	this.getFreqArray = function() {
		var analyserArr = new Uint8Array(this.analyser.frequencyBinCount);
		this.analyser.getByteFrequencyData(analyserArr);
		
		return analyserArr;
	};
	
	this.getCurrentTime = function() {};
	
	if (typeof AudioContext !== "undefined") {
	    this.ctx = new AudioContext();
	} else if (typeof webkitAudioContext !== "undefined") {
	    this.ctx = new webkitAudioContext();
	} else {
	    this.ctx = undefined;
	}		
	
	if (this.compCheck()) {
		this.getContext().sampleRate = 44100;
		this.analyser = this.getContext().createAnalyser();
		this.analyser.fftSize = 256;
		
		this.getCurrentTime = function() {
			return this.getContext().currentTime;
		};
	} else {
		this.getCurrentTime = function() {
			return this.element.currentTime;
		};	
	}	
}