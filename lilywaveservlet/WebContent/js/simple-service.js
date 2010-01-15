(function() {
	
	var errorHandler = function(response) {
		$("#resultMessage").val(response.responseText);
		$("#resultImage").attr("src", "/img/error.png");	
	}
	
	var typesetSuccessHandler = function(result) {
		$("#resultImage").attr("src", "/score.do?t=PNG&h="+result.hash);
		if (true === result.MIDI) {
			$("#midiLink").show();
			$("#midiLink a").attr("href", "/score.do?t=MIDI&h="+result.hash);
		}
		if (true === result.PDF) {
			$("#pdfLink").show();
			$("#pdfLink a").attr("href", "/score.do?t=PDF&h="+result.hash);
		}
		if (true === result.PNG) {
			$("#pngLink").show();
			$("#pngLink a").attr("href", "/score.do?t=PNG&h="+result.hash);
		}


	}
	
	var typesetHandler = function() {
		$("#midiLink").hide();
		$("#pdfLink").hide();
		$("#pngLink").hide();
		$("#resultImage").attr("src", "/img/ajax-loader.gif");		
		$.ajax({
			type: "POST",
			url: "/score.do",
			data: { q: $("#query").val(), s: $("#size").val(), rh: "1" },
			dataType: "json",
			error: errorHandler,
			success: typesetSuccessHandler
		});
		return false;
	}
	
	function init() {
		$("#typeset").click(typesetHandler);
	}
	
	init();
	
})();