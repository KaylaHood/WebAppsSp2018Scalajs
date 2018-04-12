/**
 * 
 */

const request = new XMLHttpRequest();

request.onload = (e) => {
	console.log(this);
	console.log(e);
	const div = document.getElementById("ajax");
	div.innerHTML = request.responseText;
};

request.open("GET", "http://localhost:9000/ajax/req");

request.send();