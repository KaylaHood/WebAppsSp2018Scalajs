/**
 * This file uses jQuery to perform the same Ajax calls that are in both ajax.js and tempAjax.js 
 */

$(init);



function init() {
	
	// Do the Ajax for the basic ajax page. This only happens if there is an element with an id of ajax
	$("#ajax").each((i, e) => {
		$(e).load("http://localhost:9000/ajax/req");
	})

	// Do the Ajax for the temp page. Only happens if there is an element with an id of dayInfo
	$("#dayInfo").each((i, e) => {
		$("#getDay").click(() => {
			let date = $("#ajaxDate").val();
			if(date) {
				$(e).load($("#ajaxURL").val()+"/"+date);
			} else {
				$(e).text("You need to select a date.");
			}
		});
	})

}
