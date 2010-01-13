(function() {
	
	var errorHandler = function(response) {
		$("#resultMessage").val(response.responseText);
		$("#resultImage").attr("src", "/img/error.png");	
	}
	
	var typesetSuccessHandler = function(result) {
		$("#resultImage").attr("src", "/score.do?h="+result);
	}
	
	var typesetHandler = function() {
		$("#resultImage").attr("src", "/img/ajax-loader.gif");		
		$.ajax({
			type: "POST",
			url: "/score.do",
			data: { q: $("#query").val(), s: $("#size").val(), rh: "1" },
			dataType: "text",
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