var leftArrowKeyCode = 37;
var rightArrowKeyCode = 39;
var downArrowKeyCode = 40;
var upArrowKeyCode = 38;

var spriteRadius = 10;

var mySprite = {
		id: 0, // temporary until socket tells client what its id is
		xPos: 0, // temporary until window dimensions are determined
		yPos: 0 //  temporary until window dimensions are determined
};

var spritesInRoom = {};

var canvas;

var socket;
var socketIsInitialized = false;
var socketTimer;

/**
 * the messageType can be one of four values:
 * 0: removeClient message
 * 1: updateClient message
 * 2: initSelf message
 * 3: nullMsg message
 * see the WebSocketActor.scala source file for more information
 */
const socketMsgType = {
		removeClient: 0,
		updateClient: 1,
		initSelf: 2,
		nullMsg: 3
};

document.addEventListener('DOMContentLoaded', (function() {
	//setWindowHeight();
	// Set up canvas and draw new client's sprite
	canvas = document.getElementById("sprite-canvas");
	var canvasHeight = (document.body.clientHeight - document.getElementById("sprite-header").clientHeight);
	var canvasWidth = (document.body.clientWidth);
	canvas.setAttribute('height', canvasHeight.toString() + "px");
	canvas.setAttribute('width', canvasWidth.toString() + "px");
	
	mySprite.xPos = Math.floor(Math.random() * canvasWidth);
	mySprite.yPos = Math.floor(Math.random() * canvasHeight);
	spriteRadius = (canvasWidth / 100);
	if(spriteRadius <= 0) {
		spriteRadius = 1;
	}
	
	//Create WebSocket connection.
	socket = new WebSocket('ws://'+window.location.hostname+':'+window.location.port+'/socket');
	initializeSocket();
	keepAlive();
	
	document.body.addEventListener('keydown', (function(event) {
		if(event.keyCode == leftArrowKeyCode) {
			mySprite.xPos -= 1;
			mySprite.xPos = (mySprite.xPos < 0) ? 0 : mySprite.xPos;
			if(socketIsInitialized) {
				socket.send(JSON.stringify({'messageType': socketMsgType.updateClient, 'clientSprite': mySprite}));
			}
		} else if(event.keyCode == rightArrowKeyCode) {
			mySprite.xPos += 1;
			mySprite.xPos = (mySprite.xPos > canvas.clientWidth) ? canvas.clientWidth : mySprite.xPos;
			if(socketIsInitialized) { 
				socket.send(JSON.stringify({'messageType': socketMsgType.updateClient, 'clientSprite': mySprite}));
			}
		} else if(event.keyCode == upArrowKeyCode) {
			mySprite.yPos -= 1;
			mySprite.yPos = (mySprite.yPos < 0) ? 0 : mySprite.yPos;
			if(socketIsInitialized) {
				socket.send(JSON.stringify({'messageType': socketMsgType.updateClient, 'clientSprite': mySprite}));
			}
		} else if(event.keyCode == downArrowKeyCode) {
			mySprite.yPos += 1;
			mySprite.yPos = (mySprite.yPos > canvas.clientHeight) ? canvas.clientHeight : mySprite.yPos;
			if(socketIsInitialized) {
				socket.send(JSON.stringify({'messageType': socketMsgType.updateClient, 'clientSprite': mySprite}));
			}
		}
	}));

	document.body.addEventListener('keyup', (function(event) {
		if(event.keyCode == leftArrowKeyCode) {
			console.log("left arrow released");
		} else if(event.keyCode == rightArrowKeyCode) {
			console.log("right arrow released");
		} else if(event.keyCode == upArrowKeyCode) {
			console.log("up arrow released");
		} else if(event.keyCode == downArrowKeyCode) {
			console.log("down arrow released");
		}
	}));
	
	setInterval(render,100);
	
}), false);

function initializeSocket() {
	// Connection opened 
	socket.addEventListener('open', function (event) {
		socket.send(JSON.stringify({'messageType': socketMsgType.nullMsg, 'clientSprite': mySprite}));
	});
	
	//Listen for messages
	socket.addEventListener('message', function (event) {
	    const socketMsg = JSON.parse(event.data);
	    console.log("Got socket message: " + socketMsg);
	    const msgType = socketMsg.messageType;
	    const clientSprite = socketMsg.clientSprite;
	    if((typeof clientSprite.id !== undefined) && (typeof clientSprite.xPos !== undefined) && (typeof clientSprite.yPos !== undefined)) {
	    	if(msgType == socketMsgType.initSelf) {
	    		// initSelf => set this client's id to the id given by the server
	    		mySprite.id = clientSprite.id;
	    		// position data may be invalid for the "self" object sent by the websocket
	    		// so we ignore it
	    		socketIsInitialized = true;
	    	} else if(msgType == socketMsgType.updateClient) {
	    		// updateClient => update (or add) client to sprite room
	    		var clientIdStr = (clientSprite.id).toString();
	    		var tmpObj = {'xPos': clientSprite.xPos, 'yPos': clientSprite.yPos};
	    		spritesInRoom[clientIdStr] = tmpObj;
	    	} else if(msgType == socketMsgType.removeClient) {
	    		// removeClient => remove the sprite with the given id from the sprite room
	    		var clientIdStr = (clientSprite.id).toString();
	    		delete spritesInRoom[clientIdStr];
	    	}
	    } else {
	    	console.log("Invalid socket message");
	    }
	});
}

function keepAlive() { 
    var timeout = 15000;  
    if (socket.readyState == socket.OPEN) {  
    	socket.send(JSON.stringify({'messageType': socketMsgType.nullMsg, 'clientSprite': mySprite}));
    } else if(socket.readyState == socket.CLOSED || socket.readyState == socket.CLOSING) {
    	socketIsInitialized = false;
    	socket.close();
    	socket = new WebSocket('ws://'+window.location.hostname+':'+window.location.port+'/socket');
    	initializeSocket();
    }
    socketTimer = setTimeout(function() { keepAlive(); }, timeout);  
}  
function cancelKeepAlive() {  
    if (socketTimer) {  
        clearTimeout(socketTimer);  
        socketIsInitialized = false;
    }  
}

function clearCanvas(ctx) {
	ctx.clearRect(0, 0, canvas.clientWidth, canvas.clientHeight);
	ctx.fillStyle = "#2F2F2F";
	ctx.fillRect(0,0, canvas.clientWidth, canvas.clientHeight);
}

function drawSprite(ctx, sprite, color) {
	ctx.fillStyle = color;
	ctx.strokeStyle = "#FFFFFF";
	ctx.lineWidth = 2;
	ctx.beginPath();
	ctx.arc(sprite.xPos, sprite.yPos, spriteRadius, 0, 2 * Math.PI);
	ctx.closePath();
	ctx.fill();
	ctx.stroke();
}

function render() {
	if(canvas.getContext) {
		var ctx = canvas.getContext('2d');
		clearCanvas(ctx);
		drawSprite(ctx,mySprite,"#00FF00");
		for (var spriteId in spritesInRoom) {
			if (spritesInRoom.hasOwnProperty(spriteId)) {
				var obj = spritesInRoom[spriteId];
			    drawSprite(ctx,obj,"#FF0000");
			}
		}
	}
}

function clamp(val, min, max) {
	let res = (val > max ? max : val < min ? min : val);
    return res;
}