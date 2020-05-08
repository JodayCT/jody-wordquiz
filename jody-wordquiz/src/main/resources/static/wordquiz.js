console.log("starting wordquiz javascript");

$(function() {

	$("#show-definition-button").click(function(e) {
		$("#test-definition-section").show();
		$("#show-definition-button").hide();
	});
	
	$("#delete-word-button").click(function(e) {
		 return confirm("Are you sure you want to delete this word?"); 
	});
	

});