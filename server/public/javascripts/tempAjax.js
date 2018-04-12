/**
 * 
 */

const ajaxURL = document.getElementById("ajaxURL").value;
const dayDisplay = document.getElementById("dayInfo");
const ajaxDate = document.getElementById("ajaxDate");
const getDayButton = document.getElementById("getDay");
getDayButton.onclick = (e) => {
	if(ajaxDate.value) {
		const request = new XMLHttpRequest();
		request.onload = (e) => {
			dayDisplay.innerHTML = request.responseText;
		};
	
		request.open("GET", ajaxURL+"/"+ajaxDate.value);
	
		request.send();
	}
}



