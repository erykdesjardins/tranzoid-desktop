// requires jQuery

var deviceInfo = {};
var smsDay = {};
var smsDayPlotValues = [];
var smsDayPlot = undefined;

var smsContact = {};
var smsContactPlotValues = [];
var smsContactPlot = undefined;

var pieChartPlot = undefined;

$(function() {
	
});

var requestDeviceInfo = function(bAsync, finished, fail) {
	$.ajax({
		url:'Device',
		method:'POST',
		data:JSON.stringify({action:'DVCI',data:{get:'all'}}),
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		async: bAsync,
		success:function(data) {
			deviceInfo = data;
			finished();
		},
		error:fail
	});	
};

var updateDeviceInfo = function(info, value) {
	
};

var incrementSmsCount = function() {
	deviceInfo.smscount++;
};

var computeStatsValues = function() {
	smsDay = {};
	smsDayPlotValues = [];
	smsContact = {};
	smsContactPlotValues = [];

	$(messaging.sms).each(function(index, value) {
		if (value.time != undefined) {
			var key = value.time.substr(0, 10);
			smsDay[key] = smsDay[key] == undefined ? 1 : smsDay[key] + 1;
		}
		
		if (value.writer != undefined) {
			var key = value.writer;
			smsContact[key] = smsContact[key] == undefined ? 1 : smsContact[key] + 1;
		}
	});
	
	var dayCount = 0;
	for (var key in smsDay) {
		smsDayPlotValues.push([key, smsDay[key]]);
		dayCount++;
		
		if (dayCount == 7) break;
	}
	
	var smsContactTemp = [];
	for (var key in smsContact) {
		smsContactTemp.push([key, smsContact[key]]);
	}	
	
	smsContactTemp.sort(function(a,b) {
   		return a[1] - b[1];
	}).reverse();
	
	for (var i = 0; i < 5; i++) {
		var truename = currentContacts.numberassoc[smsContactTemp[i][0]];
		smsContactPlotValues.push([truename == undefined ? smsContactTemp[i][0] : truename, smsContactTemp[i][1]]);
	}		
	
	renderStatsCanvas();
};

var renderStatsCanvas = function() {
	renderSmsDay();
	renderSmsContact();
	renderPieChart();
	renderStorageChart();
};

var renderSmsDay = function() {
	if (smsDayPlot != undefined) {
		smsDayPlot.destroy();
	}
	
	smsDayPlot = $.jqplot('deviceSmsDayCanvas', [smsDayPlotValues.reverse()], {
      title:{text:'[#devicesmsday]',fontFamily:'komika_textregular',textColor:'#FFF'},
      axes:{
        xaxis:{
          renderer:$.jqplot.CategoryAxisRenderer
        },
        yaxis:{
          min:0        
        }
      },
      highlighter: {
        show: true,
        sizeAdjust: 7.5,
        tooltipAxes:'y'
      },
      grid:{
      	borderColor:'[@theme.color1]',
      	borderWidth: 5.0
      },
      series:[{color:'[@theme.color2]'}],
      cursor: {
        show: false
      },
      textColor:"#ffffff"
  });
};

var renderSmsContact = function() {
	if (smsContactPlot != undefined) {
		smsContactPlot.destroy();
	}
	
	smsContactPlot = $.jqplot('deviceSmsContactCanvas', [smsContactPlotValues], {
      title:{text:'[#devicesmscontact]',fontFamily:'komika_textregular',textColor:'#FFF'},
      axes:{
        xaxis:{
          renderer: $.jqplot.CategoryAxisRenderer
        }
      },      
      highlighter: {
        show: true,
        sizeAdjust: 7.5,
        tooltipAxes:'y'
      },
      grid:{
      	borderColor:'[@theme.color1]',
      	borderWidth: 5.0
      },
      series:[{
      	color:'[@theme.color2]', 
      	renderer:$.jqplot.BarRenderer
      }]
  });
  
};

var renderPieChart = function() {
	if (pieChartPlot != undefined) {
		pieChartPlot.destroy();
	}
	
	var data = [
		['[#smsmenu]', messaging.sms.length],
		['[#gallerymenu]', gallery.galleryFiles.length],
		['[#contactmenu]', currentContacts.contacts.length],
		['[#musicsongs]', audioLibrary.audioFiles.length]
	];
	
	pieChartPlot = $.jqplot('pieChartCanvas', [data], {
      title:{text:'[#deviceinfotitle]',fontFamily:'komika_textregular',textColor:'#FFF'},
      
      seriesDefaults: {
        renderer: jQuery.jqplot.PieRenderer,
        rendererOptions: {
          showDataLabels: true,
          dataLabels:'value'
        },
        color:'[@theme.color2]'
      },
      legend: { show:true, location: 'e' },
      
      grid:{
      	borderColor:'[@theme.color1]',
      	borderWidth: 5.0
      }
  });
  
};

var renderStorageChart = function() {
	var totalsizesd1 = deviceInfo.internalstorage.total;
	var totalsizesd2 = deviceInfo.mountedstorage.total;

	if (totalsizesd1 != undefined) { 
		var barSize = 480 - (deviceInfo.internalstorage.free / totalsizesd1 * 480);
		var perc = (100 - Math.round(deviceInfo.internalstorage.free / totalsizesd1 * 100)).toString();
		var TotalGo = (totalsizesd1/1024/1024/1024).toFixed(2).toString();
		var FreeGo = (deviceInfo.internalstorage.free/1024/1024/1024).toFixed(2).toString();
		
		$('#sdUsageNoCanvas .sd1 .sdbar').css('width', Math.round(barSize).toString() + 'px');
		$('#sdUsageNoCanvas .sd1 .sdperc').html(perc + "%");
		$('#sdUsageNoCanvas .storageDetailSpan1').html("[#devicefreespace] : " + FreeGo + "[#devicegb] / " + TotalGo + "[#devicegb]");
	}
	
	if (totalsizesd2 != undefined) {
		var barSize = 480 - (deviceInfo.mountedstorage.free / totalsizesd2 * 480);
		var perc = (100 - Math.round(deviceInfo.mountedstorage.free / totalsizesd2 * 100)).toString();
		var TotalGo = (totalsizesd2/1024/1024/1024).toFixed(2).toString();
		var FreeGo = (deviceInfo.mountedstorage.free/1024/1024/1024).toFixed(2).toString();
		
		$('#sdUsageNoCanvas .sd2 .sdbar').css('width', Math.round(barSize).toString() + 'px');
		$('#sdUsageNoCanvas .sd2 .sdperc').html(perc + "%");
		$('#sdUsageNoCanvas .storageDetailSpan2').html("[#devicefreespace] : " + FreeGo + "[#devicegb] / " + TotalGo + "[#devicegb]");
	}
};	
	