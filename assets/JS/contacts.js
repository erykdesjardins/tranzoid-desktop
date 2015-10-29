// Depends on jQuery
// Conacts transaction are done in here

var currentContacts = {contacts:[],numberassoc:{},idassoc:{},currentContact:{}};

// Executed once DOM is ready
$(function() {
	// Save button 
	$('#contactActionSave').bind('click', updateContact);
});

var requestContactList = function(bAsync, finished, fail) {
	$.ajax({
		url:'CTNB/CONTACTS.tz',
		method:'GET',
		dataType: "json",
		async: bAsync,
		success:function(data){
			if (data != '') {
				currentContacts.contacts = sortByKey(data.contacts, 'name');
				$(data.contacts).each(function(index, value) {
					for (var i = 0; i < value.number.length; i++) {
						currentContacts.numberassoc[value.number[i]] = value.name;
						currentContacts.idassoc[value.number[i]] = value.id;
					}
				});
				
				displayContacts();
				finished();
			} else {
				notify.error("[#contactloadfail]", "[#contactmenu]");
			}
		},
		error:fail
	});
};

// Save existing contact
var updateContact = function() {
	castLoading();

	$.ajax({
		url:'Contacts',
		method:'POST',
		data:JSON.stringify({action:'SCTC',data:contactForm()}),
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success:function(data) {
			notify.success('[#ctnupdatetitle]', '[#ctnupdatemsg]');
			dismissLoading();
		}
	});	
}

var getSingleContact = function(id) {
	castLoading();
	$('#contactEditWrapper').addClass('loading').show();
	
	$.ajax({
		url:'Contact/' + id,
		method:'GET',
		contentType: "application/json; charset=utf-8",
		dayaType: "json",
		success:function(data) {
			// expecting contact details as JSON
			currentContacts.currentContact = JSON.parse(data);
			
			presentContact();
			dismissLoading();
		}
	});
}

var displayContacts = function() {
	$('#contactList').empty();
	var isA = false;
	var firstLetter = '#'
	var cList = $('#contactList');
	
	$(currentContacts.contacts).each(function(index, ct) {
		if (ct.number.length != 0) {
			var firstChar = ct.name.charAt(0);
			if (!isA && firstChar == 'A') isA = true;
		
			if (isA && firstLetter != firstChar) {
				cList.append('<li class="letterSeparator">'+firstChar+'</li>');
		
				firstLetter = firstChar;
			}
		
			cList.append(createContactCard(ct.id, ct.name, ct.number, ct.starred));
		}
	});
};

var createContactCard = function(id, name, number, starred) {
	return '<li id="cCard'+id+'" class="contactCardWrapper" onclick="getSingleContact('+id+');"><img src="CTIMG?'+id+'" /><span class="fullname">'+name+'</span>'
		   + (starred == 1 ? '<span class="contactStar"></span>' : '')  
		   + '</li>';
};

var presentContact = function() {
	var found = false;
	var contact = currentContacts.currentContact;
	
	$('#contactEditWrapper input').val('');
	
	$('#contactEditWrapper h2').html(contact.name);
	$('#contactEditWrapper h3').html(contact.number[0].number);
	$('#contactEditWrapper img').attr('src', 'CTIMG?' + contact.id);
	
	$('#ctnFullname').val(contact.name);
	
	$(defOr(contact.number, [])).each(function(i, val) {
		switch (val.type) {
			case 1: $('#ctnPhonenumberHome').val(val.number); break;
			case 2: $('#ctnPhonenumberMobile').val(val.number); break;
			case 3: $('#ctnPhonenumberWork').val(val.number); break;
		}
	});
	
	$(defOr(contact.email, [])).each(function(i, val) {
		switch (val.type) {
			case 1: $('#ctnEmailHome').val(val.address); break;
			case 2: $('#ctnEmailWork').val(val.address); break;
		}
	});	
	
	if (contact.address.length != 0) {
		$('#ctnAddress').val(emptyNullStr(contact.address.street));
		$('#ctnCity').val(emptyNullStr(contact.address.city));
		$('#ctnRegion').val(emptyNullStr(contact.address.region));
		$('#ctnCountry').val(emptyNullStr(contact.address.country));
		$('#ctnPostal').val(emptyNullStr(contact.address.postcode));
	}
	
	$('#contactEditWrapper').removeClass('loading');
}

var contactForm = function() {
	contact = {
		id : 	  currentContacts.currentContact.id,
		name : 	  $('#ctnFullname').val(),
		street:   $('#ctnAddress').val(),
		city:     $('#ctnCity').val(),
		region:   $('#ctnRegion').val(),
		country:  $('#ctnCountry').val(),
		postcode: $('#ctnPostal').val(),
		number1:  $('#ctnPhonenumberHome').val(),
		number2:  $('#ctnPhonenumberMobile').val(),
		number3:  $('#ctnPhonenumberWork').val(),
		email1:   $('#ctnEmailHome').val(),
		email2:   $('#ctnEmailWork').val()
	};
	
	return contact;
}