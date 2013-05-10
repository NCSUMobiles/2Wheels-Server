$(document).ready(function() {
			//alert("Welcome to Spinning Wellness!");
	        $('#log').hide();
			$("#rideList").attr('disabled','disabled');
			
			
			$.ajax({
				type: "GET",
				url: "resources/l2wuser/getallusers",
				contentType: "application/json; charset=utf-8",
				dataType: "json",
				success: function(response) {
					
					var o = new Option("-- Select --", "None");
					/// jquerify the DOM object 'o' so we can use the html method
					$(o).html("-- Select --");
					$("#userList").append(o);
					for (var i = 0; i < response.user.length; i++) {
						var aUsername = response.user[i].name;
						var o = new Option(aUsername, aUsername);
						/// jquerify the DOM object 'o' so we can use the html method
						$(o).html(aUsername);
						$("#userList").append(o);
					}
					
					$("#userList").change(function(){
						
					    var user = $("#userList").val() ;
					    if(user != "None"){
					    	alert("fetch rides");
						    clearrides();
						    $("#rideList").removeAttr('disabled');
						    showmyupcomingrides(user);
					    }
					});
				
			  },
			failure: function(errMsg) {
				alert("in error!");
				$("#errMessage").text(errMsg);
			}
		});
	});

function clearrides(){
	$("#rideList").empty();
}
		
function showmyupcomingrides(user){
	var dateVal = new Date().getTime();
	alert(dateVal);
	$.ajax({
		type: "GET",
		url: "resources/l2wride/mypastrides/" + user + "/" + dateVal,
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		success: function(response) {
			if(response != null){
				for (var i = 0; i < response.ride.length; i++) {
					var aride = response.ride[i];
					var o = new Option(aride.name, aride.id);
					/// jquerify the DOM object 'o' so we can use the html method
					$(o).html(aride.name);
					$("#rideList").append(o);
				}
			} else{
				 $("#errMessage").text("No rides found");
			}
			$("#rideList").change(function(){
			    var rideid = $("#rideList").val() ;
			    getRideDetails(rideid,user);
			});
		
	},
	failure: function(errMsg) {
		alert("in error!");
		$("#errMessage").text(errMsg);
	}
});	
}	
	
function getRideDetails(ride,user){
	alert("get ride details" + ride);
	$('#log').show();
	$("#submitButton").click({param1: ride, param2: user},submitDetail);
}

function submitDetail(event){
	alert("submit clicked" + event.data.param1 + " " + event.data.param2);
	
	var currentState = {}; 
	var now= new Date();
	currentState["id"] = jQuery.datepicker.parseDate(now,"mmddy") + now.getHours()+':'+now.getMinutes()+':'+now.getSeconds(); //"050913234949851";
	currentState["rideId"] = event.data.param1 ;
	currentState["userName"] = event.data.param2;
	currentState["distaceCovered"] = $("#distance").val();
	currentState["cadence"] = $("#cadence").val();
	currentState["averageSpeed"] = $("#cal").val();
	currentState["caloriesBurned"] = $("#time").val();
	currentState["heartRate"] = $("#heartrate").val();
	currentState["activityDate"] = new Date().getTime();
	
	alert(JSON.stringify(currentState));
	$.ajax({
		type: "POST",
		url: "resources/l2wuser/loguseractivity" ,
		contentType: "application/json; charset=utf-8",
		dataType: "json",
		data: JSON.stringify(currentState),
		success: function(response) {
			alert("details are logged");
		},
	failure: function(errMsg) {
		alert("in error!");
		$("#errMessage").text("Unable to log the details.");
	}
});	
}
