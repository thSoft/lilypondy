const services = ["http://localhost/lilypond-service/"]; // TODO

const defaultSize = 16;

const maxSize = 64;

const sizeDelta = 2;

function getSource() {
	return wave.getState().get("source", "");
}

function setSource(source) {
	wave.getState().submitValue("source", source);
}

function getSize() {
	size = parseFloat(wave.getState().get("size"));
	if (isNaN(size) || (size <= 0)) {
		size = defaultSize;
	}
	return size;
}

function setSize(size) {
	if ((size > 0) && (size <= maxSize)) {
		wave.getState().submitValue("size", size.toString());
	}
}

function stateUpdated() {
	hide($("menu"));
	if (getSource()) {
		$("loading").style.left = Math.max($("score").offsetLeft + $("score").width / 2 - $("loading").width / 2, 0);
		$("loading").style.top = Math.max($("score").offsetTop + $("score").height / 2 - $("loading").height / 2, 0);
		show($("loading"));
		$("score").style.opacity = 0.5;
		score = $("score");
		score.src = services[random(services.length)] + "?gmndata=" + encodeURIComponent(getSource()) + "&zoom=" + getSize() + "&crop=yes";
		score.title = getSource();
	} else {
		show($("editor"));
	}
}

function imageLoaded() {
	hide($("loading"));
	hide($("editor"));
	show($("score"));
	$("score").style.opacity = 1;
}

function showMenu(event) {
	$("menu").style.left = event.pageX - 1;
	$("menu").style.top = event.pageY - 15;
	show($("menu"));
	hide($("editor"));
}

function editSource() {
	hide($("menu"));
	show($("editor"));
	$("source").value = getSource();
	$("source").focus();
}

function keyPressed(event) {
	if (event.altKey && (event.keyCode == 13)) {
		done();
	}
}

function done() {
	if ($("source").value) {
		hide($("editor"));
		setSource($("source").value);
	}
}

function grow() {
	hide($("menu"));
	setSize(getSize() + sizeDelta);
}

function shrink() {
	hide($("menu"));
	setSize(getSize() - sizeDelta);
}

function originalSize() {
	hide($("menu"));
	setSize(defaultSize);
}

function init() {
	if (wave && wave.isInWaveContainer()) {
		wave.setStateCallback(stateUpdated);
	}
}

gadgets.util.registerOnLoadHandler(init);